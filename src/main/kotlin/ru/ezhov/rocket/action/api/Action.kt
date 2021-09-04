package ru.ezhov.rocket.action.api

import java.awt.Component

interface Action {
    fun action(): SearchableAction
    fun component(): Component
}
