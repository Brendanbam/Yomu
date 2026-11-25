package tachiyomi.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import mihon.core.metro.metroGraph
import mihon.presentation.widget.di.PresentationWidgetGraph
import tachiyomi.core.common.Constants
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.history.interactor.GetHistory
import tachiyomi.i18n.MR
import tachiyomi.presentation.widget.components.LockedWidget
import tachiyomi.presentation.widget.util.appWidgetBackgroundRadius

@HasMemberInjections
class ReadingGlanceWidget : GlanceAppWidget() {

    @Inject private lateinit var getHistory: GetHistory

    @Inject private lateinit var securityPreferences: SecurityPreferences

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        context.metroGraph<PresentationWidgetGraph>().inject(this)
        val historyFlow = getHistory.subscribe("")
        val lockFlow = securityPreferences.useAuthenticator.changes()
        val foreground = ColorProvider(Color(0xFFF1E4D4))
        val background = ColorProvider(Color(0xFF221713))
        val layout = GlanceModifier.fillMaxSize().background(
            background,
        ).appWidgetBackground().appWidgetBackgroundRadius()
        val intent = Intent(context, Class.forName(Constants.MAIN_ACTIVITY)).apply {
            action = Constants.SHORTCUT_RESUME_READING
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        provideContent {
            val locked by lockFlow.collectAsState(securityPreferences.useAuthenticator.get())
            if (locked) {
                LockedWidget(foreground = foreground, modifier = layout)
            } else {
                val history by historyFlow.collectAsState(emptyList())
                Column(
                    modifier = layout.clickable(actionStartActivity(intent)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        context.stringResource(MR.strings.yomu_home_continue_reading),
                        style = TextStyle(color = ColorProvider(Color(0xFFDBB884)), fontSize = 13.sp),
                        maxLines = 1,
                    )
                    Text(
                        history.firstOrNull()?.title ?: context.stringResource(MR.strings.yomu_widget_empty),
                        style = TextStyle(color = foreground, fontSize = 17.sp, fontWeight = FontWeight.Medium),
                        maxLines = 2,
                    )
                }
            }
        }
    }
}
