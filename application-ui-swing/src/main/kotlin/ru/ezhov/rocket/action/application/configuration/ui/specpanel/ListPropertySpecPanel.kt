package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import javax.swing.JComboBox

class ListPropertySpecPanel(
    configProperty: RocketActionPropertySpec.ListPropertySpec,
    initValue: InitValue? = null,
) : ValuePanel(MigLayout()) {
    private var valueCallback: () -> SpecValue

    init {
        val default = configProperty.defaultValue.orEmpty()
        val selectedValues = configProperty.valuesForSelect
        if (!selectedValues.contains(default)) {
            selectedValues.toMutableList().add(default)
        }
        val comboBox = JComboBox(selectedValues.toTypedArray())
        comboBox.selectedItem =
            when (initValue == null) {
                true -> default
                else -> {
                    when (selectedValues.contains(initValue.value)) {
                        true -> initValue.value
                        false -> default
                    }
                }
            }
        add(comboBox
            .also { l ->
                valueCallback = { SpecValue(l.selectedItem.toString(), null) }
            },
            "width 100%"
        )
    }

    override fun value(): SpecValue = valueCallback()
}
