package eu.kanade.presentation.theme.yomu

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import eu.kanade.presentation.manga.components.MangaCover
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Bookmark
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import kotlin.math.abs
import kotlin.math.sin
import tachiyomi.domain.manga.model.MangaCover as MangaCoverModel

/**
 * Draws a single realistic wooden shelf plank with angled lit top ledge,
 * specular front bevel, procedural walnut/oak/ebony/cherry wood grain fibers,
 * and a soft deep shadow cast onto the alcove beneath it.
 */
@Composable
fun YomuWoodShelfPlank(
    rowIndex: Int,
    modifier: Modifier = Modifier,
    topLedgeHeight: Dp = 10.dp,
    frontFaceHeight: Dp = 15.dp,
    underShadowHeight: Dp = 12.dp,
) {
    val style = LocalYomuStyle.current
    val palette = style.shelfPalette
    val lighting = style.lightingIntensity
    val decoration = style.decorationIntensity
    val shadowScale = style.shadowIntensity

    val totalHeight = topLedgeHeight + frontFaceHeight + underShadowHeight

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight),
    ) {
        val w = size.width
        val topH = topLedgeHeight.toPx()
        val faceH = frontFaceHeight.toPx()
        val shadowH = underShadowHeight.toPx()
        val faceTop = topH
        val faceBottom = topH + faceH

        // 1. Angled upper shelf surface catching warm lantern spotlight in the center
        drawRect(
            brush = Brush.horizontalGradient(
                0f to palette.shelfTopBase,
                0.28f to lerpColor(palette.shelfTopBase, palette.shelfTopLit, 0.55f + 0.35f * lighting),
                0.5f to lerpColor(palette.shelfTopBase, palette.shelfTopLit, 0.75f + 0.25f * lighting),
                0.72f to lerpColor(palette.shelfTopBase, palette.shelfTopLit, 0.55f + 0.35f * lighting),
                1f to palette.shelfTopBase,
            ),
            topLeft = Offset(0f, 0f),
            size = Size(w, topH),
        )

        // Subtle rear alcove ambient occlusion along the back edge of the top ledge
        drawRect(
            brush = Brush.verticalGradient(
                0f to palette.shelfUnderShadow.copy(alpha = 0.55f * shadowScale),
                1f to Color.Transparent,
                startY = 0f,
                endY = topH * 0.7f,
            ),
            topLeft = Offset(0f, 0f),
            size = Size(w, topH * 0.7f),
        )

        // Subtle horizontal grain lines on top ledge
        if (decoration > 0.15f) {
            val topLines = 3
            for (i in 0 until topLines) {
                val y = topH * (0.25f + i * 0.28f)
                drawLine(
                    color = palette.shelfGrainDark.copy(alpha = 0.22f * decoration),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }

        // 2. Front fascia wooden plank
        drawRect(
            brush = Brush.verticalGradient(
                0f to palette.shelfFrontTop,
                0.65f to lerpColor(palette.shelfFrontTop, palette.shelfFrontBottom, 0.55f),
                1f to palette.shelfFrontBottom,
                startY = faceTop,
                endY = faceBottom,
            ),
            topLeft = Offset(0f, faceTop),
            size = Size(w, faceH),
        )

        // Center lantern warmth across front edge
        if (lighting > 0.05f) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.lanternGlow.copy(alpha = 0.14f * lighting),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.5f, faceTop),
                    radius = w * 0.58f,
                ),
                topLeft = Offset(0f, faceTop),
                size = Size(w, faceH),
            )
        }

        // 3. Deterministic organic wood grain streaks & subtle knot curves
        if (decoration > 0.1f) {
            val seed = (rowIndex * 7919 + 104729).toFloat()
            val fiberCount = (5 + (decoration * 5).toInt()).coerceAtMost(10)
            val segments = 14
            for (i in 0 until fiberCount) {
                val path = Path()
                val baseY = faceTop + faceH * ((i + 0.6f) / (fiberCount + 0.3f))
                val amp = (1.1f + ((i + rowIndex) % 3) * 0.55f).dp.toPx() * 0.45f
                val freq = 0.008f + ((i * 3 + rowIndex) % 5) * 0.0025f
                val phase = seed * 0.013f + i * 1.9f
                for (s in 0..segments) {
                    val x = w * (s.toFloat() / segments)
                    val y = (baseY + sin(x * freq + phase) * amp)
                        .coerceIn(faceTop + 1f, faceBottom - 1f)
                    if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                val isLightFiber = (i + rowIndex) % 3 == 0
                drawPath(
                    path = path,
                    color = if (isLightFiber) {
                        palette.shelfGrainLight.copy(alpha = 0.24f * decoration)
                    } else {
                        palette.shelfGrainDark.copy(alpha = 0.34f * decoration)
                    },
                    style = Stroke(width = if (isLightFiber) 0.9.dp.toPx() else 1.25.dp.toPx()),
                )
            }

            // Subtle wood knot ring placed deterministically off-center
            val knotX = w * (0.18f + ((abs(rowIndex * 37 + 13) % 64) / 100f))
            val knotY = faceTop + faceH * (0.38f + ((rowIndex % 3) * 0.14f))
            drawOval(
                color = palette.shelfGrainDark.copy(alpha = 0.22f * decoration),
                topLeft = Offset(knotX - 10.dp.toPx(), knotY - 2.4.dp.toPx()),
                size = Size(20.dp.toPx(), 4.8.dp.toPx()),
                style = Stroke(width = 1.dp.toPx()),
            )
        }

        // 4. Crisp upper front bevel lip catching overhead light
        drawLine(
            brush = Brush.horizontalGradient(
                0f to palette.shelfBevelHighlight.copy(alpha = 0.25f),
                0.5f to palette.shelfBevelHighlight.copy(alpha = 0.55f + 0.35f * lighting),
                1f to palette.shelfBevelHighlight.copy(alpha = 0.25f),
            ),
            start = Offset(0f, faceTop + 0.6.dp.toPx()),
            end = Offset(w, faceTop + 0.6.dp.toPx()),
            strokeWidth = 1.3.dp.toPx(),
        )

        // Subtle dark lower lip of front fascia
        drawLine(
            color = palette.shelfUnderShadow.copy(alpha = 0.72f),
            start = Offset(0f, faceBottom - 0.5.dp.toPx()),
            end = Offset(w, faceBottom - 0.5.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
        )

        // 5. Cast drop shadow below the shelf onto the alcove underneath
        if (shadowScale > 0.05f) {
            drawRect(
                brush = Brush.verticalGradient(
                    0f to palette.shelfUnderShadow.copy(alpha = (if (palette.isLight) 0.32f else 0.78f) * shadowScale),
                    0.45f to
                        palette.shelfUnderShadow.copy(alpha = (if (palette.isLight) 0.14f else 0.36f) * shadowScale),
                    1f to Color.Transparent,
                    startY = faceBottom,
                    endY = faceBottom + shadowH,
                ),
                topLeft = Offset(0f, faceBottom),
                size = Size(w, shadowH),
            )
        }
    }
}

/**
 * A full bookcase shelf tier: recessed dark wood alcove with overhead warm lantern
 * spotlight, bookcase side stiles, a horizontal row of physical manga volumes resting
 * flush on the upper ledge, and the solid wood shelf plank underneath.
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
            .drawBehind {
                // Recessed wooden back panel
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to palette.alcoveBackTop,
                        1f to palette.alcoveBackBottom,
                    ),
                )
                // Subtle vertical tongue-and-groove bookcase back paneling
                if (decoration > 0.15f) {
                    val panelCount = 6
                    val step = size.width / panelCount
                    for (i in 1 until panelCount) {
                        val x = step * i
                        drawLine(
                            color = palette.alcovePanelLine.copy(alpha = 0.45f * decoration),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                }
                // Warm overhead Japanese lantern glow illuminating the alcove & books
                if (lighting > 0.05f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palette.lanternGlow.copy(alpha = (if (palette.isLight) 0.25f else 0.17f) * lighting),
                                palette.lanternGlow.copy(alpha = (if (palette.isLight) 0.08f else 0.05f) * lighting),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.5f, 0f),
                            radius = size.width * 0.62f,
                        ),
                    )
                }
                // Side walnut bookcase frame stiles
                val stileW = 5.dp.toPx()
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to palette.shelfFrontBottom,
                        1f to palette.shelfUnderShadow.copy(alpha = 0.8f),
                        startX = 0f,
                        endX = stileW,
                    ),
                    topLeft = Offset.Zero,
                    size = Size(stileW, size.height),
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to palette.shelfUnderShadow.copy(alpha = 0.8f),
                        1f to palette.shelfFrontBottom,
                        startX = size.width - stileW,
                        endX = size.width,
                    ),
                    topLeft = Offset(size.width - stileW, 0f),
                    size = Size(stileW, size.height),
                )
            },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Books rest directly onto the shelf plank top ledge (zero floating gap)
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
            // Pull shelf plank slightly underneath the books' contact shadow so volumes sit ON the wood
            YomuWoodShelfPlank(
                rowIndex = rowIndex,
                modifier = Modifier.offset(y = (-4).dp),
            )
        }
    }
}

/**
 * Physical manga volume styled to match the Primary Design bookshelf target:
 * sharp cover art, tactile side page-block thickness, spine fold highlight,
 * grounded contact shadow, amethyst unread pill, sage downloaded icon,
 * optional title scrim & reading progress bar, and smooth lift on focus/selection.
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
) {
    val style = LocalYomuStyle.current
    val palette = style.shelfPalette
    val motionScale = style.effectiveMotionScale
    val thicknessDp = ((if (collectorMode) 7f else 5f) * (0.4f + 0.6f * style.bookThickness)).dp
    val shadowScale = style.shadowIntensity

    var isFocused by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val isElevated = isSelected || isFocused
    val liftY by animateDpAsState(
        targetValue = if (isElevated && motionScale > 0f) (-6 * motionScale).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "bookLift",
    )
    val scale by animateFloatAsState(
        targetValue = if (isElevated && motionScale > 0f) 1f + 0.025f * motionScale else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bookScale",
    )

    val bookShape = YomuShapes.bookCover
    val primaryColor = MaterialTheme.colorScheme.primary
    val accessibilityLabel = buildString {
        append(title)
        if (unreadCount > 0) append(", $unreadCount unread")
        if (downloadCount > 0) append(", $downloadCount downloaded")
    }

    Box(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    scope.launch { bringIntoViewRequester.bringIntoView() }
                }
            }
            .focusable()
            .semantics { contentDescription = accessibilityLabel }
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        // Grounded contact shadow directly beneath the book resting on the wooden shelf
        if (shadowScale > 0.05f) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(10.dp)
                    .offset(y = 3.dp),
            ) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.bookSpineShadow.copy(alpha = (if (palette.isLight) 0.42f else 0.85f) * shadowScale),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.width * 0.56f,
                    ),
                    topLeft = Offset(-2.dp.toPx(), 0f),
                    size = Size(size.width + 4.dp.toPx(), size.height),
                )
            }
        }

        // Physical book assembly (lifts smoothly on selection/controller focus)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = liftY)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            // Right-side layered parchment page-block thickness (real physical volume feel!)
            if (thicknessDp > 1.dp) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 2.dp, bottom = 1.dp),
                ) {
                    val edgeW = thicknessDp.toPx()
                    val leftX = size.width - edgeW
                    drawRoundRect(
                        color = palette.bookPageEdge,
                        topLeft = Offset(leftX - 2.dp.toPx(), 0f),
                        size = Size(edgeW + 2.dp.toPx(), size.height),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    )
                    // Subtle vertical page sheet lines
                    val sheets = 3
                    for (i in 1..sheets) {
                        val x = leftX + (edgeW * i / (sheets + 1))
                        drawLine(
                            color = palette.bookPageLine.copy(alpha = 0.7f),
                            start = Offset(x, 2.dp.toPx()),
                            end = Offset(x, size.height - 2.dp.toPx()),
                            strokeWidth = 0.7.dp.toPx(),
                        )
                    }
                }
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

                // Tactile book spine fold shading & specular crease on the left edge
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val spineW = (if (collectorMode) 11.dp else 8.dp).toPx()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to Color.Black.copy(alpha = 0.42f),
                            0.35f to Color.White.copy(alpha = 0.14f),
                            0.65f to Color.Black.copy(alpha = 0.18f),
                            1f to Color.Transparent,
                            startX = 0f,
                            endX = spineW,
                        ),
                        size = Size(spineW, size.height),
                    )
                    // Subtle warm overhead top sheen
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.White.copy(alpha = 0.10f * style.lightingIntensity),
                            0.18f to Color.Transparent,
                        ),
                        size = Size(size.width, size.height * 0.18f),
                    )
                }

                // Gold bookmark ribbon accent in top-left if bookmarked
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

                // Bottom title overlay scrim (matches primary design bookshelf!)
                if (showTitle) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.46f)
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.45f to Color(0x99100D0B),
                                    1f to Color(0xEB0E0A08),
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

                // Downloaded sage green indicator (bottom-right)
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

                // Quick resume play button if enabled in library settings
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
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.92f))
                            .combinedClickable(onClick = onClickContinueReading),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                // Reading progress bar flush at the bottom of the cover
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
                                .fillMaxWidth(readProgress.coerceIn(0.05f, 1f))
                                .background(primaryColor),
                        )
                    }
                }
            }

            // Unread amethyst pill badge overlapping the top-right corner of the book (matches 02_Library.png!)
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
 * Uses muted, handcrafted cloth/leather binding tones derived deterministically from
 * series metadata (never falsely claiming generated spines are publisher art).
 */
@Composable
fun YomuBookSpineVolume(
    mangaId: Long,
    title: String,
    unreadCount: Long,
    downloadCount: Int,
    readProgress: Float?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalYomuStyle.current
    val palette = style.shelfPalette
    val motionScale = style.effectiveMotionScale
    val primaryColor = MaterialTheme.colorScheme.primary

    var isFocused by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val isElevated = isSelected || isFocused
    val liftY by animateDpAsState(
        targetValue = if (isElevated && motionScale > 0f) (-8 * motionScale).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spineLift",
    )

    val spineColors = remember {
        listOf(
            Color(0xFF3A2329), // Deep burgundy cloth
            Color(0xFF222D3D), // Midnight indigo linen
            Color(0xFF23362B), // Forest pine binding
            Color(0xFF3E2D20), // Aged saddle walnut
            Color(0xFF342742), // Royal plum cloth
            Color(0xFF2F3438), // Charcoal slate
            Color(0xFF473322), // Burnished bronze
        )
    }
    val hash = abs((mangaId * 31 + title.hashCode()).toInt())
    val spineBase = spineColors[hash % spineColors.size]
    // Vary book heights slightly like real collected manga volumes on a personal shelf
    val heightDp = (152 + (hash % 24)).dp
    val shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp)

    Box(
        modifier = modifier
            .height(heightDp)
            .offset(y = liftY)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() }
            }
            .focusable()
            .shadow(if (isElevated) 10.dp else 4.dp, shape)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    0f to spineBase.copy(alpha = 0.85f),
                    0.22f to lerpColor(spineBase, Color.White, 0.14f),
                    0.78f to spineBase,
                    1f to Color.Black.copy(alpha = 0.55f),
                ),
            )
            .then(
                when {
                    isSelected -> Modifier.border(2.dp, primaryColor, shape)
                    isFocused -> Modifier.border(2.dp, palette.goldAccent, shape)
                    else -> Modifier.border(0.7.dp, Color.White.copy(alpha = 0.12f), shape)
                },
            )
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        // Antique gold embossed headband trims near top & bottom of the spine
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

        // Unread amethyst badge at top of spine
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

        // Rotated title along the spine
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 34.dp, bottom = 22.dp, start = 2.dp, end = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = YomuColors.WarmIvory,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer { rotationZ = -90f }
                    .width(heightDp - 58.dp),
            )
        }

        // Downloaded sage indicator or progress tick near bottom
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
            if (readProgress != null && readProgress > 0f) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(primaryColor),
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
