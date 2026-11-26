package tachiyomi.presentation.widget.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import coil3.annotation.ExperimentalCoilApi
import coil3.asDrawable
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.size.Precision
import coil3.size.Scale
import coil3.transform.RoundedCornersTransformation
import eu.kanade.tachiyomi.util.system.dpToPx
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.presentation.widget.R
import tachiyomi.presentation.widget.components.CoverHeight
import tachiyomi.presentation.widget.components.CoverWidth

/** Loads [cover] at the widget cover size. */
internal suspend fun Context.loadWidgetCover(cover: MangaCover): Bitmap? {
    val roundPx = resources.getDimension(R.dimen.appwidget_inner_radius)
    return loadCover(
        cover = cover,
        widthPx = CoverWidth.value.toInt().dpToPx,
        heightPx = CoverHeight.value.toInt().dpToPx,
        // Rounded by the system from Android 12
        cornerRadiusPx = roundPx.takeIf { Build.VERSION.SDK_INT < Build.VERSION_CODES.S },
    )
}

/** Loads [cover] cropped to exactly [widthPx] by [heightPx]. */
@OptIn(ExperimentalCoilApi::class)
internal suspend fun Context.loadCover(
    cover: MangaCover,
    widthPx: Int,
    heightPx: Int,
    cornerRadiusPx: Float? = null,
): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(cover)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .precision(Precision.EXACT)
        .size(widthPx, heightPx)
        .scale(Scale.FILL)
        .let { if (cornerRadiusPx != null) it.transformations(RoundedCornersTransformation(cornerRadiusPx)) else it }
        .build()
    return imageLoader.execute(request)
        .image
        ?.asDrawable(resources)
        ?.toBitmap()
}
