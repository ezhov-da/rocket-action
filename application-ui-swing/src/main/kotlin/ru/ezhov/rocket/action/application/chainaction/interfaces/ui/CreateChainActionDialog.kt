package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
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
import javax.swing.JTextPane
import javax.swing.KeyStroke
import javax.swing.ListSelectionModel

class CreateChainActionDialog(
    private val chainActionExecutorService: ChainActionExecutorService,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
) : JDialog() {
    private val contentPane = JPanel(MigLayout("insets 5"/*"debug"*/))
    private val buttonCreate: JButton = JButton("Create")
    private val buttonCancel: JButton? = JButton("Cancel")

    private val nameTextField: JTextField = JTextField()
    private val nameLabel: JLabel = JLabel("Name:").apply { labelFor = nameTextField }

    private val descriptionTextPane: JTextPane = JTextPane()
    private val descriptionLabel: JLabel = JLabel("Description:").apply { labelFor = descriptionTextPane }

    private val allListActionsModel = DefaultListModel<AtomicAction>()
    private val allListActions = JList(allListActionsModel)
    private val selectedListActionsModel = DefaultListModel<SelectedAtomicAction>()
    private val selectedListActions = JList(selectedListActionsModel)

    init {
        allListActions.cellRenderer = AtomicActionListCellRenderer()
        selectedListActions.cellRenderer = OrderAtomicActionListCellRenderer()
        selectedListActions.selectionMode = ListSelectionModel.SINGLE_SELECTION

        selectedListActions.dragEnabled = true
        selectedListActions.dropMode = DropMode.INSERT
        selectedListActions.transferHandler = ListDropHandler(selectedListActions)
        DragListener(selectedListActions)

        fun fillActions() {
            allListActionsModel.removeAllElements()
            val atomics = atomicActionService.atomics()
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

        contentPane.add(
            JPanel(BorderLayout())
                .apply {
                    add(JScrollPane(allListActions), BorderLayout.CENTER)
                    border = BorderFactory.createTitledBorder("All atomic actions")
                },
            "grow, west, wmin 40%, height max"
        )

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
        }
        )

        contentPane.add(nameLabel)
        contentPane.add(nameTextField, "span 2, wrap, width max, grow")

        contentPane.add(descriptionLabel, "wrap")
        contentPane.add(JScrollPane(descriptionTextPane), "span, wrap, width max, hmin 30%")

        contentPane.add(
            JPanel(BorderLayout())
                .apply {
                    add(JScrollPane(selectedListActions), BorderLayout.CENTER)
                    border = BorderFactory.createTitledBorder("Selected atomic actions")
                },
            "span, width max, height max"
        )

        contentPane.add(buttonCreate, "cell 2 4, split 2, align right")
        contentPane.add(buttonCancel)

        isModal = true
        getRootPane().defaultButton = buttonCreate
        buttonCreate.addActionListener { onOK() }
        buttonCancel!!.addActionListener { onCancel() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
            { onCancel() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        setLocationRelativeTo(null)
        setSize(500, 500)
    }

    private fun onOK() {
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
            )
        )

        dispose()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

    fun showDialog() {
        nameTextField.text = ""
        descriptionTextPane.text = ""
        selectedListActionsModel.removeAllElements()

        isModal = true
        isVisible = true
    }
}

data class SelectedAtomicAction(
    val id: String = UUID.randomUUID().toString(),
    val atomicAction: AtomicAction,
)
