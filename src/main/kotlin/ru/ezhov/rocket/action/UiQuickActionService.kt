package ru.ezhov.rocket.action

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.configuration.domain.RocketActionConfigurationRepository
import ru.ezhov.rocket.action.configuration.ui.ConfigurationFrame
import ru.ezhov.rocket.action.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.domain.RocketActionUiRepository
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.properties.UsedPropertiesName
import ru.ezhov.rocket.action.types.group.GroupRocketActionUi
import ru.ezhov.rocket.action.ui.swing.common.MoveUtil
import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import java.awt.Color
import java.awt.Component
import java.awt.Desktop
import java.awt.FlowLayout
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

private val logger = KotlinLogging.logger { }

class UiQuickActionService(
    private val rocketActionSettingsRepository: RocketActionSettingsRepository,
    private val rocketActionConfigurationRepository: RocketActionConfigurationRepository,
    private val rocketActionUiRepository: RocketActionUiRepository,
    private val generalPropertiesRepository: GeneralPropertiesRepository,
) {
    private var configurationFrame: ConfigurationFrame? = null
    private var dialog: JDialog? = null

    @Throws(UiQuickActionServiceException::class)
    fun createMenu(dialog: JDialog): JMenuBar {
        this.dialog = dialog
        return try {
            val menuBar = JMenuBar()
            val menu = JMenu()
            menuBar.add(menu)
            //TODO: избранное
            //menuBar.add(createFavoriteComponent());

            menuBar.add(createSearchField(dialog, menu))
            val moveLabel = JLabel(IconRepositoryFactory.repository.by(AppIcon.MOVE))
            MoveUtil.addMoveAction(movableComponent = dialog, grabbedComponent = moveLabel)
            menuBar.add(moveLabel)
            CreateMenuWorker(menu).execute()
            menuBar
        } catch (e: Exception) {
            throw UiQuickActionServiceException("Ошибка", e)
        }
    }

    @Throws(Exception::class)
    private fun rocketActionSettings(): List<RocketActionSettings> = rocketActionSettingsRepository.actions()

    private fun createSearchField(dialog: JDialog, menu: JMenu): Component =
        JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            border = BorderFactory.createEmptyBorder()
            val textField =
                TextFieldWithText("Поиск").apply { ->
                    val tf = this
                    columns = 5
                    addKeyListener(object : KeyAdapter() {
                        override fun keyPressed(e: KeyEvent?) {
                            e?.takeIf { it.keyCode == KeyEvent.VK_ENTER }?.let {
                                val cache = RocketActionComponentCacheFactory.cache

                                if (text.isNotEmpty()) {
                                    cache
                                        .all()
                                        .filter { it.contains(text) }
                                        .takeIf { it.isNotEmpty() }
                                        ?.let { ccl ->
                                            logger.info { "found by search '$text': ${ccl.size}" }

                                            SwingUtilities.invokeLater {
                                                tf.background = Color.GREEN
                                                menu.removeAll()
                                                ccl.forEach { menu.add(it.component()) }
                                                menu.add(createTools(dialog))
                                                menu.doClick()
                                            }
                                        }
                                } else {
                                    SwingUtilities.invokeLater { tf.background = Color.WHITE }
                                    CreateMenuWorker(menu).execute()
                                }
                            }
                        }
                    })
                }
            add(textField)
            add(
                JLabel(IconRepositoryFactory.repository.by(AppIcon.CLEAR))
                    .apply {
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseReleased(e: MouseEvent?) {
                                textField.text = ""
                                SwingUtilities.invokeLater { textField.background = Color.WHITE }
                                CreateMenuWorker(menu).execute()
                            }
                        })
                    }
            )
        }

    private fun createTools(dialog: JDialog): JMenu {
        val menuTools = JMenu("Инструменты")
        menuTools.icon = IconRepositoryFactory.repository.by(AppIcon.WRENCH)
        val updateActionListener = ActionListener {
            SwingUtilities.invokeLater {
                var newMenuBar: JMenuBar? = null
                try {
                    newMenuBar = createMenu(dialog)
                } catch (ex: UiQuickActionServiceException) {
                    ex.printStackTrace()
                    NotificationFactory.notification.show(NotificationType.ERROR, "Ошибка создания меню инструментов")
                }
                if (newMenuBar != null) {
                    // пока костыль, но мы то знаем это "пока" :)
                    dialog.jMenuBar.removeAll()
                    dialog.jMenuBar = newMenuBar
                    dialog.revalidate()
                    dialog.repaint()
                }
            }
        }
        val menuItemUpdate = JMenuItem("Обновить")
        menuItemUpdate.icon = IconRepositoryFactory.repository.by(AppIcon.RELOAD)
        menuItemUpdate.addActionListener(updateActionListener)
        menuTools.add(menuItemUpdate)
        val menuItemEditor = JMenuItem("Редактор")
        menuItemEditor.icon = IconRepositoryFactory.repository.by(AppIcon.PENCIL)
        menuItemEditor.addActionListener {
            SwingUtilities.invokeLater {
                if (configurationFrame == null) {
                    try {
                        configurationFrame = ConfigurationFrame(
                            dialog,
                            rocketActionConfigurationRepository,
                            rocketActionUiRepository,
                            rocketActionSettingsRepository,
                            updateActionListener
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        NotificationFactory.notification.show(NotificationType.ERROR, "Ошибка создания меню конфигурирования")
                    }
                }
                if (configurationFrame != null) {
                    configurationFrame!!.setVisible(true)
                }
            }
        }
        menuTools.add(menuItemEditor)
        val menuInfo = JMenu("Информация")
        menuInfo.icon = IconRepositoryFactory.repository.by(AppIcon.INFO)
        val notFound = { key: String -> "Информация по полю '$key' не найдена" }
        menuInfo.add(
            JMenuItem(generalPropertiesRepository
                .asString(UsedPropertiesName.VERSION, notFound("версия"))))
        menuInfo.add(
            JMenuItem(generalPropertiesRepository
                .asString(UsedPropertiesName.INFO, notFound("информация"))))
        menuInfo.add(
            JMenuItem(generalPropertiesRepository
                .asString(UsedPropertiesName.REPOSITORY, notFound("ссылка на репозиторий")))
                .apply {
                    addActionListener {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(URI.create(text))
                        }
                    }
                })
        menuInfo.add(createPropertyMenu())
        menuTools.add(menuInfo)

        menuTools.add(JMenuItem("Выход").apply {
            icon = IconRepositoryFactory.repository.by(AppIcon.X)
            addActionListener { SwingUtilities.invokeLater { dialog.dispose() } }
        })
        return menuTools
    }

    private fun createPropertyMenu(): JMenu =
        JMenu("Доступные свойства из командной строки").apply {
            UsedPropertiesName.values().forEach { pn ->
                this.add(JTextArea("${pn.propertyName}\n${pn.description}").apply { isEditable = false })
            }
        }

    private inner class CreateMenuWorker(private val menu: JMenu) : SwingWorker<List<Component>, String?>() {
        init {
            menu.icon = ImageIcon(this::class.java.getResource("/load_16x16.gif"))
            menu.removeAll()
        }

        @Throws(Exception::class)
        override fun doInBackground(): List<Component> {
            val actionSettings = rocketActionSettings()
            fillCache(actionSettings)
            val cache = RocketActionComponentCacheFactory.cache
            val components = mutableListOf<Component>()
            for (rocketActionSettings in actionSettings) {
                rocketActionUiRepository.by(rocketActionSettings.type())?.let {
                    (
                        cache
                            .by(rocketActionSettings.id())?.let {
                                logger.debug {
                                    "found in cache type='${rocketActionSettings.type().value()}'" +
                                        "id='${rocketActionSettings.id()}"
                                }

                                it.component()
                            }
                            ?: run {
                                logger.debug {
                                    "not found in cache type='${rocketActionSettings.type().value()}'" +
                                        "id='${rocketActionSettings.id()}. Create component"
                                }

                                it.create(rocketActionSettings)?.component()
                            }
                        )
                        ?.let { component ->
                            components.add(component)
                        }
                }
            }
            components.add(createTools(dialog!!))
            return components.toList()
        }

        private fun fillCache(actionSettings: List<RocketActionSettings>) {
            RocketActionComponentCacheFactory
                .cache
                .let { cache ->
                    for (rocketActionSettings in actionSettings) {
                        val rau = rocketActionUiRepository.by(rocketActionSettings.type())
                        if (rau != null) {
                            if (rocketActionSettings.type().value() != GroupRocketActionUi.TYPE) {
                                val mustBeCreate = cache
                                    .by(rocketActionSettings.id())
                                    ?.isChanged(rocketActionSettings) ?: true

                                logger.debug {
                                    "must be create '$mustBeCreate' type='${rocketActionSettings.type().value()}'" +
                                        "id='${rocketActionSettings.id()}"
                                }

                                if (mustBeCreate) {
                                    rau.create(rocketActionSettings)
                                        ?.let { action ->
                                            logger.debug {
                                                "added to cache type='${rocketActionSettings.type().value()}'" +
                                                    "id='${rocketActionSettings.id()}"
                                            }

                                            cache.add(
                                                rocketActionSettings.id(),
                                                action
                                            )
                                        }
                                }
                            } else {
                                if (rocketActionSettings.actions().isNotEmpty()) {
                                    fillCache(rocketActionSettings.actions())
                                }
                            }
                        }
                    }
                }
        }

        override fun done() {
            val components = this.get()
            components.forEach { menu.add(it) }
            menu.icon = ImageIcon(this::class.java.getResource("/rocket_16x16.png"))
        }
    }
}