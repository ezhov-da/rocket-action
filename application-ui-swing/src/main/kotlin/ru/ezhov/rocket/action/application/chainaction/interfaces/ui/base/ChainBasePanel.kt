package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.eventui.model.ShowChainActionConfigurationUiEvent
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
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
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingUtilities

class ChainBasePanel(
    actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
    private val configurationApplication: ConfigurationApplication,
    private val searchTextTransformer: SearchTextTransformer,
    private val actionSchedulerService: ActionSchedulerService,
) : JPanel(MigLayout(/*"debug"*/"insets 0 0 5 0" /*Убираем отступы, оставляем только снизу для отображения действий*/)) {
    private val textFieldPaste = TextFieldWithText("...")
        .apply {
            toolTipText = "<html><center>Drag text to run chain<br>or paste here or type and `Enter`</center>"
        }
    private val actionExecuteStatusPanel =
        ActionExecuteStatusPanel(actionExecutorService).apply { isVisible = false }
    private val openAvailableActionsButton = JButton(Icons.Advanced.ROCKET_BLACK_16x16).apply {
        toolTipText = "Open available actions"
    }
    private val openConfigurationButton = JButton(Icons.Standard.PENCIL_16x16).apply {
        toolTipText = "Open configuration"
    }

    private val currentTimer: Timer = Timer()

    init {
        addDropTargetTo(textFieldPaste)
        addCtrlV(textFieldPaste)
        addEnter(textFieldPaste)

        add(textFieldPaste, "wmax 25px, grow 0, split")
        add(openAvailableActionsButton, "wmax 25px")
        add(openConfigurationButton, "wmax 25px, wrap")
        add(actionExecuteStatusPanel, "hmax 6, width 100%, hidemode 2")

        val chainBasePanel = this

        openAvailableActionsButton.addActionListener {
            showSelectedChain(null)
        }

        openConfigurationButton.addActionListener {
            ConfigurationUiObserverFactory.observer.notify(ShowChainActionConfigurationUiEvent(chainBasePanel))
        }
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

                        showSelectedChain(text)
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
                    text?.let { showSelectedChain(it) }
                }
            }
        })
    }

    private fun addEnter(component: JTextField) {
        component.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    val text = component.text
                    text?.let { showSelectedChain(it) }
                }
            }
        })
    }

    private fun showSelectedChain(text: String?) {
        val chainBasePanel = this
        SelectChainPopupMenu(
            actionService = atomicActionService,
            chainActionService = chainActionService,
            chains = chainActionService.chains(),
            atomics = atomicActionService.atomics(),
            searchTextTransformer = searchTextTransformer,
            configurationApplication = configurationApplication,
            actionSchedulerService = actionSchedulerService,
        ) { chain ->
            actionExecuteStatusPanel.isVisible = true

            actionExecuteStatusPanel.executeChain(input = text, action = chain) {
                textFieldPaste.text = ""

                currentTimer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            SwingUtilities.invokeLater { actionExecuteStatusPanel.isVisible = false }
                        }
                    }, 60000
                )
            }
        }.apply {
            SwingUtilities.invokeLater {
                show(chainBasePanel, 0, 0)
                activateSearchField()
            }
        }
    }

    fun getSearchTextField() = textFieldPaste

    //http://www.javapractices.com/topic/TopicAction.do?Id=82
    private fun getClipboardContents(): String? {
        var result: String? = null
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        try {
            val contents: Transferable? = clipboard.getContents(null)
            if ((contents != null) &&
                contents.isDataFlavorSupported(DataFlavor.stringFlavor)
            ) {
                result = contents.getTransferData(DataFlavor.stringFlavor) as String?
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return result
    }
}
