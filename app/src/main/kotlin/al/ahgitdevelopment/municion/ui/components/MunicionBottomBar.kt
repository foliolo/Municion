package al.ahgitdevelopment.municion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import al.ahgitdevelopment.municion.ui.navigation.Routes
import al.ahgitdevelopment.municion.ui.theme.BottomNavBackground
import al.ahgitdevelopment.municion.ui.theme.BottomNavItemActive
import al.ahgitdevelopment.municion.ui.theme.BottomNavItemInactive

/**
 * Datos para cada item del BottomNavigationBar
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * BottomNavigationBar de la aplicación Munición.
 *
 * Muestra 4 tabs principales:
 * - Licencias
 * - Guías
 * - Compras
 * - Tiradas
 *
 * @param navController Controlador de navegación
 *
 * @since v3.0.0 (Compose Migration)
 */
@Composable
fun MunicionBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(Routes.LICENCIAS, Icons.Default.Badge, "Licencias"),
        BottomNavItem(Routes.GUIAS, Icons.Default.Security, "Guías"),
        BottomNavItem(Routes.COMPRAS, Icons.Default.ShoppingCart, "Compras"),
        BottomNavItem(Routes.TIRADAS, Icons.Default.SportsScore, "Tiradas")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Solo mostrar BottomBar en las pantallas principales (no en formularios)
    val showBottomBar = items.any { it.route == currentRoute }

    if (showBottomBar) {
        NavigationBar(
            containerColor = BottomNavBackground,
            // Edge-to-edge: BottomBar handles navigation bar inset
            windowInsets = NavigationBarDefaults.windowInsets
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = { Text(item.label) },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BottomNavItemActive,
                        selectedTextColor = BottomNavItemActive,
                        unselectedIconColor = BottomNavItemInactive,
                        unselectedTextColor = BottomNavItemInactive,
                        indicatorColor = BottomNavBackground
                    )
                )
            }
        }
    }
}
