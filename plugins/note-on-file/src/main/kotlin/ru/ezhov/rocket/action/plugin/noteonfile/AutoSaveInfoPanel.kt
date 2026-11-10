package ru.ezhov.rocket.action.plugin.noteonfile

import ru.ezhov.rocket.action.plugin.noteonfile.command.CommandObserver
import ru.ezhov.rocket.action.plugin.noteonfile.command.SaveTextCommand
import ru.ezhov.rocket.action.plugin.noteonfile.event.EventObserver
import ru.ezhov.rocket.action.plugin.noteonfile.event.TextChangingListener
import ru.ezhov.rocket.action.plugin.noteonfile.event.TextLoadingListener
import ru.ezhov.rocket.action.plugin.noteonfile.event.TextSavingListener
import java.awt.BorderLayout
import javax.swing.ImageIcon
import javax.swing.JButton
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
            isStringPainted = true
        }
    private val cancelButton = JButton(ImageIcon(this::class.java.getResource("/plugin-note-on-file/cancel_16x16.png")))
        .apply {
            toolTipText = "Cancel autosave"
            addActionListener { resetTimer() }
        }
    private var currentSaveTimer: Timer? = null

    init {
        layout = BorderLayout()
        cancelButton.isVisible = false
        progressBar.isVisible = false
        add(cancelButton, BorderLayout.WEST)
        add(progressBar, BorderLayout.CENTER)
        eventObserver.register(object : TextLoadingListener {
            override fun loading(text: String) {
                resetTimer()
            }
        })

        eventObserver.register(object : TextSavingListener {
            override fun saving(text: String) {
                resetTimer()
            }
        })

        eventObserver.register(object : TextChangingListener {
            override fun changing(text: String) {
                resetTimer()
                createAndSetTimer()
            }
        })
    }

    private fun createAndSetTimer() {
        cancelButton.isVisible = true
        progressBar.value = 0
        progressBar.isVisible = true
        var counterMinus = textAutoSave.delayInSeconds
        progressBar.string = "Save text after '${counterMinus}' sec"
        var counterPlus = 0
        currentSaveTimer = Timer(1000, null)
        currentSaveTimer!!.addActionListener {
            progressBar.string = "Saving text after '${--counterMinus}' sec"
            progressBar.value = ++counterPlus
            if (counterMinus == 0) {
                commandObserver.sendCommand(SaveTextCommand())
                progressBar.string = "Saving text after '${--counterMinus}' sec"
                resetTimer()
            }
        }
        currentSaveTimer!!.start()
    }

    private fun resetTimer() {
        currentSaveTimer?.let {
            currentSaveTimer!!.stop()
            cancelButton.isVisible = false
            progressBar.isVisible = false
        }
    }
}
