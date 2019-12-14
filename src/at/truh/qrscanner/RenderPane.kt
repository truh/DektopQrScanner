package at.truh.qrscanner

import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JPanel

/**
 * Render image withing dynamically sized container.
 * Replicating the effect of CSS object-fit: contain.
 *
 * @param image The image that will be rendered int the container.
 */
class RenderPane(var image: BufferedImage? = null) : JPanel() {
    override fun paint(g: Graphics){
        super.paint(g)

        if (this.image != null) {
            // local copy of reference so it can't be overwritten while rendering
            val image = this.image!!

            val containerAspectRatio: Double = width.toDouble() / height.toDouble()
            val imageAspectRatio: Double = image.width.toDouble() / image.height.toDouble()

            val (imgWidth: Int, imgHeight: Int) =  if (containerAspectRatio > imageAspectRatio) {
                val imgHeight = height
                val imgWidth = image.width.toDouble() * imgHeight / image.height
                Pair(imgWidth.toInt(), imgHeight)
            } else {
                val imgWidth = width
                val imgHeight = image.height.toDouble() * imgWidth / image.width
                Pair(imgWidth, imgHeight.toInt())
            }

            val (imgX: Int, imgY: Int) = if (containerAspectRatio > imageAspectRatio) {
                val xOffset = (width - imgWidth) / 2
                Pair(xOffset, 0)
            } else {
                val yOffset = (height - imgHeight) / 2
                Pair(0, yOffset)
            }

            g.drawImage(image, imgX, imgY, imgWidth, imgHeight, null)
        }
    }
}