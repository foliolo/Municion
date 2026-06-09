package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.auth.AuthViewModel
import al.ahgitdevelopment.municion.ui.main.MainScreen
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable. Hosts the theme and the auth-gated single-Scaffold [MainScreen]; the auth
 * state (from GitLive's auth flow) selects the start destination and drives login/logout nav.
 */
@Composable
fun App() {
    MunicionTheme {
        val authViewModel: AuthViewModel = koinViewModel()
        val authState by authViewModel.authState.collectAsStateWithLifecycle()
        MainScreen(authState = authState)
    }
}
