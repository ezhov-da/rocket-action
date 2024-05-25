package ru.ezhov.rocket.action.application.icon.domain.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*
import javax.swing.ImageIcon

@JsonIgnoreProperties(ignoreUnknown = true)
data class IconMetaInfo(
    val base64: String,
    val name: String,
    val source: String,
    val size: Int,
) {
    private var icon: ImageIcon? = null

    fun icon(): ImageIcon = icon ?: run { icon = ImageIcon(Base64.getDecoder().decode(base64)); icon!! }
}
