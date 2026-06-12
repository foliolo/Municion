package al.ahgitdevelopment.municion.ui.settings

import al.ahgitdevelopment.municion.ads.rememberConsentOptions
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.cancelar
import al.ahgitdevelopment.municion.resources.cd_profile_photo
import al.ahgitdevelopment.municion.resources.delete
import al.ahgitdevelopment.municion.resources.ic_apple
import al.ahgitdevelopment.municion.resources.ic_google
import al.ahgitdevelopment.municion.resources.remove_ads_description
import al.ahgitdevelopment.municion.resources.remove_ads_title
import al.ahgitdevelopment.municion.resources.settings_ads_removed_thanks
import al.ahgitdevelopment.municion.resources.settings_delete_account
import al.ahgitdevelopment.municion.resources.settings_delete_account_message
import al.ahgitdevelopment.municion.resources.settings_deleting
import al.ahgitdevelopment.municion.resources.settings_privacy_description
import al.ahgitdevelopment.municion.resources.settings_privacy_options
import al.ahgitdevelopment.municion.resources.settings_provider_anonymous
import al.ahgitdevelopment.municion.resources.settings_provider_email
import al.ahgitdevelopment.municion.resources.settings_restore_purchase
import al.ahgitdevelopment.municion.resources.settings_section_account
import al.ahgitdevelopment.municion.resources.settings_section_help
import al.ahgitdevelopment.municion.resources.settings_section_premium
import al.ahgitdevelopment.municion.resources.settings_section_privacy
import al.ahgitdevelopment.municion.resources.settings_sign_out_message
import al.ahgitdevelopment.municion.resources.sign_out
import al.ahgitdevelopment.municion.resources.tutorial
import al.ahgitdevelopment.municion.resources.tutorial_description
import al.ahgitdevelopment.municion.ui.components.TutorialDialog
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.AccountSettingsViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.AccountSettingsViewModel.AccountInfo
import al.ahgitdevelopment.municion.ui.viewmodel.AccountSettingsViewModel.AuthProvider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountSettingsContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: AccountSettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasRemovedAds by viewModel.hasRemovedAds.collectAsStateWithLifecycle()
    val purchaseInFlight by viewModel.purchaseInFlight.collectAsStateWithLifecycle()
    val purchaseMessage by viewModel.purchaseMessage.collectAsStateWithLifecycle()
    val deleteInFlight by viewModel.deleteInFlight.collectAsStateWithLifecycle()
    val deleteMessage by viewModel.deleteMessage.collectAsStateWithLifecycle()
    val consentOptions = rememberConsentOptions()

    var showSignOut by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }

    LaunchedEffect(purchaseMessage) {
        purchaseMessage?.let {
            snackbarHostState.showSnackbar(getString(it))
            viewModel.consumePurchaseMessage()
        }
    }

    LaunchedEffect(deleteMessage) {
        deleteMessage?.let {
            snackbarHostState.showSnackbar(getString(it))
            viewModel.consumeDeleteMessage()
        }
    }

    if (showTutorial) TutorialDialog(onDismiss = { showTutorial = false })

    if (showSignOut) {
        ConfirmDialog(
            title = stringResource(Res.string.sign_out),
            message = stringResource(Res.string.settings_sign_out_message),
            confirmText = stringResource(Res.string.sign_out),
            onConfirm = {
                showSignOut = false
                viewModel.signOut()
            },
            onDismiss = { showSignOut = false },
        )
    }
    if (showDelete) {
        ConfirmDialog(
            title = stringResource(Res.string.settings_delete_account),
            message = stringResource(Res.string.settings_delete_account_message),
            confirmText = stringResource(Res.string.delete),
            onConfirm = {
                showDelete = false
                viewModel.deleteAccount()
            },
            onDismiss = { showDelete = false },
        )
    }

    SettingsScreenContent(
        accountInfo = (uiState as? AccountSettingsViewModel.AccountUiState.Loaded)?.accountInfo,
        hasRemovedAds = hasRemovedAds,
        purchaseInFlight = purchaseInFlight,
        deleteInFlight = deleteInFlight,
        isConsentAvailable = consentOptions.isAvailable,
        onRemoveAds = viewModel::purchaseRemoveAds,
        onRestorePurchase = viewModel::restorePurchases,
        onPrivacyOptions = consentOptions.show,
        onTutorial = { showTutorial = true },
        onSignOut = { showSignOut = true },
        onDeleteAccount = { showDelete = true },
    )
}

@Composable
private fun SettingsScreenContent(
    accountInfo: AccountInfo?,
    hasRemovedAds: Boolean,
    purchaseInFlight: Boolean,
    deleteInFlight: Boolean,
    isConsentAvailable: Boolean,
    onRemoveAds: () -> Unit,
    onRestorePurchase: () -> Unit,
    onPrivacyOptions: () -> Unit,
    onTutorial: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        accountInfo?.let { ProfileHeader(it, Modifier.fillMaxWidth()) }

        SettingsSection(title = stringResource(Res.string.settings_section_premium)) {
            if (hasRemovedAds) {
                SettingsRow(
                    icon = Icons.Filled.WorkspacePremium,
                    label = stringResource(Res.string.settings_ads_removed_thanks),
                )
            } else {
                SettingsRow(
                    icon = Icons.Filled.WorkspacePremium,
                    label = stringResource(Res.string.remove_ads_title),
                    supportingText = stringResource(Res.string.remove_ads_description),
                    enabled = !purchaseInFlight,
                    onClick = onRemoveAds,
                )
                SettingsRow(
                    icon = Icons.Filled.Restore,
                    label = stringResource(Res.string.settings_restore_purchase),
                    enabled = !purchaseInFlight,
                    onClick = onRestorePurchase,
                )
            }
        }

        if (isConsentAvailable) {
            SettingsSection(title = stringResource(Res.string.settings_section_privacy)) {
                SettingsRow(
                    icon = Icons.Filled.Shield,
                    label = stringResource(Res.string.settings_privacy_options),
                    supportingText = stringResource(Res.string.settings_privacy_description),
                    onClick = onPrivacyOptions,
                )
            }
        }

        SettingsSection(title = stringResource(Res.string.settings_section_help)) {
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                label = stringResource(Res.string.tutorial),
                supportingText = stringResource(Res.string.tutorial_description),
                onClick = onTutorial,
            )
        }

        SettingsSection(title = stringResource(Res.string.settings_section_account)) {
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = stringResource(Res.string.sign_out),
                onClick = onSignOut,
            )
            SettingsRow(
                icon = Icons.Filled.DeleteForever,
                label =
                    if (deleteInFlight) {
                        stringResource(Res.string.settings_deleting)
                    } else {
                        stringResource(Res.string.settings_delete_account)
                    },
                enabled = !deleteInFlight,
                destructive = true,
                onClick = onDeleteAccount,
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    accountInfo: AccountInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (accountInfo.photoUrl != null) {
            AsyncImage(
                model = accountInfo.photoUrl,
                contentDescription = stringResource(Res.string.cd_profile_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(96.dp).clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = stringResource(Res.string.cd_profile_photo),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp),
            )
        }
        Text(
            text = accountInfo.displayName ?: accountInfo.email ?: (accountInfo.uid.take(8) + "…"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (accountInfo.displayName != null && !accountInfo.email.isNullOrBlank()) {
            Text(
                text = accountInfo.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ProviderChip(accountInfo.provider)
    }
}

@Composable
private fun ProviderChip(provider: AuthProvider) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            when (provider) {
                AuthProvider.GOOGLE ->
                    Icon(
                        painter = painterResource(Res.drawable.ic_google),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp),
                    )
                AuthProvider.APPLE ->
                    Icon(
                        painter = painterResource(Res.drawable.ic_apple),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                AuthProvider.EMAIL ->
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                AuthProvider.ANONYMOUS ->
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
            }
            Text(
                text =
                    when (provider) {
                        AuthProvider.GOOGLE -> "Google"
                        AuthProvider.APPLE -> "Apple"
                        AuthProvider.EMAIL -> stringResource(Res.string.settings_provider_email)
                        AuthProvider.ANONYMOUS -> stringResource(Res.string.settings_provider_anonymous)
                    },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp),
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    supportingText: String? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    ListItem(
        headlineContent = {
            Text(label, color = if (destructive) MaterialTheme.colorScheme.error else Color.Unspecified)
        },
        supportingContent = supportingText?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null, tint = tint) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier =
            Modifier
                .alpha(if (enabled) 1f else 0.5f)
                .let { if (onClick != null) it.clickable(enabled = enabled, onClick = onClick) else it },
    )
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
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancelar)) } },
    )
}

@Preview
@Composable
private fun PreviewSettingsScreenContent() {
    MunicionTheme {
        Surface {
            SettingsScreenContent(
                accountInfo =
                    AccountInfo(
                        email = "pablo@example.com",
                        uid = "abc12345xyz",
                        displayName = "Pablo Hurtado",
                        photoUrl = null,
                        isAnonymous = false,
                        provider = AuthProvider.GOOGLE,
                    ),
                hasRemovedAds = false,
                purchaseInFlight = false,
                deleteInFlight = false,
                isConsentAvailable = true,
                onRemoveAds = {},
                onRestorePurchase = {},
                onPrivacyOptions = {},
                onTutorial = {},
                onSignOut = {},
                onDeleteAccount = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSettingsScreenContentPremium() {
    MunicionTheme {
        Surface {
            SettingsScreenContent(
                accountInfo =
                    AccountInfo(
                        email = "pablo@example.com",
                        uid = "abc12345xyz",
                        displayName = null,
                        photoUrl = null,
                        isAnonymous = false,
                        provider = AuthProvider.EMAIL,
                    ),
                hasRemovedAds = true,
                purchaseInFlight = false,
                deleteInFlight = false,
                isConsentAvailable = false,
                onRemoveAds = {},
                onRestorePurchase = {},
                onPrivacyOptions = {},
                onTutorial = {},
                onSignOut = {},
                onDeleteAccount = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewProfileHeaderAnonymous() {
    MunicionTheme {
        Surface {
            ProfileHeader(
                accountInfo =
                    AccountInfo(
                        email = null,
                        uid = "anon5678xyz",
                        displayName = null,
                        photoUrl = null,
                        isAnonymous = true,
                        provider = AuthProvider.ANONYMOUS,
                    ),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}
