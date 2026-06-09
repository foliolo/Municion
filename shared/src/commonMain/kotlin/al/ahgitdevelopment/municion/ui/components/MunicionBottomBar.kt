package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.outline_social_leaderboard_24
import al.ahgitdevelopment.municion.resources.section_competiciones_title
import al.ahgitdevelopment.municion.resources.section_compras_title
import al.ahgitdevelopment.municion.resources.section_guias_title
import al.ahgitdevelopment.municion.resources.section_licencias_title
import al.ahgitdevelopment.municion.ui.navigation.Compras
import al.ahgitdevelopment.municion.ui.navigation.Guias
import al.ahgitdevelopment.municion.ui.navigation.Licencias
import al.ahgitdevelopment.municion.ui.navigation.Route
import al.ahgitdevelopment.municion.ui.navigation.Tiradas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

sealed interface IconSource {
    data class Vector(
        val imageVector: ImageVector,
    ) : IconSource

    data class Drawable(
        val resource: DrawableResource,
    ) : IconSource
}

data class BottomNavItem(
    val route: Route,
    val icon: IconSource,
    val label: StringResource,
)

/** Bottom navigation with the four main tabs. Hidden on form/settings screens. */
@Composable
fun MunicionBottomBar(navController: NavHostController) {
    val items =
        listOf(
            BottomNavItem(Licencias, IconSource.Vector(Icons.Default.Badge), Res.string.section_licencias_title),
            BottomNavItem(Guias, IconSource.Vector(Icons.Default.Security), Res.string.section_guias_title),
            BottomNavItem(Compras, IconSource.Vector(Icons.Default.ShoppingCart), Res.string.section_compras_title),
            BottomNavItem(Tiradas, IconSource.Drawable(Res.drawable.outline_social_leaderboard_24), Res.string.section_competiciones_title),
        )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = items.any { it.route::class.qualifiedName == currentRoute }
    if (!showBottomBar) return

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        windowInsets = NavigationBarDefaults.windowInsets,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    when (val source = item.icon) {
                        is IconSource.Vector -> Icon(source.imageVector, contentDescription = stringResource(item.label))
                        is IconSource.Drawable -> Icon(painterResource(source.resource), contentDescription = stringResource(item.label))
                    }
                },
                label = {
                    Text(stringResource(item.label), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                },
                selected = currentRoute == item.route::class.qualifiedName,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
            )
        }
    }
}
