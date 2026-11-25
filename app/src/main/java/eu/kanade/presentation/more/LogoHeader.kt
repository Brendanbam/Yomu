package eu.kanade.presentation.more

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.theme.yomu.LocalYomuStyle
import eu.kanade.presentation.theme.yomu.YomuSerifFontFamily
import eu.kanade.tachiyomi.R
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

private val DiamondShape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(size.width / 2f, size.height)
    lineTo(0f, size.height / 2f)
    close()
}

@Composable
fun LogoHeader(
    iconPadding: PaddingValues = PaddingValues(),
) {
    val style = LocalYomuStyle.current
    val gold = style.shelfPalette.goldAccent

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            gold.copy(alpha = 0.11f * style.lightingIntensity),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(iconPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_yomu),
                contentDescription = stringResource(MR.strings.app_name),
                modifier = Modifier.size(68.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "YOMU",
                fontFamily = YomuSerifFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                letterSpacing = 5.sp,
                color = gold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Color.Transparent, gold.copy(alpha = 0.7f))),
                        ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(DiamondShape)
                        .background(gold),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(listOf(gold.copy(alpha = 0.7f), Color.Transparent)),
                        ),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(MR.strings.yomu_tagline).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.4.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }

        HorizontalDivider(color = style.shelfPalette.subtleBorder.copy(alpha = 0.65f))
    }
}
