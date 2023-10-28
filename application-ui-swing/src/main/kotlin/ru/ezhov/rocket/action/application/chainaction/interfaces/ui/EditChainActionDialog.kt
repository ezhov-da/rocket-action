package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import java.awt.BorderLayout
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
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

class EditChainActionDialog(
    private val chainActionExecutorService: ChainActionExecutorService,
    private val chainActionService: ChainActionService,
) : JDialog() {
    private val contentPane = JPanel(MigLayout("insets 5"/*"debug"*/))
    private val buttonOK: JButton = JButton("Create")
    private val buttonCancel: JButton? = JButton("Cancel")

    private val nameTextField: JTextField = JTextField()
    private val nameLabel: JLabel = JLabel("Name:").apply { labelFor = nameTextField }

    private val descriptionTextPane: JTextPane = JTextPane()
    private val descriptionLabel: JLabel = JLabel("Description:").apply { labelFor = descriptionTextPane }

    private val allListActionsModel = DefaultListModel<AtomicAction>()
    private val allListActions = JList(allListActionsModel)
    private val selectedListActionsModel = DefaultListModel<AtomicAction>()
    private val selectedListActions = JList(selectedListActionsModel)

    init {
        setContentPane(contentPane)

        contentPane.add(
            JPanel(BorderLayout())
                .apply {
                    add(JScrollPane(allListActions), BorderLayout.CENTER)
                    border = BorderFactory.createTitledBorder("All atomic actions")
                },
            "grow, west, wmin 40%, height max"
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

        contentPane.add(buttonOK, "cell 2 4, split 2, align right")
        contentPane.add(buttonCancel)

        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK.addActionListener { onOK() }
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
        // add your code here
        dispose()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

    private var action: ChainAction? = null

    fun setAtomicAction(action: ChainAction) {
        this.action = action
    }
}
