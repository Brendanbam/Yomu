package eu.kanade.presentation.theme.yomu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.KeyboardArrowRight

/**
 * Controller & keyboard friendly focus indicator with automatic scroll-into-view.
 */
fun Modifier.yomuFocusHighlight(
    shape: Shape = YomuShapes.card,
): Modifier = composed {
    val style = LocalYomuStyle.current
    var isFocused by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    this
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusChanged { state ->
            isFocused = state.isFocused
            if (state.isFocused) {
                scope.launch { bringIntoViewRequester.bringIntoView() }
            }
        }
        .then(
            if (isFocused) {
                Modifier.border(2.dp, style.shelfPalette.goldAccent, shape)
            } else {
                Modifier
            },
        )
        .focusable()
}

/**
 * Category / filter pill chip styled like the Primary Design Library & Browse chips:
 * solid amethyst when selected, dark wood with subtle border when unselected.
 */
@Composable
fun YomuPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
    leadingIcon: ImageVector? = null,
) {
    val style = LocalYomuStyle.current
    val text = if (count != null) "$label ($count)" else label
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        style.shelfPalette.cardSurface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        style.shelfPalette.subtleBorder
    }

    Surface(
        modifier = modifier
            .heightIn(min = 38.dp)
            .yomuFocusHighlight(YomuShapes.pill),
        shape = YomuShapes.pill,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Section heading with optional "View All >" action (matches Home & Browse references).
 */
@Composable
fun YomuSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    accentDot: Boolean = false,
) {
    val style = LocalYomuStyle.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = YomuSpacing.lg, vertical = YomuSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            if (accentDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(style.shelfPalette.goldAccent),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (actionLabel != null && onActionClick != null) {
            Row(
                modifier = Modifier
                    .clip(YomuShapes.pill)
                    .clickable(role = Role.Button, onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = MaterialSymbols.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/**
 * Primary amethyst action CTA button (used for Resume Reading / Start Reading).
 */
@Composable
fun YomuPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = YomuShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .yomuFocusHighlight(YomuShapes.button),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Elevated dark-wood rounded-square icon button (used beside Resume Reading on Details).
 */
@Composable
fun YomuSquareActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    val style = LocalYomuStyle.current
    Surface(
        onClick = onClick,
        shape = YomuShapes.button,
        color = if (active) MaterialTheme.colorScheme.primaryContainer else style.shelfPalette.cardSurface,
        contentColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (active) MaterialTheme.colorScheme.primary else style.shelfPalette.subtleBorder,
        ),
        modifier = modifier
            .size(48.dp)
            .yomuFocusHighlight(YomuShapes.button),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Clean dark-wood card container with subtle border (used for Continue Reading,
 * Quick Access shortcuts, Source cards, Chapter rows, and Settings groups).
 */
@Composable
fun YomuSurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = YomuShapes.card,
    elevated: Boolean = false,
    content: @Composable () -> Unit,
) {
    val style = LocalYomuStyle.current
    val container = if (elevated) style.shelfPalette.elevatedSurface else style.shelfPalette.cardSurface
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = container,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
            modifier = modifier.yomuFocusHighlight(shape),
            content = content,
        )
    } else {
        Surface(
            shape = shape,
            color = container,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
            modifier = modifier,
            content = content,
        )
    }
}

/**
 * Full-width quick-shortcut bar (matches "History >" and "Extensions >" in Primary Design).
 */
@Composable
fun YomuShortcutBar(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badgeText: String? = null,
) {
    val style = LocalYomuStyle.current
    YomuSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = YomuShapes.card,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(horizontal = YomuSpacing.lg, vertical = YomuSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = style.shelfPalette.goldAccent,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (!badgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(YomuShapes.pill)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = MaterialSymbols.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
