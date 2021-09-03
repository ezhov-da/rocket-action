package ru.ezhov.rocket.action

import org.apache.batik.swing.JSVGCanvas
import org.apache.batik.swing.JSVGScrollPane
import org.apache.batik.swing.gvt.AbstractImageZoomInteractor
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter
import org.apache.batik.swing.gvt.GVTTreeRendererEvent
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter
import org.apache.batik.swing.svg.GVTTreeBuilderEvent
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.InputEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import javax.swing.*

class SVGApplication(  // The frame.
        protected var frame: JFrame) {
    // The "Load" button, which displays up a file chooser upon clicking.
    protected var button = JButton("Load...")

    // The status label.
    protected var label = JLabel()

    // The SVG canvas.
    protected var svgCanvas = JSVGCanvas()
    fun createComponents(): JComponent {
        // Create a panel and add the button, status label and the SVG canvas.
        val panel = JPanel(BorderLayout())
        val p = JPanel(FlowLayout(FlowLayout.LEFT))
        p.add(button)
        p.add(label)
        panel.add("North", p)
        panel.add("Center", JSVGScrollPane(svgCanvas))

        // Set the button action.
        button.addActionListener {
            val fc = JFileChooser(".")
            val choice = fc.showOpenDialog(panel)
            if (choice == JFileChooser.APPROVE_OPTION) {
                val f = fc.selectedFile
                try {
                    svgCanvas.uri = f.toURL().toString()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }

        // Set the JSVGCanvas listeners.
        svgCanvas.addSVGDocumentLoaderListener(object : SVGDocumentLoaderAdapter() {
            override fun documentLoadingStarted(e: SVGDocumentLoaderEvent) {
                label.text = "Document Loading..."
            }

            override fun documentLoadingCompleted(e: SVGDocumentLoaderEvent) {
                label.text = "Document Loaded."
            }
        })
        svgCanvas.addGVTTreeBuilderListener(object : GVTTreeBuilderAdapter() {
            override fun gvtBuildStarted(e: GVTTreeBuilderEvent) {
                label.text = "Build Started..."
            }

            override fun gvtBuildCompleted(e: GVTTreeBuilderEvent) {
                label.text = "Build Done."
                //                frame.pack();
            }
        })
        svgCanvas.addGVTTreeRendererListener(object : GVTTreeRendererAdapter() {
            override fun gvtRenderingPrepare(e: GVTTreeRendererEvent) {
                label.text = "Rendering Started..."
            }

            override fun gvtRenderingCompleted(e: GVTTreeRendererEvent) {
                label.text = ""
            }
        })
        svgCanvas.enableImageZoomInteractor = true
        svgCanvas.interactors.add(object : AbstractImageZoomInteractor() {
            override fun startInteraction(ie: InputEvent): Boolean {
                println("zoom")
                return super.startInteraction(ie)
            }
        })
        return panel
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Create a new JFrame.
            val f = JFrame("Batik")
            val app = SVGApplication(f)

            // Add components to the frame.
            f.contentPane.add(app.createComponents())

            // Display the frame.
            f.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    System.exit(0)
                }
            })
            f.setSize(400, 400)
            f.isVisible = true
        }
    }
}