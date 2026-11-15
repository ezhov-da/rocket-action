package ru.ezhov.rocket.action.api.context.icon

import javax.swing.Icon

interface IconService {
    fun by(icon: AppIcon): Icon

    /**
     * Returns the icon immediately. A remote icon that is neither cached nor available locally
     * is fetched in the background, so this call never waits for the network; such an icon
     * becomes visible on a later reload.
     */
    fun load(iconUrl: String, defaultIcon: Icon): Icon

    /**
     * Returns the icon immediately and delivers a remote icon later through [onLoaded].
     *
     * The callback is invoked on the Swing event dispatch thread, so it may update a component
     * and repaint it. The default implementation keeps old implementations binary compatible:
     * it delegates to the two-argument [load] and never invokes the callback.
     */
    fun load(iconUrl: String, defaultIcon: Icon, onLoaded: (Icon) -> Unit): Icon =
        load(iconUrl, defaultIcon)
}
