package ru.ezhov.rocket.action.application.icon.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel


class IconsPanel(
    private val iconRepository: IconRepository
) : JPanel(MigLayout(/*"debug"*/)) {

    init {
        val icons = iconRepository.icons()
        val chunks = icons.chunked(15)

        chunks.forEach { chunkGroup ->
            chunkGroup.forEachIndexed { index, chunk ->
                val label = JLabel(chunk).apply {
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseReleased(e: MouseEvent) {
                            val icon = (e.source as JLabel).icon

                            println(icon.iconWidth.toString() + " " + icon.iconWidth)
                        }
                    })
                }
                if (index == chunkGroup.size - 1) {
                    add(label, "wrap")
                } else {
                    add(label)
                }
            }
        }
    }
}
