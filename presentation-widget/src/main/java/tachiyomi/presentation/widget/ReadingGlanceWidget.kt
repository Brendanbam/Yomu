package tachiyomi.presentation.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import mihon.core.metro.metroGraph
import mihon.presentation.widget.di.PresentationWidgetGraph
import tachiyomi.core.common.Constants
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.model.asMangaCover
import tachiyomi.i18n.MR
import tachiyomi.presentation.widget.components.LockedWidget
import tachiyomi.presentation.widget.util.WidgetBookshelf
import tachiyomi.presentation.widget.util.appWidgetBackgroundRadius
import tachiyomi.presentation.widget.util.loadCover
import kotlin.math.roundToInt

/**
 * The library as a bookshelf like the app's, most recently read first. Tapping a book resumes it.
 */
@HasMemberInjections
class ReadingGlanceWidget : GlanceAppWidget() {

    @Inject private lateinit var getLibraryManga: GetLibraryManga

    @Inject private lateinit var securityPreferences: SecurityPreferences

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        context.metroGraph<PresentationWidgetGraph>().inject(this)
        val lockFlow = securityPreferences.useAuthenticator.changes()
        val layout = GlanceModifier.fillMaxSize()
            .background(Color(WidgetBookshelf.ALCOVE_BACK_BOTTOM))
            .appWidgetBackground()
            .appWidgetBackgroundRadius()

        val sizes = GlanceAppWidgetManager(context).getAppWidgetSizes(id)
        val largestSize = sizes.maxByOrNull { it.height.value * it.width.value } ?: DpSize.Zero
        val bookCount = sizes.maxOfOrNull { it.shelfLayout().let { shelf -> shelf.rows * shelf.columns } } ?: 1

        provideContent {
            val locked by lockFlow.collectAsState(securityPreferences.useAuthenticator.get())
            if (locked) {
                LockedWidget(foreground = Foreground, modifier = layout)
                return@provideContent
            }

            val shelfFlow = remember {
                getLibraryManga.subscribe().map { it.prepareShelf(context, largestSize, bookCount) }
            }
            val shelf by shelfFlow.collectAsState(initial = null)
            LibraryWidget(shelf = shelf, modifier = layout)
        }
    }

    private suspend fun List<LibraryManga>.prepareShelf(context: Context, size: DpSize, count: Int): Shelf {
        // Bitmaps count against the widget's memory limit, so they aren't drawn at full density
        val scale = context.resources.displayMetrics.density.coerceAtMost(2f)
        val shelfLayout = size.shelfLayout()
        val widthPx = (size.width.value * scale).roundToInt().coerceAtLeast(1)
        val bookPx = (shelfLayout.bookWidth.value * scale).roundToInt().coerceAtLeast(8)
        val (coverWidth, coverHeight) = WidgetBookshelf.coverSize(bookPx)
        return withContext(Dispatchers.Default) {
            Shelf(
                // The wall is soft gradients and is stretched anyway, so it's drawn at 1x to save memory
                wall = WidgetBookshelf.wall(
                    size.width.value.roundToInt().coerceAtLeast(1),
                    shelfLayout.tierHeight.value.roundToInt(),
                    density = 1f,
                ),
                plank = WidgetBookshelf.plank(widthPx, (PlankHeight.value * scale).roundToInt(), scale),
                books = sortedWith(LastReadOrder)
                    .take(count)
                    .map {
                        val cover = context.loadCover(it.manga.asMangaCover(), coverWidth, coverHeight)
                        Book(it.id, it.manga.title, WidgetBookshelf.book(cover, bookPx, scale))
                    },
            )
        }
    }

    private class Shelf(val wall: Bitmap, val plank: Bitmap, val books: List<Book>)

    private class Book(val mangaId: Long, val title: String, val image: Bitmap)

    private class ShelfLayout(val columns: Int, val rows: Int, val bookWidth: Dp) {
        val bookHeight = bookWidth * WidgetBookshelf.BOOK_ASPECT
        val tierHeight = TierTopGap + bookHeight + PlankHeight
    }

    private fun DpSize.shelfLayout(): ShelfLayout {
        val inner = width - SidePadding * 2
        fun widthFor(columns: Int) = (inner - BookGap * (columns - 1)) / columns
        val maxColumns = ((inner + BookGap) / (MinBookWidth + BookGap)).toInt().coerceIn(1, 6)
        val fitsHeight = (height - HeaderHeight - TierTopGap - PlankHeight) / WidgetBookshelf.BOOK_ASPECT
        // Spare height goes to fewer, bigger books, keeping at least three on a shelf
        val columns = (minOf(MIN_COLUMNS, maxColumns)..maxColumns)
            .firstOrNull { widthFor(it) <= minOf(fitsHeight, MaxBookWidth) }
            ?: maxColumns
        val bookWidth = minOf(widthFor(columns), fitsHeight, MaxBookWidth).coerceAtLeast(24.dp)
        val shelf = ShelfLayout(columns, rows = 1, bookWidth)
        val rows = ((height - HeaderHeight) / shelf.tierHeight).toInt().coerceIn(1, 4)
        return ShelfLayout(columns, rows, bookWidth)
    }

    @Composable
    private fun LibraryWidget(shelf: Shelf?, modifier: GlanceModifier) {
        val context = LocalContext.current
        val libraryIntent = Intent(context, Class.forName(Constants.MAIN_ACTIVITY)).apply {
            action = Constants.SHORTCUT_LIBRARY
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        Column(modifier = modifier.clickable(actionStartActivity(libraryIntent))) {
            Text(
                text = context.stringResource(MR.strings.label_library),
                style = TextStyle(color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(HeaderHeight)
                    .padding(start = 16.dp, top = 8.dp),
            )
            when {
                shelf == null -> Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Foreground)
                }
                shelf.books.isEmpty() -> Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = context.stringResource(MR.strings.yomu_widget_empty),
                        style = TextStyle(color = Foreground),
                    )
                }
                else -> Bookshelf(shelf)
            }
        }
    }

    @Composable
    private fun Bookshelf(shelf: Shelf) {
        val shelfLayout = LocalSize.current.shelfLayout()
        val wall = ImageProvider(shelf.wall)
        val plank = ImageProvider(shelf.plank)
        val rows = shelf.books.take(shelfLayout.rows * shelfLayout.columns).chunked(shelfLayout.columns)
        Column(modifier = GlanceModifier.fillMaxSize()) {
            repeat(shelfLayout.rows) { index ->
                Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight().background(wall)) {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .padding(horizontal = SidePadding - BookGap / 2),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        // Gaps are padding, not spacers: Glance allows only 10 children in a row
                        rows.getOrNull(index)?.forEach { ShelfBook(it, shelfLayout) }
                    }
                    Image(
                        provider = plank,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = GlanceModifier.fillMaxWidth().height(PlankHeight),
                    )
                }
            }
        }
    }

    @Composable
    private fun ShelfBook(book: Book, shelfLayout: ShelfLayout) {
        val context = LocalContext.current
        val intent = Intent(context, Class.forName(Constants.MAIN_ACTIVITY)).apply {
            action = Constants.SHORTCUT_RESUME_READING
            putExtra(Constants.MANGA_EXTRA, book.mangaId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // https://issuetracker.google.com/issues/238793260
            addCategory(book.mangaId.toString())
        }
        Image(
            provider = ImageProvider(book.image),
            contentDescription = book.title,
            contentScale = ContentScale.Fit,
            modifier = GlanceModifier
                .size(shelfLayout.bookWidth + BookGap, shelfLayout.bookHeight)
                .padding(horizontal = BookGap / 2)
                .clickable(actionStartActivity(intent)),
        )
    }

    companion object {
        private val Foreground = ColorProvider(Color(0xFFF1E4D4))
        private val Accent = ColorProvider(Color(0xFFDBB884))
        private val HeaderHeight = 30.dp
        private val SidePadding = 14.dp
        private val BookGap = 10.dp
        private val MinBookWidth = 72.dp
        private val MaxBookWidth = 140.dp
        private const val MIN_COLUMNS = 3
        private val TierTopGap = 10.dp
        private val PlankHeight = 28.dp

        internal val LastReadOrder: Comparator<LibraryManga> =
            compareByDescending<LibraryManga> { it.lastRead }.thenBy { it.manga.title.lowercase() }
    }
}
