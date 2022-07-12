package ru.ezhov.rocket.action.plugin.noteonfile

import ru.ezhov.rocket.action.plugin.noteonfile.command.CommandObserver
import ru.ezhov.rocket.action.plugin.noteonfile.command.SaveTextCommand
import ru.ezhov.rocket.action.plugin.noteonfile.event.EventObserver
import ru.ezhov.rocket.action.plugin.noteonfile.event.TextChangingListener
import ru.ezhov.rocket.action.plugin.noteonfile.event.TextLoadingListener
import ru.ezhov.rocket.action.plugin.noteonfile.event.TextSavingListener
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.Timer

class AutoSaveInfoPanel(
    private val textAutoSave: TextAutoSave,
    private val commandObserver: CommandObserver,
    eventObserver: EventObserver,
) : JPanel() {
    private val progressBar: JProgressBar = JProgressBar(0, textAutoSave.delayInSeconds)
        .apply {
            isStringPainted = true;
        }
    private var currentSaveTimer: Timer? = null

    init {
        layout = BorderLayout()
        progressBar.isVisible = false
        add(progressBar, BorderLayout.CENTER)
        eventObserver.register(object : TextLoadingListener {
            override fun loading(text: String) {
                restTimer()
            }
        })

        eventObserver.register(object : TextSavingListener {
            override fun saving(text: String) {
                restTimer()
            }
        })

        eventObserver.register(object : TextChangingListener {
            override fun changing(text: String) {
                restTimer()
                createAndSetTimer()
            }
        })
    }

    private fun createAndSetTimer() {
        progressBar.value = 0
        progressBar.isVisible = true
        var counterMinus = textAutoSave.delayInSeconds
        progressBar.string = "Сохранение текста через '${counterMinus}' сек"
        var counterPlus = 0
        currentSaveTimer = Timer(1000, null)
        currentSaveTimer!!.addActionListener {
            progressBar.string = "Сохранение текста через '${--counterMinus}' сек"
            progressBar.value = ++counterPlus
            if (counterMinus == 0) {
                commandObserver.sendCommand(SaveTextCommand())
                progressBar.string = "Сохранение текста через '${--counterMinus}' сек"
                restTimer()
            }
        }
        currentSaveTimer!!.start()
    }

    private fun restTimer() {
        currentSaveTimer?.let {
            currentSaveTimer!!.stop()
            progressBar.isVisible = false
        }
    }
}
