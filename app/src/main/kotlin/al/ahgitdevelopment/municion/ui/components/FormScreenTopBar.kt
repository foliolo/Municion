package al.ahgitdevelopment.municion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import al.ahgitdevelopment.municion.ui.theme.OnPrimary
import al.ahgitdevelopment.municion.ui.theme.PrimaryDark

/**
 * TopAppBar para pantallas de formulario (crear/editar entidades).
 *
 * Muestra:
 * - Título dinámico (ej: "Nueva licencia" / "Editar licencia")
 * - Botón de retroceso
 *
 * @param title Título a mostrar
 * @param onBackClick Callback para el botón de retroceso
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreenTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrimaryDark,
            titleContentColor = OnPrimary,
            navigationIconContentColor = OnPrimary,
            actionIconContentColor = OnPrimary
        ),
        // Edge-to-edge: TopBar handles status bar inset
        windowInsets = TopAppBarDefaults.windowInsets
    )
}
