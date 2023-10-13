package ru.ezhov.rocket.action.application.tags.ui

import ru.ezhov.rocket.action.api.RocketAction
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JMenu
import javax.swing.SwingUtilities

// TODO ezhov in progress
class TagsUiService {
    fun createComponent(): Component{
        val iconTag = ImageIcon(this::class.java.getResource("/icons/tag_16x16.png"))
        return JMenu().apply { icon = iconTag }
    }

    fun fillTags(){

    }


}
