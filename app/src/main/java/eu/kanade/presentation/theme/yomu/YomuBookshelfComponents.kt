package eu.kanade.presentation.theme.yomu

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import eu.kanade.presentation.manga.components.MangaCover
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Bookmark
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import tachiyomi.domain.manga.model.MangaCover as MangaCoverModel

private data class CachedGrainStroke(
    val path: Path,
    val color: Color,
    val widthPx: Float,
)

/**
 * Draws a single realistic wooden shelf plank using [Modifier.drawWithCache] so all
 * brushes, cathedral grain contours, wood pore lines, and ambient shadows are allocated
 * once per size/palette change instead of every scroll frame.
 */
@Composable
fun YomuWoodShelfPlank(
    rowIndex: Int,
    modifier: Modifier = Modifier,
    topLedgeHeight: Dp = 11.dp,
    frontFaceHeight: Dp = 16.dp,
    underShadowHeight: Dp = 13.dp,
) {
    val style = LocalYomuStyle.current
    val palette = style.shelfPalette
    val lighting = style.lightingIntensity
    val decoration = style.decorationIntensity
    val shadowScale = style.shadowIntensity
    val totalHeight = topLedgeHeight + frontFaceHeight + underShadowHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .drawWithCache {
                val w = size.width
                val topH = topLedgeHeight.toPx()
                val faceH = frontFaceHeight.toPx()
                val shadowH = underShadowHeight.toPx()
                val faceTop = topH
                val faceBottom = topH + faceH

                // 1. Upper perspective shelf ledge lit by overhead Japanese lantern
                val topLedgeBrush = Brush.horizontalGradient(
                    0f to palette.shelfTopBase,
                    0.22f to lerpColor(palette.shelfTopBase, palette.shelfTopLit, 0.45f + 0.35f * lighting),
                    0.5f to lerpColor(palette.shelfTopBase, palette.shelfTopLit, 0.72f + 0.28f * lighting),
                    0.78f to lerpColor(palette.shelfTopBase, palette.shelfTopLit, 0.45f + 0.35f * lighting),
                    1f to palette.shelfTopBase,
                )
                val rearAoBrush = Brush.verticalGradient(
                    0f to palette.shelfUnderShadow.copy(alpha = (if (palette.isLight) 0.28f else 0.62f) * shadowScale),
                    0.65f to palette.shelfUnderShadow.copy(alpha = 0.14f * shadowScale),
                    1f to Color.Transparent,
                    startY = 0f,
                    endY = topH * 0.78f,
                )

                // 2. Front fascia board base with subtle satin sheen
                val frontBaseBrush = Brush.verticalGradient(
                    0f to lerpColor(palette.shelfFrontTop, palette.shelfBevelHighlight, 0.12f),
                    0.28f to palette.shelfFrontTop,
                    0.72f to lerpColor(palette.shelfFrontTop, palette.shelfFrontBottom, 0.62f),
                    1f to palette.shelfFrontBottom,
                    startY = faceTop,
                    endY = faceBottom,
                )
                val frontLanternBrush = if (lighting > 0.05f) {
                    Brush.radialGradient(
                        colors = listOf(
                            palette.lanternGlow.copy(alpha = (if (palette.isLight) 0.20f else 0.16f) * lighting),
                            palette.lanternGlow.copy(alpha = 0.04f * lighting),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.5f, faceTop + faceH * 0.15f),
                        radius = (w * 0.55f).coerceAtLeast(1f),
                    )
                } else {
                    null
                }
                val frontEdgeVignetteBrush = Brush.horizontalGradient(
                    0f to palette.shelfUnderShadow.copy(alpha = if (palette.isLight) 0.18f else 0.42f),
                    0.08f to Color.Transparent,
                    0.92f to Color.Transparent,
                    1f to palette.shelfUnderShadow.copy(alpha = if (palette.isLight) 0.18f else 0.42f),
                )

                // 3. Multi-layer organic wood grain (cathedral heartwood curves + fine longitudinal pores)
                val knotX = w * (0.16f + ((abs(rowIndex * 41 + 17) % 68) / 100f))
                val knotY = faceTop + faceH * (0.34f + ((abs(rowIndex * 7) % 4) * 0.11f))
                val grainStrokes = buildList {
                    if (decoration > 0.1f && w > 0f) {
                        val seed = (rowIndex * 7919 + 104729).toFloat()
                        val primaryFibers = (6 + (decoration * 6).toInt()).coerceAtMost(12)
                        val segments = 22
                        for (i in 0 until primaryFibers) {
                            val path = Path()
                            val norm = (i + 0.5f) / primaryFibers
                            val baseY = faceTop + faceH * (0.08f + norm * 0.84f)
                            val slowFreq = 0.0045f + ((i * 5 + rowIndex) % 4) * 0.0014f
                            val fineFreq = 0.017f + ((i * 3 + rowIndex) % 5) * 0.003f
                            val phase = seed * 0.011f + i * 2.13f
                            val drift = (0.7f + ((i + rowIndex) % 3) * 0.35f).dp.toPx()

                            for (s in 0..segments) {
                                val x = w * (s.toFloat() / segments)
                                val distToKnot = abs(x - knotX)
                                val knotRadius = 56.dp.toPx()
                                // Deflect grain organically around the wood knot like real planed timber
                                val knotDeflect = if (distToKnot < knotRadius) {
                                    val t = cos((distToKnot / knotRadius) * (Math.PI / 2.0)).toFloat()
                                    val sign = if (baseY >= knotY) 1f else -1f
                                    sign * t * t * 2.8.dp.toPx()
                                } else {
                                    0f
                                }
                                val wave = sin(x * slowFreq + phase) * drift +
                                    sin(x * fineFreq + phase * 1.7f) * (drift * 0.28f)
                                val y = (baseY + wave + knotDeflect)
                                    .coerceIn(faceTop + 0.8f, faceBottom - 0.8f)
                                if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            val isHighlight = (i + rowIndex) % 3 == 0
                            add(
                                CachedGrainStroke(
                                    path = path,
                                    color = if (isHighlight) {
                                        palette.shelfGrainLight.copy(alpha = 0.22f * decoration)
                                    } else {
                                        palette.shelfGrainDark.copy(alpha = 0.34f * decoration)
                                    },
                                    widthPx = (if (isHighlight) 0.85f else 1.15f).dp.toPx(),
                                ),
                            )
                        }

                        // Fine top-ledge horizontal grain lines
                        for (t in 0 until 4) {
                            val y = topH * (0.20f + t * 0.22f)
                            val topPath = Path().apply {
                                moveTo(0f, y)
                                cubicTo(
                                    w * 0.33f,
                                    (y + ((t % 2) * 1.4f - 0.7f).dp.toPx()).coerceIn(1f, topH - 1f),
                                    w * 0.66f,
                                    (y - ((t % 2) * 1.4f - 0.7f).dp.toPx()).coerceIn(1f, topH - 1f),
                                    w,
                                    y,
                                )
                            }
                            add(
                                CachedGrainStroke(
                                    path = topPath,
                                    color = palette.shelfGrainDark.copy(alpha = 0.18f * decoration),
                                    widthPx = 0.8.dp.toPx(),
                                ),
                            )
                        }
                    }
                }

                // 4. Crisp front bevel lip catching overhead light
                val bevelBrush = Brush.horizontalGradient(
                    0f to palette.shelfBevelHighlight.copy(alpha = 0.22f),
                    0.3f to palette.shelfBevelHighlight.copy(alpha = 0.45f + 0.25f * lighting),
                    0.5f to palette.shelfBevelHighlight.copy(alpha = 0.65f + 0.32f * lighting),
                    0.7f to palette.shelfBevelHighlight.copy(alpha = 0.45f + 0.25f * lighting),
                    1f to palette.shelfBevelHighlight.copy(alpha = 0.22f),
                )

                // 5. Cast drop shadow beneath the shelf
                val underShadowBrush = if (shadowScale > 0.05f) {
                    Brush.verticalGradient(
                        0f to palette.shelfUnderShadow.copy(
                            alpha = (if (palette.isLight) 0.30f else 0.82f) * shadowScale,
                        ),
                        0.42f to palette.shelfUnderShadow.copy(
                            alpha = (if (palette.isLight) 0.12f else 0.38f) * shadowScale,
                        ),
                        1f to Color.Transparent,
                        startY = faceBottom,
                        endY = faceBottom + shadowH,
                    )
                } else {
                    null
                }

                val knotOuterSize = Size(24.dp.toPx(), 5.4.dp.toPx())
                val knotInnerSize = Size(12.dp.toPx(), 2.6.dp.toPx())
                val knotOuterTopLeft = Offset(knotX - knotOuterSize.width / 2f, knotY - knotOuterSize.height / 2f)
                val knotInnerTopLeft = Offset(knotX - knotInnerSize.width / 2f, knotY - knotInnerSize.height / 2f)
                val knotColor = palette.shelfGrainDark.copy(alpha = 0.24f * decoration)
                val knotStroke = Stroke(width = 0.9.dp.toPx())
                val bevelY = faceTop + 0.6.dp.toPx()
                val bevelStrokePx = 1.25.dp.toPx()
                val bottomLipY = faceBottom - 0.5.dp.toPx()
                val bottomLipStrokePx = 1.dp.toPx()
                val bottomLipColor = palette.shelfUnderShadow.copy(alpha = 0.75f)

                onDrawBehind {
                    drawRect(brush = topLedgeBrush, topLeft = Offset.Zero, size = Size(w, topH))
                    drawRect(brush = rearAoBrush, topLeft = Offset.Zero, size = Size(w, topH * 0.78f))
                    drawRect(brush = frontBaseBrush, topLeft = Offset(0f, faceTop), size = Size(w, faceH))

                    if (frontLanternBrush != null) {
                        drawRect(brush = frontLanternBrush, topLeft = Offset(0f, faceTop), size = Size(w, faceH))
                    }
                    drawRect(brush = frontEdgeVignetteBrush, topLeft = Offset(0f, faceTop), size = Size(w, faceH))

                    for (stroke in grainStrokes) {
                        drawPath(
                            path = stroke.path,
                            color = stroke.color,
                            style = Stroke(width = stroke.widthPx, cap = StrokeCap.Round),
                        )
                    }

                    if (decoration > 0.25f) {
                        drawOval(
                            color = knotColor,
                            topLeft = knotOuterTopLeft,
                            size = knotOuterSize,
                            style = knotStroke,
                        )
                        drawOval(
                            color = knotColor.copy(alpha = knotColor.alpha * 0.85f),
                            topLeft = knotInnerTopLeft,
                            size = knotInnerSize,
                            style = knotStroke,
                        )
                    }

                    drawLine(
                        brush = bevelBrush,
                        start = Offset(0f, bevelY),
                        end = Offset(w, bevelY),
                        strokeWidth = bevelStrokePx,
                    )
                    drawLine(
                        color = bottomLipColor,
                        start = Offset(0f, bottomLipY),
                        end = Offset(w, bottomLipY),
                        strokeWidth = bottomLipStrokePx,
                    )

                    if (underShadowBrush != null) {
                        drawRect(
                            brush = underShadowBrush,
                            topLeft = Offset(0f, faceBottom),
                            size = Size(w, shadowH),
                        )
                    }
                }
            },
    )
}

/**
 * A full bookcase shelf tier: recessed dark wood alcove with cached tongue-and-groove
 * back paneling, overhead warm lantern spotlight cone, side bookcase stiles, physical
 * volumes resting flush on the shelf ledge, and the solid wood shelf plank underneath.
 */
@Composable
fun YomuBookshelfTier(
    rowIndex: Int,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 14.dp,
    itemSpacing: Dp = 12.dp,
    topAlcovePadding: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val style = LocalYomuStyle.current
    val palette = style.shelfPalette
    val lighting = style.lightingIntensity
    val decoration = style.decorationIntensity

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawWithCache {
                val w = size.width
                val h = size.height
                val backBrush = Brush.verticalGradient(
                    0f to palette.alcoveBackTop,
                    0.75f to palette.alcoveBackBottom,
                    1f to lerpColor(palette.alcoveBackBottom, palette.shelfUnderShadow, 0.22f),
                )
                val lanternBrush = if (lighting > 0.05f && w > 0f) {
                    Brush.radialGradient(
                        colors = listOf(
                            palette.lanternGlow.copy(alpha = (if (palette.isLight) 0.26f else 0.19f) * lighting),
                            palette.lanternGlow.copy(alpha = (if (palette.isLight) 0.09f else 0.06f) * lighting),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.5f, 0f),
                        radius = (w * 0.64f).coerceAtLeast(1f),
                    )
                } else {
                    null
                }
                val stileW = 6.dp.toPx()
                val leftStileBrush = Brush.horizontalGradient(
                    0f to palette.shelfFrontBottom,
                    0.65f to palette.shelfFrontTop.copy(alpha = 0.55f),
                    1f to palette.shelfUnderShadow.copy(alpha = if (palette.isLight) 0.28f else 0.82f),
                    startX = 0f,
                    endX = stileW,
                )
                val rightStileBrush = Brush.horizontalGradient(
                    0f to palette.shelfUnderShadow.copy(alpha = if (palette.isLight) 0.28f else 0.82f),
                    0.35f to palette.shelfFrontTop.copy(alpha = 0.55f),
                    1f to palette.shelfFrontBottom,
                    startX = (w - stileW).coerceAtLeast(0f),
                    endX = w,
                )
                val panelLines = if (decoration > 0.15f && w > 0f) {
                    val panelCount = (w / 68.dp.toPx()).toInt().coerceIn(4, 10)
                    val step = w / panelCount
                    (1 until panelCount).map { step * it }
                } else {
                    emptyList()
                }
                val grooveDark = palette.alcovePanelLine.copy(alpha = 0.52f * decoration)
                val grooveLight = palette.shelfBevelHighlight.copy(alpha = 0.08f * decoration)
                val grooveWidthPx = 1.dp.toPx()

                onDrawBehind {
                    drawRect(brush = backBrush)

                    for (x in panelLines) {
                        drawLine(
                            color = grooveDark,
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = grooveWidthPx,
                        )
                        drawLine(
                            color = grooveLight,
                            start = Offset(x + grooveWidthPx, 0f),
                            end = Offset(x + grooveWidthPx, h),
                            strokeWidth = grooveWidthPx * 0.7f,
                        )
                    }

                    if (lanternBrush != null) {
                        drawRect(brush = lanternBrush)
                    }

                    drawRect(brush = leftStileBrush, topLeft = Offset.Zero, size = Size(stileW, h))
                    drawRect(brush = rightStileBrush, topLeft = Offset(w - stileW, 0f), size = Size(stileW, h))
                }
            },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = topAlcovePadding,
                    ),
            ) {
                content()
            }
            YomuWoodShelfPlank(
                rowIndex = rowIndex,
                modifier = Modifier.offset(y = (-4).dp),
            )
        }
    }
}

/**
 * Physical manga volume styled to match the Primary Design bookshelf target:
 * crisp physical book corners, undistorted cover art, cached tactile parchment page-block,
 * subtle spine fold crease, grounded contact shadow, single clean focus target for
 * controller/keyboard navigation, and smooth lift on focus/selection.
 */
@Composable
fun YomuPhysicalMangaVolume(
    coverData: MangaCoverModel,
    title: String,
    unreadCount: Long,
    downloadCount: Int,
    readProgress: Float?,
    hasBookmarks: Boolean,
    isSelected: Boolean,
    showTitle: Boolean,
    showProgress: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickContinueReading: (() -> Unit)? = null,
    collectorMode: Boolean = false,
    modifier: Modifier = Modifier,
    isInspected: Boolean = false,
) {
    val style = LocalYomuStyle.current
    val palette = style.shelfPalette
    val motionScale = style.effectiveMotionScale
    val thicknessDp = ((if (collectorMode) 6.5f else 4.5f) * (0.4f + 0.6f * style.bookThickness)).dp
    val shadowScale = style.shadowIntensity

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    val isElevated = isSelected || isFocused || isInspected
    val liftY by animateDpAsState(
        targetValue = if (isElevated && motionScale > 0f) (-6 * motionScale).dp else 0.dp,
        animationSpec = if (motionScale == 0f) {
            snap()
        } else {
            spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
        },
        label = "bookLift",
    )
    val scale by animateFloatAsState(
        targetValue = if (isElevated && motionScale > 0f) 1f + 0.025f * motionScale else 1f,
        animationSpec = if (motionScale == 0f) snap() else spring(stiffness = Spring.StiffnessMediumLow),
        label = "bookScale",
    )

    val bookShape = YomuShapes.bookCover
    val primaryColor = MaterialTheme.colorScheme.primary
    val unreadLabel = stringResource(MR.strings.yomu_home_unread_remaining, unreadCount)
    val downloadLabel = stringResource(MR.strings.yomu_downloaded_count, downloadCount)
    val inspectedLabel = stringResource(MR.strings.yomu_tablet_inspected_book)
    val accessibilityLabel = buildList {
        add(title)
        if (unreadCount > 0) add(unreadLabel)
        if (downloadCount > 0) add(downloadLabel)
    }.joinToString(", ")

    Box(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .semantics {
                contentDescription = accessibilityLabel
                if (isInspected) stateDescription = inspectedLabel
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        // Cached grounded contact shadow directly beneath the book resting on the wooden shelf
        if (shadowScale > 0.05f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(10.dp)
                    .offset(y = 3.dp)
                    .drawWithCache {
                        val shadowBrush = Brush.radialGradient(
                            colors = listOf(
                                palette.bookSpineShadow.copy(
                                    alpha = (if (palette.isLight) 0.40f else 0.88f) * shadowScale,
                                ),
                                palette.bookSpineShadow.copy(
                                    alpha = (if (palette.isLight) 0.14f else 0.35f) * shadowScale,
                                ),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.5f),
                            radius = (size.width * 0.58f).coerceAtLeast(1f),
                        )
                        val pad = 2.dp.toPx()
                        onDrawBehind {
                            drawOval(
                                brush = shadowBrush,
                                topLeft = Offset(-pad, 0f),
                                size = Size(size.width + pad * 2f, size.height),
                            )
                        }
                    },
            )
        }

        // Physical book assembly
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = liftY)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            // Cached right-side layered parchment page-block thickness
            if (thicknessDp > 1.dp) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 1.5.dp, bottom = 1.dp)
                        .drawWithCache {
                            val edgeW = thicknessDp.toPx()
                            val leftX = (size.width - edgeW).coerceAtLeast(0f)
                            val overlap = 2.dp.toPx()
                            val corner = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            val pageBrush = Brush.horizontalGradient(
                                0f to palette.bookPageLine,
                                0.35f to palette.bookPageEdge,
                                0.85f to palette.bookPageEdge,
                                1f to palette.bookPageLine,
                                startX = leftX,
                                endX = size.width,
                            )
                            val lineColor = palette.bookPageLine.copy(alpha = 0.65f)
                            val lineStrokePx = 0.65.dp.toPx()
                            val sheetXs = (1..3).map { i -> leftX + (edgeW * i / 4f) }
                            val padY = 2.dp.toPx()
                            onDrawBehind {
                                drawRoundRect(
                                    brush = pageBrush,
                                    topLeft = Offset(leftX - overlap, 0f),
                                    size = Size(edgeW + overlap, size.height),
                                    cornerRadius = corner,
                                )
                                for (x in sheetXs) {
                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x, padY),
                                        end = Offset(x, size.height - padY),
                                        strokeWidth = lineStrokePx,
                                    )
                                }
                            }
                        },
                )
            }

            // Front cover board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = thicknessDp)
                    .aspectRatio(MangaCover.Book.ratio)
                    .shadow(
                        elevation = (if (isElevated) 12 else 6).dp,
                        shape = bookShape,
                        ambientColor = palette.bookSpineShadow,
                        spotColor = palette.bookSpineShadow,
                    )
                    .clip(bookShape)
                    .background(palette.cardSurface)
                    .then(
                        when {
                            isSelected -> Modifier.border(2.5.dp, primaryColor, bookShape)
                            isFocused -> Modifier.border(2.5.dp, palette.goldAccent, bookShape)
                            isInspected -> Modifier.border(2.5.dp, primaryColor, bookShape)
                            else -> Modifier.border(0.8.dp, Color.White.copy(alpha = 0.12f), bookShape)
                        },
                    ),
            ) {
                AsyncImage(
                    model = coverData,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isSelected) 0.82f else 1f),
                )

                // Cached spine hinge crease & subtle overhead lantern sheen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            val spineW = (if (collectorMode) 10.dp else 7.5.dp).toPx()
                            val spineBrush = Brush.horizontalGradient(
                                0f to Color.Black.copy(alpha = 0.44f),
                                0.34f to Color.White.copy(alpha = 0.14f),
                                0.66f to Color.Black.copy(alpha = 0.18f),
                                1f to Color.Transparent,
                                startX = 0f,
                                endX = spineW,
                            )
                            val topSheenBrush = Brush.verticalGradient(
                                0f to Color.White.copy(alpha = 0.10f * style.lightingIntensity),
                                0.18f to Color.Transparent,
                                startY = 0f,
                                endY = size.height * 0.18f,
                            )
                            onDrawBehind {
                                drawRect(brush = spineBrush, size = Size(spineW, size.height))
                                drawRect(brush = topSheenBrush, size = Size(size.width, size.height * 0.18f))
                            }
                        },
                )

                if (hasBookmarks) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Bookmark,
                        contentDescription = null,
                        tint = palette.goldAccent,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 5.dp, top = 4.dp)
                            .size(16.dp),
                    )
                }

                if (showTitle) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.34f)
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.48f to Color(0x78100D0B),
                                    1f to Color(0xED0E0A08),
                                ),
                            ),
                    )
                    val baseTitleStyle = if (collectorMode) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.labelLarge
                    }
                    Text(
                        text = title,
                        color = YomuColors.WarmIvory,
                        style = baseTitleStyle.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = if (collectorMode) 17.sp else 15.sp,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 1.5f),
                                blurRadius = 4f,
                            ),
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(
                                start = 7.dp,
                                end = if (downloadCount > 0 || onClickContinueReading != null) 26.dp else 7.dp,
                                bottom = if (showProgress && readProgress != null && readProgress > 0f) 9.dp else 6.dp,
                            ),
                    )
                }

                if (downloadCount > 0 && onClickContinueReading == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = 5.dp,
                                bottom = if (showProgress && readProgress != null &&
                                    readProgress > 0f
                                ) {
                                    8.dp
                                } else {
                                    5.dp
                                },
                            )
                            .size(19.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC100D0B))
                            .border(0.8.dp, palette.sageAccent.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Check,
                            contentDescription = null,
                            tint = palette.sageAccent,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }

                if (onClickContinueReading != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = 5.dp,
                                bottom = if (showProgress && readProgress != null &&
                                    readProgress > 0f
                                ) {
                                    8.dp
                                } else {
                                    5.dp
                                },
                            )
                            .size(48.dp)
                            .yomuFocusHighlight(CircleShape)
                            .clickable(role = Role.Button, onClick = onClickContinueReading),
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.92f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
                                contentDescription = stringResource(MR.strings.yomu_home_resume_reading),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                if (showProgress && readProgress != null && readProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color.Black.copy(alpha = 0.55f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(readProgress.coerceIn(0f, 1f))
                                .background(primaryColor),
                        )
                    }
                }
            }

            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-5).dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(primaryColor)
                        .border(1.2.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (unreadCount > 999) "999+" else unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Vertical physical book spine resting flush on a wooden shelf for Book Spine Mode.
 * Uses muted handcrafted cloth/leather binding tones derived deterministically from
 * series metadata with a single clean focus target for controller/keyboard navigation.
 */
@Composable
fun YomuBookSpineVolume(
    mangaId: Long,
    onReadNext: (() -> Unit)?,
    title: String,
    unreadCount: Long,
    downloadCount: Int,
    readProgress: Float?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isInspected: Boolean = false,
) {
    val style = LocalYomuStyle.current
    val palette = style.shelfPalette
    val motionScale = style.effectiveMotionScale
    val primaryColor = MaterialTheme.colorScheme.primary

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    val isElevated = isSelected || isFocused || isInspected
    val liftY by animateDpAsState(
        targetValue = if (isElevated && motionScale > 0f) (-8 * motionScale).dp else 0.dp,
        animationSpec = if (motionScale == 0f) snap() else spring(stiffness = Spring.StiffnessMediumLow),
        label = "spineLift",
    )

    val spineColors = remember {
        listOf(
            Color(0xFF3A2329),
            Color(0xFF222D3D),
            Color(0xFF23362B),
            Color(0xFF3E2D20),
            Color(0xFF342742),
            Color(0xFF2F3438),
            Color(0xFF473322),
        )
    }
    val hash = (mangaId * 31 + title.hashCode()).toInt().ushr(1)
    val spineBase = spineColors[hash % spineColors.size]
    val heightDp = (168 + (hash % 24)).dp
    val shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 1.dp, bottomEnd = 1.dp)
    val unreadLabel = stringResource(MR.strings.yomu_home_unread_remaining, unreadCount)
    val downloadLabel = stringResource(MR.strings.yomu_downloaded_count, downloadCount)
    val inspectedLabel = stringResource(MR.strings.yomu_tablet_inspected_book)
    val accessibilityLabel = buildList {
        add(title)
        if (unreadCount > 0) add(unreadLabel)
        if (downloadCount > 0) add(downloadLabel)
    }.joinToString(", ")

    Box(
        modifier = modifier
            .height(heightDp)
            .offset(y = liftY)
            .bringIntoViewRequester(bringIntoViewRequester)
            .semantics {
                contentDescription = accessibilityLabel
                if (isInspected) stateDescription = inspectedLabel
            }
            .shadow(if (isElevated) 10.dp else 4.dp, shape)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    0f to spineBase.copy(alpha = 0.88f),
                    0.22f to lerpColor(spineBase, Color.White, 0.14f),
                    0.78f to spineBase,
                    1f to Color.Black.copy(alpha = 0.55f),
                ),
            )
            .then(
                when {
                    isSelected -> Modifier.border(2.dp, primaryColor, shape)
                    isFocused -> Modifier.border(2.dp, palette.goldAccent, shape)
                    isInspected -> Modifier.border(2.dp, primaryColor, shape)
                    else -> Modifier.border(0.7.dp, Color.White.copy(alpha = 0.12f), shape)
                },
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .fillMaxWidth()
                .height(1.5.dp)
                .background(palette.goldAccent.copy(alpha = 0.55f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
                .fillMaxWidth()
                .height(1.5.dp)
                .background(palette.goldAccent.copy(alpha = 0.55f)),
        )

        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(
                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 34.dp, bottom = if (onReadNext == null) 22.dp else 53.dp, start = 2.dp, end = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = YomuColors.WarmIvory,
                fontFamily = YomuSerifFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            constraints.copy(
                                minWidth = 0,
                                maxWidth = constraints.maxHeight,
                                minHeight = 0,
                                maxHeight = constraints.maxWidth,
                            ),
                        )
                        layout(placeable.height, placeable.width) {
                            placeable.place(
                                x = (placeable.height - placeable.width) / 2,
                                y = (placeable.width - placeable.height) / 2,
                            )
                        }
                    }
                    .graphicsLayer { rotationZ = -90f },
            )
        }

        if (onReadNext != null) {
            IconButton(
                onClick = onReadNext,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                    .size(40.dp).yomuFocusHighlight(CircleShape),
            ) {
                Icon(
                    imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
                    contentDescription = stringResource(MR.strings.yomu_read_next_title, title),
                    tint = YomuColors.WarmIvory,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (downloadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(palette.sageAccent),
                )
            }
            if (style.showShelfProgress && readProgress != null && readProgress > 0f) {
                LinearProgressIndicator(
                    progress = { readProgress },
                    modifier = Modifier.width(21.dp).height(4.dp),
                    color = primaryColor,
                    trackColor = Color.White.copy(alpha = 0.23f),
                    drawStopIndicator = {},
                )
            }
        }
    }
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = start.alpha + (end.alpha - start.alpha) * f,
    )
}
