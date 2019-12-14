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
            val imageLocal = this.image!!

            val containerAspectRatio: Double = this.width.toDouble() / this.height.toDouble()
            val imageAspectRatio: Double = imageLocal.width.toDouble() / imageLocal.height.toDouble()

            val (imgWidth: Int, imgHeight: Int) =  if (containerAspectRatio > imageAspectRatio) {
                val imgWidth = imageLocal.width * this.height / imageLocal.height
                Pair(imgWidth, this.height)
            } else {
                val imgHeight = imageLocal.height * this.width / imageLocal.width
                Pair(this.width, imgHeight)
            }

            val imgX = (this.width - imgWidth) / 2
            val imgY = (this.height - imgHeight) / 2

            g.drawImage(imageLocal, imgX, imgY, imgWidth, imgHeight, null)
        }
    }
}