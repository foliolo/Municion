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
import al.ahgitdevelopment.municion.ui.navigation.Routes
import al.ahgitdevelopment.municion.ui.theme.OnPrimary
import al.ahgitdevelopment.municion.ui.theme.PrimaryDark
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel.SyncState

/**
 * TopBar dinámico unificado para toda la aplicación.
 *
 * Cambia según el tipo de pantalla:
 * - ListScreens (tabs): Título app + Sync + Settings
 * - FormScreens: Back + Título de la entidad
 * - Settings: Back + Título
 *
 * @param currentRoute Ruta actual de navegación
 * @param syncState Estado de sincronización (solo para ListScreens)
 * @param onSyncClick Callback para sincronización manual
 * @param onSettingsClick Callback para ir a settings
 * @param onBackClick Callback para volver atrás
 * @param formTitle Título opcional para formularios (override automático)
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MunicionTopBar(
    currentRoute: String?,
    syncState: SyncState = SyncState.Idle,
    onSyncClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    formTitle: String? = null,
    modifier: Modifier = Modifier
) {
    val isListScreen = currentRoute in listScreenRoutes
    val isFormScreen = currentRoute?.contains("Form") == true
    val isSettings = currentRoute == Routes.SETTINGS

    when {
        isListScreen -> {
            ListTopBar(
                syncState = syncState,
                onSyncClick = onSyncClick,
                onSettingsClick = onSettingsClick,
                modifier = modifier
            )
        }
        isFormScreen || isSettings -> {
            val title = formTitle ?: getFormTitle(currentRoute)
            FormTopBar(
                title = title,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
    }
}

/**
 * TopBar para pantallas de lista (tabs principales).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListTopBar(
    syncState: SyncState,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        modifier = modifier,
        actions = {
            when (syncState) {
                is SyncState.Syncing -> {
                    CircularProgressIndicator(
                        color = OnPrimary,
                        strokeWidth = 2.dp
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
                else -> { }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración"
                )
            }
        },
        colors = topAppBarColors(),
        windowInsets = TopAppBarDefaults.windowInsets
    )
}

/**
 * TopBar para pantallas de formulario y settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTopBar(
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
        colors = topAppBarColors(),
        windowInsets = TopAppBarDefaults.windowInsets
    )
}

/**
 * Colores compartidos para todos los TopBars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = PrimaryDark,
    titleContentColor = OnPrimary,
    navigationIconContentColor = OnPrimary,
    actionIconContentColor = OnPrimary
)

/**
 * Obtiene el título del TopBar según la ruta del formulario.
 */
private fun getFormTitle(route: String?): String {
    return when {
        route == null -> ""
        route == Routes.SETTINGS -> "Configuración de cuenta"
        route.startsWith(Routes.LICENCIA_FORM) -> "Licencia"
        route.startsWith(Routes.GUIA_FORM) -> "Guía"
        route.startsWith(Routes.COMPRA_FORM) -> "Compra"
        route.startsWith(Routes.TIRADA_FORM) -> "Tirada"
        else -> ""
    }
}

/**
 * Rutas que corresponden a pantallas de lista (tabs principales).
 */
private val listScreenRoutes = setOf(
    Routes.LICENCIAS,
    Routes.GUIAS,
    Routes.COMPRAS,
    Routes.TIRADAS
)
