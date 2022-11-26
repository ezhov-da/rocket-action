package ru.ezhov.rocket.action.plugin.noteonfile

import arrow.core.Either
import arrow.core.getOrHandle
import mu.KotlinLogging
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.plugin.noteonfile.command.CommandObserver
import ru.ezhov.rocket.action.plugin.noteonfile.command.SaveTextCommand
import ru.ezhov.rocket.action.plugin.noteonfile.command.SaveTextCommandListener
import ru.ezhov.rocket.action.plugin.noteonfile.event.EventObserver
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.swing.AbstractAction
import javax.swing.Box
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JTextField
import javax.swing.JToolBar
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.WindowConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

private val logger = KotlinLogging.logger {}

internal class TextPanel(
    private val path: String,
    private val label: String,
    private val style: String?,
    private val loadOnInitialize: Boolean,
    private val addStyleSelected: Boolean,
    private val delimiter: String,
    private val textAutoSave: TextAutoSave?,
    private val context: RocketActionContext,
) : JPanel() {
    private val commandObserver: CommandObserver = CommandObserver()
    private val eventObserver: EventObserver = EventObserver()

    private val pointPanel: PointPanel = PointPanel { textPane.caretPosition = it.index }
    private val defaultStyle = SyntaxConstants.SYNTAX_STYLE_NONE
    private val textPane = RSyntaxTextArea().apply {
        val textPane = this
        textPane.syntaxEditingStyle = style ?: defaultStyle;
        textPane.isCodeFoldingEnabled = true;
        textPane.background = JLabel().background
    }

    private val delimiterTextField = JTextField(delimiter, 5).apply { isEditable = false }

    private val comboBoxListStyles = JComboBox(StylesList.styles.toTypedArray()).also { cb ->
        cb.selectedItem = style ?: defaultStyle
        cb.model.addListDataListener(object : ListDataListener {
            override fun intervalAdded(e: ListDataEvent?) {}

            override fun intervalRemoved(e: ListDataEvent?) {}

            override fun contentsChanged(e: ListDataEvent) {
                val value = cb.selectedItem as String
                SwingUtilities.invokeLater { textPane.syntaxEditingStyle = value }
            }
        })
    }

    private val labelPath = JLabel(path)

    private val toolBar = JToolBar().apply {
        add(JButton(
            object : AbstractAction() {
                init {
                    putValue(SHORT_DESCRIPTION, "Скопировать текст в буфер")
                    putValue(SMALL_ICON, context.icon().by(AppIcon.COPY_WRITING))
                }

                override fun actionPerformed(e: ActionEvent?) {
                    val defaultToolkit = Toolkit.getDefaultToolkit()
                    val clipboard = defaultToolkit.systemClipboard
                    clipboard.setContents(StringSelection(textPane.text), null)
                    context.notification().show(
                        type = NotificationType.INFO,
                        text = "Текст скопирован в буфер"
                    )
                }
            }
        ))
        add(JButton(
            object : AbstractAction() {
                init {
                    putValue(SHORT_DESCRIPTION, "Открыть в отдельном окне")
                    putValue(SMALL_ICON, context.icon().by(AppIcon.ARROW_TOP))
                }

                override fun actionPerformed(e: ActionEvent?) {
                    openInNewWindow(alwaysOnTop = false)
                }
            }
        ))
        add(JButton(
            object : AbstractAction() {
                init {
                    putValue(SHORT_DESCRIPTION, "Открыть в отдельном окне поверх всех окон")
                    putValue(SMALL_ICON, context.icon().by(AppIcon.BROWSER))
                }

                override fun actionPerformed(e: ActionEvent?) {
                    openInNewWindow(alwaysOnTop = true)
                }
            }
        ))

        add(JButton(
            object : AbstractAction() {
                init {
                    putValue(NAME, "Сохранить")
                    putValue(SHORT_DESCRIPTION, "Сохранить")
                }

                override fun actionPerformed(e: ActionEvent?) {
                    WriteSwingWorker(
                        path = path,
                        text = textPane.text,
                        eventObserver = eventObserver,
                        context = context,
                    ).execute()
                }
            }
        ))

        add(JButton(
            object : AbstractAction() {
                init {
                    putValue(NAME, "Загрузить")
                    putValue(SHORT_DESCRIPTION, "Загрузить")
                }

                override fun actionPerformed(e: ActionEvent?) {
                    ReadSwingWorker(
                        path = path,
                        textPane = textPane,
                        onLoad = { text -> pointPanel.calculate(delimiter, text) },
                        eventObserver = eventObserver,
                        context = context,
                    ).execute()
                }
            }
        ))

        val delimiterLabel = JLabel("Разделитель")
        delimiterLabel.labelFor = delimiterTextField
        if (addStyleSelected) {
            add(comboBoxListStyles)
        }

        add(delimiterLabel)
        add(delimiterTextField)

        add(InfoAboutSaveTextPanel(eventObserver = eventObserver))
        textAutoSave?.let {
            if (it.enable) {
                add(
                    AutoSaveInfoPanel(
                        textAutoSave = it,
                        eventObserver = eventObserver,
                        commandObserver = commandObserver
                    )
                )
            }
        }

        add(Box.createGlue())
    }

    init {
        commandObserver.register(object : SaveTextCommandListener {
            override fun save(command: SaveTextCommand) {
                WriteSwingWorker(
                    path = path,
                    text = textPane.text,
                    eventObserver = eventObserver,
                    context = context,
                ).execute()
            }
        })

        this.layout = BorderLayout()
        add(toolBar, BorderLayout.NORTH)
        add(labelPath, BorderLayout.SOUTH)
        val dimensionScreen = Toolkit.getDefaultToolkit().screenSize
        val dimension = Dimension(
            (dimensionScreen.width * 0.3).toInt(),
            (dimensionScreen.height * 0.2).toInt()
        )
        maximumSize = dimension
        preferredSize = dimension

        if (loadOnInitialize) {
            ReadSwingWorker(
                path = path,
                textPane = textPane,
                onLoad = { text -> pointPanel.calculate(delimiter, text) },
                eventObserver = eventObserver,
                context = context,
            ).execute()
        }

        labelPath.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                Desktop.getDesktop().open(File(path))
            }
        })

        textPane.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == 83 /*S*/ && e.isControlDown) {
                    WriteSwingWorker(
                        path = path,
                        text = textPane.text,
                        eventObserver = eventObserver,
                        context = context,
                    ).execute()
                }
            }
        })

        textPane.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                eventObserver.notifyTextChanging(textPane.text)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                eventObserver.notifyTextChanging(textPane.text)
            }

            override fun changedUpdate(e: DocumentEvent?) {
                eventObserver.notifyTextChanging(textPane.text)
            }
        })

        when (delimiter.isNotBlank()) {
            true -> {
                val split = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
                split.leftComponent = pointPanel
                split.rightComponent = RTextScrollPane(textPane)
                split.setDividerLocation(0.2)
                add(split, BorderLayout.CENTER)
                textPane.document.addDocumentListener(object : DocumentListener {
                    override fun insertUpdate(e: DocumentEvent?) {
                        recalculate()
                    }

                    override fun removeUpdate(e: DocumentEvent?) {
                        recalculate()
                    }

                    override fun changedUpdate(e: DocumentEvent?) {
                        recalculate()
                    }

                    private fun recalculate() {
                        pointPanel.calculate(delimiter, textPane.text)
                        eventObserver.notifyTextChanging(textPane.text)
                    }
                })

                delimiterTextField.addKeyListener(object : KeyAdapter() {
                    override fun keyReleased(e: KeyEvent?) {
                        delimiterTextField.text.takeIf { it.isNotEmpty() }?.let { text ->
                            pointPanel.calculate(text, textPane.text)
                        }
                    }
                })
            }

            false -> add(RTextScrollPane(textPane), BorderLayout.CENTER)
        }
    }

    private class ReadSwingWorker(
        private val path: String,
        private val textPane: RSyntaxTextArea,
        private val onLoad: (text: String) -> Unit,
        private val eventObserver: EventObserver,
        private val context: RocketActionContext,
    ) : SwingWorker<Either<Exception, String>, String>() {
        override fun doInBackground(): Either<Exception, String> =
            try {
                Either.Right(
                    File(path)
                        .takeIf { file -> file.exists() && file.isFile }?.let {
                            FileInputStream(path).use {
                                it.reader(charset = Charsets.UTF_8).readText()
                            }
                        } ?: run {
                        logger.info { "Path is not file or not exists. Return empty" }
                        ""
                    }
                )
            } catch (ex: Exception) {
                Either.Left(ex)
            }

        override fun done() {
            val result = this.get()
            val text = result.getOrHandle { ex ->
                val textError = "Error when read file by '$path'"
                logger.warn(ex) { textError }
                context.notification().show(type = NotificationType.WARN, text = textError)
                ""
            }
            textPane.text = text
            onLoad(text)
            if (result.isRight()) {
                context.notification().show(type = NotificationType.INFO, text = "Текст загружен")
                eventObserver.notifyTextLoading(text)
            }
        }
    }

    private class WriteSwingWorker(
        private val path: String,
        private val text: String,
        private val eventObserver: EventObserver,
        private val context: RocketActionContext,
    ) : SwingWorker<Either<Exception, Unit>, String>() {
        override fun doInBackground(): Either<Exception, Unit> =
            try {
                val file = File(path)
                Either.Right(run {
                    if (file.isFile || !file.exists()) {
                        val parent = file.parentFile
                        if (!parent.exists()) {
                            val result = parent.mkdirs()
                            if (result) {
                                logger.info { "Directory '${parent.absolutePath}' created" }
                            } else {
                                logger.warn { "Directory '${parent.absolutePath}' is not created" }
                            }
                        }
                    }

                    FileOutputStream(path).use {
                        it.write(text.toByteArray(charset = Charsets.UTF_8))
                    }
                }
                )
            } catch (ex: Exception) {
                Either.Left(ex)
            }

        override fun done() {
            val result = this.get()
            result.getOrHandle { ex ->
                val textError = "Error when write file to '$path'"
                logger.warn(ex) { textError }
                context.notification().show(type = NotificationType.WARN, text = textError)
                ""
            }
            if (result.isRight()) {
                context.notification().show(type = NotificationType.INFO, text = "Текст сохранён $text")
                eventObserver.notifyTextSaving(text)
            }
        }
    }

    private fun openInNewWindow(alwaysOnTop: Boolean) {
        SwingUtilities.invokeLater {
            val dimension = Toolkit.getDefaultToolkit().screenSize
            val frame = JFrame(label)
            frame.iconImage = context.icon().by(AppIcon.ROCKET_APP).toImage()
            frame.add(
                TextPanel(
                    path = path,
                    label = label,
                    loadOnInitialize = loadOnInitialize,
                    style = style,
                    addStyleSelected = true,
                    delimiter = delimiter,
                    textAutoSave = textAutoSave,
                    context = context,
                ),
                BorderLayout.CENTER
            )
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            frame.setSize(
                (dimension.width * 0.8).toInt(),
                (dimension.height * 0.8).toInt()
            )
            frame.isAlwaysOnTop = alwaysOnTop
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
        }
    }
}

private fun Icon.toImage(): Image =
    when (this is ImageIcon) {
        true -> this.image
        false -> {
            val w = this.iconWidth
            val h = this.iconHeight
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val gd = ge.defaultScreenDevice
            val gc = gd.defaultConfiguration
            val image = gc.createCompatibleImage(w, h)
            val g = image.createGraphics()
            this.paintIcon(null, g, 0, 0)
            g.dispose()
            image
        }
    }
