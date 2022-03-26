package ru.ezhov.rocket.action.icon

import org.junit.Ignore
import org.junit.Test
import javax.swing.ImageIcon

@Ignore
class IconServiceTest {
    @Test
    fun `test load icon`() {
        IconService().load(
            iconUrl = "https://cdn.urlencoder.org/assets/images/url-16.webp",
            defaultIcon = ImageIcon(javaClass.getResource("/default_16x16.png"))
        )
    }
}