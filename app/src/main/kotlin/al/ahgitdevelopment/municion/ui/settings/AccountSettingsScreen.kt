package al.ahgitdevelopment.municion.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import al.ahgitdevelopment.municion.ui.theme.LicenseExpired
import al.ahgitdevelopment.municion.ui.theme.LicenseValid
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme

/**
 * Pantalla de configuración de cuenta (Stateful).
 *
 * Permite vincular cuenta, gestionar PIN y biometría.
 *
 * @param navController Controlador de navegación
 * @param viewModel ViewModel de configuración
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun AccountSettingsScreen(
    navController: NavHostController,
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val linkingState by viewModel.linkingState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Dialogs state
    var showLinkEmailDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    // Mostrar mensajes de linkingState
    LaunchedEffect(linkingState) {
        when (linkingState) {
            is AccountSettingsViewModel.LinkingState.Success -> {
                snackbarHostState.showSnackbar(
                    (linkingState as AccountSettingsViewModel.LinkingState.Success).message
                )
                viewModel.resetLinkingState()
            }
            is AccountSettingsViewModel.LinkingState.Error -> {
                snackbarHostState.showSnackbar(
                    (linkingState as AccountSettingsViewModel.LinkingState.Error).message
                )
                viewModel.resetLinkingState()
            }
            else -> {}
        }
    }

    // Dialog vincular email
    if (showLinkEmailDialog) {
        LinkEmailDialog(
            onDismiss = { showLinkEmailDialog = false },
            onConfirm = { email, password ->
                viewModel.linkWithEmail(email, password)
                showLinkEmailDialog = false
            }
        )
    }

    // Dialog cerrar sesión
    if (showSignOutDialog) {
        val isAnonymous = (uiState as? AccountSettingsViewModel.AccountUiState.Loaded)
            ?.accountInfo?.isAnonymous == true

        SignOutDialog(
            isAnonymous = isAnonymous,
            onDismiss = { showSignOutDialog = false },
            onConfirm = {
                viewModel.signOut()
                showSignOutDialog = false
                navController.popBackStack()
            }
        )
    }

    // Dialog cambiar PIN
    if (showChangePinDialog) {
        ChangePinDialog(
            onDismiss = { showChangePinDialog = false },
            onConfirm = { currentPin, newPin ->
                val result = viewModel.changePin(currentPin, newPin)
                showChangePinDialog = false
                // El snackbar se mostrará desde el ViewModel
            }
        )
    }

    AccountSettingsScreenContent(
        uiState = uiState,
        linkingState = linkingState,
        snackbarHostState = snackbarHostState,
        onBackClick = { navController.popBackStack() },
        onLinkEmailClick = { showLinkEmailDialog = true },
        onLinkGoogleClick = { /* TODO: Implementar Google Sign-In */ },
        onBiometricChange = { viewModel.setBiometricEnabled(it) },
        onChangePinClick = { showChangePinDialog = true },
        onSignOutClick = { showSignOutDialog = true }
    )
}

/**
 * Contenido de configuración de cuenta (Stateless).
 *
 * @since v3.0.0 (Compose Migration)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreenContent(
    uiState: AccountSettingsViewModel.AccountUiState,
    linkingState: AccountSettingsViewModel.LinkingState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onLinkEmailClick: () -> Unit,
    onLinkGoogleClick: () -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    onChangePinClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración de cuenta") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is AccountSettingsViewModel.AccountUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando...")
                }
            }

            is AccountSettingsViewModel.AccountUiState.NotAuthenticated -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = LicenseExpired,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No autenticado",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            is AccountSettingsViewModel.AccountUiState.Loaded -> {
                LoadedContent(
                    accountInfo = uiState.accountInfo,
                    securityInfo = uiState.securityInfo,
                    isLinking = linkingState is AccountSettingsViewModel.LinkingState.Linking,
                    onLinkEmailClick = onLinkEmailClick,
                    onLinkGoogleClick = onLinkGoogleClick,
                    onBiometricChange = onBiometricChange,
                    onChangePinClick = onChangePinClick,
                    onSignOutClick = onSignOutClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun LoadedContent(
    accountInfo: AccountSettingsViewModel.AccountInfo,
    securityInfo: AccountSettingsViewModel.SecurityInfo,
    isLinking: Boolean,
    onLinkEmailClick: () -> Unit,
    onLinkGoogleClick: () -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    onChangePinClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Estado de cuenta
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (accountInfo.isAnonymous) Icons.Default.Close else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (accountInfo.isAnonymous) LicenseExpired else LicenseValid,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "Estado de cuenta",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = accountInfo.statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Vincular cuenta (solo si es anónima)
        if (accountInfo.isAnonymous) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vincular cuenta",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vincula tu cuenta para sincronizar tus datos entre dispositivos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLinking) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        OutlinedButton(
                            onClick = onLinkEmailClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Vincular con Email")
                        }
                    }
                }
            }

            // Beneficios de vincular
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Beneficios de vincular tu cuenta",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Sincronización en la nube\n• Acceso desde múltiples dispositivos\n• Recuperación de datos",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            // Cuenta vinculada
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cuenta vinculada",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = accountInfo.email ?: "Sin email",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "UID: ${accountInfo.uid.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Seguridad
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Seguridad",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Biometría
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Text("Autenticación biométrica")
                    }
                    Switch(
                        checked = securityInfo.biometricEnabled,
                        onCheckedChange = onBiometricChange,
                        enabled = securityInfo.biometricAvailable
                    )
                }

                if (!securityInfo.biometricAvailable) {
                    Text(
                        text = "No disponible en este dispositivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = LicenseExpired
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Column {
                            Text("PIN")
                            Text(
                                text = if (securityInfo.hasPinConfigured) "Configurado" else "No configurado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    TextButton(onClick = onChangePinClick) {
                        Text("Cambiar")
                    }
                }
            }
        }

        // Cerrar sesión
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSignOutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LinkEmailDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vincular con Email") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(email, password) }) {
                Text("Vincular")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun SignOutDialog(
    isAnonymous: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val message = if (isAnonymous) {
        "Si cierras sesión con una cuenta anónima, perderás acceso a tus datos en la nube. " +
                "Los datos locales se mantendrán.\n\n¿Estás seguro?"
    } else {
        "¿Estás seguro de que deseas cerrar sesión?"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar sesión") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ChangePinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = { currentPin = it; error = null },
                    label = { Text("PIN actual") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it; error = null },
                    label = { Text("Nuevo PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it; error = null },
                    label = { Text("Confirmar nuevo PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newPin != confirmPin) {
                    error = "Los PINs no coinciden"
                } else {
                    onConfirm(currentPin, newPin)
                }
            }) {
                Text("Cambiar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AccountSettingsScreenContentPreview() {
    MunicionTheme {
        AccountSettingsScreenContent(
            uiState = AccountSettingsViewModel.AccountUiState.Loaded(
                accountInfo = AccountSettingsViewModel.AccountInfo(
                    isAnonymous = true,
                    email = null,
                    uid = "abc123def456",
                    displayName = null
                ),
                securityInfo = AccountSettingsViewModel.SecurityInfo(
                    hasPinConfigured = true,
                    biometricEnabled = false,
                    biometricAvailable = true
                )
            ),
            linkingState = AccountSettingsViewModel.LinkingState.Idle,
            snackbarHostState = SnackbarHostState(),
            onBackClick = {},
            onLinkEmailClick = {},
            onLinkGoogleClick = {},
            onBiometricChange = {},
            onChangePinClick = {},
            onSignOutClick = {}
        )
    }
}
