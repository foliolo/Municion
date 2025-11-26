package al.ahgitdevelopment.municion.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
        startDestination = Routes.LICENCIAS,
        modifier = modifier.padding(innerPadding)
    ) {
        // ========== TABS PRINCIPALES ==========

        composable(route = Routes.LICENCIAS) {
            onRegisterSaveCallback(null)
            LicenciasContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        composable(route = Routes.GUIAS) {
            onRegisterSaveCallback(null)
            GuiasContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        composable(route = Routes.COMPRAS) {
            onRegisterSaveCallback(null)
            ComprasContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        composable(route = Routes.TIRADAS) {
            onRegisterSaveCallback(null)
            TiradasContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        composable(route = Routes.SETTINGS) {
            onRegisterSaveCallback(null)
            AccountSettingsContent(
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }

        // ========== FORMULARIOS ==========

        // LicenciaForm: opcional licenciaId
        composable(
            route = "${Routes.LICENCIA_FORM}?licenciaId={licenciaId}",
            arguments = listOf(
                navArgument("licenciaId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val licenciaId = backStackEntry.arguments?.getInt("licenciaId")?.takeIf { it != -1 }
            LicenciaFormContent(
                licenciaId = licenciaId,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }

        // GuiaForm: tipoLicencia obligatorio, guiaId opcional
        composable(
            route = "${Routes.GUIA_FORM}/{tipoLicencia}?guiaId={guiaId}",
            arguments = listOf(
                navArgument("tipoLicencia") { type = NavType.StringType },
                navArgument("guiaId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val tipoLicencia = backStackEntry.arguments?.getString("tipoLicencia") ?: ""
            val guiaId = backStackEntry.arguments?.getInt("guiaId")?.takeIf { it != -1 }
            GuiaFormContent(
                tipoLicencia = tipoLicencia,
                guiaId = guiaId,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }

        // CompraForm: guiaId, cupoDisponible, cupoTotal obligatorios, compraId opcional
        composable(
            route = "${Routes.COMPRA_FORM}/{guiaId}/{cupoDisponible}/{cupoTotal}?compraId={compraId}",
            arguments = listOf(
                navArgument("guiaId") { type = NavType.IntType },
                navArgument("cupoDisponible") { type = NavType.IntType },
                navArgument("cupoTotal") { type = NavType.IntType },
                navArgument("compraId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val guiaId = backStackEntry.arguments?.getInt("guiaId") ?: 0
            val cupoDisponible = backStackEntry.arguments?.getInt("cupoDisponible") ?: 0
            val cupoTotal = backStackEntry.arguments?.getInt("cupoTotal") ?: 0
            val compraId = backStackEntry.arguments?.getInt("compraId")?.takeIf { it != -1 }
            CompraFormContent(
                guiaId = guiaId,
                compraId = compraId,
                cupoDisponible = cupoDisponible,
                cupoTotal = cupoTotal,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }

        // TiradaForm: opcional tiradaId
        composable(
            route = "${Routes.TIRADA_FORM}?tiradaId={tiradaId}",
            arguments = listOf(
                navArgument("tiradaId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val tiradaId = backStackEntry.arguments?.getInt("tiradaId")?.takeIf { it != -1 }
            TiradaFormContent(
                tiradaId = tiradaId,
                navController = navController,
                snackbarHostState = snackbarHostState,
                onRegisterSaveCallback = onRegisterSaveCallback
            )
        }
    }
}
