package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import ru.ezhov.rocket.action.application.chainaction.api.ResultApi
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JPanel

class ResultChainDialog(
    result: Any?,
    parent: JComponent,
) : JDialog() {
    private val resultChainPanel: ResultChainPanel

    init {
        title = "Result"

        resultChainPanel = if (result is ResultApi) {
            ResultChainPanel(result.result.toString())
        } else {
            ResultChainPanel(result?.toString())
        }

        val panel = JPanel(BorderLayout())
        panel.add(resultChainPanel, BorderLayout.CENTER)


        add(panel, BorderLayout.CENTER)

        if (result is ResultApi) {
            if (result.uiWidth == null) {
                if (result.uiWidthPercent == null) {
                    setSize(500, 400)
                } else {
                    size = SizeUtil.dimension(result.uiWidthPercent, result.uiHeightPercent!!)
                }
            } else {
                setSize(result.uiWidth, result.uiHeight!!)
            }

            if (result.isUseParentLocation) {
                setLocationRelativeTo(parent)
            } else {
                setLocationRelativeTo(null)
            }
        } else {
            setLocationRelativeTo(parent)
            setSize(500, 400)
        }

        defaultCloseOperation = DISPOSE_ON_CLOSE
        isAlwaysOnTop = true
        isModal = false
    }
}
