package al.ahgitdevelopment.municion.ui.settings

import al.ahgitdevelopment.municion.BuildConfig
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.ui.components.TutorialDialog
import al.ahgitdevelopment.municion.ui.theme.LicenseExpired
import al.ahgitdevelopment.municion.ui.theme.LicenseValid
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.AccountSettingsViewModel
import android.app.Activity
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

/**
 * Contenido de configuracion de cuenta para Single Scaffold Architecture.
 *
 * v3.4.0: Auth Simplification
 * - Eliminado: PIN, biometrics, vinculacion
 * - Solo muestra info de cuenta, tutorial y cerrar sesion
 *
 * @param navController Controlador de navegacion
 * @param snackbarHostState Estado del snackbar compartido desde MainScreen
 * @param viewModel ViewModel de configuracion
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 * @updated v3.4.0 (Auth Simplification)
 */
@Composable
fun AccountSettingsContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isAdsRemoved by viewModel.isAdsRemoved.collectAsStateWithLifecycle()
    val isPurchaseAvailable by viewModel.isPurchaseAvailable.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Dialogs state
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }

    // Dialog cerrar sesion
    if (showSignOutDialog) {
        val isAnonymous = (uiState as? AccountSettingsViewModel.AccountUiState.Loaded)
            ?.accountInfo?.isAnonymous == true

        SignOutDialog(
            isAnonymous = isAnonymous,
            onDismiss = { showSignOutDialog = false },
            onConfirm = {
                showSignOutDialog = false
                viewModel.signOut()
            }
        )
    }

    // Dialog tutorial
    if (showTutorialDialog) {
        TutorialDialog(
            onDismiss = { showTutorialDialog = false }
        )
    }

    AccountSettingsFields(
        uiState = uiState,
        isAdsRemoved = isAdsRemoved,
        isPurchaseAvailable = isPurchaseAvailable,
        onShowTutorialClick = { showTutorialDialog = true },
        onSignOutClick = { showSignOutDialog = true },
        onRemoveAdsClick = {
            if (context is Activity) {
                viewModel.launchPurchaseFlow(context)
            }
        }
    )
}

/**
 * Campos de configuracion de cuenta (Stateless).
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 * @updated v3.4.0 (Auth Simplification)
 */
@Composable
fun AccountSettingsFields(
    uiState: AccountSettingsViewModel.AccountUiState,
    isAdsRemoved: Boolean,
    isPurchaseAvailable: Boolean,
    onShowTutorialClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onRemoveAdsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is AccountSettingsViewModel.AccountUiState.Loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.loading))
            }
        }

        is AccountSettingsViewModel.AccountUiState.NotAuthenticated -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
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
                    stringResource(R.string.not_authenticated),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        is AccountSettingsViewModel.AccountUiState.Loaded -> {
            LoadedContent(
                accountInfo = uiState.accountInfo,
                isAdsRemoved = isAdsRemoved,
                isPurchaseAvailable = isPurchaseAvailable,
                onShowTutorialClick = onShowTutorialClick,
                onSignOutClick = onSignOutClick,
                onRemoveAdsClick = onRemoveAdsClick,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LoadedContent(
    accountInfo: AccountSettingsViewModel.AccountInfo,
    isAdsRemoved: Boolean,
    isPurchaseAvailable: Boolean,
    onShowTutorialClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onRemoveAdsClick: () -> Unit,
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
                            text = stringResource(R.string.account_status),
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

        // Info de cuenta
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.account_info),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = accountInfo.email ?: stringResource(R.string.no_email),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.uid_display, accountInfo.uid.take(8)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Remove Ads / Premium Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAdsRemoved) LicenseValid.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isAdsRemoved && isPurchaseAvailable) { onRemoveAdsClick() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isAdsRemoved) LicenseValid else MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = if (isAdsRemoved) "Premium (Sin Anuncios)" else "Eliminar Publicidad",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!isAdsRemoved) {
                            Text(
                                text = if (isPurchaseAvailable) "Apoya el desarrollo y elimina los anuncios" else "Cargando información...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                if (!isAdsRemoved) {
                    if (isPurchaseAvailable) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }

        // Tutorial
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowTutorialClick() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Help,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.tutorial),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.tutorial_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Cerrar sesion
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSignOutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.sign_out))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Version
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SignOutDialog(
    isAnonymous: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val message = if (isAnonymous) {
        stringResource(R.string.sign_out_anonymous_warning)
    } else {
        stringResource(R.string.sign_out_confirm)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sign_out)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.sign_out), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AccountSettingsFieldsPreview() {
    MunicionTheme {
        AccountSettingsFields(
            uiState = AccountSettingsViewModel.AccountUiState.Loaded(
                accountInfo = AccountSettingsViewModel.AccountInfo(
                    email = "user@example.com",
                    uid = "abc123def456",
                    displayName = null,
                    isAnonymous = false
                )
            ),
            isAdsRemoved = false,
            isPurchaseAvailable = true,
            onShowTutorialClick = {},
            onSignOutClick = {},
            onRemoveAdsClick = {}
        )
    }
}