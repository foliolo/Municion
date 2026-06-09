package al.ahgitdevelopment.municion.ui.navigation

import al.ahgitdevelopment.municion.ui.auth.LoginScreen
import al.ahgitdevelopment.municion.ui.auth.MigrationScreen
import al.ahgitdevelopment.municion.ui.compras.ComprasContent
import al.ahgitdevelopment.municion.ui.guias.GuiasContent
import al.ahgitdevelopment.municion.ui.licencias.LicenciasContent
import al.ahgitdevelopment.municion.ui.tiradas.TiradasContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Single NavHost. Child screens are content-only (the Scaffold lives in MainScreen).
 * Routes carry entity ids; form/settings screens are placeholders until phases 6 (forms) / 8.
 */
@Composable
fun MunicionNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    startDestination: Route = Licencias,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(innerPadding),
    ) {
        composable<Login> {
            onRegisterSaveCallback(null)
            LoginScreen(onLoginSuccess = {})
        }
        composable<Migration> {
            onRegisterSaveCallback(null)
            MigrationScreen(onMigrationSuccess = {})
        }
        composable<Licencias> {
            onRegisterSaveCallback(null)
            LicenciasContent(navController, snackbarHostState)
        }
        composable<Guias> {
            onRegisterSaveCallback(null)
            GuiasContent(navController, snackbarHostState)
        }
        composable<Compras> {
            onRegisterSaveCallback(null)
            ComprasContent(navController, snackbarHostState)
        }
        composable<Tiradas> {
            onRegisterSaveCallback(null)
            TiradasContent(navController, snackbarHostState)
        }
        composable<Settings> {
            onRegisterSaveCallback(null)
            PlaceholderContent("Ajustes (próximamente)")
        }
        // Forms — real screens land in the forms sub-milestone (phase 6).
        composable<LicenciaForm> {
            onRegisterSaveCallback(null)
            PlaceholderContent("Formulario de licencia (próximamente)")
        }
        composable<GuiaForm> {
            onRegisterSaveCallback(null)
            PlaceholderContent("Formulario de guía (próximamente)")
        }
        composable<CompraForm> {
            onRegisterSaveCallback(null)
            PlaceholderContent("Formulario de compra (próximamente)")
        }
        composable<TiradaForm> {
            onRegisterSaveCallback(null)
            PlaceholderContent("Formulario de tirada (próximamente)")
        }
    }
}

@Composable
private fun PlaceholderContent(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
