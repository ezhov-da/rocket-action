package ru.ezhov.rocket.action.icon

import javax.swing.Icon
import javax.swing.ImageIcon

class ResourceIconRepository : IconRepository {
    private val defaultIcon = this.javaClass.getResource("/default_16x16.png")
    override fun by(icon: AppIcon): Icon = when (icon) {
        AppIcon.LOADER, AppIcon.ROCKET_APP -> ImageIcon(
            this.javaClass.getResource(icon.iconName) ?: defaultIcon
        )
        else -> ImageIcon(
            this.javaClass.getResource("/open-iconic/png/" + icon.iconName + ".png")
                ?: defaultIcon
        )
    }
}