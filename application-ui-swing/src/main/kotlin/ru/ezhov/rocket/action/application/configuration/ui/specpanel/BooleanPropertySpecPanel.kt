package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import java.awt.BorderLayout
import javax.swing.JCheckBox

class BooleanPropertySpecPanel(
    private val configProperty: RocketActionPropertySpec.BooleanPropertySpec,
    initValue: InitValue? = null,
) : ValuePanel(BorderLayout()) {
    private var valueCallback: () -> SpecValue

    init {
        add(
            JCheckBox()
                .also { cb ->
                    when (initValue == null) {
                        true -> cb.isSelected = configProperty.defaultValue.toBoolean()
                        false -> cb.isSelected = initValue.value.toBoolean()
                    }

                    valueCallback = { SpecValue(cb.isSelected.toString(), null) }
                },
            BorderLayout.CENTER
        )
    }

    override fun value(): SpecValue = valueCallback()
}
