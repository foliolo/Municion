package al.ahgitdevelopment.municion.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import al.ahgitdevelopment.municion.ui.navigation.navtypes.municionTypeMap
import al.ahgitdevelopment.municion.ui.compras.ComprasContent
import al.ahgitdevelopment.municion.ui.forms.CompraFormContent
import al.ahgitdevelopment.municion.ui.forms.GuiaFormContent
import al.ahgitdevelopment.municion.ui.forms.LicenciaFormContent
import al.ahgitdevelopment.municion.ui.forms.TiradaFormContent
import al.ahgitdevelopment.municion.ui.guias.GuiasContent
import al.ahgitdevelopment.municion.ui.licencias.LicenciasContent
import al.ahgitdevelopment.municion.ui.settings.AccountSettingsContent
import al.ahgitdevelopment.municion.ui.tiradas.TiradasContent

/**
 * NavHost principal de la aplicación Munición.
 *
 * Arquitectura de Scaffold único:
 * - Las pantallas NO tienen Scaffold propio (solo contenido)
 * - TopBar, BottomBar y FAB están en MainScreen
 * - Las pantallas reciben callbacks para registrar funciones de guardado
 *
 * @param navController Controlador de navegación
 * @param innerPadding Padding del Scaffold padre
 * @param snackbarHostState Estado del snackbar compartido
 * @param onRegisterSaveCallback Callback para registrar función de guardado de formularios
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration - Single Scaffold Architecture)
 */
@Composable
fun MunicionNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onRegisterSaveCallback: ((() -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Licencias,
        modifier = modifier.padding(innerPadding),
        typeMap = municionTypeMap  // ← Type-safe serialization
    ) {
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
            val route: LicenciaForm = try {
                backStackEntry.toRoute<LicenciaForm>()
            } catch (e: Exception) {
                // Fallback: navegar back si hay error de deserialización
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar("Error cargando formulario de licencia")
                    navController.popBackStack()
                }
                return@composable  // Early return
            }
            LicenciaFormContent(
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
            val route: GuiaForm = try {
                backStackEntry.toRoute<GuiaForm>()
            } catch (e: Exception) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar("Error cargando formulario de guía")
                    navController.popBackStack()
                }
                return@composable
            }
            GuiaFormContent(
                guia = route.guia,
                tipoLicencia = route.tipoLicencia,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }

        // CompraForm: objeto completo Compra (null para nueva) + Guia obligatoria
        composable<CompraForm>(
            typeMap = municionTypeMap
        ) { backStackEntry ->
            val route: CompraForm = try {
                backStackEntry.toRoute<CompraForm>()
            } catch (e: Exception) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar("Error cargando formulario de compra")
                    navController.popBackStack()
                }
                return@composable
            }
            CompraFormContent(
                compra = route.compra,
                guia = route.guia,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }

        // TiradaForm: objeto completo Tirada (null para nueva)
        composable<TiradaForm>(
            typeMap = municionTypeMap
        ) { backStackEntry ->
            val route: TiradaForm = try {
                backStackEntry.toRoute<TiradaForm>()
            } catch (e: Exception) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar("Error cargando formulario de tirada")
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
