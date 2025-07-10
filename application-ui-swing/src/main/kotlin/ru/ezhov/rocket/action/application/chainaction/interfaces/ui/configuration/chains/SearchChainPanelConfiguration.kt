package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.chains

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.InputValueContractType
import ru.ezhov.rocket.action.application.chainaction.domain.model.OutputValueContractType
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.ChainIcons
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToggleButton

class SearchChainPanelConfiguration : JPanel(MigLayout("insets 0")) {
    companion object {
        const val SEARCH_ACTION_PROPERTY_NAME = "changed"
    }

    private val searchTextTransformer = SearchTextTransformer.INSTANCE!!
    private val allContractButton = JToggleButton("All")
    private val inOutButton = JToggleButton(ChainIcons.IN_OUT_ICON_16x16)
    private val inUnitButton = JToggleButton(ChainIcons.IN_UNIT_ICON_16x16)
    private val unitOutButton = JToggleButton(ChainIcons.UNIT_OUT_ICON_16x16)
    private val unitUnitButton = JToggleButton(ChainIcons.UNIT_UNIT_ICON_16x16)

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

        allContractButton.isSelected = true
//        add(allContractButton)
//        add(inOutButton, "wmax 35, hmax 25")
//        add(inUnitButton, "wmax 35, hmax 25")
//        add(unitOutButton, "wmax 35, hmax 25")
//        add(unitUnitButton, "wmax 35, hmax 25")

        add(textField, "width 100%")
        add(resetButton, "wmax 25, hmax 25")

        val actionListener = ActionListener {
            firePropertyChange(
                SEARCH_ACTION_PROPERTY_NAME,
                null,
                SearchAction.SearchInfo(conditions = searchConditions(), text = searchText(textField.text))
            )
        }

        allContractButton.addActionListener(actionListener)
        inOutButton.addActionListener(actionListener)
        inUnitButton.addActionListener(actionListener)
        unitOutButton.addActionListener(actionListener)
        unitUnitButton.addActionListener(actionListener)

        textField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                firePropertyChange(
                    SEARCH_ACTION_PROPERTY_NAME,
                    null,
                    SearchAction.SearchInfo(conditions = searchConditions(), text = searchText(textField.text))
                )
            }
        })
        resetButton.addActionListener {
            textField.text = ""
            firePropertyChange(SEARCH_ACTION_PROPERTY_NAME, null, SearchAction.Reset(searchConditions()))
        }
    }

    fun searchAction(): SearchAction = SearchAction.SearchInfo(conditions = searchConditions(), text = searchText(textField.text))

    private fun searchConditions(): List<SearchAction.SearchCondition> =
        mutableListOf<SearchAction.SearchCondition>().apply {
            inOutButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.IN_OUT) }
            inUnitButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.IN_UNIT) }
            unitOutButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.UNIT_OUT) }
            unitUnitButton.takeIf { it.isSelected }?.let { add(SearchAction.SearchCondition.UNIT_UNIT) }
        }

    private fun searchText(inputText: String): List<String> =
        searchTextTransformer.transformedText(inputText)
}

sealed class SearchAction(
    val conditions: List<SearchCondition>,
) {
    class Reset(
        conditions: List<SearchCondition>,
    ) : SearchAction(conditions)

    class SearchInfo(
        conditions: List<SearchCondition>,
        val text: List<String>
    ) : SearchAction(conditions)

    enum class SearchCondition(
        val input: InputValueContractType,
        val outputValueContractType: OutputValueContractType
    ) {
        IN_OUT(InputValueContractType.IN, OutputValueContractType.OUT),
        IN_UNIT(InputValueContractType.IN, OutputValueContractType.UNIT),
        UNIT_OUT(InputValueContractType.UNIT, OutputValueContractType.OUT),
        UNIT_UNIT(InputValueContractType.UNIT, OutputValueContractType.UNIT),
    }
}


