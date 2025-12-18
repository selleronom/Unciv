package com.unciv.app.ios

import com.badlogic.gdx.graphics.Pixmap
import com.unciv.ui.components.fonts.FontFamilyData
import com.unciv.ui.components.fonts.FontImplementation
import com.unciv.ui.components.fonts.FontMetricsCommon
import com.unciv.ui.components.fonts.Fonts
import org.robovm.apple.coregraphics.CGBitmapContext
import org.robovm.apple.coregraphics.CGColorSpace
import org.robovm.apple.coregraphics.CGImageAlphaInfo
import org.robovm.apple.coregraphics.CGRect
import org.robovm.apple.foundation.NSString
import org.robovm.apple.uikit.NSAttributedStringAttributes
import org.robovm.apple.uikit.NSStringDrawingContext
import org.robovm.apple.uikit.NSStringDrawingOptions
import org.robovm.apple.uikit.UIColor
import org.robovm.apple.uikit.UIGraphics
import org.robovm.apple.uikit.UIFont
import org.robovm.apple.coregraphics.CGSize
import kotlin.math.ceil
 

/** iOS font implementation using CoreText/CoreGraphics to render glyphs into Pixmaps. */
class IOSFont : FontImplementation {
    private var uiFont: UIFont? = null
    private var sizePx: Int = Fonts.ORIGINAL_FONT_SIZE.toInt()
    private var familyName: String = Fonts.DEFAULT_FONT_FAMILY

    override fun setFontFamily(fontFamilyData: FontFamilyData, size: Int) {
        familyName = fontFamilyData.invariantName
        sizePx = size
        uiFont = createUIFont(familyName.ifBlank { defaultSystemFamily() }, size.toDouble())
    }

    override fun getFontSize(): Int = sizePx

    override fun getCharPixmap(symbolString: String): Pixmap {
        val text = if (symbolString.isEmpty()) " " else symbolString
        val font = uiFont ?: createUIFont(defaultSystemFamily(), sizePx.toDouble())

        // Measure text using UIKit NSString drawing APIs
        val attrs = NSAttributedStringAttributes().apply {
            setFont(font)
            setForegroundColor(UIColor.white())
        }
        val maxSize = CGSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        val options = NSStringDrawingOptions.UsesLineFragmentOrigin
        val bounds = NSString.getBoundingRect(text, maxSize, options, attrs, NSStringDrawingContext())
        var w = ceil(bounds.size.width).toInt().coerceAtLeast(1)
        var h = ceil(bounds.size.height).toInt().coerceAtLeast(1)

        // Create RGBA bitmap context with transparent background
        val colorSpace = CGColorSpace.createDeviceRGB()
        val totalBytes = w * h * 4
        val ctxBytes = ByteArray(totalBytes)
        val ctx = CGBitmapContext.create(
            ctxBytes, w.toLong(), h.toLong(), 8,
            (w * 4).toLong(), colorSpace, CGImageAlphaInfo.PremultipliedLast
        )
        // Clear to transparent
        ctx.setFillColor(UIColor.clear().cgColor)
        ctx.fillRect(CGRect(0.0, 0.0, w.toDouble(), h.toDouble()))

        // Draw text using UIKit into this context
        UIGraphics.pushContext(ctx)
        try {
            // UIKit coordinates are top-left origin in image contexts; draw at 0,0
            NSString.draw(text, CGRect(0.0, 0.0, w.toDouble(), h.toDouble()), options, attrs, NSStringDrawingContext())
        } finally {
            UIGraphics.popContext()
        }

    // Extract pixel data: transfer from the backing direct buffer
        val pixmap = Pixmap(w, h, Pixmap.Format.RGBA8888)
        val rowStride = w * 4
        val flipped = ByteArray(totalBytes)
        var y = 0
        while (y < h) {
            val src = (h - 1 - y) * rowStride
            val dst = y * rowStride
            System.arraycopy(ctxBytes, src, flipped, dst, rowStride)
            y++
        }
        val bb = pixmap.pixels
        bb.clear()
        bb.put(flipped)
        bb.flip()
        return pixmap
    }

    override fun getSystemFonts(): Sequence<FontFamilyData> = sequenceOf(
        FontFamilyData(Fonts.DEFAULT_FONT_FAMILY),
        FontFamilyData("Helvetica"),
        FontFamilyData("Times New Roman"),
        FontFamilyData("Courier New")
    )

    override fun getMetrics(): FontMetricsCommon {
        val font = uiFont ?: createUIFont(defaultSystemFamily(), sizePx.toDouble())
        val ascent = font.ascender.toFloat()
        val descent = -font.descender.toFloat() // descender is negative in UIKit
        val leading = font.leading.toFloat()
        val height = ascent + descent + leading
        return FontMetricsCommon(ascent, descent, height, leading)
    }

    // Helpers
    private fun createUIFont(family: String, size: Double): UIFont =
    UIFont.getFont(family, size) ?: UIFont.getSystemFont(size)

    private fun defaultSystemFamily(): String = ".AppleSystemUIFont"
}

// no-op helpers retained if needed later
