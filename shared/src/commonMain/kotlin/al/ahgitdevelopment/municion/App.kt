package al.ahgitdevelopment.municion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Root composable of the shared UI.
 *
 * Placeholder during the KMP migration: real navigation (auth gate, bottom-nav tabs and
 * entity forms) is wired up in later phases (see migration plan). Kept minimal so both
 * platform entry points ([MainViewController] on iOS and `MainActivity` on Android) compile.
 */
@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Munición")
            }
        }
    }
}
