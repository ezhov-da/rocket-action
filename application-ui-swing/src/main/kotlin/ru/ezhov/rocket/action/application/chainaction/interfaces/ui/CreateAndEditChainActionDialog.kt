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
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base.ActionExecutePanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.AtomicActionsFilter
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.SearchActionPanelConfiguration
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.SortActionPanelConfiguration
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.dnd.DragListener
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.dnd.ListDropHandler
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.AtomicActionListCellRenderer
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.OrderAtomicActionListCellRenderer
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import ru.ezhov.rocket.action.application.icon.interfaces.ui.SelectIconPanel
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.plugin.clipboard.ClipboardUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.beans.PropertyChangeListener
import java.util.*
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.DropMode
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

class CreateAndEditChainActionDialog(
    actionExecutorService: ActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
    iconRepository: IconRepository,
    actionSchedulerService: ActionSchedulerService,
) : JFrame() {

    private val createAndEditAtomicActionDialog = CreateAndEditAtomicActionDialog(
        atomicActionService = atomicActionService,
        actionExecutorService = actionExecutorService,
        iconRepository = iconRepository,
    )

    private val contentPane = JPanel(MigLayout("insets 5"/*"debug"*/))

    private val sortActionPanelConfiguration = SortActionPanelConfiguration()
    private val searchActionPanelConfiguration = SearchActionPanelConfiguration()

    private val buttonSave: JButton = JButton("Save")
    private val buttonSaveAndClose: JButton = JButton("Save & Close")
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

    private val actionExecutePanel = ActionExecutePanel(actionExecutorService)

    init {
        allListActions.cellRenderer = AtomicActionListCellRenderer(
            chainActionService = chainActionService,
            actionSchedulerService = actionSchedulerService,
        )
        selectedListActions.cellRenderer = OrderAtomicActionListCellRenderer(
            chainActionService = chainActionService,
            actionSchedulerService = actionSchedulerService
        )
        selectedListActions.selectionMode = ListSelectionModel.SINGLE_SELECTION

        selectedListActions.dragEnabled = true
        selectedListActions.dropMode = DropMode.INSERT
        selectedListActions.transferHandler = ListDropHandler(selectedListActions)
        DragListener(selectedListActions)

        val propertyChangeListener = PropertyChangeListener {
            if (
                it.propertyName == SortActionPanelConfiguration.SORT_INFO_PROPERTY_NAME ||
                it.propertyName == SearchActionPanelConfiguration.SEARCH_ACTION_PROPERTY_NAME
            ) {
                fillList()
            }
        }

        fillList()

        sortActionPanelConfiguration.addPropertyChangeListener(propertyChangeListener)
        searchActionPanelConfiguration.addPropertyChangeListener(propertyChangeListener)

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                fillList()
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

                if (e.button == MouseEvent.BUTTON3) {
                    allListActions.selectedValue?.let {
                        showPopupMenu(component = allListActions, element = it, event = e)
                    }
                }
            }
        })

        selectedListActions.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    selectedListActions.selectedValue?.let { value ->
                        createAndEditAtomicActionDialog.showEditDialog(value.atomicAction)
                    }
                }

                if (e.button == MouseEvent.BUTTON3) {
                    selectedListActions.selectedValue?.let {
                        showPopupMenu(component = selectedListActions, element = it.atomicAction, event = e)
                    }
                }
            }
        })

        contentPane.add(idLabel, "split 4")
        contentPane.add(idTextField, "wmin 25%")
        contentPane.add(copyIdButton)
        contentPane.add(selectIconPanel, "wrap")

        contentPane.add(nameLabel, "split 2")
        contentPane.add(nameTextField, "wrap, width max, grow")

        contentPane.add(descriptionLabel, "wrap")
        contentPane.add(RTextScrollPane(descriptionTextPane, false), "span, wrap, width max, hmin 18%")

        contentPane.add(
            JPanel(MigLayout())
                .apply {
                    add(sortActionPanelConfiguration, "width 100%, wrap")
                    add(searchActionPanelConfiguration, "width 100%, wrap")
                    add(JScrollPane(allListActions), "height 100%, width 100%")
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

        contentPane.add(actionExecutePanel, "hidemode 3, span, grow, h 150!")

        contentPane.add(
            JPanel(MigLayout(/*"debug"*/"insets 0 0 0 0")).apply {
                add(JLabel(), "push, align right")
                add(buttonSave)
                add(buttonSaveAndClose)
                add(buttonCancel)
            }, "width max, span"
        )

        getRootPane().defaultButton = buttonSave
        buttonSave.addActionListener { onOK(isClosed = false) }
        buttonSaveAndClose.addActionListener { onOK(isClosed = true) }
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

    private fun fillList() {
        val sortInfo = sortActionPanelConfiguration.sortInfo()
        val searchAction = searchActionPanelConfiguration.searchAction()

        val filtered = AtomicActionsFilter.filter(sortInfo, searchAction, atomicActionService.atomics())

        allListActionsModel.removeAllElements()
        filtered.forEach {
            allListActionsModel.addElement(it)
        }
    }

    private var dialogType: DialogType? = null
    private var currentEditChainAction: ChainAction? = null
    private fun onOK(isClosed: Boolean) {
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
            }
        }

        if (isClosed) {
            dispose()
        }
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

        actionExecutePanel.isVisible = false
        buttonSave.isVisible = false

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

        actionExecutePanel.isVisible = false
        buttonSave.isVisible = false

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

        actionExecutePanel.isVisible = true
        actionExecutePanel.setCurrentAction(chainAction)

        selectedListActionsModel.removeAllElements()

        selectIconPanel.setIcon(chainAction.icon)

        buttonSave.isVisible = true

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

    private fun showPopupMenu(component: JComponent, element: AtomicAction, event: MouseEvent) {
        val popup = JPopupMenu()
        popup.add(
            JMenu("Info").apply {
                add(
                    InfoActionPopupMenuPanel(action = element)
                )
            }
        )

        popup.show(component, event.x, event.y)
    }
}

data class SelectedAtomicAction(
    val id: String = UUID.randomUUID().toString(),
    val atomicAction: AtomicAction,
)

private enum class DialogType {
    CREATE, EDIT
}
