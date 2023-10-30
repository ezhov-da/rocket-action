package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.eventui.model.ShowChainActionConfigurationUiEvent
import ru.ezhov.rocket.action.ui.utils.swing.common.MoveUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.Color
import java.awt.Component
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
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

class ChainBasePanel(
    private val movableComponent: Component,
    chainActionExecutorService: ChainActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
) : JPanel(MigLayout(/*"debug"*/)) {
    private val labelDropDown =
        JLabel("<html><center>Drag text<br>to run chain<br>or paste here<br>or type and `Enter`</center>")
    private val textFieldPaste = TextFieldWithText()
    private val chainExecuteStatusPanel =
        ChainExecuteStatusPanel(chainActionExecutorService).apply { isVisible = false }
    private val configurationButton = JButton("C").apply {
        toolTipText = "Open configuration"
    }

    private val popupMenu = JPopupMenu()
    private val menuItemSelectChain = JMenuItem("Select chain")
    private val menuItemOpenConfiguration = JMenuItem("Open configuration")

    private val currentTimer: Timer = Timer()

    init {
        labelDropDown.horizontalTextPosition = SwingConstants.CENTER
        labelDropDown.horizontalAlignment = SwingConstants.CENTER

        MoveUtil.addMoveAction(movableComponent = movableComponent, grabbedComponent = labelDropDown)
        addDropTargetTo(labelDropDown)
        addDropTargetTo(textFieldPaste)
        addCtrlV(textFieldPaste)
        addEnter(textFieldPaste)

        add(labelDropDown, "wrap, width max, height max, id labelDropDown")
        add(textFieldPaste, "width max, split 2")
        add(configurationButton, "wrap, wmax 25")
        add(chainExecuteStatusPanel, "width max, hidemode 2")

        popupMenu.add(menuItemSelectChain)
        popupMenu.add(menuItemOpenConfiguration)

        val chainBasePanel = this

        configurationButton.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                popupMenu.show(configurationButton, 0, 0)
            }
        })

        menuItemSelectChain.addActionListener {
            showSelectedChain(null)
        }
        menuItemOpenConfiguration.addActionListener {
            ConfigurationUiObserverFactory.observer.notify(ShowChainActionConfigurationUiEvent(chainBasePanel))
        }

        border = BorderFactory.createLineBorder(Color.GRAY)
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
        val chains = chainActionService.chains()
        val chainBasePanel = this
        SelectChainDialog(
            actionService = atomicActionService,
            chains = chains,
        ) { chain ->
            chainExecuteStatusPanel.isVisible = true

            chainExecuteStatusPanel.executeChain(input = text, chainAction = chain) {
                currentTimer.schedule(object : TimerTask() {
                    override fun run() {
                        SwingUtilities.invokeLater { chainExecuteStatusPanel.isVisible = false }
                    }
                }, 10000)
            }
        }.apply {
            setLocationRelativeTo(chainBasePanel)
            isVisible = true
        }
    }

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
