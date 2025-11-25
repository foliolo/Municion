package al.ahgitdevelopment.municion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.ui.theme.OnPrimary
import al.ahgitdevelopment.municion.ui.theme.PrimaryDark
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel.SyncState

/**
 * TopAppBar de la aplicación Munición.
 *
 * Muestra:
 * - Título de la app
 * - Indicador de sincronización
 * - Botón de settings
 * - Botón de back (cuando aplica)
 *
 * @param title Título a mostrar
 * @param syncState Estado actual de sincronización
 * @param showBackButton Si se debe mostrar el botón de retroceso
 * @param onBackClick Callback para el botón de retroceso
 * @param onSettingsClick Callback para el botón de settings
 * @param onSyncClick Callback para sincronización manual
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MunicionTopBar(
    title: String = stringResource(R.string.app_name),
    syncState: SyncState = SyncState.Idle,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        },
        actions = {
            // Indicador de sincronización
            when (syncState) {
                is SyncState.Syncing -> {
                    CircularProgressIndicator(
                        color = OnPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                    )
                }
                is SyncState.Idle -> {
                    IconButton(onClick = onSyncClick) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar"
                        )
                    }
                }
                else -> {
                    // Success, Error, etc. - no mostrar indicador
                }
            }

            // Botón de settings
            if (!showBackButton) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrimaryDark,
            titleContentColor = OnPrimary,
            navigationIconContentColor = OnPrimary,
            actionIconContentColor = OnPrimary
        )
    )
}
