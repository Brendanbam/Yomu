package tachiyomi.presentation.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
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
import tachiyomi.presentation.widget.components.UpdatesMangaCover
import tachiyomi.presentation.widget.util.appWidgetBackgroundRadius
import tachiyomi.presentation.widget.util.calculateRowAndColumnCount
import tachiyomi.presentation.widget.util.loadWidgetCover

/** The library as a grid of covers, most recently read first. Tapping a cover resumes that book. */
@HasMemberInjections
class ReadingGlanceWidget : GlanceAppWidget() {

    @Inject private lateinit var getLibraryManga: GetLibraryManga

    @Inject private lateinit var securityPreferences: SecurityPreferences

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        context.metroGraph<PresentationWidgetGraph>().inject(this)
        val lockFlow = securityPreferences.useAuthenticator.changes()
        val layout = GlanceModifier.fillMaxSize()
            .background(Background)
            .appWidgetBackground()
            .appWidgetBackgroundRadius()

        val largestSize = GlanceAppWidgetManager(context).getAppWidgetSizes(id)
            .maxByOrNull { it.height.value * it.width.value }
            ?: DpSize.Zero
        val (rowCount, columnCount) = largestSize.calculateRowAndColumnCount(HeaderHeight, BottomPadding)

        provideContent {
            val locked by lockFlow.collectAsState(securityPreferences.useAuthenticator.get())
            if (locked) {
                LockedWidget(foreground = Foreground, modifier = layout)
                return@provideContent
            }

            val booksFlow = remember {
                getLibraryManga.subscribe().map { it.prepareData(context, rowCount * columnCount) }
            }
            val books by booksFlow.collectAsState(initial = null)
            LibraryWidget(books = books, modifier = layout)
        }
    }

    private suspend fun List<LibraryManga>.prepareData(context: Context, count: Int): List<Book> {
        return withContext(Dispatchers.Default) {
            sortedWith(LastReadOrder)
                .take(count)
                .map { Book(it.id, it.manga.title, context.loadWidgetCover(it.manga.asMangaCover())) }
        }
    }

    private data class Book(val mangaId: Long, val title: String, val cover: Bitmap?)

    @Composable
    private fun LibraryWidget(books: List<Book>?, modifier: GlanceModifier) {
        val context = LocalContext.current
        val libraryIntent = Intent(context, Class.forName(Constants.MAIN_ACTIVITY)).apply {
            action = Constants.SHORTCUT_LIBRARY
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        Column(modifier = modifier.padding(bottom = BottomPadding)) {
            Text(
                text = context.stringResource(MR.strings.label_library),
                style = TextStyle(color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(HeaderHeight)
                    .padding(start = 14.dp, top = 10.dp)
                    .clickable(actionStartActivity(libraryIntent)),
            )
            Box(
                modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(libraryIntent)),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    books == null -> CircularProgressIndicator(color = Foreground)
                    books.isEmpty() -> Text(
                        text = context.stringResource(MR.strings.yomu_widget_empty),
                        style = TextStyle(color = Foreground),
                    )
                    else -> CoverGrid(books)
                }
            }
        }
    }

    @Composable
    private fun CoverGrid(books: List<Book>) {
        val context = LocalContext.current
        val (rowCount, columnCount) = LocalSize.current.calculateRowAndColumnCount(HeaderHeight, BottomPadding)
        Column(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            books.take(rowCount * columnCount).chunked(columnCount).forEach { row ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    row.forEach { book ->
                        val intent = Intent(context, Class.forName(Constants.MAIN_ACTIVITY)).apply {
                            action = Constants.SHORTCUT_RESUME_READING
                            putExtra(Constants.MANGA_EXTRA, book.mangaId)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            // https://issuetracker.google.com/issues/238793260
                            addCategory(book.mangaId.toString())
                        }
                        Box(modifier = GlanceModifier.padding(horizontal = 3.dp)) {
                            UpdatesMangaCover(
                                cover = book.cover,
                                contentDescription = book.title,
                                modifier = GlanceModifier.clickable(actionStartActivity(intent)),
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val Foreground = ColorProvider(Color(0xFFF1E4D4))
        private val Background = ColorProvider(Color(0xFF221713))
        private val Accent = ColorProvider(Color(0xFFDBB884))
        private val HeaderHeight = 32.dp
        private val BottomPadding = 6.dp

        internal val LastReadOrder: Comparator<LibraryManga> =
            compareByDescending<LibraryManga> { it.lastRead }.thenBy { it.manga.title.lowercase() }
    }
}
