package ru.ezhov.rocket.action.application.about

import ru.ezhov.rocket.action.application.BaseDialogFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.ui.utils.swing.MarkdownEditorPane
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import javax.swing.JDialog
import javax.swing.WindowConstants

class AboutDialog(
    generalPropertiesRepository: GeneralPropertiesRepository,
) : JDialog() {
    init {
        title = "About"
        setIconImage(Icons.Standard.QUESTION_MARK_16x16.toImage())

        val version = generalPropertiesRepository
            .asString(UsedPropertiesName.VERSION, "Version not defined")

        val linkToRepository = generalPropertiesRepository
            .asString(UsedPropertiesName.REPOSITORY, "Link to repository")

        val text = """
            **Version**: $version

            **Repository**: [$linkToRepository]($linkToRepository)
        """.trimIndent()


        val markdownPanel = MarkdownEditorPane.fromText(text)
        add(markdownPanel, BorderLayout.CENTER)

        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        SizeUtil.setAllSize(this, SizeUtil.dimension(0.4, 0.3))
        isModal = true
    }

    fun showAboutDialog() {
        setLocationRelativeTo(BaseDialogFactory.INSTANCE!!)
        isVisible = true
    }
}
