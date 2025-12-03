package al.ahgitdevelopment.municion.ui.auth

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import al.ahgitdevelopment.municion.ui.viewmodel.LoginViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Pantalla de Login/Registro con email y password.
 *
 * Reemplaza LoginActivity (legacy) con una implementacion Compose moderna.
 * Soporta dos modos: Login (usuarios existentes) y Registro (nuevos usuarios).
 *
 * @param onLoginSuccess Callback cuando el login/registro es exitoso
 * @param viewModel ViewModel inyectado por Hilt
 *
 * @since v3.4.0 (Auth Simplification)
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observar estado de exito
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginViewModel.LoginUiState.Success -> {
                onLoginSuccess()
            }

            is LoginViewModel.LoginUiState.PasswordResetSent -> {
                snackbarHostState.showSnackbar("Email de recuperacion enviado. Revisa tu bandeja de entrada.")
                viewModel.resetState()
            }

            is LoginViewModel.LoginUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as LoginViewModel.LoginUiState.Error).message)
                viewModel.resetState()
            }

            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LoginContent(
            uiState = uiState,
            onSignIn = { email, password -> viewModel.signIn(email, password) },
            onCreateAccount = { email, password, confirm -> viewModel.createAccount(email, password, confirm) },
            onResetPassword = { email -> viewModel.resetPassword(email) },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Contenido de la pantalla de Login (Stateless).
 */
@Composable
private fun LoginContent(
    uiState: LoginViewModel.LoginUiState,
    onSignIn: (String, String) -> Unit,
    onCreateAccount: (String, String, String) -> Unit,
    onResetPassword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isLoading = uiState is LoginViewModel.LoginUiState.Loading

    // Estados de campos
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // Modo: true = Login, false = Registro
    var isLoginMode by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo
        Image(
            painter = painterResource(R.drawable.ic_launcher_4_transparent),
            contentDescription = stringResource(R.string.cd_logo),
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Titulo
        Text(
            text = stringResource(R.string.app_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitulo
        Text(
            text = if (isLoginMode) stringResource(R.string.login_subtitle) else stringResource(R.string.register_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.label_email)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.label_password)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) stringResource(R.string.action_hide) else stringResource(
                            R.string.action_show
                        )
                    )
                }
            },
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = {
                    focusManager.clearFocus()
                    if (isLoginMode) onSignIn(email, password)
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo Confirmar Password (solo en modo registro)
        if (!isLoginMode) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.label_confirm_password)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) stringResource(R.string.action_hide) else stringResource(
                                R.string.action_show
                            )
                        )
                    }
                },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onCreateAccount(email, password, confirmPassword)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Boton principal
        Button(
            onClick = {
                focusManager.clearFocus()
                if (isLoginMode) {
                    onSignIn(email, password)
                } else {
                    onCreateAccount(email, password, confirmPassword)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (isLoginMode) stringResource(R.string.action_login) else stringResource(R.string.action_create_account),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Login/Registro
        TextButton(
            onClick = {
                isLoginMode = !isLoginMode
                confirmPassword = ""
            },
            enabled = !isLoading
        ) {
            Text(
                text = if (isLoginMode) stringResource(R.string.switch_to_register) else stringResource(R.string.switch_to_login),
                color = MaterialTheme.colorScheme.primary
            )
        }

//        // Link recuperar contraseña (solo en modo login)
//        if (isLoginMode) {
//            TextButton(
//                onClick = { onResetPassword(email) },
//                enabled = !isLoading
//            ) {
//                Text(
//                    text = stringResource(R.string.forgot_password),
//                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
//                )
//            }
//        }

        Spacer(modifier = Modifier.height(32.dp))

        // Texto informativo
        Text(
            text = "Tus datos se guardaran de forma segura en la nube y podras acceder desde cualquier dispositivo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun LoginContentPreview() {
    MunicionTheme {
        LoginContent(
            uiState = LoginViewModel.LoginUiState.Idle,
            onSignIn = { _, _ -> },
            onCreateAccount = { _, _, _ -> },
            onResetPassword = { }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun LoginContentLoadingPreview() {
    MunicionTheme {
        LoginContent(
            uiState = LoginViewModel.LoginUiState.Loading,
            onSignIn = { _, _ -> },
            onCreateAccount = { _, _, _ -> },
            onResetPassword = { }
        )
    }
}
