package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.plugin.clipboard.ClipboardUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTextField
import javax.swing.KeyStroke

class CreateAndEditAtomicActionDialog(
    private val atomicActionService: AtomicActionService,
    actionExecutor: ActionExecutor,
) : JDialog() {
    private val contentPane = JPanel(MigLayout(/*"debug"*/))
    private val buttonSave: JButton = JButton("Save")
    private val buttonCancel: JButton = JButton("Cancel")

    private val idTextField: JTextField = JTextField().apply { isEditable = false }
    private val idLabel: JLabel = JLabel("ID:").apply { labelFor = idTextField }
    private val idButton: JButton = JButton(Icons.Advanced.COPY_16x16).apply {
        toolTipText = "Copy ID to clipboard"
        addActionListener {
            ClipboardUtil.copyToClipboard(idTextField.text)
        }
    }

    private val nameTextField: JTextField = JTextField()
    private val nameLabel: JLabel = JLabel("Name:").apply { labelFor = nameTextField }

    private val aliasTextField: JTextField = JTextField()
    private val aliasLabel: JLabel = JLabel("Alias:").apply { labelFor = aliasTextField }

    private val descriptionTextPane: RSyntaxTextArea = RSyntaxTextArea()
    private val descriptionLabel: JLabel = JLabel("Description:").apply { labelFor = descriptionTextPane }

    private val contractLabel: JLabel = JLabel("Contract:")
    private val inOutRadioButton: JRadioButton = JRadioButton("IN and OUT")
    private val inUnitRadioButton: JRadioButton = JRadioButton("IN and UNIT")
    private val unitOutRadioButton: JRadioButton = JRadioButton("UNIT and OUT")
    private val unitUnitRadioButton: JRadioButton = JRadioButton("UNIT and UNIT")

    private val engineLabel: JLabel = JLabel("Engine:")
    private val kotlinEngine: JRadioButton = JRadioButton("Kotlin")
    private val groovyEngine: JRadioButton = JRadioButton("Groovy")

    private val sourceLabel: JLabel = JLabel("Source:")
    private val textSource: JRadioButton = JRadioButton("Text")
    private val fileSource: JRadioButton = JRadioButton("File")

    private val dataTextPane: RSyntaxTextArea = RSyntaxTextArea()

    private val dataText = listOf(
        "Data:",
    ) + actionExecutor.additionalVariables().map { "<b>${it.name}</b> - ${it.description}" }

    private val dataLabel: JLabel =
        JLabel("<html>${dataText.joinToString(separator = "<br>")}").apply { labelFor = dataTextPane }

    init {
        kotlinEngine.isSelected = true
        val engineButtonGroup = ButtonGroup()
        engineButtonGroup.add(kotlinEngine)
        engineButtonGroup.add(groovyEngine)

        textSource.isSelected = true
        val sourceButtonGroup = ButtonGroup()
        sourceButtonGroup.add(fileSource)
        sourceButtonGroup.add(textSource)

        inOutRadioButton.isSelected = true
        val contractButtonGroup = ButtonGroup()
        contractButtonGroup.add(inOutRadioButton)
        contractButtonGroup.add(inUnitRadioButton)
        contractButtonGroup.add(unitOutRadioButton)
        contractButtonGroup.add(unitUnitRadioButton)

        contentPane.add(idLabel, "split 3")
        contentPane.add(idTextField, "wmin 25%")
        contentPane.add(idButton, "wrap")

        contentPane.add(nameLabel, "split 4")
        contentPane.add(nameTextField, "wmin 25%")
        contentPane.add(aliasLabel)
        contentPane.add(aliasTextField, "wrap, wmin 25%")


        contentPane.add(descriptionLabel, "wrap")
        contentPane.add(RTextScrollPane(descriptionTextPane, false), "span, grow, shrink 50, hmin 18%")

        contentPane.add(contractLabel, "split 5")
        contentPane.add(inOutRadioButton)
        contentPane.add(inUnitRadioButton)
        contentPane.add(unitOutRadioButton)
        contentPane.add(unitUnitRadioButton, "wrap")

        contentPane.add(engineLabel, "split 3")
        contentPane.add(kotlinEngine)
        contentPane.add(groovyEngine, "wrap")

        contentPane.add(sourceLabel, "split 3")
        contentPane.add(textSource)
        contentPane.add(fileSource, "wrap")

        contentPane.add(dataLabel, "wrap")
        contentPane.add(RTextScrollPane(dataTextPane), "span, height max, width max")

        contentPane.add(
            JPanel(MigLayout(/*"debug"*/"insets 0 0 0 0")).apply {
                add(buttonSave, "push, align right")
                add(buttonCancel)
            }, "width max, span"
        )

        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonSave
        buttonSave.addActionListener { onOK() }
        buttonCancel.addActionListener { onCancel() }

        setKotlinSyntax()
        kotlinEngine.addActionListener {
            setKotlinSyntax()
        }

        setGroovySyntax()
        groovyEngine.addActionListener {
            setGroovySyntax()
        }

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

        size = SizeUtil.dimension(0.8, 0.8)
        setLocationRelativeTo(null)
    }

    private fun setKotlinSyntax() {
        if (kotlinEngine.isSelected) {
            dataTextPane.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_KOTLIN;
        }
    }

    private fun setGroovySyntax() {
        if (groovyEngine.isSelected) {
            dataTextPane.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_GROOVY;
        }
    }

    // TODO ezhov
    private fun onOK() {
        when (dialogType!!) {
            DialogType.CREATE -> {
                atomicActionService.addAtomic(
                    AtomicAction(
                        id = UUID.randomUUID().toString(),
                        name = nameTextField.text,
                        description = descriptionTextPane.text,
                        contractType = if (inOutRadioButton.isSelected) {
                            ContractType.IN_OUT
                        } else if (inUnitRadioButton.isSelected) {
                            ContractType.IN_UNIT
                        } else if (unitOutRadioButton.isSelected) {
                            ContractType.UNIT_OUT
                        } else {
                            ContractType.UNIT_UNIT
                        },
                        engine = if (groovyEngine.isSelected) {
                            AtomicActionEngine.GROOVY
                        } else {
                            AtomicActionEngine.KOTLIN
                        },
                        source = if (textSource.isSelected) {
                            AtomicActionSource.TEXT
                        } else {
                            AtomicActionSource.FILE
                        },
                        data = dataTextPane.text,
                        alias = aliasTextField.text.takeIf { it.isNotEmpty() },
                    )
                )
            }

            DialogType.EDIT -> {
                currentAction!!.apply {
                    name = nameTextField.text
                    description = descriptionTextPane.text
                    contractType = if (inOutRadioButton.isSelected) {
                        ContractType.IN_OUT
                    } else if (inUnitRadioButton.isSelected) {
                        ContractType.IN_UNIT
                    } else if (unitOutRadioButton.isSelected) {
                        ContractType.UNIT_OUT
                    } else {
                        ContractType.UNIT_UNIT
                    }
                    engine = if (groovyEngine.isSelected) {
                        AtomicActionEngine.GROOVY
                    } else {
                        AtomicActionEngine.KOTLIN
                    }
                    source = if (textSource.isSelected) {
                        AtomicActionSource.TEXT
                    } else {
                        AtomicActionSource.FILE
                    }
                    data = dataTextPane.text
                    alias = aliasTextField.text.takeIf { it.isNotEmpty() }
                }

                atomicActionService.updateAtomic(atomicAction = currentAction!!)
            }
        }

        dispose()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

    private var dialogType: DialogType? = null

    fun showCreateDialog() {
        title = "Create atomic action"

        dialogType = DialogType.CREATE

        nameTextField.text = ""
        descriptionTextPane.text = ""
        kotlinEngine.isSelected = true
        textSource.isSelected = true
        dataTextPane.text = ""
        aliasTextField.text = ""

        isModal = true
        isVisible = true
    }

    private var currentAction: AtomicAction? = null

    fun showEditDialog(action: AtomicAction) {
        title = "Edit atomic action"

        dialogType = DialogType.EDIT

        this.currentAction = action

        idTextField.text = action.id

        nameTextField.text = action.name

        descriptionTextPane.text = action.description

        when (action.contractType) {
            ContractType.IN_OUT -> inOutRadioButton.isSelected = true
            ContractType.IN_UNIT -> inUnitRadioButton.isSelected = true
            ContractType.UNIT_OUT -> unitOutRadioButton.isSelected = true
            ContractType.UNIT_UNIT -> unitUnitRadioButton.isSelected = true
        }

        when (action.engine) {
            AtomicActionEngine.KOTLIN -> kotlinEngine.isSelected = true
            AtomicActionEngine.GROOVY -> groovyEngine.isSelected = true
        }

        when (action.source) {
            AtomicActionSource.FILE -> fileSource.isSelected = true
            AtomicActionSource.TEXT -> textSource.isSelected = true
        }
        dataTextPane.text = action.data
        aliasTextField.text = action.alias.orEmpty()

        isModal = true
        isVisible = true
    }

    private enum class DialogType {
        CREATE, EDIT
    }
}
