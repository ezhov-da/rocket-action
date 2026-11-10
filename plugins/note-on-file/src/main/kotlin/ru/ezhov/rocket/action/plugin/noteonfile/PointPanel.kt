package ru.ezhov.rocket.action.plugin.noteonfile

import java.awt.BorderLayout
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane

class PointPanel(
    selectPointCallback: (point: Point) -> Unit
) : JPanel() {
    private val calculatePointService = CalculatePointService()
    private val defaultModel = DefaultListModel<Point>()
    private val list: JList<Point> = JList(defaultModel)

    init {
        list.addListSelectionListener { list.selectedValue?.let { selectPointCallback(it) } }
        layout = BorderLayout()
        list.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                value?.let {
                    label.text = (it as Point).text
                }
                return label
            }
        }
        add(JScrollPane(list), BorderLayout.CENTER)
    }

    fun calculate(delimiter: String, text: String) {
        if (delimiter.isNotBlank()) {
            val points = calculatePointService.calculate(delimiter, text)
            defaultModel.removeAllElements()
            points.forEach { defaultModel.addElement(it) }
        }
    }
}
