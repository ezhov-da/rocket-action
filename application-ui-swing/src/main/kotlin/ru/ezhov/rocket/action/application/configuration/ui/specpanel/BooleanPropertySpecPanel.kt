package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import javax.swing.JCheckBox

class BooleanPropertySpecPanel(
    private val configProperty: RocketActionPropertySpec.BooleanPropertySpec,
    initValue: InitValue? = null,
) : ValuePanel(MigLayout()) {
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
                }
        )
    }

    override fun value(): SpecValue = valueCallback()
}
