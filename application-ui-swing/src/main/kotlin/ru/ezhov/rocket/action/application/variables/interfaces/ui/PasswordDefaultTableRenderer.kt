package ru.ezhov.rocket.action.application.variables.interfaces.ui

import org.jdesktop.swingx.renderer.DefaultTableRenderer
import javax.swing.JPasswordField
import javax.swing.JTable

class PasswordDefaultTableRenderer(
    private val columnNumber: Int
) : DefaultTableRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): java.awt.Component {
        val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        return when (column == columnNumber) {
            true -> JPasswordField("************")
                .apply {
                    isEditable = false
                    background = label.background
                }

            false -> label
        }
    }
}
