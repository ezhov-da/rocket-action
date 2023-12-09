package ru.ezhov.rocket.action.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.icon.domain.model.IconMetaInfo
import java.io.File
import java.util.*
import javax.swing.ImageIcon

class BuildIconsTest {
    @Test
    fun `build icons`() {
        val file =
            File("./src/main/resources/icons")
                .walk()
                .filter { it.isFile && it.name.endsWith(".png") }
                .toList()
                .filter { f ->
                    f.name.contains("16x16") ||
                        f.name.contains("-2x") ||
                        !"\\d+".toRegex().containsMatchIn(f.name)
                }

        val mapper = ObjectMapper().registerModule(kotlinModule())

        val meta = file.map {
            IconMetaInfo(
                base64 = Base64.getEncoder().encodeToString(it.readBytes()),
                name = it.name,
                source = "Inner",
                size = ImageIcon(it.path).iconWidth,
            )
        }

        mapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(File("./src/main/resources/icons/icons.json"), meta)
    }
}
