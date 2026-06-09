package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.action_back
import al.ahgitdevelopment.municion.resources.action_settings
import al.ahgitdevelopment.municion.resources.action_sync
import al.ahgitdevelopment.municion.resources.app_name
import al.ahgitdevelopment.municion.resources.section_compras_title
import al.ahgitdevelopment.municion.resources.section_competiciones_title
import al.ahgitdevelopment.municion.resources.section_guias_title
import al.ahgitdevelopment.municion.resources.section_licencias_title
import al.ahgitdevelopment.municion.resources.title_account_settings
import al.ahgitdevelopment.municion.resources.title_competition
import al.ahgitdevelopment.municion.resources.title_guide
import al.ahgitdevelopment.municion.resources.title_license
import al.ahgitdevelopment.municion.resources.title_purchase
import al.ahgitdevelopment.municion.ui.navigation.Compras
import al.ahgitdevelopment.municion.ui.navigation.Guias
import al.ahgitdevelopment.municion.ui.navigation.Licencias
import al.ahgitdevelopment.municion.ui.navigation.Settings as SettingsRoute
import al.ahgitdevelopment.municion.ui.navigation.Tiradas
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel.SyncState
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource

/**
 * Unified dynamic top bar. List tabs show the app/section title + sync + settings; form/settings
 * screens show a back arrow + title. (The Android launcher-logo navigation icon was dropped in
 * the KMP migration — that drawable is app-only.)
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
    val isSettings = currentRoute == SettingsRoute::class.qualifiedName

    when {
        isListScreen -> ListTopBar(currentRoute, syncState, onSyncClick, onSettingsClick, modifier)
        isFormScreen || isSettings -> FormTopBar(formTitle ?: getFormTitle(currentRoute), onBackClick, modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListTopBar(
    currentRoute: String?,
    syncState: SyncState,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(Res.string.app_name))
                Text(text = getSectionTitle(currentRoute), fontSize = 13.sp, fontWeight = FontWeight.Normal)
            }
        },
        modifier = modifier,
        actions = {
            when (syncState) {
                is SyncState.Syncing -> CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
                is SyncState.Idle -> IconButton(onClick = onSyncClick) {
                    Icon(Icons.Default.Sync, contentDescription = stringResource(Res.string.action_sync))
                }
                else -> Unit
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.action_settings))
            }
        },
        colors = topAppBarColors(),
        windowInsets = TopAppBarDefaults.windowInsets,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTopBar(title: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back))
            }
        },
        colors = topAppBarColors(),
        windowInsets = TopAppBarDefaults.windowInsets,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
)

@Composable
private fun getSectionTitle(route: String?): String = when (route) {
    Licencias::class.qualifiedName -> stringResource(Res.string.section_licencias_title)
    Guias::class.qualifiedName -> stringResource(Res.string.section_guias_title)
    Compras::class.qualifiedName -> stringResource(Res.string.section_compras_title)
    Tiradas::class.qualifiedName -> stringResource(Res.string.section_competiciones_title)
    else -> ""
}

@Composable
private fun getFormTitle(route: String?): String = when {
    route == null -> ""
    route == SettingsRoute::class.qualifiedName -> stringResource(Res.string.title_account_settings)
    route.contains("LicenciaForm") -> stringResource(Res.string.title_license)
    route.contains("GuiaForm") -> stringResource(Res.string.title_guide)
    route.contains("CompraForm") -> stringResource(Res.string.title_purchase)
    route.contains("TiradaForm") -> stringResource(Res.string.title_competition)
    else -> ""
}

private val listScreenRoutes = setOf(
    Licencias::class.qualifiedName,
    Guias::class.qualifiedName,
    Compras::class.qualifiedName,
    Tiradas::class.qualifiedName,
)
