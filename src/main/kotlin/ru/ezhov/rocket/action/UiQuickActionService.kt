package ru.ezhov.rocket.action

import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.configuration.domain.RocketActionConfigurationRepository
import ru.ezhov.rocket.action.configuration.ui.ConfigurationFrame
import ru.ezhov.rocket.action.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.domain.RocketActionUiRepository
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.infrastructure.YmlRocketActionSettingsRepository
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.properties.ResourceGeneralPropertiesRepository
import ru.ezhov.rocket.action.types.group.GroupRocketActionUi
import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Point
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.SwingWorker


class UiQuickActionService(
        userPathToAction: String?,
        private val rocketActionConfigurationRepository: RocketActionConfigurationRepository,
        private val rocketActionUiRepository: RocketActionUiRepository
) {

    private val rocketActionSettingsRepository: RocketActionSettingsRepository
    private var configurationFrame: ConfigurationFrame? = null
    private var dialog: JDialog? = null

    init {
        val uri = if (userPathToAction != null) {
            File(userPathToAction).toURI()
        } else {
            LOGGER.log(Level.INFO, "Use absolute path to `action.xml` file as argument")
            UiQuickActionService::class.java.getResource("/actions.yml").toURI()
        }
        rocketActionSettingsRepository = YmlRocketActionSettingsRepository(uri)
    }

    @Throws(UiQuickActionServiceException::class)
    fun createMenu(dialog: JDialog): JMenuBar {
        this.dialog = dialog
        return try {
            RocketActionComponentCacheFactory.cache.clear()
            val menuBar = JMenuBar()
            val menu = JMenu()
            menu.icon = ImageIcon(this::class.java.getResource("/load_16x16.gif"))

            menuBar.add(menu)
            //TODO: избранное
            //menuBar.add(createFavoriteComponent());

            menuBar.add(createSearchField(dialog, menu))
            menuBar.add(createMoveComponent(dialog))
            CreateMenuWorker(menu).execute()
            menuBar
        } catch (e: Exception) {
            throw UiQuickActionServiceException("Error", e)
        }
    }

    @Throws(Exception::class)
    private fun rocketActionSettings(): List<RocketActionSettings> = rocketActionSettingsRepository.actions()

    private fun createSearchField(dialog: JDialog, menu: JMenu): Component =
            JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
                border = BorderFactory.createEmptyBorder()
                val textField =
                        TextFieldWithText("Search").apply {
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
                                                        LOGGER.info("found by search '$text': ${ccl.size}")

                                                        SwingUtilities.invokeLater {
                                                            menu.removeAll()
                                                            ccl.forEach { menu.add(it.component()) }
                                                            menu.add(createTools(dialog))
                                                            menu.doClick()
                                                        }
                                                    }
                                        } else {
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
                                            CreateMenuWorker(menu).execute()
                                        }
                                    })
                                }
                )
            }

    private fun createTools(dialog: JDialog): JMenu {
        val menuTools = JMenu("Tools")
        menuTools.icon = IconRepositoryFactory.repository.by(AppIcon.WRENCH)
        val updateActionListener = ActionListener { e: ActionEvent? ->
            SwingUtilities.invokeLater {
                var newMenuBar: JMenuBar? = null
                try {
                    newMenuBar = createMenu(dialog)
                } catch (ex: UiQuickActionServiceException) {
                    ex.printStackTrace()
                    NotificationFactory.notification.show(NotificationType.ERROR, "Tools menu created error")
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
        val menuItemUpdate = JMenuItem("Update")
        menuItemUpdate.icon = IconRepositoryFactory.repository.by(AppIcon.RELOAD)
        menuItemUpdate.addActionListener(updateActionListener)
        menuTools.add(menuItemUpdate)
        val menuItemEditor = JMenuItem("Editor")
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
                        NotificationFactory.notification.show(NotificationType.ERROR, "Editor menu created error")
                    }
                }
                if (configurationFrame != null) {
                    configurationFrame!!.setVisible(true)
                }
            }
        }
        menuTools.add(menuItemEditor)
        val menuInfo = JMenu("Info")
        menuInfo.icon = IconRepositoryFactory.repository.by(AppIcon.INFO)
        val repository: GeneralPropertiesRepository = ResourceGeneralPropertiesRepository()
        menuInfo.add(JLabel(repository.all().getProperty("version", "not found")))
        var info = "undefined"
        try {
            BufferedInputStream(this.javaClass.getResourceAsStream("/info.html")).use { `is` ->
                val bytes = ByteArray(`is`.available())
                `is`.read(bytes)
                info = String(bytes, StandardCharsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            NotificationFactory.notification.show(NotificationType.ERROR, "Info menu created error")
        }
        val label = JLabel("<html>$info")
        menuInfo.add(label)
        menuTools.add(menuInfo)
        val menuItemClose = JMenuItem("Exit")
        menuItemClose.icon = IconRepositoryFactory.repository.by(AppIcon.X)
        menuItemClose.addActionListener { e: ActionEvent? -> SwingUtilities.invokeLater { dialog!!.dispose() } }
        menuTools.add(menuItemClose)
        return menuTools
    }

    private fun createMoveComponent(dialog: JDialog?): Component {
        val label = JLabel(IconRepositoryFactory.repository.by(AppIcon.MOVE))
        val mouseAdapter: MouseAdapter = object : MouseAdapter() {
            var pressed = false
            var x = 0
            var y = 0
            override fun mousePressed(e: MouseEvent) {
                pressed = true
                val mousePoint = e.point
                SwingUtilities.convertPointToScreen(mousePoint, label)
                val framePoint = dialog!!.location
                x = mousePoint.x - framePoint.x
                y = mousePoint.y - framePoint.y
            }

            override fun mouseReleased(e: MouseEvent) {
                pressed = false
            }

            override fun mouseDragged(e: MouseEvent) {
                if (pressed) {
                    val mousePoint = e.point
                    SwingUtilities.convertPointToScreen(mousePoint, label)
                    dialog!!.location = Point(mousePoint.x - x, mousePoint.y - y)
                }
            }
        }
        label.addMouseListener(mouseAdapter)
        label.addMouseMotionListener(mouseAdapter)
        return label
    }

    private fun createFavoriteComponent(): Component {
        val menu = JMenu()
        menu.icon = IconRepositoryFactory.repository.by(AppIcon.STAR)
        menu.dropTarget = DropTarget(
                menu,
                object : DropTargetAdapter() {
                    override fun drop(dtde: DropTargetDropEvent) {
                        try {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY)
                            val text = dtde.transferable.getTransferData(DataFlavor.stringFlavor) as String
                            menu.add(JLabel(text))
                        } catch (e: UnsupportedFlavorException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
        )
        return menu
    }

    private inner class CreateMenuWorker(private val menu: JMenu) : SwingWorker<String?, String?>() {
        @Throws(Exception::class)
        override fun doInBackground(): String? {
            menu.removeAll()
            val actionSettings = rocketActionSettings()
            fillCache(actionSettings)
            val cache = RocketActionComponentCacheFactory.cache
            for (rocketActionSettings in actionSettings) {
                rocketActionUiRepository.by(rocketActionSettings.type())?.let {
                    (
                            cache
                                    .by(rocketActionSettings.id())?.component()
                                    ?: it.create(rocketActionSettings)?.component()
                            )
                            ?.let { component ->
                                menu.add(component)
                            }
                }
            }
            menu.add(createTools(dialog!!))
            return null
        }

        private fun fillCache(actionSettings: List<RocketActionSettings>) {
            RocketActionComponentCacheFactory
                    .cache
                    .let { cache ->
                        for (rocketActionSettings in actionSettings) {
                            val rau = rocketActionUiRepository.by(rocketActionSettings.type())
                            if (rau != null) {
                                if (rocketActionSettings.type() != GroupRocketActionUi.TYPE) {
                                    rau.create(rocketActionSettings)
                                            ?.let { action ->
                                                cache.add(
                                                        rocketActionSettings.id(),
                                                        action
                                                )
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
            menu.icon = ImageIcon(this::class.java.getResource("/rocket_16x16.png"))
        }
    }

    companion object {
        private val LOGGER = Logger.getLogger(UiQuickActionService::class.java.name)
    }
}