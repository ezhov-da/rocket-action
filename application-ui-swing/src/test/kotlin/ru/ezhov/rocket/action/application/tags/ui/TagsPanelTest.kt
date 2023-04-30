package ru.ezhov.rocket.action.application.tags.ui

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.tags.application.TagDto
import javax.swing.JFrame
import javax.swing.UIManager

fun main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (ex: Throwable) {
        //
    }
    val frame = JFrame("_________")
    val tags: List<TagDto> = listOf(
        mockk {
            every { name } returns "test"
        },
        mockk {
            every { name } returns "day"
        },
        mockk {
            every { name } returns "тест"
        },
        mockk {
            every { name } returns "просто"
        },
        mockk {
            every { name } returns "good"
        },
        mockk {
            every { name } returns "опять"
        },

        )
    frame.add(
        TagsPanel(listOf("test", "one", "two"),
            mockk {
                every { tags(any()) } returns tags
                every { tags() } returns tags
            }
        )
    )
    frame.setSize(1000, 600)
    frame.setLocationRelativeTo(null)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}

internal class TagsPanelTest
