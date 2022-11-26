package ru.ezhov.rocket.action.api.context.icon

import javax.swing.Icon

interface IconService {
    fun by(icon: AppIcon): Icon

    fun load(iconUrl: String, defaultIcon: Icon): Icon
}
