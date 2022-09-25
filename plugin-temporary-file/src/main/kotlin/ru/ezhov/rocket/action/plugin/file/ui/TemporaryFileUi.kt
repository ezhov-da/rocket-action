package ru.ezhov.rocket.action.plugin.file.ui

import arrow.core.Either
import mu.KotlinLogging
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.plugin.file.domain.TemporaryFileService
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JButton
import javax.swing.JPanel

private val logger = KotlinLogging.logger {}

class TemporaryFileUi(
    private val temporaryFileService: TemporaryFileService,
    private val extension: String = ".txt"

) : JPanel() {
    init {
        layout = BorderLayout()
        val dimensionResult = Toolkit
            .getDefaultToolkit()
            .screenSize
            .let { screenDimension ->
                Dimension(
                    (screenDimension.width * 0.5).toInt(),
                    (screenDimension.height * 0.5).toInt()
                )
            }

        size = dimensionResult
        preferredSize = dimensionResult
        minimumSize = dimensionResult
        maximumSize = dimensionResult

        val textArea = RSyntaxTextArea()
        add(
            JPanel(BorderLayout()).apply {
                add(RTextScrollPane(textArea), BorderLayout.CENTER)
            },
            BorderLayout.CENTER
        )

        add(
            JPanel().apply {
                add(JButton("Очистить текст").apply {
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseReleased(e: MouseEvent) {
                            textArea.text = ""
                        }
                    })
                })
                add(JButton("Сохранить и скопировать абсолютный путь в буфер обмена").apply {
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseReleased(e: MouseEvent?) {
                            save(textArea.text)?.let {
                                val defaultToolkit = Toolkit.getDefaultToolkit()
                                val clipboard = defaultToolkit.systemClipboard
                                clipboard.setContents(StringSelection(it.absolutePath), null)
                                NotificationFactory
                                    .notification
                                    .show(type = NotificationType.INFO, text = "Absolute path copy")
                            }
                        }
                    })
                })
                add(JButton("Сохранить и открыть файл").apply {
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseReleased(e: MouseEvent) {
                            save(textArea.text)?.let {
                                Desktop.getDesktop().open(it)
                            }
                        }
                    })
                })
            },
            BorderLayout.SOUTH
        )
    }

    private fun save(text: String): File? =
        when (
            val result = temporaryFileService.createTemporaryFile(
                text = text,
                extension = extension
            )
        ) {
            is Either.Left -> {
                logger.error(result.value) { "Error save temporary file" }
                NotificationFactory
                    .notification
                    .show(type = NotificationType.WARN, text = "Error save temporary file")
                null
            }
            is Either.Right -> result.value
        }
}
