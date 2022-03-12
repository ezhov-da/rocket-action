package ru.ezhov.rocket.action.types.noteonfile

import arrow.core.Either
import arrow.core.getOrHandle
import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.ui.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.JToolBar
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.WindowConstants

private val logger = KotlinLogging.logger {}

class NoteOnFileRocketActionUi : AbstractRocketAction() {

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[PATH_AND_NAME]
            ?.takeIf {
                if (it.isEmpty()) {
                    logger.info { "Path and name note on file is empty" }
                }
                it.isNotEmpty()
            }
            ?.let { path ->
                val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }
                    ?: path.let { File(path).name }
                val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: path

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                        path.contains(search, ignoreCase = true)
                            .or(label.contains(search, ignoreCase = true))
                            .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = JMenu(label).apply {
                        this.icon = IconRepositoryFactory.repository.by(AppIcon.TEXT)
                        this.add(TextPanel(path = path, label = label, inputText = ""))
                    }
                }
            }

    private class TextPanel(
        private val path: String,
        private val label: String,
        private val inputText: String?,
    ) : JPanel() {
        private val textPane = JTextPane().apply {
            val textPane = this
            textPane.text = inputText
            background = JLabel().background
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
        }

        init {
            this.layout = BorderLayout()
            add(toolBar, BorderLayout.NORTH)
            add(JScrollPane(textPane), BorderLayout.CENTER)
            add(labelPath, BorderLayout.SOUTH)
            val dimensionScreen = Toolkit.getDefaultToolkit().screenSize
            val dimension = Dimension(
                (dimensionScreen.width * 0.3).toInt(),
                (dimensionScreen.height * 0.2).toInt()
            )
            maximumSize = dimension
            preferredSize = dimension

            labelPath.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    Desktop.getDesktop().open(File(path))
                }
            })
        }

        private class ReadSwingWorker(
            private val path: String,
            private val textPane: JTextPane,
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
                    .repository.by(AppIcon.ROCKET_APP)
                    .toImage()
                frame.add(TextPanel(path = path, label = label, inputText = textPane.text), BorderLayout.CENTER)
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

    override fun type(): RocketActionType = RocketActionType { "NOTE_ON_FILE" }

    override fun description(): String = "Записка в файле. " +
        "Позволяет сохранять информацию в файл, а так же иметь быстрый доступ к файлу"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(
        LABEL,
        PATH_AND_NAME,
        DESCRIPTION,
    )

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = LABEL,
                name = "Заголовок в меню",
                description = "Заголовок в меню",
                required = true
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = "Описание",
                description = """Описание, которое будет всплывать при наведении, 
                            |в случае отсутствия будет отображаться путь""".trimMargin(),
                required = false
            ),
            createRocketActionProperty(
                key = PATH_AND_NAME,
                name = "Путь к файлу и его название",
                description = "Путь по которому будет располагаться файл",
                required = true,
                default = File("./notes/${UUID.randomUUID()}.txt").path

            )
        )
    }

    override fun name(): String = "Записка в файле"

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val PATH_AND_NAME = RocketActionConfigurationPropertyKey("pathAndName")
    }
}