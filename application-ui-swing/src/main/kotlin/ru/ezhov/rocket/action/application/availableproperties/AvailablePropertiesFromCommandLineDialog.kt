package ru.ezhov.rocket.action.application.availableproperties

import ru.ezhov.rocket.action.application.configuration.ui.ConfigurationFrameFactory
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.ui.utils.swing.MarkdownEditorPane
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import javax.swing.JDialog
import javax.swing.JScrollPane
import javax.swing.WindowConstants

class AvailablePropertiesFromCommandLineDialog : JDialog() {
    init {
        title = "Available properties from the command line"
        setIconImage(Icons.Standard.LIST_16x16.toImage())

        val properties = UsedPropertiesName.values()
        properties.sortBy { it.propertyName }
        val mdProperties = properties
            .map {
                listOf("## ${it.propertyName}", it.description)
            }
            .flatten()
            .joinToString(separator = "\n") {
                it.trim() + "  "
            }

        val markdownPanel = MarkdownEditorPane.fromText(mdProperties)
        add(JScrollPane(markdownPanel), BorderLayout.CENTER)

        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        SizeUtil.setAllSize(this, SizeUtil.dimension(0.5, 0.5))
    }

    fun showDialog() {
        setLocationRelativeTo(ConfigurationFrameFactory.INSTANCE!!.frame)
        isVisible = true
    }
}
