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

fun renderQrCodes(image: BufferedImage, qrCodes: List<QrCode>): BufferedImage {
    println("QrCodes (${qrCodes.size}):")
    qrCodes.forEach { qrCode: QrCode ->
        println(qrCode.message)
    }
    return image
}

class MainPanel(val setWindowVisible: (Boolean) -> Unit) : JPanel(BorderLayout()) {
    private val qrCodeValues: ArrayList<String> = ArrayList<String>()
    private var renderedImage: BufferedImage? = null

    init {
        val toolBar = JToolBar()
        toolBar.isFloatable = false
        val scanAreaButton = JButton().apply {
            text = "Select Area"
            toolTipText = text
            addActionListener {
                println(text)
                screenshotAndDetectQrCodes(true)
            }
        }
        toolBar.add(scanAreaButton)

        val scanScreenButton = JButton().apply {
            text = "Entire Screen"
            toolTipText = text
            addActionListener {
                println(toolTipText)
                screenshotAndDetectQrCodes(false)
            }
        }
        toolBar.add(scanScreenButton)

        add(toolBar, BorderLayout.PAGE_START)
    }

    private fun screenshotAndDetectQrCodes(shouldSelectArea: Boolean) {
        setWindowVisible(false)

        val rectangle = if (shouldSelectArea) selectArea() else null
        val screenShot = createScreenShot(rectangle)

        setWindowVisible(true)

        val qrCodes = detectQrCodes(screenShot)
        qrCodes.forEach { qrCode: QrCode ->
            qrCodeValues.add(qrCode.message)
        }
        renderedImage = renderQrCodes(screenShot, qrCodes)
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
