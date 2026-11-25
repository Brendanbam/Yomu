package eu.kanade.presentation.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.result.LocalResultEventBus
import eu.kanade.presentation.browse.components.BaseSourceItem
import eu.kanade.presentation.browse.components.SourceIcon
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuShapes
import eu.kanade.presentation.theme.yomu.YomuShortcutBar
import eu.kanade.presentation.theme.yomu.YomuSpacing
import eu.kanade.presentation.theme.yomu.YomuSurfaceCard
import eu.kanade.presentation.theme.yomu.yomuFocusHighlight
import eu.kanade.tachiyomi.ui.browse.BrowseShowExtensionsEvent
import eu.kanade.tachiyomi.ui.browse.source.SourcesViewModel
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceViewModel.Listing
import eu.kanade.tachiyomi.util.system.LocaleHelper
import mihon.core.navigation.GlobalSearchRoute
import mihon.core.navigation.LocalNavigator
import mihon.core.navigation.SourcesFilterRoute
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Keep
import mihon.icons.materialsymbols.rounded.Search
import mihon.icons.materialsymbols.roundedfilled.Extension
import mihon.icons.materialsymbols.roundedfilled.Keep
import tachiyomi.domain.source.model.Pin
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.SECONDARY_ALPHA
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.plus
import tachiyomi.source.local.isLocal

@Composable
fun SourcesScreen(
    state: SourcesViewModel.State,
    contentPadding: PaddingValues,
    onClickItem: (Source, Listing) -> Unit,
    onClickPin: (Source) -> Unit,
    onLongClickItem: (Source) -> Unit,
) {
    val navigator = LocalNavigator.current
    val resultEventBus = LocalResultEventBus.current
    val style = LocalYomuStyle.current

    when {
        state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
        state.isEmpty -> EmptyScreen(
            stringRes = MR.strings.source_empty_screen,
            modifier = Modifier.padding(contentPadding),
        )
        else -> {
            val pinnedSources = remember(state.items) {
                state.items
                    .filterIsInstance<SourceUiModel.Item>()
                    .map { it.source }
                    .filter { Pin.Actual in it.pin || it.isUsedLast }
                    .distinctBy { it.id }
            }

            ScrollbarLazyColumn(
                contentPadding = contentPadding + topSmallPaddingValues + PaddingValues(bottom = 16.dp),
            ) {
                // Refined global search pill bar (matches 01_Primary_Design/04_Browse.png)
                item(key = "yomu_browse_search_bar") {
                    Surface(
                        onClick = { navigator.push(GlobalSearchRoute()) },
                        shape = YomuShapes.pill,
                        color = style.shelfPalette.cardSurface,
                        border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = YomuSpacing.lg, vertical = 8.dp)
                            .heightIn(min = 48.dp)
                            .yomuFocusHighlight(YomuShapes.pill),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Search,
                                contentDescription = stringResource(MR.strings.action_global_search),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = stringResource(MR.strings.yomu_browse_search_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                // Pinned Sources uniform card carousel (matches 04_Browse.png!)
                if (pinnedSources.isNotEmpty()) {
                    item(key = "yomu_pinned_sources_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = YomuSpacing.lg, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(MR.strings.yomu_browse_pinned_sources),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = stringResource(MR.strings.yomu_browse_manage),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable(role = Role.Button) { navigator.push(SourcesFilterRoute) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                            )
                        }
                    }

                    item(key = "yomu_pinned_sources_row") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = YomuSpacing.lg, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(
                                items = pinnedSources,
                                key = { "pinned_card_${it.id}" },
                            ) { source ->
                                PinnedSourceCard(
                                    source = source,
                                    onClick = { onClickItem(source, Listing.Popular) },
                                    onLongClick = { onLongClickItem(source) },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(
                    items = state.items,
                    contentType = {
                        when (it) {
                            is SourceUiModel.Header -> "header"
                            is SourceUiModel.Item -> "item"
                        }
                    },
                    key = {
                        when (it) {
                            is SourceUiModel.Header -> it.hashCode()
                            is SourceUiModel.Item -> "source-${it.source.key()}"
                        }
                    },
                ) { model ->
                    when (model) {
                        is SourceUiModel.Header -> {
                            SourceHeader(
                                modifier = Modifier.animateItem(),
                                language = model.language,
                            )
                        }
                        is SourceUiModel.Item -> SourceItem(
                            modifier = Modifier.animateItem(),
                            source = model.source,
                            onClickItem = onClickItem,
                            onLongClickItem = onLongClickItem,
                            onClickPin = onClickPin,
                        )
                    }
                }

                // Sleek Extensions shortcut bar at bottom of catalog (matches 04_Browse.png)
                item(key = "yomu_browse_extensions_shortcut") {
                    Spacer(modifier = Modifier.height(12.dp))
                    YomuShortcutBar(
                        title = stringResource(MR.strings.label_extensions),
                        icon = MaterialSymbols.RoundedFilled.Extension,
                        onClick = { resultEventBus.sendResult(result = BrowseShowExtensionsEvent) },
                        modifier = Modifier.padding(horizontal = YomuSpacing.lg),
                    )
                }
            }
        }
    }
}

@Composable
private fun PinnedSourceCard(
    source: Source,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val style = LocalYomuStyle.current
    val langLabel = remember(source.lang) {
        if (source.isLocal()) {
            "Local"
        } else {
            LocaleHelper.getSourceDisplayName(source.lang, context)
        }
    }

    Surface(
        shape = YomuShapes.card,
        color = style.shelfPalette.cardSurface,
        border = BorderStroke(1.dp, style.shelfPalette.subtleBorder),
        modifier = Modifier
            .width(114.dp)
            .yomuFocusHighlight(YomuShapes.card)
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            if (Pin.Pinned in source.pin) {
                Icon(
                    imageVector = MaterialSymbols.RoundedFilled.Keep,
                    contentDescription = null,
                    tint = style.shelfPalette.goldAccent,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(14.dp),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SourceIcon(
                    source = source,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = source.name,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = langLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SourceHeader(
    language: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val style = LocalYomuStyle.current
    Text(
        text = LocaleHelper.getSourceDisplayName(language, context),
        modifier = modifier
            .padding(horizontal = YomuSpacing.lg, vertical = YomuSpacing.sm),
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = style.shelfPalette.goldAccent,
    )
}

@Composable
private fun SourceItem(
    source: Source,
    onClickItem: (Source, Listing) -> Unit,
    onLongClickItem: (Source) -> Unit,
    onClickPin: (Source) -> Unit,
    modifier: Modifier = Modifier,
) {
    YomuSurfaceCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = YomuSpacing.lg, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        BaseSourceItem(
            source = source,
            onClickItem = { onClickItem(source, Listing.Popular) },
            onLongClickItem = { onLongClickItem(source) },
            action = {
                if (source.supportsLatest) {
                    TextButton(onClick = { onClickItem(source, Listing.Latest) }) {
                        Text(
                            text = stringResource(MR.strings.latest),
                            style = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
                SourcePinButton(
                    isPinned = Pin.Pinned in source.pin,
                    onClick = { onClickPin(source) },
                )
            },
        )
    }
}

@Composable
private fun SourcePinButton(
    isPinned: Boolean,
    onClick: () -> Unit,
) {
    val style = LocalYomuStyle.current
    val icon = if (isPinned) MaterialSymbols.RoundedFilled.Keep else MaterialSymbols.Rounded.Keep
    val tint = if (isPinned) {
        style.shelfPalette.goldAccent
    } else {
        MaterialTheme.colorScheme.onBackground.copy(
            alpha = SECONDARY_ALPHA,
        )
    }
    val description = if (isPinned) MR.strings.action_unpin else MR.strings.action_pin
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            tint = tint,
            contentDescription = stringResource(description),
        )
    }
}

@Composable
fun SourceOptionsDialog(
    source: Source,
    onClickPin: () -> Unit,
    onClickDisable: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = source.visualName)
        },
        text = {
            Column {
                val textId = if (Pin.Pinned in source.pin) MR.strings.action_unpin else MR.strings.action_pin
                Text(
                    text = stringResource(textId),
                    modifier = Modifier
                        .clickable(onClick = onClickPin)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
                if (!source.isLocal()) {
                    Text(
                        text = stringResource(MR.strings.action_disable),
                        modifier = Modifier
                            .clickable(onClick = onClickDisable)
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {},
    )
}

sealed interface SourceUiModel {
    data class Item(val source: Source) : SourceUiModel
    data class Header(val language: String) : SourceUiModel
}
