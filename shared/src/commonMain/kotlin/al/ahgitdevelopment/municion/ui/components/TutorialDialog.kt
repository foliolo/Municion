package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.tutorial_01
import al.ahgitdevelopment.municion.resources.tutorial_02
import al.ahgitdevelopment.municion.resources.tutorial_03
import al.ahgitdevelopment.municion.resources.tutorial_04
import al.ahgitdevelopment.municion.resources.tutorial_05
import al.ahgitdevelopment.municion.resources.tutorial_06
import al.ahgitdevelopment.municion.resources.tutorial_07
import al.ahgitdevelopment.municion.resources.tutorial_08
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.painterResource

/** Onboarding tutorial: a horizontal pager over the 8 tutorial images. */
@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    val pages = listOf(
        Res.drawable.tutorial_01, Res.drawable.tutorial_02, Res.drawable.tutorial_03, Res.drawable.tutorial_04,
        Res.drawable.tutorial_05, Res.drawable.tutorial_06, Res.drawable.tutorial_07, Res.drawable.tutorial_08,
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                Image(
                    painter = painterResource(pages[page]),
                    contentDescription = "Tutorial ${page + 1}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(pages.size) { i ->
                    val selected = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            ),
                    )
                }
            }
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    }
}
