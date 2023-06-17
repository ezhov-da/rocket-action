package ru.ezhov.rocket.action.ui.utils.swing.common

import java.awt.Component
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities


object MoveUtil {
    fun addMoveAction(movableComponent: Component, grabbedComponent: Component) {
        val mouseAdapter: MouseAdapter = object : MouseAdapter() {
            var pressed = false
            var x = 0
            var y = 0
            override fun mousePressed(e: MouseEvent) {
                pressed = true
                val mousePoint = e.point
                SwingUtilities.convertPointToScreen(mousePoint, grabbedComponent)
                val framePoint = movableComponent.location
                x = mousePoint.x - framePoint.x
                y = mousePoint.y - framePoint.y
            }

            override fun mouseReleased(e: MouseEvent) {
                pressed = false

                // TODO ezhov groundwork for https://github.com/ezhov-da/rocket-action/issues/7
//                val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
//                val gs = ge.screenDevices
//                for (curGs in gs) {
//                    val gc = curGs.configurations
//                    for (curGc in gc) {
//                        val bounds = curGc.bounds
//                        println(
//                            bounds.getX()
//                                .toString() + "," +
//                                bounds.getY() + " " +
//                                bounds.getWidth() + "x" +
//                                bounds.getHeight()
//                        )
//                    }
//                }
//
//                println(Toolkit.getDefaultToolkit().screenSize)
//                println("${movableComponent.x} ${movableComponent.y}")
            }

            override fun mouseDragged(e: MouseEvent) {
                if (pressed) {
                    val mousePoint = e.point
                    SwingUtilities.convertPointToScreen(mousePoint, grabbedComponent)
                    movableComponent.location = Point(mousePoint.x - x, mousePoint.y - y)
                }
            }
        }
        grabbedComponent.addMouseListener(mouseAdapter)
        grabbedComponent.addMouseMotionListener(mouseAdapter)
    }
}
