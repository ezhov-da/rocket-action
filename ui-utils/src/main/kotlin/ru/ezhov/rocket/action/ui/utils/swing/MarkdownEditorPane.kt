package ru.ezhov.rocket.action.ui.utils.swing

import ru.ezhov.rocket.action.plugin.markdown.Markdown
import java.awt.Desktop
import java.io.IOException
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLFrameHyperlinkEvent

class MarkdownEditorPane private constructor() : JEditorPane() {
    init {
        contentType = "text/html"
        isEditable = false
        addHyperlinkListener(createHyperLinkListener(this))
    }

    private fun createHyperLinkListener(editorPane: JEditorPane): HyperlinkListener =
        HyperlinkListener { e ->
            if (e.eventType === HyperlinkEvent.EventType.ACTIVATED) {
                if (e is HTMLFrameHyperlinkEvent) {
                    (editorPane.document as HTMLDocument).processHTMLFrameHyperlinkEvent(e)
                } else {
                    try {
                        if (e.url?.toURI()?.isAbsolute == true) {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().browse(e.url.toURI())
                            }
                        } else {
                            val description = e.description
                            if (description.startsWith("/") && description.endsWith(".md")) {
                                editorPane.text = Markdown.resourceMarkdownToHtml(description)
                            } else {
                                editorPane.page = e.url
                            }
                        }
                    } catch (ioe: IOException) {
                        println("IOE: $ioe")
                    }
                }
            }
        }

    companion object {
        fun fromResource(resource: String): MarkdownEditorPane =
            MarkdownEditorPane().apply { text = Markdown.resourceMarkdownToHtml(resource) }

        fun fromText(text: String) =
            MarkdownEditorPane().apply { this.text = Markdown.textMarkdownToHtml(text) }
    }
}
