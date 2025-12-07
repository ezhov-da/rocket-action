package ru.ezhov.rocket.action.application.variables.interfaces.ui

import org.jdesktop.swingx.renderer.DefaultTableRenderer
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.plugin.clipboard.ClipboardUtil
import java.awt.Component
import javax.swing.DefaultCellEditor
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPasswordField
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.table.TableCellRenderer

class VariablesDefaultTableRenderer(
    private val passwordColumnNumber: Int
) : DefaultTableRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        return when (column == passwordColumnNumber) {
            true -> JPasswordField("************")
                .apply {
                    isEditable = false
                    background = label.background
                }

            false -> label
        }
    }
}

internal class VariableButtonRenderer : JButton(), TableCellRenderer {
    init {
        isOpaque = true
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        if (isSelected) {
            foreground = table.selectionForeground
            background = table.selectionBackground
        } else {
            foreground = table.foreground
            background = UIManager.getColor("Button.background")
        }
        text = value?.toString().orEmpty()
        icon = Icons.Advanced.COPY_16x16
        return this
    }
}

internal class VariableButtonEditor(
    checkBox: JCheckBox,
    private val notificationService: NotificationService
) : DefaultCellEditor(checkBox) {
    protected var button: JButton = JButton(Icons.Advanced.COPY_16x16)
    private var label: String? = null
    private var isPushed = false
    private var table: JTable? = null
    private var row: Int? = null
    private var column: Int? = null

    init {
        button.isOpaque = true
        button.addActionListener { _ -> fireEditingStopped() }
    }

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        if (isSelected) {
            button.foreground = table.selectionForeground
            button.background = table.selectionBackground
        } else {
            button.foreground = table.foreground
            button.background = table.background
        }
        label = value?.toString().orEmpty()
        button.text = label
        isPushed = true

        this.table = table
        this.row = row
        this.column = column
        return button
    }

    override fun getCellEditorValue(): Any {
        if (isPushed) {
            if (table != null) {
                val value = table!!.model.getValueAt(row!!, column!! - 1)
                ClipboardUtil.copyToClipboard(value?.toString().orEmpty())
                notificationService.show(NotificationType.INFO, "Copied")
            }
        }
        isPushed = false
        return label!!
    }

    override fun stopCellEditing(): Boolean {
        isPushed = false
        return super.stopCellEditing()
    }
}
