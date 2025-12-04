package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.ui.navigation.Compras
import al.ahgitdevelopment.municion.ui.navigation.Guias
import al.ahgitdevelopment.municion.ui.navigation.Licencias
import al.ahgitdevelopment.municion.ui.navigation.Settings
import al.ahgitdevelopment.municion.ui.navigation.Tiradas
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel.SyncState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    modifier: Modifier = Modifier,
    syncState: SyncState = SyncState.Idle,
    onSyncClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    formTitle: String? = null,
) {
    val isListScreen = currentRoute in listScreenRoutes
    val isFormScreen = currentRoute?.contains("Form") == true
    val isSettings = currentRoute == Settings::class.qualifiedName

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
 *
 * @param syncState Estado de sincronización
 * @param onSyncClick Callback para sincronizar
 * @param onSettingsClick Callback para ir a settings
 * @param modifier Modificador opcional
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
        navigationIcon = {
            Image(
                painter = painterResource(R.drawable.ic_launcher_3_light),
                contentDescription = stringResource(R.string.cd_logo),
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clip(shape = MaterialTheme.shapes.extraSmall)
                    .size(40.dp)
            )
        },
        modifier = modifier,
        actions = {
            when (syncState) {
                is SyncState.Syncing -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }

                is SyncState.Idle -> {
                    IconButton(onClick = onSyncClick) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = stringResource(R.string.action_sync)
                        )
                    }
                }

                else -> {}
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.action_settings)
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
                    contentDescription = stringResource(R.string.action_back)
                )
            }
        },
        colors = topAppBarColors(),
        windowInsets = TopAppBarDefaults.windowInsets
    )
}

/**
 * Colores compartidos para todos los TopBars.
 * Usa colores del tema Material 3 para adaptarse a light/dark mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
)

/**
 * Obtiene el título del TopBar según la ruta del formulario.
 */
@Composable
private fun getFormTitle(route: String?): String {
    return when {
        route == null -> ""
        route == Settings::class.qualifiedName -> stringResource(R.string.title_account_settings)
        route.contains("LicenciaForm") -> stringResource(R.string.title_license)
        route.contains("GuiaForm") -> stringResource(R.string.title_guide)
        route.contains("CompraForm") -> stringResource(R.string.title_purchase)
        route.contains("TiradaForm") -> stringResource(R.string.title_competition)
        else -> ""
    }
}

/**
 * Rutas que corresponden a pantallas de lista (tabs principales).
 */
private val listScreenRoutes = setOf(
    Licencias::class.qualifiedName,
    Guias::class.qualifiedName,
    Compras::class.qualifiedName,
    Tiradas::class.qualifiedName
)

@Preview(name = "List Screen TopBar", showBackground = true)
@Composable
private fun PreviewMunicionTopBarList() {
    MunicionTopBar(
        currentRoute = Licencias::class.qualifiedName,
        syncState = SyncState.Idle,
    )
}

@Preview(name = "Form Screen TopBar", showBackground = true)
@Composable
private fun PreviewMunicionTopBarForm() {
    MunicionTopBar(
        currentRoute = "LicenciaForm",
    )
}