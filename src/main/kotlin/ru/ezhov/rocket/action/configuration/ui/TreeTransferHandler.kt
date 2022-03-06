package ru.ezhov.rocket.action.configuration.ui

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

// https://stackoverflow.com/questions/4588109/drag-and-drop-nodes-in-jtree
// https://coderanch.com/t/346509/java/JTree-drag-drop-tree-Java
internal class TreeTransferHandler : TransferHandler() {
    private var nodesFlavor: DataFlavor? = null
    private var flavors = arrayOfNulls<DataFlavor>(1)
    private var nodesToRemove: Array<DefaultMutableTreeNode> = arrayOf()

    override fun canImport(support: TransferSupport): Boolean {
        if (!support.isDrop) {
            return false
        }
        support.setShowDropLocation(true)
        return if (!support.isDataFlavorSupported(nodesFlavor)) {
            false
        } else true
        //        // Do not allow a drop on the drag source selections.
//        JTree.DropLocation dl =
//                (JTree.DropLocation) support.getDropLocation();
//        JTree tree = (JTree) support.getComponent();
//        int dropRow = tree.getRowForPath(dl.getPath());
//        int[] selRows = tree.getSelectionRows();
//        for (int i = 0; i < selRows.length; i++) {
//            if (selRows[i] == dropRow) {
//                return false;
//            }
//        }
//        // Do not allow MOVE-action drops if a non-leaf node is
//        // selected unless all of its children are also selected.
//        int action = support.getDropAction();
//        if (action == MOVE) {
//            return haveCompleteNode(tree);
//        }
//        // Do not allow a non-leaf node to be copied to a level
//        // which is less than its source level.
//        TreePath dest = dl.getPath();
//        DefaultMutableTreeNode target =
//                (DefaultMutableTreeNode) dest.getLastPathComponent();
//        TreePath path = tree.getPathForRow(selRows[0]);
//        DefaultMutableTreeNode firstNode =
//                (DefaultMutableTreeNode) path.getLastPathComponent();
//        if (firstNode.getChildCount() > 0 &&
//                target.getLevel() < firstNode.getLevel()) {
//            return false;
//        }
    }

    private fun haveCompleteNode(tree: JTree): Boolean {
        val selRows = tree.selectionRows
        var path = tree.getPathForRow(selRows[0])
        val first = path.lastPathComponent as DefaultMutableTreeNode
        val childCount = first.childCount
        // first has children and no children are selected.
        if (childCount > 0 && selRows.size == 1) return false
        // first may have children.
        for (i in 1 until selRows.size) {
            path = tree.getPathForRow(selRows[i])
            val next = path.lastPathComponent as DefaultMutableTreeNode
            if (first.isNodeChild(next)) {
                // Found a child of first.
                if (childCount > selRows.size - 1) {
                    // Not all children of first are selected.
                    return false
                }
            }
        }
        return true
    }

    override fun createTransferable(c: JComponent): Transferable? {
        val tree = c as JTree
        val paths = tree.selectionPaths
        if (paths != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            val copies: MutableList<DefaultMutableTreeNode> = ArrayList()
            val toRemove: MutableList<DefaultMutableTreeNode> = ArrayList()
            for (i in paths.indices) {
                val node = paths[i].lastPathComponent as DefaultMutableTreeNode
                val copyNode = copy(node)
                copies.add(copyNode)
                toRemove.add(node)
            }

//            DefaultMutableTreeNode node =
//                    (DefaultMutableTreeNode) paths[0].getLastPathComponent();
//            DefaultMutableTreeNode copy = copy(node);
//            copies.add(copy);
//            toRemove.add(node);
//            for (int i = 1; i < paths.length; i++) {
//                DefaultMutableTreeNode next =
//                        (DefaultMutableTreeNode) paths[i].getLastPathComponent();
//                // Do not allow higher level nodes to be added to list.
//                if (next.getLevel() < node.getLevel()) {
//                    break;
//                } else if (next.getLevel() > node.getLevel()) {  // child node
//                    copy.add(copy(next));
//                    // node already contains child
//                } else {                                        // sibling
//                    copies.add(copy(next));
//                    toRemove.add(next);
//                }
//            }
            val nodes = copies.toTypedArray()
            nodesToRemove = toRemove.toTypedArray()
            return NodesTransferable(nodes)
        }
        return null
    }

    /**
     * Defensive copy used in createTransferable.
     */
    private fun copy(copy: DefaultMutableTreeNode): DefaultMutableTreeNode {
        val treeNodeTo = DefaultMutableTreeNode(copy.userObject, true)
        for (i in 0 until copy.childCount) {
            val child = copy.getChildAt(i) as DefaultMutableTreeNode
            treeNodeTo.add(copy(child))
        }
        return treeNodeTo
    }

    override fun exportDone(source: JComponent, data: Transferable, action: Int) {
        if (action and MOVE == MOVE) {
            val tree = source as JTree
            val model = tree.model as DefaultTreeModel
            // Remove nodes saved in nodesToRemove in createTransferable.
            for (i in nodesToRemove.indices) {
                model.removeNodeFromParent(nodesToRemove[i])
            }
        }
    }

    override fun getSourceActions(c: JComponent): Int {
        return COPY_OR_MOVE
    }

    override fun importData(support: TransferSupport): Boolean {
        if (!canImport(support)) {
            return false
        }
        // Extract transfer data.
        var nodes: Array<DefaultMutableTreeNode?>? = null
        try {
            val t = support.transferable
            nodes = t.getTransferData(nodesFlavor) as Array<DefaultMutableTreeNode?>
        } catch (ufe: UnsupportedFlavorException) {
            println("UnsupportedFlavor: " + ufe.message)
        } catch (ioe: IOException) {
            println("I/O error: " + ioe.message)
        }
        // Get drop location info.
        val dl = support.dropLocation as JTree.DropLocation
        val childIndex = dl.childIndex
        val dest = dl.path
        val parent = dest.lastPathComponent as DefaultMutableTreeNode
        val tree = support.component as JTree
        val model = tree.model as DefaultTreeModel
        // Configure for drop mode.
        var index = childIndex // DropMode.INSERT
        if (childIndex == -1) {     // DropMode.ON
            index = parent.childCount
        }
        // Add data to model.
        for (i in nodes!!.indices) {
            model.insertNodeInto(nodes[i], parent, index++)
        }
        return true
    }

    override fun toString(): String {
        return javaClass.name
    }

    inner class NodesTransferable(var nodes: Array<DefaultMutableTreeNode>) : Transferable {
        @Throws(UnsupportedFlavorException::class)
        override fun getTransferData(flavor: DataFlavor): Any {
            if (!isDataFlavorSupported(flavor)) throw UnsupportedFlavorException(flavor)
            return nodes
        }

        override fun getTransferDataFlavors(): Array<DataFlavor?> {
            return flavors
        }

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
            return nodesFlavor!!.equals(flavor)
        }
    }

    init {
        try {
            val mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                ";class=\"" +
                Array<DefaultMutableTreeNode>::class.java.name +
                "\""
            nodesFlavor = DataFlavor(mimeType)
            flavors[0] = nodesFlavor
        } catch (e: ClassNotFoundException) {
            println("ClassNotFound: " + e.message)
        }
    }
}