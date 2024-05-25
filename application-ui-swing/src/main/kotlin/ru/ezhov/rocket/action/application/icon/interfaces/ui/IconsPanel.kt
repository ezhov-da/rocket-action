package ru.ezhov.rocket.action.application.icon.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane


class IconsPanel(
    private val iconRepository: IconRepository,
) : JPanel(BorderLayout()) {
    private var selectCallback: ((String) -> Unit)? = null

    init {
        val tabPane = JTabbedPane()
        val bySize = iconRepository.icons().sortedBy { it.size }.groupBy { it.size }

        bySize.forEach { (k, v) ->
            val panel = JPanel(MigLayout())
            val chunks = v.chunked(15)

            chunks.forEach { chunkGroup ->
                chunkGroup.forEachIndexed { index, chunk ->
                    val button = JButton(chunk.icon()).apply {
                        toolTipText = chunk.name
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseReleased(e: MouseEvent) {
                                selectCallback!!.invoke(chunk.base64)
                            }
                        })
                    }
                    if (index == chunkGroup.size - 1) {
                        panel.add(button, "wrap")
                    } else {
                        panel.add(button)
                    }
                }
            }

            tabPane.add(k.toString(), panel)
        }

        add(tabPane, BorderLayout.CENTER)
    }

    fun setCallback(callback: (String) -> Unit) {
        selectCallback = callback
    }
}
