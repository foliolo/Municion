package al.ahgitdevelopment.municion.ui.main

import al.ahgitdevelopment.municion.BuildConfig
import al.ahgitdevelopment.municion.ads.AdaptiveBanner
import al.ahgitdevelopment.municion.auth.AuthViewModel
import al.ahgitdevelopment.municion.platform.AppPermission
import al.ahgitdevelopment.municion.platform.rememberPermissionRequester
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
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Single-Scaffold host: one TopBar/BottomBar/FAB driven by the current route; child screens are
 * content-only. The auth state decides the start destination and drives login/logout navigation.
 */
@Composable
fun MainScreen(
    authState: AuthViewModel.AuthState,
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = koinViewModel(),
    guiaViewModel: GuiaViewModel = koinViewModel(),
) {
    val startDestination: Route =
        when (authState) {
            is AuthViewModel.AuthState.NotAuthenticated, is AuthViewModel.AuthState.Error -> Login
            is AuthViewModel.AuthState.RequiresMigration -> Migration
            else -> Licencias
        }

    val scope = rememberCoroutineScope()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val showAds by viewModel.showAds.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Ask for the notification permission once the user is in (Android 13+; no-op elsewhere).
    val notificationPermission = rememberPermissionRequester(AppPermission.NOTIFICATIONS)
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Authenticated && !notificationPermission.isGranted) {
            notificationPermission.request()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var formSaveCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showLicenciaDialog by remember { mutableStateOf(false) }
    var showGuiaDialog by remember { mutableStateOf(false) }

    val licencias by guiaViewModel.licencias.collectAsStateWithLifecycle()
    val guias by guiaViewModel.guias.collectAsStateWithLifecycle()

    // Auth-driven navigation (login / logout / migration).
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.NotAuthenticated, is AuthViewModel.AuthState.Error ->
                navController.navigate(Login) { popUpTo(0) { inclusive = true } }
            is AuthViewModel.AuthState.RequiresMigration ->
                navController.navigate(Migration) { popUpTo(0) { inclusive = true } }
            is AuthViewModel.AuthState.Authenticated -> {
                val r = navController.currentDestination?.route
                if (r == Login::class.qualifiedName || r == Migration::class.qualifiedName) {
                    navController.navigate(Licencias) { popUpTo(0) { inclusive = true } }
                }
                viewModel.initSync()
            }
            else -> Unit
        }
    }

    val isAuthScreen = currentRoute in authScreenRoutes
    val showTopBar = !isAuthScreen
    val showBottomBar = currentRoute in listScreenRoutes
    val showFab =
        !isAuthScreen &&
            (currentRoute in listScreenRoutes || (currentRoute?.contains("Form") == true && formSaveCallback != null))

    if (showLicenciaDialog) {
        LicenciaSelectionDialog(
            licencias = licencias,
            onSelect = { licencia ->
                showLicenciaDialog = false
                navController.navigate(GuiaForm(tipoLicencia = licencia.tipo))
            },
            onDismiss = { showLicenciaDialog = false },
        )
    }
    if (showGuiaDialog) {
        GuiaSelectionDialog(
            guias = guias,
            onSelect = { guia ->
                showGuiaDialog = false
                navController.navigate(CompraForm(guiaId = guia.id))
            },
            onDismiss = { showGuiaDialog = false },
        )
    }

    LaunchedEffect(syncState) {
        when (val s = syncState) {
            is MainViewModel.SyncState.Error -> snackbarHostState.showSnackbar("Error de sincronización: ${s.message}")
            is MainViewModel.SyncState.SuccessWithParseErrors ->
                snackbarHostState.showSnackbar("Sincronizado con ${s.parseErrorCount} elementos por revisar")
            else -> Unit
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
                    onSettingsClick = { navController.navigate(Settings) },
                    onBackClick = { navController.popBackStack() },
                )
            }
        },
        bottomBar = {
            Column {
                val isSettingsScreen = currentRoute == Settings::class.qualifiedName
                if (!isAuthScreen && !isSettingsScreen && showAds) {
                    AdaptiveBanner(adUnitId = BuildConfig.ADMOB_BOTTOM_BANNER_ID, modifier = Modifier.fillMaxWidth())
                }
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    MunicionBottomBar(navController = navController)
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                MunicionFAB(
                    currentRoute = currentRoute,
                    onAddLicencia = { navController.navigate(LicenciaForm()) },
                    onAddGuia = {
                        if (licencias.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("No hay licencias. Crea una licencia primero.") }
                        } else {
                            showLicenciaDialog = true
                        }
                    },
                    onAddCompra = {
                        if (guias.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("No hay guías. Crea una guía primero.") }
                        } else {
                            showGuiaDialog = true
                        }
                    },
                    onAddTirada = { navController.navigate(TiradaForm()) },
                    onSave = { formSaveCallback?.invoke() },
                    hasSaveCallback = formSaveCallback != null,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        MunicionNavHost(
            navController = navController,
            innerPadding = innerPadding,
            snackbarHostState = snackbarHostState,
            onRegisterSaveCallback = { formSaveCallback = it },
            startDestination = startDestination,
        )
    }
}

private val authScreenRoutes = setOf(Login::class.qualifiedName, Migration::class.qualifiedName)
private val listScreenRoutes =
    setOf(
        Licencias::class.qualifiedName,
        Guias::class.qualifiedName,
        Compras::class.qualifiedName,
        Tiradas::class.qualifiedName,
    )
