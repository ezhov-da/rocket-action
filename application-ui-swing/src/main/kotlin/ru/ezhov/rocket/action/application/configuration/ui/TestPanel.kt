package ru.ezhov.rocket.action.application.configuration.ui

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.SwingUtilities

private val logger = KotlinLogging.logger { }

class TestPanel(
    private val rocketActionPluginRepository: RocketActionPluginRepository,
    private val callback: CreateTestCallback
) : JPanel(BorderLayout()) {
    private var panelTest: JPanel? = null
    private fun createTest(settings: MutableRocketActionSettings) {
        val panel: JPanel =
            when (val actionUi = rocketActionPluginRepository.by(settings.type)
                ?.factory(RocketActionContextFactory.context)) {
                null -> {
                    val p = JPanel(BorderLayout())
                    p.add(JLabel("Не найдено действие для типа '${settings.type}'"))
                    p
                }

                else -> {
                    val p = JPanel(BorderLayout())
                    val menuBar = JMenuBar()
                    val component = try {
                        actionUi
                            .create(settings = settings.to(), context = RocketActionContextFactory.context)
                            ?.component()
                            ?: JLabel("Компонент не создан")
                    } catch (ex: Exception) {
                        logger.error(ex) { "Error when create action" }
                        RocketActionContextFactory.context.notification()
                            .show(NotificationType.ERROR, "Error when create test action")

                        JLabel("Ошибка создания компонента")
                    }
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
        fun create(): MutableRocketActionSettings?
    }
}
