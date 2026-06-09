package al.ahgitdevelopment.municion.ui.navigation

import al.ahgitdevelopment.municion.ui.auth.LoginScreen
import al.ahgitdevelopment.municion.ui.auth.MigrationScreen
import al.ahgitdevelopment.municion.ui.compras.ComprasContent
import al.ahgitdevelopment.municion.ui.forms.compra.CompraFormScreen
import al.ahgitdevelopment.municion.ui.forms.guia.GuiaFormScreen
import al.ahgitdevelopment.municion.ui.forms.licencia.LicenciaFormScreen
import al.ahgitdevelopment.municion.ui.forms.tirada.TiradaFormScreen
import al.ahgitdevelopment.municion.ui.guias.GuiasContent
import al.ahgitdevelopment.municion.ui.licencias.LicenciasContent
import al.ahgitdevelopment.municion.ui.settings.AccountSettingsContent
import al.ahgitdevelopment.municion.ui.tiradas.TiradasContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

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
            AccountSettingsContent(navController, snackbarHostState)
        }
        // Forms — real screens land in the forms sub-milestone (phase 6).
        composable<LicenciaForm> { entry ->
            LicenciaFormScreen(
                licenciaId = entry.toRoute<LicenciaForm>().licenciaId,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback,
            )
        }
        composable<GuiaForm> { entry ->
            val route = entry.toRoute<GuiaForm>()
            GuiaFormScreen(
                guiaId = route.guiaId,
                tipoLicencia = route.tipoLicencia,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback,
            )
        }
        composable<CompraForm> { entry ->
            val route = entry.toRoute<CompraForm>()
            CompraFormScreen(
                compraId = route.compraId,
                guiaId = route.guiaId,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback,
            )
        }
        composable<TiradaForm> { entry ->
            TiradaFormScreen(
                tiradaId = entry.toRoute<TiradaForm>().tiradaId,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback,
            )
        }
    }
}
