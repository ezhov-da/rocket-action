package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.dnd

import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.SelectedAtomicAction
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.dnd.DnDConstants
import java.awt.dnd.DragGestureEvent
import java.awt.dnd.DragGestureListener
import java.awt.dnd.DragSource
import java.awt.dnd.DragSourceDragEvent
import java.awt.dnd.DragSourceDropEvent
import java.awt.dnd.DragSourceEvent
import java.awt.dnd.DragSourceListener
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.TransferHandler

// http://www.java2s.com/Tutorial/Java/0240__Swing/Usedraganddroptoreorderalist.htm
internal class DragListener(private val list: JList<*>) : DragSourceListener, DragGestureListener {
    var ds = DragSource()

    init {
        ds.createDefaultDragGestureRecognizer(
            list,
            DnDConstants.ACTION_MOVE,
            this
        )
    }

    override fun dragGestureRecognized(dge: DragGestureEvent) {
        val transferable = StringSelection(Integer.toString(list.getSelectedIndex()))
        ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this)
    }

    override fun dragEnter(dsde: DragSourceDragEvent) {}
    override fun dragExit(dse: DragSourceEvent) {}
    override fun dragOver(dsde: DragSourceDragEvent) {}
    override fun dragDropEnd(dsde: DragSourceDropEvent) {
        // TODO ezhov
        if (dsde.dropSuccess) {
            println("Succeeded")
        } else {
            println("Failed")
        }
    }

    override fun dropActionChanged(dsde: DragSourceDragEvent) {}
}

internal class ListDropHandler(private val list: JList<SelectedAtomicAction>) : TransferHandler() {
    override fun canImport(support: TransferSupport): Boolean {
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false
        }
        val dl = support.dropLocation as JList.DropLocation
        return dl.index != -1
    }

    override fun importData(support: TransferSupport): Boolean {
        if (!canImport(support)) {
            return false
        }
        val transferable = support.transferable
        val indexString: String
        indexString = try {
            transferable.getTransferData(DataFlavor.stringFlavor) as String
        } catch (e: Exception) {
            return false
        }
        val index = indexString.toInt()
        val dl = support.dropLocation as JList.DropLocation
        val dropTargetIndex = dl.index

        val element = list.model.getElementAt(index)
        (list.model as DefaultListModel).add(dropTargetIndex, element)

        // TODO ezhov
        if (index == 0) {
            (list.model as DefaultListModel).remove(index)
        } else {
            if (dropTargetIndex > index) {
                (list.model as DefaultListModel).remove(index - 1)
            } else {
                (list.model as DefaultListModel).remove(index + 1)
            }
        }

        list.setSelectedValue(element, true)

        println("$dropTargetIndex : ")
        println("inserted")
        return true
    }
}
