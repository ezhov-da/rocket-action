package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ChainActionListCellRenderer
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane

class SelectChainDialog(
    chains: List<ChainAction>,
    selectedChainCallback: (ChainAction) -> Unit
) : JDialog() {
    private val listChainsModel = DefaultListModel<ChainAction>()
    private val chainList = JList(listChainsModel)
    private val buttonCancel = JButton("Cancel")

    init {
        chains.forEach { listChainsModel.addElement(it) }
        chainList.cellRenderer = ChainActionListCellRenderer()

        val selectChainDialog = this
        chainList.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                chainList.selectedValue?.let { selected ->
                    selectChainDialog.isVisible = false
                    selectChainDialog.dispose()
                    selectedChainCallback(selected)
                }
            }
        })

        buttonCancel.addActionListener {
            this.isVisible = false
            this.dispose()
        }

        val panel = JPanel(BorderLayout())
        panel.add(JScrollPane(chainList), BorderLayout.CENTER)
        panel.add(buttonCancel, BorderLayout.SOUTH)

        add(panel, BorderLayout.CENTER)
        setSize(300, 400)
        isAlwaysOnTop = true
        isUndecorated = true
        isModal = true
    }
}
