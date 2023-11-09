package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import java.awt.BorderLayout
import javax.swing.JComboBox
import javax.swing.JScrollPane

class ListPropertySpecPanel(
    configProperty: RocketActionPropertySpec.ListPropertySpec,
    initValue: InitValue? = null,
) : ValuePanel(BorderLayout()) {
    private var valueCallback: () -> SpecValue

    init {
        val default = configProperty.defaultValue.orEmpty()
        val selectedValues = configProperty.valuesForSelect
        if (!selectedValues.contains(default)) {
            selectedValues.toMutableList().add(default)
        }
        val list = JComboBox(selectedValues.toTypedArray())
        list.selectedItem =
            when (initValue == null) {
                true -> default
                else -> {
                    when (selectedValues.contains(initValue.value)) {
                        true -> initValue.value
                        false -> default
                    }
                }
            }
        add(JScrollPane(
            list
                .also { l ->
                    valueCallback = { SpecValue(l.selectedItem.toString(), null) }
                }
        ), BorderLayout.CENTER)
    }

    override fun value(): SpecValue = valueCallback()
}
