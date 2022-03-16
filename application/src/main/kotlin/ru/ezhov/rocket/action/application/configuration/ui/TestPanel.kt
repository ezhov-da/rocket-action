package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.SwingUtilities

class TestPanel(
    private val rocketActionPluginRepository: RocketActionPluginRepository,
    private val callback: CreateTestCallback
) : JPanel(BorderLayout()) {
    private var panelTest: JPanel? = null
    private fun createTest(settings: RocketActionSettings) {
        val panel: JPanel =
            when (val actionUi = rocketActionPluginRepository.by(settings.type())?.factory()) {
                null -> {
                    val p = JPanel(BorderLayout())
                    p.add(JLabel("Не найдено действие для типа '${settings.type()}'"))
                    p
                }
                else -> {
                    val p = JPanel(BorderLayout())
                    val menuBar = JMenuBar()
                    val component = actionUi.create(settings)?.component() ?: JLabel("Компонент не создан")
                    menuBar.add(component)
                    p.add(menuBar, BorderLayout.CENTER)
                    p

                }
            }
        if (panelTest != null) {
            clearTest()
        }
        panelTest = panel
        add(panel, BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    fun clearTest() {
        if (panelTest != null) {
            this.remove(panelTest)
            revalidate()
            this.repaint()
        }
    }

    init {
        val panel = JPanel()
        val buttonTest = JButton("Протестировать")
        buttonTest.addActionListener { SwingUtilities.invokeLater { callback.create()?.let { rs -> createTest(rs) } } }
        panel.add(buttonTest)
        add(panel, BorderLayout.SOUTH)
    }

    fun interface CreateTestCallback {
        fun create(): RocketActionSettings?
    }
}
