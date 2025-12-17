package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

/**
 * Lista de imagenes del tutorial.
 * Android selecciona automaticamente drawable-en/ para idioma ingles.
 */
private val tutorialImages = listOf(
    R.drawable.tutorial_01,
    R.drawable.tutorial_02,
    R.drawable.tutorial_03,
    R.drawable.tutorial_04,
    R.drawable.tutorial_05,
    R.drawable.tutorial_06,
    R.drawable.tutorial_07,
    R.drawable.tutorial_08
)

/**
 * Dialog fullscreen que muestra el tutorial en formato de slides.
 *
 * Usa HorizontalPager para permitir swipe entre paginas,
 * con indicadores de pagina (dots) y botones de navegacion.
 *
 * @param onDismiss Callback cuando se cierra el dialog
 *
 * @since v3.2.1 (Tutorial feature)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0) { tutorialImages.size }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.tutorial)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_close)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HorizontalPager con las imagenes
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    Image(
                        painter = painterResource(id = tutorialImages[page]),
                        contentDescription = "Tutorial pagina ${page + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

//                // Indicadores de pagina (dots)
//                PageIndicator(
//                    pageCount = tutorialImages.size,
//                    currentPage = pagerState.currentPage,
//                    modifier = Modifier.padding(vertical = 16.dp)
//                )

                // Controles de navegacion
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Boton Anterior
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.cd_previous),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Contador de pagina
                    Text(
                        text = "${pagerState.currentPage + 1} / ${tutorialImages.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Boton Siguiente o Cerrar
                    if (pagerState.currentPage == tutorialImages.size - 1) {
                        Button(onClick = onDismiss) {
                            Text(stringResource(R.string.cd_close))
                        }
                    } else {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.cd_next),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * Indicadores de pagina (dots) para el pager.
 *
 * @param pageCount Numero total de paginas
 * @param currentPage Pagina actual (0-indexed)
 * @param modifier Modificador opcional
 */
@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isSelected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PageIndicatorPreview() {
    MunicionTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Page 3 of 8: ")
            Spacer(modifier = Modifier.width(8.dp))
            PageIndicator(pageCount = 8, currentPage = 2)
        }
    }
}
