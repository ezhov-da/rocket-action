package ru.ezhov.rocket.action.ui.utils.swing.common

import java.awt.Component
import java.awt.Dimension
import java.awt.Toolkit

object SizeUtil {
    fun dimension(wPercent: Double, hPercent: Double) =
        Toolkit.getDefaultToolkit().screenSize.let { screenSize ->
            Dimension(
                (screenSize.width * wPercent).toInt(),
                (screenSize.height * hPercent).toInt()
            )
        }

    fun setAllSize(component: Component, dimension: Dimension) {
        component.apply {
            minimumSize = dimension
            maximumSize = dimension
            preferredSize = dimension
        }
    }
}
