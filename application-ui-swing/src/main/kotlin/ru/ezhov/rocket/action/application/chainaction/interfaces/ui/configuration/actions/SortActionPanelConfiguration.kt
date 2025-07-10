package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.resources.Icons
import java.awt.event.ActionListener
import java.beans.PropertyChangeListener
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JToggleButton

class SortActionPanelConfiguration : JPanel(MigLayout("insets 0")) {
    companion object {
        const val SORT_INFO_PROPERTY_NAME = "sortInfo"
    }

    private val sortDirectionPanel = SortDirectionPanel()
    private val sortFieldsPanel = SortFieldsPanel()

    init {
        border = BorderFactory.createTitledBorder("Sort")

        add(sortDirectionPanel)
        add(sortFieldsPanel)

        val propertyChangeListener = PropertyChangeListener {
            if (it.propertyName == "direction" || it.propertyName == "sortField") {
                firePropertyChange(SORT_INFO_PROPERTY_NAME, null, sortInfo())
            }
        }

        sortDirectionPanel.addPropertyChangeListener(propertyChangeListener)
        sortFieldsPanel.addPropertyChangeListener(propertyChangeListener)
    }

    fun sortInfo(): SortInfo = SortInfo(
        direction = sortDirectionPanel.direction(),
        sortField = sortFieldsPanel.sortField(),
    )
}

data class SortInfo(
    val direction: Direction,
    val sortField: SortField,
)

enum class Direction {
    ASC, DESC
}

enum class SortField {
    NAME, CONTRACT, ENGINE, SOURCE
}

private class SortDirectionPanel : JPanel(MigLayout("insets 0")) {
    private val directionLabel = JLabel("Direction:")
    private val azButton = JToggleButton(Icons.Standard.ARROW_BOTTOM_16x16)
    private val zaButton = JToggleButton(Icons.Standard.ARROW_TOP_16x16)

    init {
        ButtonGroup().apply {
            add(azButton)
            add(zaButton)
        }
        azButton.isSelected = true
        add(directionLabel)
        add(azButton)
        add(zaButton)

        val actionListener = ActionListener {
            firePropertyChange("direction", null, direction())
        }

        azButton.addActionListener(actionListener)
        zaButton.addActionListener(actionListener)
    }

    fun direction(): Direction = when {
        azButton.isSelected -> Direction.ASC
        zaButton.isSelected -> Direction.DESC
        else -> throw IllegalStateException("Wrong direction")
    }
}

private class SortFieldsPanel : JPanel(MigLayout("insets 0")) {
    private val fieldLabel = JLabel("Field:")
    private val nameButton = JToggleButton("Name")
    private val contractButton = JToggleButton("Contract")
    private val engineButton = JToggleButton("Engine")
    private val sourceButton = JToggleButton("Source")

    init {
        ButtonGroup().apply {
            add(nameButton)
            add(contractButton)
            add(engineButton)
            add(sourceButton)
        }
        nameButton.isSelected = true
        add(fieldLabel)
        add(nameButton)
        add(contractButton)
        add(engineButton)
        add(sourceButton)

        val actionListener = ActionListener {
            firePropertyChange("sortField", null, sortField())
        }

        nameButton.addActionListener(actionListener)
        contractButton.addActionListener(actionListener)
        engineButton.addActionListener(actionListener)
        sourceButton.addActionListener(actionListener)
    }

    fun sortField(): SortField = when {
        nameButton.isSelected -> SortField.NAME
        contractButton.isSelected -> SortField.CONTRACT
        engineButton.isSelected -> SortField.ENGINE
        sourceButton.isSelected -> SortField.SOURCE
        else -> throw IllegalStateException("Wrong sort field")
    }
}
