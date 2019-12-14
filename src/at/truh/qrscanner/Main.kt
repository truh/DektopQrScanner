package at.truh.qrscanner

import boofcv.abst.fiducial.QrCodeDetector
import boofcv.alg.fiducial.qrcode.QrCode
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.gui.feature.VisualizeShapes
import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.GrayU8
import java.awt.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.max


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

fun detectQrCodes(image: BufferedImage): QrCodeDetector<GrayU8> {
    val gray: GrayU8 = ConvertBufferedImage.convertFrom(image, null as GrayU8?)
    val detector: QrCodeDetector<GrayU8> = FactoryFiducial.qrcode(null, GrayU8::class.java)
    detector.process(gray)
    return detector
}

fun renderQrCodes(image: BufferedImage, detector: QrCodeDetector<GrayU8>): BufferedImage {
    val qrCodes = detector.detections
    println("QrCodes (${qrCodes.size}):")
    qrCodes.forEach { qr: QrCode ->
        println(qr.message)
    }

    val g2: Graphics2D = image.createGraphics()
    g2.stroke = BasicStroke(4f)

    val failures: List<QrCode> = detector.failures
    g2.color = Color.RED
    for (qr in failures) {
        if (qr.failureCause.ordinal < QrCode.Failure.ERROR_CORRECTION.ordinal) continue
        VisualizeShapes.drawPolygon(qr.bounds, true, 1.0, g2)
    }

    g2.color = Color.GREEN
    for (qr in qrCodes) {
        println("message: " + qr.message)
        VisualizeShapes.drawPolygon(qr.bounds, true, 1.0, g2)
    }

    return image
}

class RenderPane() : JPanel() {
    var renderImage: BufferedImage? = null
    override fun paint(g: Graphics){
        super.paint(g)

        if (this.renderImage != null) {
            val renderImage = this.renderImage!!
            val containerAspectRatio: Double = width.toDouble() / height.toDouble()
            val imageAspectRatio: Double = renderImage.width.toDouble() / renderImage.height.toDouble()

            val (imgWidth: Int, imgHeight: Int) =  if (containerAspectRatio > imageAspectRatio) {
                val imgHeight = height
                val imgWidth = renderImage.width.toDouble() * imgHeight / renderImage.height
                Pair(imgWidth.toInt(), imgHeight)
            } else {
                val imgWidth = width
                val imgHeight = renderImage.height.toDouble() * imgWidth / renderImage.width
                Pair(imgWidth, imgHeight.toInt())
            }

            val (imgX: Int, imgY: Int) = if (containerAspectRatio > imageAspectRatio) {
                val xOffset = (width - imgWidth) / 2
                Pair(xOffset, 0)
            } else {
                val yOffset = (height - imgHeight) / 2
                Pair(0, yOffset)
            }

            g.drawImage(renderImage, imgX, imgY, imgWidth, imgHeight, null)
        }
    }
}

class MainPanel(val setWindowVisible: (Boolean) -> Unit) : JPanel(BorderLayout()) {
    private val renderPane = RenderPane()
    private val textArea: JTextArea = JTextArea().apply {
        isEditable = false
        rows = 10
    }

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
//        toolBar.add(scanAreaButton)

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
        add(JScrollPane(textArea), BorderLayout.SOUTH)
        add(renderPane, BorderLayout.CENTER)
    }

    private fun screenshotAndDetectQrCodes(shouldSelectArea: Boolean) {
        setWindowVisible(false)

        val rectangle = if (shouldSelectArea) selectArea() else null
        val screenShot = createScreenShot(rectangle)

        setWindowVisible(true)

        val detector = detectQrCodes(screenShot)
        detector.detections.forEach { qrCode: QrCode ->
            textArea.append(qrCode.message)
            textArea.append("\n")
        }
        renderPane.renderImage = renderQrCodes(screenShot, detector)
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
