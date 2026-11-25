package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.theme.yomu.YomuPillChip
import eu.kanade.presentation.theme.yomu.YomuSpacing
import tachiyomi.domain.category.model.Category

@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    pagerState: PagerState,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
) {
    val currentPageIndex = pagerState.currentPage.coerceIn(0, (categories.lastIndex).coerceAtLeast(0))
    val listState = rememberLazyListState()

    LaunchedEffect(currentPageIndex) {
        if (categories.isNotEmpty()) {
            listState.animateScrollToItem(currentPageIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = YomuSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(
            items = categories,
            key = { _, category -> "cat_tab_${category.id}" },
        ) { index, category ->
            YomuPillChip(
                label = category.visualName,
                count = getItemCountForCategory(category),
                selected = currentPageIndex == index,
                onClick = { onTabItemClick(index) },
            )
        }
    }
}
