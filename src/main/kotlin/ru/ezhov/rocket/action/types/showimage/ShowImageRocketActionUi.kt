package ru.ezhov.rocket.action.types.showimage

import org.jdesktop.swingx.JXImageView
import org.jdesktop.swingx.JXPanel
import ru.ezhov.rocket.action.api.Action
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.SearchableAction
import ru.ezhov.rocket.action.caching.CacheFactory
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.types.ConfigurationUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.concurrent.ExecutionException
import javax.imageio.ImageIO
import javax.swing.AbstractAction
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.JToolBar
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.WindowConstants
import javax.swing.event.ChangeEvent

class ShowImageRocketActionUi : AbstractRocketAction() {

    override fun create(settings: RocketActionSettings): Action {
        val label = ConfigurationUtil.getValue(settings.settings(), LABEL)
        val menu = JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL))
        menu.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        menu.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
        LoadImageWorker(menu, settings).execute()
        return object : Action {
            override fun action(): SearchableAction = object : SearchableAction {
                override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
            }

            override fun component(): Component = menu
        }
    }

    override fun type(): String = "SHOW_IMAGE"

    override fun description(): String = "Show *.png and *.jpg images"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "TEST", true),
                createRocketActionProperty(IMAGE_URL, IMAGE_URL, "TEST", true)
        )
    }

    override fun name(): String = "Показать изображение *.png и *.jpg"

    private inner class LoadImageWorker(private val menu: JMenu, private val settings: RocketActionSettings) : SwingWorker<Image?, String?>() {
        private var cachedImage: File? = null

        @Throws(Exception::class)
        override fun doInBackground(): Image? {
            val url = ConfigurationUtil.getValue(settings.settings(), IMAGE_URL)
            val file = CacheFactory.cache.get(URL(url))
            return file?.let { f -> cachedImage = f; ImageIO.read(f) }
        }

        override fun done() {
            menu.icon = IconRepositoryFactory.repository.by(AppIcon.IMAGE)
            try {
                val component: Component
                if (settings.settings().containsKey(IMAGE_URL)) {
                    component = ImagePanel(this.get(), cachedImage!!)
                } else {
                    val panel = JPanel()
                    panel.add(JLabel(ConfigurationUtil.getValue(settings.settings(), IMAGE_URL)))
                    component = panel
                }
                menu.add(component)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
    }

    private inner class ImagePanel(image: Image?, cachedImage: File) : JXPanel(BorderLayout()) {
        init {
            val dimension = Toolkit.getDefaultToolkit().screenSize
            val widthNew = (dimension.width * 0.5).toInt()
            val heightNew = (dimension.height * 0.5).toInt()
            val newDimension = Dimension(widthNew, heightNew)
            val toolBar = JToolBar()
            toolBar.isFloatable = false
            toolBar.add(object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent) {
                    SwingUtilities.invokeLater {
                        val frame = JFrame(cachedImage.absolutePath)
                        frame.add(ImagePanel(image, cachedImage))
                        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
                        frame.setSize((dimension.width * 0.8).toInt(), (dimension.height * 0.8).toInt())
                        frame.setLocationRelativeTo(null)
                        frame.isVisible = true
                    }
                }

                init {
                    putValue(NAME, "Open in window")
                }
            })
            preferredSize = newDimension
            maximumSize = newDimension
            minimumSize = newDimension
            val panelImage = JXPanel(BorderLayout())
            val imageView = JXImageView()
            imageView.image = image
            imageView.autoscrolls = true
            panelImage.add(
                    imageView,
                    BorderLayout.CENTER
            )
            add(
                    toolBar,
                    BorderLayout.NORTH
            )
            add(
                    panelImage,
                    BorderLayout.CENTER
            )
            val slider = JSlider(1, 100, 100)
            toolBar.add(slider)
            slider.addChangeListener { e: ChangeEvent? -> imageView.scale = slider.value / 100.0 }
            val cachedLabel = JLabel("Cached: " + cachedImage.absolutePath)
            cachedLabel.addMouseListener(object : MouseAdapter() {
                override fun mouseReleased(e: MouseEvent) {
                    SwingUtilities.invokeLater {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(cachedImage.parentFile)
                            } catch (ioException: IOException) {
                                ioException.printStackTrace()
                            }
                        }
                    }
                }

                override fun mouseEntered(e: MouseEvent) {
                    SwingUtilities.invokeLater { cachedLabel.foreground = Color.BLUE }
                }

                override fun mouseExited(e: MouseEvent) {
                    SwingUtilities.invokeLater { cachedLabel.foreground = JLabel().foreground }
                }
            })
            add(
                    cachedLabel,
                    BorderLayout.SOUTH
            )
        }
    }

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val IMAGE_URL = "imageUrl"
    }
}