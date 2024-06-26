package ru.ezhov.rocket.action.ui.utils.swing.common

import java.awt.Component
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.util.*
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JFrame

fun String.toIcon() = ImageIcon(Base64.getDecoder().decode(this))

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

/**
 * Show window and make active
 */
fun JFrame.showToFront(parent: Component? = null) {
    if (!isVisible) {
        parent?.let {
            setLocationRelativeTo(parent)
        }
    }
    isVisible = true
    state = Frame.NORMAL; // restores minimized windows
    toFront(); // brings to front without needing to setAlwaysOnTop
    requestFocus();
}
