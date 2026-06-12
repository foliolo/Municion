package al.ahgitdevelopment.municion.ui.auth

import al.ahgitdevelopment.municion.auth.SocialLoginAvailability
import al.ahgitdevelopment.municion.auth.SocialLoginProvider
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.app_logo
import al.ahgitdevelopment.municion.resources.app_name
import al.ahgitdevelopment.municion.resources.auth_apple_button
import al.ahgitdevelopment.municion.resources.auth_confirm_password_label
import al.ahgitdevelopment.municion.resources.auth_create_account_button
import al.ahgitdevelopment.municion.resources.auth_email_label
import al.ahgitdevelopment.municion.resources.auth_email_section
import al.ahgitdevelopment.municion.resources.auth_forgot_password
import al.ahgitdevelopment.municion.resources.auth_google_button
import al.ahgitdevelopment.municion.resources.auth_hide_password
import al.ahgitdevelopment.municion.resources.auth_login_subtitle
import al.ahgitdevelopment.municion.resources.auth_login_title
import al.ahgitdevelopment.municion.resources.auth_logo_content_description
import al.ahgitdevelopment.municion.resources.auth_password_label
import al.ahgitdevelopment.municion.resources.auth_password_reset_sent
import al.ahgitdevelopment.municion.resources.auth_register_subtitle
import al.ahgitdevelopment.municion.resources.auth_register_title
import al.ahgitdevelopment.municion.resources.auth_show_password
import al.ahgitdevelopment.municion.resources.auth_sign_in_button
import al.ahgitdevelopment.municion.resources.auth_toggle_to_login
import al.ahgitdevelopment.municion.resources.auth_toggle_to_register
import al.ahgitdevelopment.municion.resources.ic_apple
import al.ahgitdevelopment.municion.resources.ic_google
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.LoginViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Email/password login + registration screen with configured social providers. */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val socialLoginAvailability by viewModel.socialLoginAvailability.collectAsStateWithLifecycle()
    val loadingProvider by viewModel.loadingProvider.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val passwordResetSentMessage = stringResource(Res.string.auth_password_reset_sent)

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginViewModel.LoginUiState.Success -> onLoginSuccess()
            is LoginViewModel.LoginUiState.PasswordResetSent -> {
                snackbarHostState.showSnackbar(passwordResetSentMessage)
                viewModel.resetState()
            }
            is LoginViewModel.LoginUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LoginContent(
            isRegisterMode = isRegisterMode,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            passwordVisible = passwordVisible,
            isLoading = uiState is LoginViewModel.LoginUiState.Loading,
            loadingProvider = loadingProvider,
            socialLoginAvailability = socialLoginAvailability,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onConfirmPasswordChange = { confirmPassword = it },
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            onPrimaryAction = {
                if (isRegisterMode) {
                    viewModel.createAccount(email, password, confirmPassword)
                } else {
                    viewModel.signIn(email, password)
                }
            },
            onToggleMode = { isRegisterMode = !isRegisterMode },
            onResetPassword = { viewModel.resetPassword(email) },
            onSocialLogin = viewModel::signInWithProvider,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
        )
    }
}

@Composable
private fun LoginContent(
    isRegisterMode: Boolean,
    email: String,
    password: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    loadingProvider: SocialLoginProvider?,
    socialLoginAvailability: SocialLoginAvailability,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onPrimaryAction: () -> Unit,
    onToggleMode: () -> Unit,
    onResetPassword: () -> Unit,
    onSocialLogin: (SocialLoginProvider) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = stringResource(Res.string.auth_logo_content_description),
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(20.dp)),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text =
                if (isRegisterMode) {
                    stringResource(Res.string.auth_register_title)
                } else {
                    stringResource(Res.string.auth_login_title)
                },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text =
                if (isRegisterMode) {
                    stringResource(Res.string.auth_register_subtitle)
                } else {
                    stringResource(Res.string.auth_login_subtitle)
                },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (socialLoginAvailability.hasAnyProvider) {
            Spacer(Modifier.height(28.dp))
            if (socialLoginAvailability.google) {
                SocialLoginButton(
                    text = stringResource(Res.string.auth_google_button),
                    icon = Res.drawable.ic_google,
                    tintIcon = false,
                    provider = SocialLoginProvider.Google,
                    isLoading = loadingProvider == SocialLoginProvider.Google,
                    enabled = !isLoading,
                    onClick = onSocialLogin,
                )
                Spacer(Modifier.height(12.dp))
            }
            if (socialLoginAvailability.apple) {
                SocialLoginButton(
                    text = stringResource(Res.string.auth_apple_button),
                    icon = Res.drawable.ic_apple,
                    tintIcon = true,
                    provider = SocialLoginProvider.Apple,
                    isLoading = loadingProvider == SocialLoginProvider.Apple,
                    enabled = !isLoading,
                    onClick = onSocialLogin,
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = stringResource(Res.string.auth_email_section),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(Res.string.auth_email_label)) },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(Res.string.auth_password_label)) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done,
                ),
            enabled = !isLoading,
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility, enabled = !isLoading) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription =
                            if (passwordVisible) {
                                stringResource(Res.string.auth_hide_password)
                            } else {
                                stringResource(Res.string.auth_show_password)
                            },
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (isRegisterMode) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text(stringResource(Res.string.auth_confirm_password_label)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onPrimaryAction,
            enabled = !isLoading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
        ) {
            if (isLoading && loadingProvider == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    if (isRegisterMode) {
                        stringResource(Res.string.auth_create_account_button)
                    } else {
                        stringResource(Res.string.auth_sign_in_button)
                    },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onToggleMode,
            enabled = !isLoading,
        ) {
            Text(
                text =
                    if (isRegisterMode) {
                        stringResource(Res.string.auth_toggle_to_login)
                    } else {
                        stringResource(Res.string.auth_toggle_to_register)
                    },
                textAlign = TextAlign.Center,
            )
        }

        if (!isRegisterMode) {
            TextButton(
                onClick = onResetPassword,
                enabled = !isLoading,
            ) {
                Text(stringResource(Res.string.auth_forgot_password))
            }
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    icon: DrawableResource,
    tintIcon: Boolean,
    provider: SocialLoginProvider,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: (SocialLoginProvider) -> Unit,
) {
    OutlinedButton(
        onClick = { onClick(provider) },
        enabled = enabled,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                // Google's logo is multicolour (no tint); Apple's is monochrome and tinted to the
                // button's content colour so it adapts to light/dark themes.
                colorFilter = if (tintIcon) ColorFilter.tint(LocalContentColor.current) else null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(text)
        }
    }
}

@Preview
@Composable
fun PreviewLoginContent() {
    MunicionTheme {
        LoginContent(
            isRegisterMode = false,
            email = "usuario@example.com",
            password = "123456",
            confirmPassword = "",
            passwordVisible = false,
            isLoading = false,
            loadingProvider = null,
            socialLoginAvailability = SocialLoginAvailability(google = true, apple = true),
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onPrimaryAction = {},
            onToggleMode = {},
            onResetPassword = {},
            onSocialLogin = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
        )
    }
}
