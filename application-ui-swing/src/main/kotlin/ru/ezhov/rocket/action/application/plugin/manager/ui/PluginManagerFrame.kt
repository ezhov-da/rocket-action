package ru.ezhov.rocket.action.application.plugin.manager.ui

import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSourceType
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginSpec
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class PluginManagerFrame(
    rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    parent: JFrame? = null,
) : JFrame() {
    private val tableModel = object : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }
    }
        .apply {
            addColumn("Plugin source")
            addColumn("Detailed information")
            addColumn("Download time in ms")
            addColumn("Error Information")
        }
    private val table = JTable(tableModel).apply {
        tableHeader.reorderingAllowed = false
    }

    init {
        rocketActionPluginApplicationService
            .allSpec()
            .forEach { spec ->
                when (spec) {
                    is RocketActionPluginSpec.Success -> {
                        tableModel.addRow(
                            arrayOf(
                                spec.sourceType.toText(),
                                spec.from,
                                spec.loadTime.toMillis(),
                                "",
                            )
                        )
                    }

                    is RocketActionPluginSpec.Failure -> {
                        tableModel.addRow(
                            arrayOf(
                                spec.sourceType.toText(),
                                spec.from,
                                "",
                                spec.error,
                            )
                        )
                    }
                }
            }

        add(JScrollPane(table), BorderLayout.CENTER)

        size = Dimension(600, 500)
        defaultCloseOperation = HIDE_ON_CLOSE
        title = "Plugin Download Information"
        setLocationRelativeTo(parent)
    }
}

private fun RocketActionPluginSourceType.toText() = when (this) {
    RocketActionPluginSourceType.JAR -> "JAR file"
    RocketActionPluginSourceType.INNER -> "Interior"
    RocketActionPluginSourceType.GROOVY_SCRIPT -> "Groovy"
    RocketActionPluginSourceType.KOTLIN_SCRIPT -> "Kotlin"
    RocketActionPluginSourceType.CLASS_PATH -> "Class path"
}
