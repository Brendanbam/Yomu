package tachiyomi.presentation.widget.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Bitmaps for the widget bookshelf, drawn after the app's Dark Walnut bookshelf
 * (`YomuBookshelfTier`, `YomuWoodShelfPlank` and `YomuPhysicalMangaVolume`), since widgets can't run Compose drawing.
 */
internal object WidgetBookshelf {

    /** Width of the page block beside the cover, as a share of the book's width. */
    private const val PAGE_BLOCK = 0.06f
    private const val COVER_RATIO = 1.5f

    /** Book bitmap height per unit of width. */
    const val BOOK_ASPECT = (1f - PAGE_BLOCK) * COVER_RATIO

    private const val ALCOVE_BACK_TOP = 0xFF1F140D.toInt()
    const val ALCOVE_BACK_BOTTOM = 0xFF120C08.toInt()
    private const val ALCOVE_PANEL_LINE = 0xFF2E1E14.toInt()
    private const val LANTERN_GLOW = 0xFFFFBE73.toInt()
    private const val SHELF_TOP_LIT = 0xFF9E6B40.toInt()
    private const val SHELF_TOP_BASE = 0xFF6B4122.toInt()
    private const val SHELF_BEVEL_HIGHLIGHT = 0xFFC49260.toInt()
    private const val SHELF_FRONT_TOP = 0xFF7D4E2C.toInt()
    private const val SHELF_FRONT_BOTTOM = 0xFF4B2B15.toInt()
    private const val SHELF_GRAIN_DARK = 0xFF3A200E.toInt()
    private const val SHELF_GRAIN_LIGHT = 0xFF9C693F.toInt()
    private const val SHELF_UNDER_SHADOW = 0xFF070402.toInt()
    private const val BOOK_PAGE_EDGE = 0xFFE7DAC6.toInt()
    private const val BOOK_PAGE_LINE = 0xFFBBA992.toInt()
    private const val CARD_SURFACE = 0xFF1D1713.toInt()

    /** The recessed wall behind one shelf of books, lit from above. */
    fun wall(widthPx: Int, heightPx: Int, density: Float): Bitmap {
        val w = widthPx.toFloat()
        val h = heightPx.toFloat()
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            h,
            intArrayOf(ALCOVE_BACK_TOP, ALCOVE_BACK_BOTTOM, blend(ALCOVE_BACK_BOTTOM, SHELF_UNDER_SHADOW, 0.22f)),
            floatArrayOf(0f, 0.75f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        val panelCount = (w / (68 * density)).toInt().coerceIn(4, 10)
        val step = w / panelCount
        paint.strokeWidth = density
        for (i in 1 until panelCount) {
            val x = step * i
            paint.color = alpha(ALCOVE_PANEL_LINE, 0.44f)
            canvas.drawLine(x, 0f, x, h, paint)
            paint.color = alpha(SHELF_BEVEL_HIGHLIGHT, 0.07f)
            canvas.drawLine(x + density, 0f, x + density, h, paint)
        }

        paint.shader = RadialGradient(
            w / 2f,
            0f,
            (w * 0.64f).coerceAtLeast(1f),
            intArrayOf(alpha(LANTERN_GLOW, 0.14f), alpha(LANTERN_GLOW, 0.045f), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, w, h, paint)

        val stile = 6 * density
        val stileShadow = alpha(SHELF_UNDER_SHADOW, 0.82f)
        val stileMid = alpha(SHELF_FRONT_TOP, 0.55f)
        paint.shader = LinearGradient(
            0f,
            0f,
            stile,
            0f,
            intArrayOf(SHELF_FRONT_BOTTOM, stileMid, stileShadow),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, stile, h, paint)
        paint.shader = LinearGradient(
            w - stile,
            0f,
            w,
            0f,
            intArrayOf(stileShadow, stileMid, SHELF_FRONT_BOTTOM),
            floatArrayOf(0f, 0.35f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(w - stile, 0f, w, h, paint)
        return bitmap
    }

    /** The wooden shelf the books stand on: lit top ledge, grained front board and the shadow it casts. */
    fun plank(widthPx: Int, heightPx: Int, density: Float): Bitmap {
        val w = widthPx.toFloat()
        val h = heightPx.toFloat()
        val ledge = h * 0.29f
        val faceBottom = h * 0.68f
        val face = faceBottom - ledge
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val lit = blend(SHELF_TOP_BASE, SHELF_TOP_LIT, 0.71f)
        val brightest = blend(SHELF_TOP_BASE, SHELF_TOP_LIT, 0.93f)
        paint.shader = LinearGradient(
            0f,
            0f,
            w,
            0f,
            intArrayOf(SHELF_TOP_BASE, lit, brightest, lit, SHELF_TOP_BASE),
            floatArrayOf(0f, 0.22f, 0.5f, 0.78f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, w, ledge, paint)
        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            ledge * 0.78f,
            intArrayOf(alpha(SHELF_UNDER_SHADOW, 0.5f), alpha(SHELF_UNDER_SHADOW, 0.11f), Color.TRANSPARENT),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, w, ledge * 0.78f, paint)

        paint.shader = LinearGradient(
            0f,
            ledge,
            0f,
            faceBottom,
            intArrayOf(
                blend(SHELF_FRONT_TOP, SHELF_BEVEL_HIGHLIGHT, 0.12f),
                SHELF_FRONT_TOP,
                blend(SHELF_FRONT_TOP, SHELF_FRONT_BOTTOM, 0.62f),
                SHELF_FRONT_BOTTOM,
            ),
            floatArrayOf(0f, 0.28f, 0.72f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, ledge, w, faceBottom, paint)
        paint.shader = RadialGradient(
            w / 2f,
            ledge + face * 0.15f,
            (w * 0.55f).coerceAtLeast(1f),
            intArrayOf(alpha(LANTERN_GLOW, 0.12f), alpha(LANTERN_GLOW, 0.03f), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, ledge, w, faceBottom, paint)
        val edgeShade = alpha(SHELF_UNDER_SHADOW, 0.42f)
        paint.shader = LinearGradient(
            0f,
            0f,
            w,
            0f,
            intArrayOf(edgeShade, Color.TRANSPARENT, Color.TRANSPARENT, edgeShade),
            floatArrayOf(0f, 0.08f, 0.92f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, ledge, w, faceBottom, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val fibers = 9
        for (i in 0 until fibers) {
            val baseY = ledge + face * (0.08f + (i + 0.5f) / fibers * 0.84f)
            val slow = (0.0045f + (i * 5 % 4) * 0.0014f) / density
            val fine = (0.017f + (i * 3 % 5) * 0.003f) / density
            val drift = (0.7f + (i % 3) * 0.35f) * density
            val path = Path()
            val segments = 40
            for (s in 0..segments) {
                val x = w * s / segments
                val y = (baseY + sin(x * slow + i * 2.13f) * drift + sin(x * fine + i * 3.6f) * drift * 0.28f)
                    .coerceIn(ledge + 1f, faceBottom - 1f)
                if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            val highlight = i % 3 == 0
            paint.color = if (highlight) alpha(SHELF_GRAIN_LIGHT, 0.19f) else alpha(SHELF_GRAIN_DARK, 0.29f)
            paint.strokeWidth = (if (highlight) 0.85f else 1.15f) * density
            canvas.drawPath(path, paint)
        }
        paint.style = Paint.Style.FILL

        paint.strokeWidth = 1.25f * density
        paint.shader = LinearGradient(
            0f,
            0f,
            w,
            0f,
            intArrayOf(
                alpha(SHELF_BEVEL_HIGHLIGHT, 0.22f),
                alpha(SHELF_BEVEL_HIGHLIGHT, 0.64f),
                alpha(SHELF_BEVEL_HIGHLIGHT, 0.89f),
                alpha(SHELF_BEVEL_HIGHLIGHT, 0.64f),
                alpha(SHELF_BEVEL_HIGHLIGHT, 0.22f),
            ),
            floatArrayOf(0f, 0.3f, 0.5f, 0.7f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawLine(0f, ledge + 0.6f * density, w, ledge + 0.6f * density, paint)
        paint.shader = null
        paint.strokeWidth = density
        paint.color = alpha(SHELF_UNDER_SHADOW, 0.75f)
        canvas.drawLine(0f, faceBottom - 0.5f * density, w, faceBottom - 0.5f * density, paint)

        paint.shader = LinearGradient(
            0f,
            faceBottom,
            0f,
            h,
            intArrayOf(alpha(SHELF_UNDER_SHADOW, 0.66f), alpha(SHELF_UNDER_SHADOW, 0.3f), Color.TRANSPARENT),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, faceBottom, w, h, paint)
        return bitmap
    }

    /** The size to load a cover at for a book [widthPx] wide. */
    fun coverSize(widthPx: Int): Pair<Int, Int> {
        val coverWidth = widthPx - pageBlockWidth(widthPx)
        return coverWidth to (coverWidth * COVER_RATIO).roundToInt()
    }

    /** A standing book: [cover] on the front board with a spine crease, and the page block on its right. */
    fun book(cover: Bitmap?, widthPx: Int, density: Float): Bitmap {
        val (coverWidth, heightPx) = coverSize(widthPx)
        val cw = coverWidth.toFloat()
        val h = heightPx.toFloat()
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val corner = 2.5f * density

        paint.shader = LinearGradient(
            cw,
            0f,
            widthPx.toFloat(),
            0f,
            intArrayOf(BOOK_PAGE_LINE, BOOK_PAGE_EDGE, BOOK_PAGE_EDGE, BOOK_PAGE_LINE),
            floatArrayOf(0f, 0.35f, 0.85f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRoundRect(
            RectF(cw - 2 * density, 1.5f * density, widthPx.toFloat(), h - density),
            corner,
            corner,
            paint,
        )
        paint.shader = null
        paint.color = alpha(BOOK_PAGE_LINE, 0.65f)
        paint.strokeWidth = 0.65f * density
        val edge = widthPx - cw
        for (i in 1..3) {
            val x = cw + edge * i / 4f
            canvas.drawLine(x, 3.5f * density, x, h - 3f * density, paint)
        }

        val board = RectF(0f, 0f, cw, h)
        canvas.save()
        canvas.clipPath(Path().apply { addRoundRect(board, corner, corner, Path.Direction.CW) })
        canvas.drawColor(CARD_SURFACE)
        if (cover != null) canvas.drawBitmap(cover, centerCrop(cover, cw / h), board, paint)
        val spine = 7.5f * density
        paint.shader = LinearGradient(
            0f,
            0f,
            spine,
            0f,
            intArrayOf(
                alpha(Color.BLACK, 0.44f),
                alpha(Color.WHITE, 0.14f),
                alpha(Color.BLACK, 0.18f),
                Color.TRANSPARENT,
            ),
            floatArrayOf(0f, 0.34f, 0.66f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, spine, h, paint)
        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            h * 0.18f,
            alpha(Color.WHITE, 0.075f),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, cw, h * 0.18f, paint)
        canvas.restore()

        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * density
        paint.color = alpha(Color.WHITE, 0.12f)
        val inset = paint.strokeWidth / 2f
        canvas.drawRoundRect(RectF(inset, inset, cw - inset, h - inset), corner, corner, paint)
        return bitmap
    }

    private fun pageBlockWidth(widthPx: Int) = (widthPx * PAGE_BLOCK).roundToInt().coerceAtLeast(2)

    private fun centerCrop(source: Bitmap, ratio: Float): Rect {
        val sourceRatio = source.width.toFloat() / source.height
        return if (sourceRatio > ratio) {
            val width = (source.height * ratio).roundToInt()
            val left = (source.width - width) / 2
            Rect(left, 0, left + width, source.height)
        } else {
            val height = (source.width / ratio).roundToInt()
            val top = (source.height - height) / 2
            Rect(0, top, source.width, top + height)
        }
    }

    private fun blend(from: Int, to: Int, fraction: Float) = ColorUtils.blendARGB(from, to, fraction)

    private fun alpha(color: Int, alpha: Float) = ColorUtils.setAlphaComponent(color, (alpha * 255).roundToInt())
}
