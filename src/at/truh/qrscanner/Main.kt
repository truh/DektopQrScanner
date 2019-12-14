package at.truh.qrscanner

import boofcv.abst.fiducial.QrCodeDetector
import boofcv.alg.fiducial.qrcode.QrCode
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.GrayU8
import java.awt.*
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

class MainPanel(setWindowVisible: (Boolean) -> Unit) : JPanel(BorderLayout()) {
    private val qrCodeValues: ArrayList<String> = ArrayList<String>()
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
