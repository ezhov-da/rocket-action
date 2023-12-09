package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.dnd.DragListener
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.dnd.ListDropHandler
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.AtomicActionListCellRenderer
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.OrderAtomicActionListCellRenderer
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import ru.ezhov.rocket.action.application.icon.interfaces.ui.SelectIconPanel
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.plugin.clipboard.ClipboardUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import java.awt.BorderLayout
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.DropMode
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

class CreateAndEditChainActionDialog(
    private val actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
    private val iconRepository: IconRepository,
) : JDialog() {
    private val contentPane = JPanel(MigLayout("insets 5"/*"debug"*/))
    private val buttonSave: JButton = JButton("Save")
    private val buttonCancel: JButton = JButton("Cancel")

    private val idTextField: JTextField = JTextField().apply { isEditable = false }
    private val idLabel: JLabel = JLabel("ID:").apply { labelFor = idTextField }
    private val copyIdButton: JButton = JButton(Icons.Advanced.COPY_16x16).apply {
        toolTipText = "Copy ID to clipboard"
        addActionListener {
            ClipboardUtil.copyToClipboard(idTextField.text)
        }
    }
    private val selectIconPanel: SelectIconPanel = SelectIconPanel(iconRepository)

    private val nameTextField: JTextField = JTextField()
    private val nameLabel: JLabel = JLabel("Name:").apply { labelFor = nameTextField }

    private val descriptionTextPane: RSyntaxTextArea = RSyntaxTextArea()
    private val descriptionLabel: JLabel = JLabel("Description:").apply { labelFor = descriptionTextPane }

    private val allListActionsModel = DefaultListModel<AtomicAction>()
    private val allListActions = JList(allListActionsModel)
    private val selectedListActionsModel = DefaultListModel<SelectedAtomicAction>()
    private val selectedListActions = JList(selectedListActionsModel)

    init {
        allListActions.cellRenderer = AtomicActionListCellRenderer(chainActionService)
        selectedListActions.cellRenderer = OrderAtomicActionListCellRenderer(chainActionService)
        selectedListActions.selectionMode = ListSelectionModel.SINGLE_SELECTION

        selectedListActions.dragEnabled = true
        selectedListActions.dropMode = DropMode.INSERT
        selectedListActions.transferHandler = ListDropHandler(selectedListActions)
        DragListener(selectedListActions)

        fun fillActions() {
            allListActionsModel.removeAllElements()
            val atomics = atomicActionService.atomics().sortedBy { it.name }
            atomics.forEach {
                allListActionsModel.addElement(it)
            }
        }

        fillActions()

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                fillActions()
            }

            override fun subscribedToEventType(): List<Class<*>> = listOf(
                AtomicActionCreatedDomainEvent::class.java,
                AtomicActionDeletedDomainEvent::class.java,
                AtomicActionUpdatedDomainEvent::class.java,
            )
        })


        setContentPane(contentPane)

        allListActions.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    allListActions.selectedValue?.let {
                        selectedListActionsModel.addElement(
                            SelectedAtomicAction(
                                atomicAction = it
                            )
                        )
                    }
                }
            }
        })

        contentPane.add(idLabel, "split 4")
        contentPane.add(idTextField, "wmin 25%")
        contentPane.add(copyIdButton)
        contentPane.add(selectIconPanel, "wrap")

        contentPane.add(nameLabel)
        contentPane.add(nameTextField, "span 2, wrap, width max, grow")

        contentPane.add(descriptionLabel, "wrap")
        contentPane.add(RTextScrollPane(descriptionTextPane, false), "span, wrap, width max, hmin 30%")

        contentPane.add(
            JPanel(BorderLayout())
                .apply {
                    add(JScrollPane(allListActions), BorderLayout.CENTER)
                    border = BorderFactory.createTitledBorder("All atomic actions")
                },
            "grow, west, wmin 40%, height max"
        )

        contentPane.add(
            JPanel(MigLayout()).apply {
                val deleteButton = JButton("Delete")

                deleteButton.addActionListener {
                    SwingUtilities.invokeLater {
                        selectedListActions.selectedValue?.let { selectedValue ->
                            val index = selectedListActionsModel.indexOf(selectedValue)
                            selectedListActionsModel.removeElement(selectedValue)
                            if (!selectedListActionsModel.isEmpty) {
                                selectedListActions.selectedIndex = index
                            }
                        }
                    }
                }

                add(deleteButton, "wrap")
                add(JScrollPane(selectedListActions), "width max, height max")
                border = BorderFactory.createTitledBorder("Selected atomic actions")
            }, "span, width max, height max"
        )

        contentPane.add(buttonSave, "cell 2 4, split 2, align right")
        contentPane.add(buttonCancel)

        getRootPane().defaultButton = buttonSave
        buttonSave.addActionListener { onOK() }
        buttonCancel.addActionListener { onCancel() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
            { onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        size = SizeUtil.dimension(0.8, 0.8)
        setLocationRelativeTo(null)
    }

    private var dialogType: DialogType? = null
    private var currentEditChainAction: ChainAction? = null
    private fun onOK() {
        when (dialogType!!) {
            DialogType.CREATE -> {
                chainActionService.addChain(
                    ChainAction(
                        id = UUID.randomUUID().toString(),
                        name = nameTextField.text,
                        description = descriptionTextPane.text,
                        actions = selectedListActionsModel.elements().toList().map {
                            ActionOrder(
                                chainOrderId = it.id,
                                actionId = it.atomicAction.id,
                            )
                        },
                        icon = selectIconPanel.selectedIcon(),
                    )
                )
            }

            DialogType.EDIT -> {
                chainActionService.updateChain(currentEditChainAction!!.apply {
                    name = nameTextField.text
                    description = descriptionTextPane.text
                    actions = selectedListActionsModel.elements().toList().map {
                        ActionOrder(
                            chainOrderId = it.id,
                            actionId = it.atomicAction.id,
                        )
                    }
                    icon = selectIconPanel.selectedIcon()
                })

                isVisible = false
            }
        }

        dispose()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

    fun showCreateDialog() {
        title = "Create chain action"
        dialogType = DialogType.CREATE

        idTextField.text = ""
        nameTextField.text = ""
        descriptionTextPane.text = ""
        selectedListActionsModel.removeAllElements()

        selectIconPanel.setIcon(null)

        isVisible = true
    }

    fun showCreateDialogWith(atomicAction: AtomicAction) {
        title = "Create chain action"
        dialogType = DialogType.CREATE

        idTextField.text = ""
        nameTextField.text = ""
        descriptionTextPane.text = ""

        selectedListActionsModel.removeAllElements()

        selectIconPanel.setIcon(null)

        allListActionsModel.elements().toList().firstOrNull { it.id == atomicAction.id }?.let {
            selectedListActionsModel.addElement(
                SelectedAtomicAction(atomicAction = it)
            )
        }

        isVisible = true
    }

    fun showEditDialog(chainAction: ChainAction) {
        title = "Edit chain action"
        dialogType = DialogType.EDIT

        idTextField.text = chainAction.id

        this.currentEditChainAction = chainAction
        nameTextField.text = chainAction.name
        descriptionTextPane.text = chainAction.description

        selectedListActionsModel.removeAllElements()

        selectIconPanel.setIcon(chainAction.icon)

        val actions = atomicActionService.atomics().associateBy { it.id }
        chainAction.actions.forEach { order ->
            actions[order.actionId]?.let { action ->
                selectedListActionsModel.addElement(
                    SelectedAtomicAction(
                        id = order.chainOrderId, atomicAction = action
                    )
                )
            }
        }

        isVisible = true
    }
}

data class SelectedAtomicAction(
    val id: String = UUID.randomUUID().toString(),
    val atomicAction: AtomicAction,
)

private enum class DialogType {
    CREATE, EDIT
}
