package ru.ezhov.rocket.action.icon

import java.awt.GraphicsEnvironment
import java.awt.Image
import javax.swing.Icon
import javax.swing.ImageIcon

fun Icon.toImage(): Image =
    when (this is ImageIcon) {
        true -> this.image
        false -> {
            val w = this.iconWidth
            val h = this.iconHeight
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val gd = ge.defaultScreenDevice
            val gc = gd.defaultConfiguration
            val image = gc.createCompatibleImage(w, h)
            val g = image.createGraphics()
            this.paintIcon(null, g, 0, 0)
            g.dispose()
            image
        }
    }