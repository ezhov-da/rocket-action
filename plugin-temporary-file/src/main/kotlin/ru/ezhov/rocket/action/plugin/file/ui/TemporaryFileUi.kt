package ru.ezhov.rocket.action.plugin.file.ui

import arrow.core.Either
import mu.KotlinLogging
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.plugin.file.domain.TemporaryFileService
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

private val logger = KotlinLogging.logger {}
private const val DEFAULT_FILENAME: String = "tmp"
private const val DEFAULT_EXTENSION: String = ".txt"

class TemporaryFileUi(
    private val temporaryFileService: TemporaryFileService,
    private val context: RocketActionContext,

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

        val fileNameTextField = TextFieldWithText("File name").apply { text = DEFAULT_FILENAME }
        val fileExtensionTextField = TextFieldWithText("File extension").apply { text = DEFAULT_EXTENSION }

        add(
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
                add(fileNameTextField)
                add(fileExtensionTextField)
            },
            BorderLayout.NORTH
        )

        val textArea = RSyntaxTextArea()
        add(
            JPanel(BorderLayout()).apply {
                add(RTextScrollPane(textArea), BorderLayout.CENTER)
            },
            BorderLayout.CENTER
        )

        fun prefixAndExtension(): Pair<String, String> {
            val fileName = fileNameTextField.text.takeIf { it.isNotBlank() }
                ?.let {
                    val minLen = 3
                    if (it.length < minLen) {
                        val lastChar = it.toCharArray()[it.length - 1].toString()
                        it + lastChar.repeat(minLen - it.length)
                    } else {
                        it
                    }
                }
                ?: DEFAULT_FILENAME
            val fileExtension = fileExtensionTextField.text.takeIf { it.isNotBlank() } ?: DEFAULT_EXTENSION

            return Pair(fileName, fileExtension)
        }

        add(
            JPanel().apply {
                add(JButton("Clear text").apply {
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseReleased(e: MouseEvent) {
                            textArea.text = ""
                        }
                    })
                })
                add(JButton("Save and copy absolute path to clipboard").apply {
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseReleased(e: MouseEvent?) {
                            val prefixAndExtension = prefixAndExtension()
                            save(
                                text = textArea.text,
                                prefix = prefixAndExtension.first,
                                extension = prefixAndExtension.second
                            )?.let {
                                val defaultToolkit = Toolkit.getDefaultToolkit()
                                val clipboard = defaultToolkit.systemClipboard
                                clipboard.setContents(StringSelection(it.absolutePath), null)
                                context
                                    .notification()
                                    .show(type = NotificationType.INFO, text = "Absolute path copy")
                            }
                        }
                    })
                })
                add(JButton("Save and open file").apply {
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseReleased(e: MouseEvent) {
                            val prefixAndExtension = prefixAndExtension()
                            save(
                                text = textArea.text,
                                prefix = prefixAndExtension.first,
                                extension = prefixAndExtension.second
                            )?.let {
                                Desktop.getDesktop().open(it)
                            }
                        }
                    })
                })
            },
            BorderLayout.SOUTH
        )
    }

    private fun save(
        text: String,
        prefix: String,
        extension: String
    ): File? =
        when (
            val result = temporaryFileService.createTemporaryFile(
                text = text,
                prefix = prefix,
                extension = extension,
            )
        ) {
            is Either.Left -> {
                logger.error(result.value) { "Error save temporary file" }
                context
                    .notification()
                    .show(type = NotificationType.WARN, text = "Error save temporary file")
                null
            }

            is Either.Right -> result.value
        }
}
