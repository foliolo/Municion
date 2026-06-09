package al.ahgitdevelopment.municion.ui.settings

import al.ahgitdevelopment.municion.ui.components.TutorialDialog
import al.ahgitdevelopment.municion.ui.viewmodel.AccountSettingsViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountSettingsContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: AccountSettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pending by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val failed by viewModel.failedSyncCount.collectAsStateWithLifecycle()

    var showSignOut by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }

    if (showTutorial) TutorialDialog(onDismiss = { showTutorial = false })

    if (showSignOut) {
        ConfirmDialog(
            title = "Cerrar sesión",
            message = if (pending > 0) {
                "Tienes $pending cambios sin sincronizar. Si cierras sesión ahora podrían no subirse. ¿Continuar?"
            } else {
                "¿Seguro que quieres cerrar sesión? Se borrarán los datos locales de este dispositivo."
            },
            confirmText = "Cerrar sesión",
            onConfirm = { showSignOut = false; viewModel.signOut() },
            onDismiss = { showSignOut = false },
        )
    }
    if (showDelete) {
        ConfirmDialog(
            title = "Eliminar cuenta",
            message = "Esto eliminará tu cuenta y los datos locales de forma permanente. Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            onConfirm = { showDelete = false; viewModel.deleteAccount() },
            onDismiss = { showDelete = false },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        (uiState as? AccountSettingsViewModel.AccountUiState.Loaded)?.let { loaded ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(loaded.accountInfo.statusText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sincronización", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        failed > 0 -> "$failed cambios fallaron al sincronizar"
                        pending > 0 -> "$pending cambios pendientes de sincronizar"
                        else -> "Todo sincronizado"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = { viewModel.forceSync() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Forzar sincronización")
                }
                if (failed > 0) {
                    OutlinedButton(onClick = { viewModel.retryFailedSync() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Reintentar sincronizaciones fallidas")
                    }
                }
            }
        }

        OutlinedButton(onClick = { showTutorial = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Ver tutorial")
        }

        OutlinedButton(onClick = { showSignOut = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }

        Button(
            onClick = { showDelete = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text("Eliminar cuenta")
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText, color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
