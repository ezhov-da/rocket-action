package ru.ezhov.rocket.action.icon

import javax.swing.Icon

interface IconRepository {
    fun by(icon: AppIcon): Icon
}