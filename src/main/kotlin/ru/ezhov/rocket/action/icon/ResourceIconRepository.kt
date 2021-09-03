package ru.ezhov.rocket.action.icon

import javax.swing.Icon
import javax.swing.ImageIcon

class ResourceIconRepository : IconRepository {
    private val defaultIcon = this.javaClass.getResource("/default_16x16.png")
    override fun by(icon: AppIcon): Icon = ImageIcon(
            this.javaClass.getResource("/open-iconic/png/" + icon.iconName + ".png")
                    ?: defaultIcon
    )
}