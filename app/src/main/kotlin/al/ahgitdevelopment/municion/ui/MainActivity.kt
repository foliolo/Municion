package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.auth.LoginActivity
import al.ahgitdevelopment.municion.ui.main.MainScreen
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainActivity con Jetpack Compose.
 *
 * FASE 5: Migración completa a Compose
 * - ComponentActivity base
 * - setContent con Compose
 * - MunicionTheme
 * - MainScreen como UI principal
 * - Hilt DI
 *
 * Reemplaza la versión anterior con ViewBinding y Navigation Component Fragments.
 *
 * @since v3.0.0 (Compose Migration)
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ViewModels inyectados
    private val mainViewModel: MainViewModel by viewModels()

    // Firebase inyectado con Hilt
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var crashlytics: FirebaseCrashlytics

    // Calendar permission launcher
    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        _permissionResult.tryEmit(isGranted)
    }

    // Permission result flow for Compose
    private val _permissionResult = MutableSharedFlow<Boolean>(replay = 0, extraBufferCapacity = 1)
    private val permissionResult: SharedFlow<Boolean> = _permissionResult.asSharedFlow()

    // Track if calendar permission was already requested this session
    private var calendarPermissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge con iconos claros (blancos) en status bar y navigation bar
        // porque TopBar y BottomBar usan PrimaryDark (fondo oscuro)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        // Observe auth state for navigation to login
        observeAuthState()

        setContent {
            MunicionTheme {
                MunicionApp(
                    mainViewModel = mainViewModel,
                    permissionResult = permissionResult,
                    onRequestCalendarPermission = ::requestCalendarPermission,
                    onShowCalendarRationale = ::shouldShowCalendarRationale,
                    calendarPermissionGranted = isCalendarPermissionGranted()
                )
            }
        }
    }

    /**
     * Observe auth state to navigate to LoginActivity when unauthenticated.
     * This runs in lifecycleScope and handles navigation outside of Compose.
     */
    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collect { state ->
                    when (state) {
                        is MainViewModel.MainUiState.Authenticated -> {
                            // Request calendar permission once per session
                            if (!calendarPermissionRequested) {
                                calendarPermissionRequested = true
                                // Sync is triggered in MainScreen via LaunchedEffect
                            }
                        }

                        is MainViewModel.MainUiState.Unauthenticated -> {
                            navigateToLogin()
                        }

                        is MainViewModel.MainUiState.Loading -> {
                            // Do nothing, waiting for auth state
                        }
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        firebaseAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun isCalendarPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowCalendarRationale(): Boolean {
        return shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)
    }

    private fun requestCalendarPermission() {
        calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
    }
}

/**
 * Composable principal de la aplicación.
 *
 * Maneja:
 * - Estado de autenticación
 * - Solicitud de permisos de calendario
 * - Visualización de MainScreen cuando está autenticado
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun MunicionApp(
    mainViewModel: MainViewModel,
    permissionResult: SharedFlow<Boolean>,
    onRequestCalendarPermission: () -> Unit,
    onShowCalendarRationale: () -> Boolean,
    calendarPermissionGranted: Boolean
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dialog states
    var showCalendarRationaleDialog by remember { mutableStateOf(false) }
    var permissionRequestedThisSession by remember { mutableStateOf(false) }

    // Collect permission results
    LaunchedEffect(Unit) {
        permissionResult.collect { granted ->
            val message = if (granted) {
                "Permiso de calendario concedido"
            } else {
                "Permiso de calendario denegado"
            }
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    // Request calendar permission when authenticated
    LaunchedEffect(uiState) {
        if (uiState is MainViewModel.MainUiState.Authenticated && !permissionRequestedThisSession) {
            permissionRequestedThisSession = true
            if (!calendarPermissionGranted) {
                if (onShowCalendarRationale()) {
                    showCalendarRationaleDialog = true
                } else {
                    onRequestCalendarPermission()
                }
            }
            // Trigger sync
            mainViewModel.syncFromFirebase()
        }
    }

    // Calendar permission rationale dialog
    if (showCalendarRationaleDialog) {
        CalendarPermissionDialog(
            onConfirm = {
                showCalendarRationaleDialog = false
                onRequestCalendarPermission()
            },
            onDismiss = {
                showCalendarRationaleDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Permiso de calendario denegado",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    // Main content
    when (uiState) {
        is MainViewModel.MainUiState.Loading -> {
            // Could show loading indicator, but MainScreen handles this too
        }

        is MainViewModel.MainUiState.Authenticated,
        is MainViewModel.MainUiState.Unauthenticated -> {
            MainScreen(viewModel = mainViewModel)
        }
    }
}

/**
 * Dialog para explicar por qué se necesita el permiso de calendario.
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun CalendarPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permiso de calendario") },
        text = {
            Text("La app necesita acceso al calendario para crear recordatorios de caducidad de licencias")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Permitir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
