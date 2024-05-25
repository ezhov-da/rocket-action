package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.api.RocketActionPropertySpec

class ComponentPropertySpecPanel(
    configProperty: RocketActionPropertySpec.ComponentPropertySpec,
    initValue: InitValue? = null,
) : ValuePanel(MigLayout()) {
    private var valueCallback: () -> SpecValue

    init {
        val default = initValue?.value
        add(
            configProperty.component.let {
                if (!default.isNullOrEmpty()) {
                    it.setPropertyValueValue(default)
                }

                valueCallback = { SpecValue(it.getPropertyValue().orEmpty(), null) }

                it.component()
            },
            "height 100%, width 100%"
        )
    }

    override fun value(): SpecValue = valueCallback()
}
