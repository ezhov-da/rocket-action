package ru.ezhov.rocket.action.application.variables.interfaces.ui

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType
import ru.ezhov.rocket.action.application.variables.domain.model.Variables
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.*
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

private val logger = KotlinLogging.logger { }

class VariablesFrame(parent: JFrame? = null) : JFrame() {
    private val variablesApplication = VariablesApplication()
    private val applicationTableModel = DefaultTableModel().apply {
        addColumn("*Имя")
        addColumn("*Значение")
        addColumn("*Описание")
    }

    private val propertiesEnableModel = DefaultTableModel().apply {
        addColumn("*Имя")
        addColumn("*Значение")
    }
    private val environmentEnableModel = DefaultTableModel().apply {
        addColumn("*Имя")
        addColumn("*Значение")
    }

    private val applicationTable = JTable(applicationTableModel).apply {
        tableHeader.reorderingAllowed = false
    }
    private val propertiesTable = JTable(propertiesEnableModel).apply {
        tableHeader.reorderingAllowed = false
    }
    private val environmentTable = JTable(environmentEnableModel).apply {
        tableHeader.reorderingAllowed = false
    }

    private val addRowButton = JButton("Добавить строку")
    private val removeRowButton = JButton("Удалить строку")

    private val saveButton = JButton("Сохранить")
    private val refreshButton = JButton("Обновить")

    private val tabbedPane = JTabbedPane()

    init {
        val panel = JPanel(BorderLayout())
        tabbedPane.addTab("Приложение", panel)

        tabbedPane.addTab("Java свойства", JPanel(BorderLayout()).apply {
            add(JScrollPane(propertiesTable), BorderLayout.CENTER)
        })
        tabbedPane.addTab("Окружение", JPanel(BorderLayout()).apply {
            add(JScrollPane(environmentTable), BorderLayout.CENTER)
        })

        panel.add(JPanel().apply {
            add(addRowButton)
            add(removeRowButton)
        }, BorderLayout.NORTH)
        panel.add(JScrollPane(applicationTable), BorderLayout.CENTER)
        panel.add(JPanel().apply {
            add(saveButton)
        }, BorderLayout.SOUTH)

        loadTable()

        iconImage = RocketActionContextFactory.context.icon().by(AppIcon.ROCKET_APP).toImage()
        size = Dimension(600, 500)
        defaultCloseOperation = HIDE_ON_CLOSE

        addRowButton.addActionListener {
            applicationTableModel.addRow(emptyArray())
        }

        removeRowButton.addActionListener {
            val selectedRow = applicationTable.selectedRow
            if (selectedRow != -1) {
                applicationTableModel.removeRow(selectedRow)
            }
            applicationTable.repaint()
        }

        saveButton.addActionListener { saveTable() }
        refreshButton.addActionListener { loadTable() }

        add(JMenuBar().apply {
            add(refreshButton)
        }, BorderLayout.NORTH)

        add(tabbedPane, BorderLayout.CENTER)
        title = "Переменные"

        setLocationRelativeTo(parent)
    }

    private fun loadTable() {
        val variables = variablesApplication.all()
        loadTable(applicationTable, applicationTableModel, VariableType.APPLICATION, variables)
        loadTable(propertiesTable, propertiesEnableModel, VariableType.PROPERTIES, variables)
        loadTable(environmentTable, environmentEnableModel, VariableType.ENVIRONMENT, variables)
    }

    private fun loadTable(table: JTable, model: DefaultTableModel, type: VariableType, variables: Variables) {
        while (table.rowCount != 0) {
            model.removeRow(table.rowCount - 1)
        }

        when (type) {
            VariableType.APPLICATION -> {
                variables
                    .variables
                    .filter { it.type == type }
                    .sortedBy { it.name }
                    .forEach { vari ->
                        model.addRow(
                            arrayOf(
                                vari.name,
                                vari.value,
                                vari.description,
                            )
                        )
                    }
            }

            else -> {
                variables
                    .variables
                    .filter { it.type == type }
                    .sortedBy { it.name }
                    .forEach { vari ->
                        model.addRow(
                            arrayOf(
                                vari.name,
                                vari.value,
                            )
                        )
                    }
            }
        }

        table.repaint()
    }

    private fun saveTable() {
        val data = applicationTableModel.dataVector
        val variables = data.mapNotNull { rawRow ->
            val row = rawRow as Vector<String>
            val name = row[0]
            val value = row[1]
            val description = row[2]

            if (name != null && description != null && value != null) {
                Variable(
                    name = name,
                    description = description,
                    value = value,
                    type = VariableType.APPLICATION,
                )
            } else null
        }

        try {
            variablesApplication.save(Variables(variables))

            RocketActionContextFactory.context.notification().show(NotificationType.INFO, "Variables saved")
        } catch (ex: Exception) {
            logger.error(ex) { "Variables unsaved" }
            RocketActionContextFactory.context.notification().show(NotificationType.ERROR, "Variables unsaved")
        }
    }
}
