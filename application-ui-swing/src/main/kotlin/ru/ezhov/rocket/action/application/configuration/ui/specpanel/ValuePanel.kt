package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import java.awt.LayoutManager
import javax.swing.JPanel

abstract class ValuePanel(layout: LayoutManager?) : JPanel(layout) {
    abstract fun value(): SpecValue
}
