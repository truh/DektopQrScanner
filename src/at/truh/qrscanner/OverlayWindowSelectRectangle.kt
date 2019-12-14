package at.truh.qrscanner

import org.jnativehook.GlobalScreen
import org.jnativehook.mouse.NativeMouseEvent
import org.jnativehook.mouse.NativeMouseListener
import org.jnativehook.mouse.NativeMouseMotionListener
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.JWindow

class OverlayWindowSelectRectangle(val rectangleCallback: (Rectangle) -> Unit) : JWindow(),
    NativeMouseListener,
    NativeMouseMotionListener {

    var startX: Int? = null
    var startY: Int? = null
    var currentX: Int? = null
    var currentY: Int? = null

    init {
        GlobalScreen.addNativeMouseMotionListener(this)
        GlobalScreen.addNativeMouseListener(this)
        background = Color(0, 0, 0, 127)
    }

    private fun finish() {
        GlobalScreen.removeNativeMouseMotionListener(this)
        GlobalScreen.removeNativeMouseListener(this)
        rectangleCallback(Rectangle(startX!!, startY!!, currentX!!, currentY!!))
    }

    override fun update(g: Graphics) {
        paint(g)
        if (startX != null && startY != null && currentX != null && currentY != null) {
            g.clearRect(0, 0, graphicsConfiguration.bounds.width, graphicsConfiguration.bounds.height)
            g.color = Color.RED
            g.drawRect(startX!!, startY!!, currentX!!, currentY!!)
        }
    }

    override fun nativeMouseMoved(event: NativeMouseEvent) {
        currentX = event.x
        currentY = event.y
        println("nativeMouseMoved")
        println("$startX $startY $currentX $currentY")
    }

    override fun nativeMousePressed(event: NativeMouseEvent) {
        startX = event.x
        startY = event.y
        currentX = event.x
        currentY = event.y
        println("nativeMousePressed")
        println("$startX $startY $currentX $currentY")
    }

    override fun nativeMouseReleased(event: NativeMouseEvent) {
        println("nativeMouseReleased")
        println("$startX $startY $currentX $currentY")
        finish()
    }

    override fun nativeMouseClicked(event: NativeMouseEvent) {}
    override fun nativeMouseDragged(event: NativeMouseEvent) {}
}

fun selectArea(timeout: Long = 10000): Rectangle? {
    var rectangle: Rectangle? = null
    var done: Boolean = false
    val overlayWindow =
        OverlayWindowSelectRectangle { rect: Rectangle ->
            rectangle = rect
            done = true
            println("rectangle")
            println(rectangle)
        }

    overlayWindow.isAlwaysOnTop = true
    overlayWindow.bounds = overlayWindow.graphicsConfiguration.bounds
    overlayWindow.isVisible = true
    overlayWindow.isEnabled = true

    var timePassed = 0

    while (!done && timeout > timePassed) {
        Thread.sleep(50)
        timePassed += 50
    }

    return rectangle
}
