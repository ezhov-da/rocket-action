package ru.ezhov.rocket.action.plugin.note.ui

import mu.KotlinLogging
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.toImage
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.plugin.note.application.NoteApplicationService
import ru.ezhov.rocket.action.plugin.note.domain.model.Note
import ru.ezhov.rocket.action.plugin.url.parser.UrlParser
import ru.ezhov.rocket.action.plugin.url.parser.UrlParserFilter
import ru.ezhov.rocket.action.plugin.url.parser.UrlParserResult
import ru.ezhov.rocket.action.ui.swing.common.MoveUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.IOException
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.WindowConstants

private val logger = KotlinLogging.logger {}

class NoteDialog(
    noteApplicationService: NoteApplicationService
) : JDialog() {
    companion object {
        private const val TEXT = "вставьте сюда"
    }

    private val labelDropDown = JLabel("<html><center>Перетащите текст<br>для сохранения<br>или</center>")
    private val textFieldPaste = JTextField(TEXT)
    private val basicPanel = JPanel(BorderLayout())
    private val createDialog = CreateDialog(noteApplicationService = noteApplicationService, owner = this, modal = true)

    init {
        labelDropDown.horizontalTextPosition = SwingConstants.CENTER
        labelDropDown.horizontalAlignment = SwingConstants.CENTER

        setSize(150, 100)
        isAlwaysOnTop = true
        isUndecorated = true

        MoveUtil.addMoveAction(movableComponent = this, grabbedComponent = labelDropDown)
        addDropTargetTo(labelDropDown)
        addDropTargetTo(textFieldPaste)
        addCtrlV(textFieldPaste)

        basicPanel.add(labelDropDown, BorderLayout.CENTER)
        basicPanel.add(textFieldPaste, BorderLayout.SOUTH)
        basicPanel.border = BorderFactory.createLineBorder(Color.GRAY)

        add(basicPanel, BorderLayout.CENTER)
        isUndecorated = true
        setLocationRelativeTo(null)
    }

    private fun addDropTargetTo(component: JComponent) {
        component.dropTarget = DropTarget(
            component,
            object : DropTargetAdapter() {
                private val defaultBorder = component.border

                override fun drop(dtde: DropTargetDropEvent) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY)
                        val text = dtde.transferable.getTransferData(DataFlavor.stringFlavor) as String

                        showDialogSave(text)
                    } catch (e: UnsupportedFlavorException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    component.border = defaultBorder
                }

                override fun dragEnter(dtde: DropTargetDragEvent?) {
                    component.border = BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createDashedBorder(null, 5F, 5F)
                    )
                }

                override fun dragExit(dte: DropTargetEvent?) {
                    component.border = defaultBorder
                }
            }
        )
    }

    private fun addCtrlV(component: JTextField) {
        component.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (e.isControlDown && e.keyCode == KeyEvent.VK_V) {
                    val text = getClipboardContents()
                    text?.let { showDialogSave(it) }
                    component.text = TEXT
                }
            }
        })
    }


    private fun showDialogSave(text: String) {
        SwingUtilities.invokeLater { createDialog.showDialog(text) }
    }

    //http://www.javapractices.com/topic/TopicAction.do?Id=82
    private fun getClipboardContents(): String? {
        var result: String? = null
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        try {
            val contents: Transferable? = clipboard.getContents(null)
            if ((contents != null) &&
                contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                result = contents.getTransferData(DataFlavor.stringFlavor) as String?
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return result
    }

    private class CreateDialog(
        private val noteApplicationService: NoteApplicationService,
        owner: JDialog,
        modal: Boolean
    ) : JDialog(owner, modal) {
        private val textPaneText = JTextPane()
        private val textPaneDescription = JTextPane()
        private val buttonSave = JButton("Сохранить")

        init {
            layout = BorderLayout()

            val dialog = this

            dialog.setIconImage(IconRepositoryFactory.repository.by(AppIcon.ROCKET_APP).toImage())

            add(JScrollPane(textPaneText), BorderLayout.NORTH)
            add(JScrollPane(textPaneDescription), BorderLayout.CENTER)
            add(buttonSave, BorderLayout.SOUTH)
            size = Dimension(450, 300)
            defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
            setLocationRelativeTo(owner)

            buttonSave.addActionListener {
                object : SwingWorker<Note, Any>() {
                    override fun doInBackground(): Note =
                        createNote(
                            text = textPaneText.text,
                            description = textPaneDescription.text,
                        )

                    override fun done() {
                        try {
                            val note = get()
                            NotificationFactory
                                .notification
                                .show(type = NotificationType.INFO, text = "Заметка сохранена. $note")
                            dialog.isVisible = false
                        } catch (ex: Exception) {
                            logger.warn(ex) { "Error when save note ${textPaneText.text} ${textPaneDescription.text}" }

                            NotificationFactory
                                .notification
                                .show(type = NotificationType.WARN, text = "Ошибка сохранения заметки")
                        }
                    }
                }.execute()
            }
        }

        private fun createNote(text: String, description: String): Note =
            noteApplicationService.create(text = text, description = description)

        fun showDialog(text: String) {
            title = text.takeIf { it.length >= 60 }?.substring(0, 60) ?: text
            textPaneText.text = ""
            textPaneDescription.text = ""
            if (text.startsWith("http")) {
                buttonSave.isEnabled = false
                try {
                    UrlLoader(
                        url = text,
                        executeOnDone = { value ->
                            textPaneText.text = text
                            textPaneDescription.text = value
                        }
                    ).execute()
                } finally {
                    buttonSave.isEnabled = true
                }
            } else {
                textPaneText.text = text
            }
            isVisible = true
        }

        private class UrlLoader(
            private val url: String,
            private val executeOnDone: (value: String) -> Unit
        ) : SwingWorker<UrlParserResult, String>() {
            override fun doInBackground(): UrlParserResult =
                UrlParser(url = url, headers = emptyMap()).parse(UrlParserFilter())

            override fun done() {
                executeOnDone(this.get().title.orEmpty())
            }
        }
    }
}
