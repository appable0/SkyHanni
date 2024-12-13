package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import net.minecraft.client.Minecraft
import java.awt.Color

/**
 * A renderable single line of text, which can contain format codes. The text content of this field may
 * change at any time.
 */
class RenderableString(
    text: String = "",
    val scale: Double = 1.0,
    val color: Color = Color.WHITE,
    override val horizontalAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
    override val verticalAlign: VerticalAlignment = VerticalAlignment.CENTER,
) : Renderable {

    /**
     * The text to render. If null or blank, nothing is rendered.
     */
    var text: String = text
        set(value) {
            cachedWidth = -1
            field = value
        }

    private var cachedWidth: Int = -1

    /**
     * Returns true if there is any renderable content.
     */
    val hasContent get() = text.isNotBlank()

    /**
     * Clears the text content.
     */
    fun clear() {
        this.text = ""
    }

    override val width
        get() = if (cachedWidth != -1) {
            cachedWidth
        } else {
            Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) * scale.toInt() + 1
        }

    override val height = (9 * scale).toInt() + 1

    override fun render(posX: Int, posY: Int) {
        if (hasContent) {
            RenderableUtils.renderString(text, scale, color, 1 / scale)
        }
    }
}
