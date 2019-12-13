package at.truh.qrscanner

import boofcv.abst.fiducial.QrCodeDetector
import boofcv.alg.fiducial.qrcode.QrCode
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.GrayU8
import org.jnativehook.GlobalScreen
import org.jnativehook.mouse.NativeMouseEvent
import org.jnativehook.mouse.NativeMouseListener
import org.jnativehook.mouse.NativeMouseMotionListener
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.image.BufferedImage
import javax.swing.*


fun createScreenShot(screenRectangle: Rectangle? = null): BufferedImage {
    val screenRect: Rectangle = if (screenRectangle == null) {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        Rectangle(0, 0, screenSize.width, screenSize.height)
    } else {
        screenRectangle
    }
    val wallE = Robot()
    return wallE.createScreenCapture(screenRect)
}

fun detectQrCodes(image: BufferedImage): List<QrCode> {
    val gray: GrayU8 = ConvertBufferedImage.convertFrom(image, null as GrayU8?)
    val detector: QrCodeDetector<GrayU8> = FactoryFiducial.qrcode(null, GrayU8::class.java)
    detector.process(gray)
    return detector.detections
}

fun renderQrCodes(image: BufferedImage, qrCodes: List<QrCode>) {
    println("QrCodes (${qrCodes.size}):")
    qrCodes.forEach { qrCode: QrCode ->
        println(qrCode.message)
    }
}

class OverlayWindowSelectRectangle(val rectangleCallback: (Rectangle) -> Unit) : JWindow(), NativeMouseListener, NativeMouseMotionListener {

    var startX: Int? = null
    var startY: Int? = null
    var currentX: Int? = null
    var currentY: Int? = null

    init {
        GlobalScreen.addNativeMouseMotionListener(this)
        GlobalScreen.addNativeMouseListener(this)
        background = Color(0, 0, 0, 127)
    }

    fun finish() {
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
    val overlayWindow = OverlayWindowSelectRectangle { rect: Rectangle ->
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

class MainPanel(setWindowVisible: (Boolean) -> Unit) : JPanel(BorderLayout()) {
    private var qrCodeValues: ArrayList<String>? = null
    private var renderedImage: BufferedImage? = null

    init {
        val toolBar = JToolBar()
        toolBar.isFloatable = false
        val scanAreaButton = JButton("Select Area").apply {
            toolTipText = "Select Area"
            addActionListener {
                println("Select Area")
                setWindowVisible(false)
                val rectangle = selectArea()
                val screenShot = createScreenShot(rectangle)
                setWindowVisible(true)
                val qrCodes = detectQrCodes(screenShot)
                renderQrCodes(screenShot, qrCodes)
            }
        }
//        toolBar.add(scanAreaButton)

        val scanScreenButton = JButton("Entire Screen").apply {
            toolTipText = "Entire Screen"
            addActionListener {
                println("Entire Screen")
                setWindowVisible(false)
                val screenShot = createScreenShot()
                setWindowVisible(true)
                val qrCodes = detectQrCodes(screenShot)
                renderQrCodes(screenShot, qrCodes)
            }
        }
        toolBar.add(scanScreenButton)

        add(toolBar, BorderLayout.PAGE_START)
    }
}

fun createAndShowGui() {
    val frame = JFrame()
    val mainPanel = MainPanel { visible: Boolean -> frame.isVisible = visible }
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.add(mainPanel)
    frame.pack()
    frame.isVisible = true
}

fun main() {
    SwingUtilities.invokeLater {
        createAndShowGui()
    }
}
