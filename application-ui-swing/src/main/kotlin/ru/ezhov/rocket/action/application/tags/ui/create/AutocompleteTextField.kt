package ru.ezhov.rocket.action.application.tags.ui.create

import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.Dimension
import java.awt.Window
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.AbstractListModel
import javax.swing.JList
import javax.swing.JScrollPane
import javax.swing.JWindow
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * @author Mikle Garin
 * @see https://stackoverflow.com/questions/45439231/implementing-autocomplete-with-jtextfield-and-jpopupmenu
 */
class AutocompleteTextField(
    /**
     * [Function] for text lookup.
     * It simply returns [List] of [String] for the text we are looking results for.
     */
    private val lookup: (String) -> List<String>,
    private val selectedCallback: (String) -> Unit,
    placeholder: String,
) : TextFieldWithText(placeholder), FocusListener, DocumentListener, KeyListener {
    /**
     * [List] of lookup results.
     * It is cached to optimize performance for more complex lookups.
     */
    private val results: MutableList<String>

    /**
     * [JWindow] used to display offered options.
     */
    private val popup: JWindow

    /**
     * Lookup results [JList].
     */
    private val list: JList<*>

    /**
     * [.list] model.
     */
    private val model: ListModel

    /**
     * Constructs [AutocompleteField].
     *
     * @param lookup [Function] for text lookup
     */
    init {
        results = ArrayList()
        val parent = SwingUtilities.getWindowAncestor(this)
        popup = JWindow(parent)
        popup.type = Window.Type.POPUP
        popup.focusableWindowState = false
        popup.isAlwaysOnTop = true
        model = ListModel()
        list = JList(model)
        popup.add(object : JScrollPane(list) {
            override fun getPreferredSize(): Dimension {
                val ps = super.getPreferredSize()
                ps.width = this@AutocompleteTextField.width
                return ps
            }
        })
        addFocusListener(this)
        document.addDocumentListener(this)
        addKeyListener(this)
    }

    /**
     * Displays autocomplete popup at the correct location.
     */
    private fun showAutocompletePopup() {
        val los = this@AutocompleteTextField.locationOnScreen
        popup.setLocation(los.x, los.y + height)
        popup.isVisible = true
    }

    /**
     * Closes autocomplete popup.
     */
    private fun hideAutocompletePopup() {
        popup.isVisible = false
    }

    override fun focusGained(e: FocusEvent) {
        SwingUtilities.invokeLater {
            if (results.size > 0 && text.isNotEmpty()) {
                showAutocompletePopup()
            }
        }
    }

    private fun documentChanged() {
        SwingUtilities.invokeLater {
            // Updating results list
            results.clear()

            results.addAll(lookup(text))

            // Updating list view
            model.updateView()
            list.visibleRowCount = results.size.coerceAtMost(10)

            // Selecting first result
            if (results.size > 0) {
                list.selectedIndex = 0
            }

            // Ensure autocomplete popup has correct size
            popup.pack()

            // Display or hide popup depending on the results
            if (results.size > 0 && text.isNotBlank()) {
                showAutocompletePopup()
            } else {
                hideAutocompletePopup()
            }
        }
    }

    override fun focusLost(e: FocusEvent) {
        SwingUtilities.invokeLater { hideAutocompletePopup() }
    }

    override fun keyPressed(e: KeyEvent) {
        if (e.keyCode == KeyEvent.VK_UP) {
            val index = list.selectedIndex
            if (index != -1 && index > 0) {
                list.selectedIndex = index - 1
            }
        } else if (e.keyCode == KeyEvent.VK_DOWN) {
            val index = list.selectedIndex
            if (index != -1 && list.model.size > index + 1) {
                list.selectedIndex = index + 1
            }
        } else if (e.keyCode == KeyEvent.VK_ENTER) {
            val selectedText = list.takeIf { it.model.size != 0 }?.selectedValue as? String
            if (selectedText != null) {
                selectedCallback(selectedText)
                text = ""
                caretPosition = text.length
            } else if (text.isNotEmpty()) {
                selectedCallback(text)
                text = ""
            }
            hideAutocompletePopup()
            results.clear()
        } else if (e.keyCode == KeyEvent.VK_ESCAPE) {
            hideAutocompletePopup()
        }
    }

    override fun insertUpdate(e: DocumentEvent) {
        documentChanged()
    }

    override fun removeUpdate(e: DocumentEvent) {
        documentChanged()
    }

    override fun changedUpdate(e: DocumentEvent) {
        documentChanged()
    }

    override fun keyTyped(e: KeyEvent) {
        // Do nothing
    }

    override fun keyReleased(e: KeyEvent) {
        // Do nothing
    }

    /**
     * Custom list model providing data and bridging view update call.
     */
    private inner class ListModel : AbstractListModel<Any?>() {
        override fun getSize(): Int {
            return results.size
        }

        override fun getElementAt(index: Int): Any {
            return results[index]
        }

        /**
         * Properly updates list view.
         */
        fun updateView() {
            super.fireContentsChanged(this@AutocompleteTextField, 0, size)
        }
    }
}
