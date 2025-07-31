package ru.ezhov.rocket.action.application.plugin.context.icon

import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import javax.swing.Icon
import javax.swing.ImageIcon

private val logger = KotlinLogging.logger { }

@Component
class ResourceIconRepository {
    private val defaultIcon = this.javaClass.getResource("/icons/default_16x16.png")

    fun by(icon: AppIcon): Icon =
        when (val innerIcon = icon.toInner()) {
            AppIconInner.LOADER,
            AppIconInner.ROCKET_APP,
            AppIconInner.COLLAPSE,
            AppIconInner.EXPAND,
            AppIconInner.SAVE -> ImageIcon(
                this.javaClass.getResource(innerIcon.iconName) ?: defaultIcon
            )

            else -> ImageIcon(
                this.javaClass.getResource("/icons/open-iconic/png/" + innerIcon.iconName + ".png")
                    ?: defaultIcon
            )
        }

}

private fun AppIcon.toInner() = when (this) {
    AppIcon.ROCKET_APP -> AppIconInner.ROCKET_APP
    AppIcon.WRENCH -> AppIconInner.WRENCH
    AppIcon.RELOAD -> AppIconInner.RELOAD
    AppIcon.PENCIL -> AppIconInner.PENCIL
    AppIcon.STAR -> AppIconInner.STAR
    AppIcon.INFO -> AppIconInner.INFO
    AppIcon.X -> AppIconInner.X
    AppIcon.MOVE -> AppIconInner.MOVE
    AppIcon.BOOKMARK -> AppIconInner.BOOKMARK
    AppIcon.PROJECT -> AppIconInner.PROJECT
    AppIcon.IMAGE -> AppIconInner.IMAGE
    AppIcon.FILE -> AppIconInner.FILE
    AppIcon.LINK_INTACT -> AppIconInner.LINK_INTACT
    AppIcon.CLIPBOARD -> AppIconInner.CLIPBOARD
    AppIcon.FIRE -> AppIconInner.FIRE
    AppIcon.WARNING -> AppIconInner.WARNING
    AppIcon.BAN -> AppIconInner.BAN
    AppIcon.CLEAR -> AppIconInner.CLEAR
    AppIcon.TEXT -> AppIconInner.TEXT
    AppIcon.FLASH -> AppIconInner.FLASH
    AppIcon.BOLT -> AppIconInner.BOLT
    AppIcon.ARROW_TOP -> AppIconInner.ARROW_TOP
    AppIcon.ARROW_BOTTOM -> AppIconInner.ARROW_BOTTOM
    AppIcon.COPY_WRITING -> AppIconInner.COPY_WRITING
    AppIcon.BROWSER -> AppIconInner.BROWSER
    AppIcon.MINUS -> AppIconInner.MINUS
    AppIcon.PLUS -> AppIconInner.PLUS
    AppIcon.FORK -> AppIconInner.FORK
    AppIcon.CLOCK -> AppIconInner.CLOCK
    AppIcon.LOADER -> AppIconInner.LOADER
    AppIcon.SAVE -> AppIconInner.SAVE
    AppIcon.COLLAPSE -> AppIconInner.COLLAPSE
    AppIcon.EXPAND -> AppIconInner.EXPAND
    AppIcon.SHIELD -> AppIconInner.SHIELD
    AppIcon.EXPORT -> AppIconInner.EXPORT
}
