package al.ahgitdevelopment.municion.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import al.ahgitdevelopment.municion.ui.components.MunicionBottomBar
import al.ahgitdevelopment.municion.ui.navigation.MunicionNavHost
import al.ahgitdevelopment.municion.ui.navigation.Routes
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel

/**
 * Pantalla principal de la aplicación Munición.
 *
 * Contiene:
 * - BottomNavigationBar con 4 tabs
 * - NavHost para navegación entre screens
 * - Cada pantalla gestiona su propio TopBar
 *
 * @param navController Controlador de navegación (opcional, se crea uno nuevo si no se proporciona)
 * @param viewModel ViewModel principal (inyectado por Hilt)
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar snackbar para estados de sincronización
    LaunchedEffect(syncState) {
        when (syncState) {
            is MainViewModel.SyncState.Success -> {
                val count = (syncState as MainViewModel.SyncState.Success).count
                snackbarHostState.showSnackbar("Sincronización completada: $count items")
            }
            is MainViewModel.SyncState.SuccessWithParseErrors -> {
                val state = syncState as MainViewModel.SyncState.SuccessWithParseErrors
                val message = if (state.autoFixApplied) {
                    "Sincronizado con correcciones automáticas"
                } else {
                    "Sincronizado con ${state.parseErrorCount} errores"
                }
                snackbarHostState.showSnackbar(message)
            }
            is MainViewModel.SyncState.Error -> {
                snackbarHostState.showSnackbar(
                    "Error de sincronización: ${(syncState as MainViewModel.SyncState.Error).message}"
                )
            }
            else -> { /* No mostrar nada para Idle, Syncing, PartialSuccess */ }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            MunicionBottomBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Edge-to-edge: Don't add extra insets, let TopBar/BottomBar handle them
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        MunicionNavHost(
            navController = navController,
            bottomPadding = innerPadding.calculateBottomPadding(),
            syncState = syncState,
            onSyncClick = { viewModel.syncFromFirebase() },
            onSettingsClick = { navController.navigate(Routes.SETTINGS) }
        )
    }
}
