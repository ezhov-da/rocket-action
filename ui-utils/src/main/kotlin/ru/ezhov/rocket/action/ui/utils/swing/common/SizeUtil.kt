package ru.ezhov.rocket.action.ui.utils.swing.common

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
}
