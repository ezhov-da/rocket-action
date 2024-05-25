package ru.ezhov.rocket.action.application.tags.ui.view

import ru.ezhov.rocket.action.application.tags.application.TagAndKeys
import ru.ezhov.rocket.action.application.tags.application.TagsService
import java.awt.BorderLayout
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

// TODO ezhov in progress
class ViewTagsPanel(
    private val tagsService: TagsService,
) : JPanel(BorderLayout()) {
    private val iconTag = ImageIcon(this::class.java.getResource("/icons/tag_16x16.png"))

    private val tableModel = object : DefaultTableModel() {
        override fun getValueAt(row: Int, column: Int): Any {
            return when (column) {
                0 -> (super.dataVector[row] as TagUi).tagAndKeys.name
                else -> (super.dataVector[row] as TagUi).tagAndKeys.keys.size
            }
        }

        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }
    }.apply {
        addColumn("Name")
        addColumn("Amount")
    }

    private val table = JTable(tableModel).apply {
        tableHeader.reorderingAllowed = false
        autoCreateRowSorter = false
    }

    init {
        table.selectionModel.apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            addListSelectionListener {
                val tagUi = (tableModel.dataVector[it.firstIndex] as TagUi)
                tagUi.invokeSearch(tagUi.tagAndKeys.keys)
            }
        }

        add(JScrollPane(table), BorderLayout.CENTER)
    }

    fun load(invokeSearch: (keys: Set<String>) -> Unit) {
        tableModel.dataVector.removeAllElements()
        tagsService.tagAndKeys().forEach { t ->
            tableModel.dataVector.add(TagUi(t, invokeSearch))
        }
    }

    data class TagUi(
        val tagAndKeys: TagAndKeys,
        val invokeSearch: (keys: Set<String>) -> Unit,
    )
}
