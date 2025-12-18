package al.ahgitdevelopment.municion.ui.navigation

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.ui.auth.LoginScreen
import al.ahgitdevelopment.municion.ui.auth.MigrationScreen
import al.ahgitdevelopment.municion.ui.compras.ComprasContent
import al.ahgitdevelopment.municion.ui.forms.compra.CompraFormContent
import al.ahgitdevelopment.municion.ui.forms.licencia.LicenciaFormScreen
import al.ahgitdevelopment.municion.ui.forms.tirada.TiradaFormContent
import al.ahgitdevelopment.municion.ui.forms.guia.GuiaFormScreen
import al.ahgitdevelopment.municion.ui.guias.GuiasContent
import al.ahgitdevelopment.municion.ui.licencias.LicenciasContent
import al.ahgitdevelopment.municion.ui.navigation.navtypes.municionTypeMap
import al.ahgitdevelopment.municion.ui.settings.AccountSettingsContent
import al.ahgitdevelopment.municion.ui.tiradas.TiradasContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

/**
 * NavHost principal de la aplicacion Municion.
 *
 * Arquitectura de Scaffold unico:
 * - Las pantallas NO tienen Scaffold propio (solo contenido)
 * - TopBar, BottomBar y FAB estan en MainScreen
 * - Las pantallas reciben callbacks para registrar funciones de guardado
 *
 * v3.4.0: Auth Simplification
 * - startDestination determinado por AuthState (Login, Migration, Licencias)
 * - onAuthStateChange callback para refrescar auth despues de login/migracion
 *
 * @param navController Controlador de navegacion
 * @param innerPadding Padding del Scaffold padre
 * @param snackbarHostState Estado del snackbar compartido
 * @param onRegisterSaveCallback Callback para registrar funcion de guardado de formularios
 * @param startDestination Ruta inicial basada en AuthState
 * @param onAuthStateChange Callback para refrescar estado de auth
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 * @updated v3.2.2 (Auth Simplification)
 */
@Composable
fun MunicionNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    startDestination: Route = Licencias,
    onAuthStateChange: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(innerPadding),
        typeMap = municionTypeMap  // ← Type-safe serialization
    ) {
        // ========== AUTENTICACIÓN ==========

        composable<Login> {
            onRegisterSaveCallback(null)
            LoginScreen(
                onLoginSuccess = {
                    onAuthStateChange() // Refresh auth state
                    navController.navigate(Licencias) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Migration> {
            onRegisterSaveCallback(null)
            MigrationScreen(
                onMigrationSuccess = {
                    onAuthStateChange() // Refresh auth state
                    navController.navigate(Licencias) {
                        popUpTo(Migration) { inclusive = true }
                    }
                }
            )
        }

        // ========== TABS PRINCIPALES ==========

        composable<Licencias> {
            onRegisterSaveCallback(null)
            LicenciasContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        composable<Guias> {
            onRegisterSaveCallback(null)
            GuiasContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        composable<Compras> {
            onRegisterSaveCallback(null)
            ComprasContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        composable<Tiradas> {
            onRegisterSaveCallback(null)
            TiradasContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        composable<Settings> {
            onRegisterSaveCallback(null)
            AccountSettingsContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        // ========== FORMULARIOS ==========

        // LicenciaForm: objeto completo Licencia (null para nueva)
        composable<LicenciaForm>(
            typeMap = municionTypeMap
        ) { backStackEntry ->
            val context = LocalContext.current
            val route: LicenciaForm = try {
                backStackEntry.toRoute<LicenciaForm>()
            } catch (e: Exception) {
                // Fallback: navegar back si hay error de deserialización
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_loading_license_form))
                    navController.popBackStack()
                }
                return@composable  // Early return
            }
            LicenciaFormScreen(
                licencia = route.licencia,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }

        // GuiaForm: objeto completo Guia (null para nueva) + tipoLicencia
        composable<GuiaForm>(
            typeMap = municionTypeMap
        ) { backStackEntry ->
            val context = LocalContext.current
            val route: GuiaForm = try {
                backStackEntry.toRoute<GuiaForm>()
            } catch (e: Exception) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_loading_guide_form))
                    navController.popBackStack()
                }
                return@composable
            }
            GuiaFormScreen(
                guia = route.guia,
                tipoLicencia = route.tipoLicencia,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }

        // CompraForm: objeto completo Compra (null para nueva) + Guia asociada
        composable<CompraForm>(
            typeMap = municionTypeMap
        ) { backStackEntry ->
            val context = LocalContext.current
            val route: CompraForm = try {
                backStackEntry.toRoute<CompraForm>()
            } catch (e: Exception) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_loading_purchase_form))
                    navController.popBackStack()
                }
                return@composable
            }
            // Validar que guia no sea null (requerida para el formulario)
            val guia = route.guia
            if (guia == null) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_loading_purchase_form))
                    navController.popBackStack()
                }
                return@composable
            }
            CompraFormContent(
                compra = route.compra,
                guia = guia,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }

        // TiradaForm: objeto completo Tirada (null para nueva)
        composable<TiradaForm>(
            typeMap = municionTypeMap
        ) { backStackEntry ->
            val context = LocalContext.current
            val route: TiradaForm = try {
                backStackEntry.toRoute<TiradaForm>()
            } catch (e: Exception) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_loading_competition_form))
                    navController.popBackStack()
                }
                return@composable
            }
            TiradaFormContent(
                tirada = route.tirada,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }
    }
}
