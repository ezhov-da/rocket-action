package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import java.awt.BorderLayout
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class IntPropertySpecPanel(
    configProperty: RocketActionPropertySpec.IntPropertySpec,
    initValue: InitValue? = null,
) : ValuePanel(BorderLayout()) {
    private var valueCallback: () -> SpecValue

    init {
        val default = initValue?.value?.toIntOrNull()
            ?: configProperty.defaultValue?.toIntOrNull()
            ?: 0
        add(
            JSpinner(SpinnerNumberModel(default, configProperty.min, configProperty.max, 1))
                .also {
                    valueCallback = { SpecValue(it.model.value.toString(), null) }
                },
            BorderLayout.CENTER
        )
    }

    override fun value(): SpecValue = valueCallback()
}
