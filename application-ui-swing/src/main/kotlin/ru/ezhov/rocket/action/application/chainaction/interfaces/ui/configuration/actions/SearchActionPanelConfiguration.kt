package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.ChainIcons
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToggleButton

class SearchActionPanelConfiguration : JPanel(MigLayout()) {
    companion object {
        const val SEARCH_ACTION_PROPERTY_NAME = "changed"
    }

    private val allContractButton = JToggleButton("All")
    private val inOutButton = JToggleButton(ChainIcons.IN_OUT_ICON_16x16)
    private val inUnitButton = JToggleButton(ChainIcons.IN_UNIT_ICON_16x16)
    private val unitOutButton = JToggleButton(ChainIcons.UNIT_OUT_ICON_16x16)
    private val unitUnitButton = JToggleButton(ChainIcons.UNIT_UNIT_ICON_16x16)
    private val allEngineButton = JToggleButton("All")
    private val kotlinButton = JToggleButton(Icons.Advanced.KOTLIN_16x16)
    private val groovyButton = JToggleButton(Icons.Advanced.GROOVY_16x16)
    private val allSourceButton = JToggleButton("All")
    private val textButton = JToggleButton(Icons.Standard.ALIGN_CENTER_16x16)
    private val fileButton = JToggleButton(Icons.Standard.FILE_16x16)

    private val textField = TextFieldWithText("Search")
    private val resetButton = JButton(Icons.Standard.X_16x16)

    init {
        border = BorderFactory.createTitledBorder("Search")

        ButtonGroup().apply {
            add(allContractButton)
            add(inOutButton)
            add(inUnitButton)
            add(unitOutButton)
            add(unitUnitButton)
        }

        ButtonGroup().apply {
            add(allEngineButton)
            add(kotlinButton)
            add(groovyButton)
        }

        ButtonGroup().apply {
            add(allSourceButton)
            add(textButton)
            add(fileButton)
        }

        allContractButton.isSelected = true
        add(allContractButton)
        add(inOutButton, "wmax 35, hmax 25")
        add(inUnitButton, "wmax 35, hmax 25")
        add(unitOutButton, "wmax 35, hmax 25")
        add(unitUnitButton, "wmax 35, hmax 25")

        allEngineButton.isSelected = true
        add(allEngineButton)
        add(kotlinButton, "wmax 35, hmax 25")
        add(groovyButton, "wmax 35, hmax 25")

        allSourceButton.isSelected = true
        add(allSourceButton)
        add(textButton, "wmax 35, hmax 25")
        add(fileButton, "wmax 35, hmax 25")

        add(textField, "width 100%")
        add(resetButton, "wmax 25, hmax 25")

        val actionListener = ActionListener {
            firePropertyChange(
                SEARCH_ACTION_PROPERTY_NAME,
                null,
                SearchAction.SearchInfo(conditions = searchConditions(), text = textField.text)
            )
        }

        allContractButton.addActionListener(actionListener)
        inOutButton.addActionListener(actionListener)
        inUnitButton.addActionListener(actionListener)
        unitOutButton.addActionListener(actionListener)
        unitUnitButton.addActionListener(actionListener)


        allEngineButton.addActionListener(actionListener)
        kotlinButton.addActionListener(actionListener)
        groovyButton.addActionListener(actionListener)

        allSourceButton.addActionListener(actionListener)
        textButton.addActionListener(actionListener)
        fileButton.addActionListener(actionListener)


        textField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                firePropertyChange(
                    SEARCH_ACTION_PROPERTY_NAME,
                    null,
                    SearchAction.SearchInfo(conditions = searchConditions(), text = textField.text)
                )
            }
        })
        resetButton.addActionListener {
            textField.text = ""
            firePropertyChange(SEARCH_ACTION_PROPERTY_NAME, null, SearchAction.Reset(searchConditions()))
        }
    }

    fun searchAction(): SearchAction = SearchAction.SearchInfo(conditions = searchConditions(), text = textField.text)

    private fun searchConditions(): List<SearchAction.SearchCondition> =
        mutableListOf<SearchAction.SearchCondition>().apply {
            inOutButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.IN_OUT) }
            inUnitButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.IN_UNIT) }
            unitOutButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.UNIT_OUT) }
            unitUnitButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.UNIT_UNIT) }
            kotlinButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.KOTLIN) }
            groovyButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.GROOVY) }
            textButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.TEXT) }
            fileButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.FILE) }
        }
}

sealed class SearchAction(
    val conditions: List<SearchCondition>,
) {
    class Reset(
        conditions: List<SearchCondition>,
    ) : SearchAction(conditions)

    class SearchInfo(
        conditions: List<SearchCondition>,
        val text: String
    ) : SearchAction(conditions)

    enum class SearchCondition {
        IN_OUT,
        IN_UNIT,
        UNIT_OUT,
        UNIT_UNIT,
        KOTLIN,
        GROOVY,
        TEXT,
        FILE,
    }
}


