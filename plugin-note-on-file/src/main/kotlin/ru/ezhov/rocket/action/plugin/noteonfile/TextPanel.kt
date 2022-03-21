package ru.ezhov.rocket.action.plugin.noteonfile

import arrow.core.Either
import arrow.core.getOrHandle
import mu.KotlinLogging
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.toImage
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JToolBar
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.WindowConstants
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

private val logger = KotlinLogging.logger {}

internal class TextPanel(
    private val path: String,
    private val label: String,
    private val style: String?,
    private val loadOnInitialize: Boolean,
    private val addStyleSelected: Boolean,
) : JPanel() {
    private val defaultStyle = SyntaxConstants.SYNTAX_STYLE_NONE
    private val textPane = RSyntaxTextArea().apply {
        val textPane = this
        textPane.syntaxEditingStyle = style ?: defaultStyle;
        textPane.isCodeFoldingEnabled = true;
        textPane.background = JLabel().background
    }

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
                    putValue(SMALL_ICON, IconRepositoryFactory.repository.by(AppIcon.COPY_WRITING))
                }

                override fun actionPerformed(e: ActionEvent?) {
                    val defaultToolkit = Toolkit.getDefaultToolkit()
                    val clipboard = defaultToolkit.systemClipboard
                    clipboard.setContents(StringSelection(textPane.text), null)
                    NotificationFactory.notification.show(
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
                    putValue(SMALL_ICON, IconRepositoryFactory.repository.by(AppIcon.ARROW_TOP))
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
                    putValue(SMALL_ICON, IconRepositoryFactory.repository.by(AppIcon.BROWSER))
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
                    WriteSwingWorker(path = path, text = textPane.text).execute()
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
                    ReadSwingWorker(path = path, textPane = textPane).execute()
                }
            }
        ))

        if (addStyleSelected) {
            add(comboBoxListStyles)
        }
    }

    init {
        this.layout = BorderLayout()
        add(toolBar, BorderLayout.NORTH)
        add(RTextScrollPane(textPane), BorderLayout.CENTER)
        add(labelPath, BorderLayout.SOUTH)
        val dimensionScreen = Toolkit.getDefaultToolkit().screenSize
        val dimension = Dimension(
            (dimensionScreen.width * 0.3).toInt(),
            (dimensionScreen.height * 0.2).toInt()
        )
        maximumSize = dimension
        preferredSize = dimension

        if (loadOnInitialize) {
            ReadSwingWorker(path, textPane).execute()
        }

        labelPath.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                Desktop.getDesktop().open(File(path))
            }
        })

        textPane.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == 83 /*S*/ && e.isControlDown) {
                    WriteSwingWorker(path = path, text = textPane.text).execute()
                }
            }
        })
    }

    private class ReadSwingWorker(
        private val path: String,
        private val textPane: RSyntaxTextArea,
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
            textPane.text = result.getOrHandle { ex ->
                val textError = "Error when read file by '$path'"
                logger.warn(ex) { textError }
                NotificationFactory.notification.show(type = NotificationType.WARN, text = textError)
                ""
            }
            if (result.isRight()) {
                NotificationFactory.notification.show(type = NotificationType.INFO, text = "Текст загружен")
            }
        }
    }

    private class WriteSwingWorker(
        private val path: String,
        private val text: String,
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
                NotificationFactory.notification.show(type = NotificationType.WARN, text = textError)
                ""
            }
            if (result.isRight()) {
                NotificationFactory.notification.show(type = NotificationType.INFO, text = "Текст сохранён")
            }
        }
    }

    private fun openInNewWindow(alwaysOnTop: Boolean) {
        SwingUtilities.invokeLater {
            val dimension = Toolkit.getDefaultToolkit().screenSize
            val frame = JFrame(label)
            frame.iconImage = IconRepositoryFactory
                .repository.by(AppIcon.ROCKET_APP).toImage()
            frame.add(
                TextPanel(
                    path = path, label = label, loadOnInitialize = loadOnInitialize,
                    style = style, addStyleSelected = true
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