package ru.ezhov.rocket.action.ui.utils.swing.common

import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.awt.Image
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JFrame

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
fun JFrame.showToFront() {
    isVisible = true
    state = Frame.NORMAL; // restores minimized windows
    toFront(); // brings to front without needing to setAlwaysOnTop
    requestFocus();
}
