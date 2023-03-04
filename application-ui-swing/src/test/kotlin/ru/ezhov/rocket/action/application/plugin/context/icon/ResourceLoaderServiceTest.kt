package ru.ezhov.rocket.action.application.plugin.context.icon

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import javax.swing.ImageIcon

@Disabled
class ResourceLoaderServiceTest {
    @Test
    fun `test load icon`() {
        ResourceLoaderService().load(
            iconUrl = "https://cdn.urlencoder.org/assets/images/url-16.webp",
            defaultIcon = ImageIcon(javaClass.getResource("/icons/default_16x16.png"))
        )
    }
}
