package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.auth.AuthViewModel
import al.ahgitdevelopment.municion.ui.main.MainScreen
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel
import android.Manifest
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
 * v3.4.0: Auth Simplification
 * - LoginActivity eliminado, auth se maneja en NavHost
 * - AuthViewModel determina startDestination (Login, Migration, o Licencias)
 * - MainScreen contiene NavHost con todas las rutas
 *
 * @since v3.0.0 (Compose Migration)
 * @updated v3.4.0 (Auth Simplification)
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ViewModels inyectados
    private val mainViewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge con iconos claros (blancos) en status bar y navigation bar
        // porque TopBar y BottomBar usan MaterialTheme.colorScheme.primary
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            MunicionTheme {
                MunicionApp(
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel,
                    permissionResult = permissionResult,
                    onRequestCalendarPermission = ::requestCalendarPermission,
                    onShowCalendarRationale = ::shouldShowCalendarRationale,
                    calendarPermissionGranted = isCalendarPermissionGranted()
                )
            }
        }
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
 * Composable principal de la aplicacion.
 *
 * Maneja:
 * - Estado de autenticacion (Login, Migration, Authenticated)
 * - Solicitud de permisos de calendario
 * - Visualizacion de MainScreen cuando esta autenticado
 *
 * @since v3.0.0 (Compose Migration)
 * @updated v3.4.0 (Auth Simplification)
 */
@Composable
fun MunicionApp(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    permissionResult: SharedFlow<Boolean>,
    onRequestCalendarPermission: () -> Unit,
    onShowCalendarRationale: () -> Boolean,
    calendarPermissionGranted: Boolean
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
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
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Authenticated && !permissionRequestedThisSession) {
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

    // Main content - auth state determines startDestination in MainScreen
    MainScreen(
        viewModel = mainViewModel,
        authState = authState,
        onAuthStateChange = { authViewModel.checkAuthState() }
    )
}

/**
 * Dialog para explicar por que se necesita el permiso de calendario.
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
