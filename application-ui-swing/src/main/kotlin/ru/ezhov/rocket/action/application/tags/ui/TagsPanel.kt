package ru.ezhov.rocket.action.application.tags.ui

import ru.ezhov.rocket.action.application.tags.application.TagsService
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.ScrollPaneConstants

class TagsPanel(
    private val tags: List<String>,
    private val tagsService: TagsService,
) : JPanel(BorderLayout()) {
    private val autocompleteTextField: AutocompleteTextField
    private val selectedTagsPanel: SelectedTagsPanel

    init {
        selectedTagsPanel = SelectedTagsPanel().apply {
            setTags(tags)
        }
        autocompleteTextField = AutocompleteTextField(
            lookup = { text -> tagsService.tags(text).map { it.name } },
            selectedCallback = { text -> selectedTagsPanel.addTag(text) },
            placeholder = "Start typing the name of a previously created or new tag and press ENTER"
        )

        add(autocompleteTextField, BorderLayout.NORTH)
        add(JScrollPane(selectedTagsPanel).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        }, BorderLayout.CENTER)

    }

    fun tags(): List<String> = selectedTagsPanel.tags()

    fun setTags(tags: List<String>) {
        selectedTagsPanel.setTags(tags)
    }

    fun clearTags() {
        autocompleteTextField.text = ""
        selectedTagsPanel.clearTags()
    }
}

private class SelectedTagsPanel : JPanel(BorderLayout()) {
    private val textPane = JTextPane()

    init {
        add(JScrollPane(textPane), BorderLayout.CENTER)
    }

    fun addTag(tag: String) {
        textPane.document.insertString(textPane.document.length, " $tag", null)
    }

    fun setTags(tags: List<String>) {
        textPane.text = ""
        tags.forEach { label ->
            addTag(label)
        }
    }

    fun clearTags() {
        textPane.text = ""
    }

    fun tags(): List<String> = textPane
        .text
        .replace("\n", "")
        .split(" ")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}
