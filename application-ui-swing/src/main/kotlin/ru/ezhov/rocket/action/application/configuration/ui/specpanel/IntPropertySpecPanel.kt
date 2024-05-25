package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class IntPropertySpecPanel(
    configProperty: RocketActionPropertySpec.IntPropertySpec,
    initValue: InitValue? = null,
) : ValuePanel(MigLayout()) {
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
            "width 100%"
        )
    }

    override fun value(): SpecValue = valueCallback()
}
