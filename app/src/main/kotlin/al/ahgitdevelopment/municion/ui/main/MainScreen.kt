package al.ahgitdevelopment.municion.ui.main

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.Utils
import al.ahgitdevelopment.municion.auth.AuthViewModel
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
import al.ahgitdevelopment.municion.ui.navigation.Login
import al.ahgitdevelopment.municion.ui.navigation.Migration
import al.ahgitdevelopment.municion.ui.navigation.MunicionNavHost
import al.ahgitdevelopment.municion.ui.navigation.Route
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
 * Pantalla principal de la aplicacion Municion.
 *
 * Arquitectura de Scaffold unico siguiendo las mejores practicas de Google:
 * - Un solo Scaffold con TopBar, BottomBar y FAB dinamicos
 * - Pantallas hijas sin Scaffold (solo contenido)
 * - Visibilidad condicional basada en la ruta actual
 *
 * v3.4.0: Auth Simplification
 * - authState determina startDestination (Login, Migration, Licencias)
 * - onAuthStateChange callback para refrescar estado despues de login/migracion
 *
 * @param navController Controlador de navegacion
 * @param viewModel ViewModel principal (sincronizacion, auth)
 * @param guiaViewModel ViewModel de guias (para dialogos de seleccion)
 * @param authState Estado de autenticacion actual
 * @param onAuthStateChange Callback para refrescar estado de auth
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 * @updated v3.4.0 (Auth Simplification)
 */
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel(),
    guiaViewModel: GuiaViewModel = hiltViewModel(),
    authState: AuthViewModel.AuthState = AuthViewModel.AuthState.Loading,
    onAuthStateChange: () -> Unit = {}
) {
    // Calcular startDestination basado en authState
    val startDestination: Route = when (authState) {
        is AuthViewModel.AuthState.Loading -> Licencias // Temporal, se actualizara
        is AuthViewModel.AuthState.NotAuthenticated -> Login
        is AuthViewModel.AuthState.RequiresMigration -> Migration
        is AuthViewModel.AuthState.Authenticated -> Licencias
        is AuthViewModel.AuthState.Error -> Login
    }
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

    // Navegar automaticamente cuando cambia el estado de auth
    // Esto maneja el logout correctamente - cuando AuthStateListener detecta signOut,
    // authState cambia a NotAuthenticated y navegamos a Login
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.NotAuthenticated -> {
                // Usuario cerro sesion - navegar a Login y limpiar back stack
                navController.navigate(Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthViewModel.AuthState.RequiresMigration -> {
                // Usuario anonimo - navegar a Migration
                navController.navigate(Migration) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthViewModel.AuthState.Authenticated -> {
                // Usuario autenticado - navegar a Licencias si no estamos ya en contenido principal
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == Login::class.qualifiedName ||
                    currentRoute == Migration::class.qualifiedName) {
                    navController.navigate(Licencias) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> { /* Loading, Error - no hacer nada */ }
        }
    }

    // Determinar visibilidad de componentes
    // Ocultar en pantallas de auth (Login, Migration)
    val isAuthScreen = currentRoute in authScreenRoutes
    val showTopBar = !isAuthScreen
    val showBottomBar = currentRoute in listScreenRoutes && !isAuthScreen
    val showFab = (currentRoute in fabScreenRoutes ||
            (currentRoute?.contains("Form") == true && formSaveCallback != null)) && !isAuthScreen

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
//                snackbarHostState.showSnackbar("Sincronización completada: $count items")
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
            if (showTopBar) {
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
            }
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
                    onAddLicencia = { navController.navigateSafely(LicenciaForm(licencia = null))
                    },
                    onAddGuia = {
                        if (licencias.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.dialog_no_licenses_available))
                            }
                        } else {
                            showLicenciaDialog = true
                        }
                    },
                    onAddCompra = {
                        if (guias.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.dialog_no_guides_available))
                            }
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
            onRegisterSaveCallback = { callback -> formSaveCallback = callback },
            startDestination = startDestination,
            onAuthStateChange = onAuthStateChange
        )
    }
}

/**
 * Rutas de pantallas de autenticacion (sin TopBar, BottomBar, FAB).
 */
private val authScreenRoutes = setOf(
    Login::class.qualifiedName,
    Migration::class.qualifiedName
)

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
 * Rutas que muestran FAB de anadir.
 */
private val fabScreenRoutes = listScreenRoutes
