package al.ahgitdevelopment.municion.ui.main

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.Utils
import al.ahgitdevelopment.municion.ui.components.MunicionBottomBar
import al.ahgitdevelopment.municion.ui.components.MunicionFAB
import al.ahgitdevelopment.municion.ui.components.MunicionTopBar
import al.ahgitdevelopment.municion.ui.compras.GuiaSelectionDialog
import al.ahgitdevelopment.municion.ui.guias.LicenciaSelectionDialog
import al.ahgitdevelopment.municion.ui.navigation.CompraForm
import al.ahgitdevelopment.municion.ui.navigation.Compras
import al.ahgitdevelopment.municion.ui.navigation.GuiaForm
import al.ahgitdevelopment.municion.ui.navigation.Guias
import al.ahgitdevelopment.municion.ui.navigation.LicenciaForm
import al.ahgitdevelopment.municion.ui.navigation.Licencias
import al.ahgitdevelopment.municion.ui.navigation.MunicionNavHost
import al.ahgitdevelopment.municion.ui.navigation.Settings
import al.ahgitdevelopment.municion.ui.navigation.TiradaForm
import al.ahgitdevelopment.municion.ui.navigation.Tiradas
import al.ahgitdevelopment.municion.ui.navigation.navtypes.navigateSafely
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

/**
 * Pantalla principal de la aplicación Munición.
 *
 * Arquitectura de Scaffold único siguiendo las mejores prácticas de Google:
 * - Un solo Scaffold con TopBar, BottomBar y FAB dinámicos
 * - Pantallas hijas sin Scaffold (solo contenido)
 * - Visibilidad condicional basada en la ruta actual
 *
 * @param navController Controlador de navegación
 * @param viewModel ViewModel principal (sincronización, auth)
 * @param guiaViewModel ViewModel de guías (para diálogos de selección)
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel(),
    guiaViewModel: GuiaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observar ruta actual para UI dinámica
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Callback de save para formularios (registrado por cada FormContent)
    var formSaveCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Estados para diálogos de selección
    var showLicenciaDialog by remember { mutableStateOf(false) }
    var showGuiaDialog by remember { mutableStateOf(false) }

    // Datos para diálogos
    val licencias by guiaViewModel.licencias.collectAsStateWithLifecycle()
    val guias by guiaViewModel.guias.collectAsStateWithLifecycle()

    // Determinar visibilidad de componentes
    val showBottomBar = currentRoute in listScreenRoutes
    val showFab = currentRoute in fabScreenRoutes ||
            (currentRoute?.contains("Form") == true && formSaveCallback != null)

    // Dialog de selección de licencia (para crear guía)
    if (showLicenciaDialog) {
        LicenciaSelectionDialog(
            licencias = licencias,
            onLicenciaSelected = { licencia ->
                showLicenciaDialog = false
                val tipoLicencia = licencia.getNombre(context)
                navController.navigateSafely(GuiaForm(guia = null, tipoLicencia = tipoLicencia))
            },
            onDismiss = { showLicenciaDialog = false }
        )
    }

    // Dialog de selección de guía (para crear compra)
    if (showGuiaDialog) {
        GuiaSelectionDialog(
            guias = guias,
            onGuiaSelected = { guia ->
                showGuiaDialog = false
                navController.navigateSafely(
                    CompraForm(
                        compra = null,
                        guia = guia
                    )
                )
            },
            onDismiss = { showGuiaDialog = false }
        )
    }

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

            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MunicionTopBar(
                currentRoute = currentRoute,
                syncState = syncState,
                onSyncClick = { viewModel.syncFromFirebase() },
                onSettingsClick = { navController.navigateSafely(Settings) },
                onBackClick = { navController.popBackStack() },
                onScoreTableClick = {
                    try {
                        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.image_table)
                        Utils.showImage(context, bitmap, "score_table")
                    } catch (ex: Exception) {
                        Log.e("MainScreen", "Error mostrando la tabla de tiradas", ex)
                        scope.launch {
                            snackbarHostState.showSnackbar("Error al mostrar la tabla de puntuaciones")
                        }
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                MunicionBottomBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (showFab) {
                MunicionFAB(
                    currentRoute = currentRoute,
                    onAddLicencia = {
                        navController.navigateSafely(LicenciaForm(licencia = null))
                    },
                    onAddGuia = {
                        if (licencias.isEmpty()) {
                            // TODO: Show snackbar "Primero crea una licencia"
                        } else {
                            showLicenciaDialog = true
                        }
                    },
                    onAddCompra = {
                        if (guias.isEmpty()) {
                            // TODO: Show snackbar "Primero crea una guía"
                        } else {
                            showGuiaDialog = true
                        }
                    },
                    onAddTirada = {
                        navController.navigateSafely(TiradaForm(tirada = null))
                    },
                    onSave = { formSaveCallback?.invoke() },
                    hasSaveCallback = formSaveCallback != null
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        MunicionNavHost(
            navController = navController,
            innerPadding = innerPadding,
            snackbarHostState = snackbarHostState,
            onRegisterSaveCallback = { callback -> formSaveCallback = callback }
        )
    }
}

/**
 * Rutas de pantallas de lista (tabs principales).
 */
private val listScreenRoutes = setOf(
    Licencias::class.qualifiedName,
    Guias::class.qualifiedName,
    Compras::class.qualifiedName,
    Tiradas::class.qualifiedName
)

/**
 * Rutas que muestran FAB de añadir.
 */
private val fabScreenRoutes = listScreenRoutes
