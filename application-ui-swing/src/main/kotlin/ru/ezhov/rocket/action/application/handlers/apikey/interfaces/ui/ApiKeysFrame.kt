package ru.ezhov.rocket.action.application.handlers.apikey.interfaces.ui

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeyDto
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeysApplication
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeysDto
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.util.*
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

private val logger = KotlinLogging.logger { }

class ApiKeysFrame(
    parent: JFrame? = null,
    private val apiKeysApplication: ApiKeysApplication,
    private val notificationService: NotificationService,
    iconService: IconService,
) : JFrame() {
    private val apiKeysTableModel = DefaultTableModel().apply {
        addColumn("*Description")
        addColumn("*Key")
    }

    private val apiKeyTable = JTable(apiKeysTableModel).apply {
        tableHeader.reorderingAllowed = false
        setDefaultRenderer(Any::class.java, PasswordDefaultTableRenderer(1))
    }

    private val addRowButton = JButton("Add line")
    private val addRowWithGeneratedButton = JButton("Add line with generated key")
    private val removeRowButton = JButton("Delete line")

    private val saveButton = JButton("Save")
    private val refreshButton = JButton("Refresh")

    init {
        val apiKeys = apiKeysApplication.all()

        loadTable()

        iconImage = iconService.by(AppIcon.ROCKET_APP).toImage()
        size = SizeUtil.dimension(0.6, 0.5)
        defaultCloseOperation = HIDE_ON_CLOSE

        addRowButton.addActionListener {
            apiKeysTableModel.addRow(emptyArray())
        }

        addRowWithGeneratedButton.addActionListener {
            apiKeysTableModel.addRow(arrayOf("", UUID.randomUUID().toString()))
        }

        removeRowButton.addActionListener {
            val selectedRow = apiKeyTable.selectedRow
            if (selectedRow != -1) {
                apiKeysTableModel.removeRow(selectedRow)
            }
            apiKeyTable.repaint()
        }

        saveButton.addActionListener { saveTable() }
        refreshButton.addActionListener { loadTable() }

        add(JMenuBar().apply {
            add(refreshButton)
        }, BorderLayout.NORTH)

        add(applicationPanel(apiKeysApplication.all()), BorderLayout.CENTER)
        title = "API keys"

        setLocationRelativeTo(parent)
    }

    private fun applicationPanel(apiKeys: ApiKeysDto): JPanel {
        val panel = JPanel(BorderLayout())

        panel.add(JPanel().apply {
            add(addRowButton)
            add(addRowWithGeneratedButton)
            add(removeRowButton)
        }, BorderLayout.NORTH)
        panel.add(JScrollPane(apiKeyTable), BorderLayout.CENTER)
        panel.add(JPanel().apply {
            add(saveButton)
        }, BorderLayout.SOUTH)

        return panel
    }

    private fun loadTable() {
        val apiKeys = apiKeysApplication.all()
        loadTable(apiKeyTable, apiKeysTableModel, apiKeys)
    }

    private fun loadTable(table: JTable, model: DefaultTableModel, apiKeys: ApiKeysDto) {
        while (table.rowCount != 0) {
            model.removeRow(table.rowCount - 1)
        }

        apiKeys
            .keys
            .sortedBy { it.description }
            .forEach { vari ->
                model.addRow(
                    arrayOf(
                        vari.description,
                        vari.value,
                    )
                )
            }
        table.repaint()
    }

    private fun saveTable() {
        val data = apiKeysTableModel.dataVector
        val keys = data.mapNotNull { rawRow ->
            val row = rawRow as Vector<String>
            val description = row[0]
            val value = row[1]

            if (description != null && value != null) {
                ApiKeyDto(
                    description = description,
                    value = value,
                )
            } else null
        }

        try {
            apiKeysApplication.save(ApiKeysDto(keys = keys))
            notificationService.show(NotificationType.INFO, "Api keys saved")
        } catch (ex: Exception) {
            logger.error(ex) { "Api keys unsaved" }
            notificationService.show(NotificationType.ERROR, "Api keys unsaved")
        }
    }
}
