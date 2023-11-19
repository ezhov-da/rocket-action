package ru.ezhov.rocket.action.application.configuration.ui.tree.move

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.configuration.ui.tree.TreeRocketActionSettings
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.tree.DefaultMutableTreeNode

internal class MoveToPanelTest

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________");

        val current = DefaultMutableTreeNode(mockk<TreeRocketActionSettings> {
            every { asString() } returns "111"
        })

        frame.add(
            MoveToPanel(
                currentNode = current,
                nodes = listOf(
                    current,
                    DefaultMutableTreeNode(mockk<TreeRocketActionSettings> {
                        every { asString() } returns "111"
                    }),
                    DefaultMutableTreeNode(
                        mockk<TreeRocketActionSettings> {
                            every { asString() } returns "222"
                        }
                    ),
                    DefaultMutableTreeNode(
                        mockk<TreeRocketActionSettings> {
                            every { asString() } returns "333"
                        }
                    ),
                    DefaultMutableTreeNode(
                        mockk<TreeRocketActionSettings> {
                            every { asString() } returns "444"
                        }
                    ),
                ),
                selectCallback = { node -> println(node) }
            ),
        )
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;
        frame.isVisible = true;
    }
}
