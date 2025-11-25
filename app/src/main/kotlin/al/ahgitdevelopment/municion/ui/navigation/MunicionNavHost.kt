package al.ahgitdevelopment.municion.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import al.ahgitdevelopment.municion.ui.compras.ComprasScreen
import al.ahgitdevelopment.municion.ui.forms.CompraFormScreen
import al.ahgitdevelopment.municion.ui.forms.GuiaFormScreen
import al.ahgitdevelopment.municion.ui.forms.LicenciaFormScreen
import al.ahgitdevelopment.municion.ui.forms.TiradaFormScreen
import al.ahgitdevelopment.municion.ui.guias.GuiasScreen
import al.ahgitdevelopment.municion.ui.licencias.LicenciasScreen
import al.ahgitdevelopment.municion.ui.settings.AccountSettingsScreen
import al.ahgitdevelopment.municion.ui.tiradas.TiradasScreen

/**
 * NavHost principal de la aplicación Munición.
 *
 * Contiene todas las rutas de navegación:
 * - 4 tabs principales (Licencias, Guías, Compras, Tiradas)
 * - 4 formularios para crear/editar entidades
 * - Settings
 *
 * @param navController Controlador de navegación
 * @param innerPadding Padding interno del Scaffold
 * @param modifier Modificador opcional
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun MunicionNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LICENCIAS,
        modifier = modifier.padding(innerPadding)
    ) {
        // ========== TABS PRINCIPALES ==========

        composable(route = Routes.LICENCIAS) {
            LicenciasScreen(navController = navController)
        }

        composable(route = Routes.GUIAS) {
            GuiasScreen(navController = navController)
        }

        composable(route = Routes.COMPRAS) {
            ComprasScreen(navController = navController)
        }

        composable(route = Routes.TIRADAS) {
            TiradasScreen(navController = navController)
        }

        composable(route = Routes.SETTINGS) {
            AccountSettingsScreen(navController = navController)
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
            LicenciaFormScreen(
                licenciaId = licenciaId,
                navController = navController
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
            GuiaFormScreen(
                tipoLicencia = tipoLicencia,
                guiaId = guiaId,
                navController = navController
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
            CompraFormScreen(
                guiaId = guiaId,
                compraId = compraId,
                cupoDisponible = cupoDisponible,
                cupoTotal = cupoTotal,
                navController = navController
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
            TiradaFormScreen(
                tiradaId = tiradaId,
                navController = navController
            )
        }
    }
}
