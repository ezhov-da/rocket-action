package ru.ezhov.rocket.action.plugin.noteonfile

import ru.ezhov.rocket.action.plugin.noteonfile.event.EventObserver
import ru.ezhov.rocket.action.plugin.noteonfile.event.TextChangingListener
import ru.ezhov.rocket.action.plugin.noteonfile.event.TextLoadingListener
import ru.ezhov.rocket.action.plugin.noteonfile.event.TextSavingListener
import java.awt.BorderLayout
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel

class InfoAboutSaveTextPanel(
    private val eventObserver: EventObserver,
) : JPanel() {
    private val label: JLabel = JLabel()

    init {
        layout = BorderLayout()
        add(label, BorderLayout.CENTER)

        val actual: () -> Unit = {
            label.icon = ImageIcon(this::class.java.getResource("/plugin-note-on-file/success_16x16.png"))
            label.text = "Текст сохранён " +
                "'${LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}'"
        }

        eventObserver.register(object : TextLoadingListener {
            override fun loading(text: String) {
                actual.invoke()
            }
        })

        eventObserver.register(object : TextSavingListener {
            override fun saving(text: String) {
                actual.invoke()
            }
        })

        eventObserver.register(object : TextChangingListener {
            override fun changing(text: String) {
                label.icon = ImageIcon(this::class.java.getResource("/plugin-note-on-file/error_16x16.png"))
                label.text = "Текст не сохранён"
            }
        })
    }
}
