package ru.ezhov.rocket.action.types

import org.apache.batik.swing.JSVGCanvas
import org.apache.batik.swing.JSVGScrollPane
import org.apache.batik.swing.svg.JSVGComponent
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.caching.CacheFactory
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.*

class ShowSvgImageRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Component {
        val menu = JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL))
        menu.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        menu.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
        LoadImageWorker(menu, settings).execute()
        return menu
    }

    override fun type(): String {
        return "SHOW_SVG_IMAGE"
    }

    override fun description(): String {
        return "SVG image show (beta)"
    }

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, "TEST", true),
                createRocketActionProperty(IMAGE_URL, "TEST", true)
        )
    }

    private inner class LoadImageWorker(private val menu: JMenu, private val settings: RocketActionSettings) : SwingWorker<Image?, String?>() {
        private var cachedImage: File? = null

        @Throws(Exception::class)
        override fun doInBackground(): Image? {
            val url = ConfigurationUtil.getValue(settings.settings(), IMAGE_URL)
            val file = CacheFactory.cache.get(URL(url))
            return file?.let { f ->
                cachedImage = f
                ImageIO.read(f)
            }
        }

        override fun done() {
            menu.icon = IconRepositoryFactory.repository.by(AppIcon.IMAGE)
            try {
                val component = if (settings.settings().containsKey(IMAGE_URL)) {
                    ImagePanel(cachedImage!!)
                } else {
                    val panel = JPanel()
                    panel.add(JLabel(ConfigurationUtil.getValue(settings.settings(), IMAGE_URL)))
                    panel
                }
                menu.add(component)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private inner class ImagePanel(cachedImage: File) : JPanel(BorderLayout()) {
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
                        frame.add(ImagePanel(cachedImage))
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
            val panelImage = JPanel()
            val svgCanvas = JSVGCanvas()
            svgCanvas.setDocumentState(JSVGComponent.ALWAYS_DYNAMIC)
            svgCanvas.uri = cachedImage.toURI().toString()
            panelImage.add(JSVGScrollPane(svgCanvas))
            add(
                    toolBar,
                    BorderLayout.NORTH
            )
            add(
                    JScrollPane(panelImage),
                    BorderLayout.CENTER
            )

            //TODO scale
            //JSlider slider = new JSlider(1, 100, 100);
            //toolBar.add(slider);
            //slider.addChangeListener(e -> {
            //imageView.setScale(slider.getValue() / 100D);
            //});
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