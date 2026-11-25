package eu.kanade.presentation.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuShapes
import eu.kanade.presentation.theme.yomu.yomuFocusHighlight
import eu.kanade.presentation.util.formatChapterNumber
import eu.kanade.tachiyomi.util.lang.toTimestampString
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Delete
import mihon.icons.materialsymbols.rounded.Favorite
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val HistoryItemHeight = 96.dp

@Composable
fun HistoryItem(
    history: HistoryWithRelations,
    timeFormatter: DateTimeFormatter,
    onClickCover: () -> Unit,
    onClickResume: () -> Unit,
    onClickDelete: () -> Unit,
    onClickFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalYomuStyle.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .yomuFocusHighlight(YomuShapes.card),
        shape = YomuShapes.card,
        color = style.shelfPalette.cardSurface,
        border = BorderStroke(1.dp, style.shelfPalette.subtleBorder.copy(alpha = 0.8f)),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClickResume)
                .height(HistoryItemHeight)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MangaCover.Book(
                modifier = Modifier.fillMaxHeight(),
                data = history.coverData,
                onClick = onClickCover,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = MaterialTheme.padding.medium, end = MaterialTheme.padding.small),
            ) {
                Text(
                    text = history.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val readAt = remember(timeFormatter, history.readAt) {
                    history.readAt?.toTimestampString(timeFormatter) ?: ""
                }
                Text(
                    text = if (history.chapterNumber > -1) {
                        stringResource(
                            MR.strings.recent_manga_time,
                            formatChapterNumber(history.chapterNumber),
                            readAt,
                        )
                    } else {
                        readAt
                    },
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!history.coverData.isMangaFavorite) {
                IconButton(onClick = onClickFavorite) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Favorite,
                        contentDescription = stringResource(MR.strings.add_to_library),
                        tint = style.shelfPalette.goldAccent,
                    )
                }
            }

            IconButton(onClick = onClickDelete) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Delete,
                    contentDescription = stringResource(MR.strings.action_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                onClick = onClickResume,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
                        contentDescription = stringResource(MR.strings.yomu_home_resume_reading),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HistoryItemPreviews(
    @PreviewParameter(HistoryWithRelationsProvider::class)
    historyWithRelations: HistoryWithRelations,
) {
    TachiyomiPreviewTheme {
        Surface {
            HistoryItem(
                history = historyWithRelations,
                timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT),
                onClickCover = {},
                onClickResume = {},
                onClickDelete = {},
                onClickFavorite = {},
            )
        }
    }
}
