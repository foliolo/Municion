package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.app_name
import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource

/**
 * Root composable of the shared UI.
 *
 * Placeholder during the KMP migration: real navigation (auth gate, bottom-nav tabs and
 * entity forms) is wired up in later phases. Already exercises the shared theme and the
 * Compose resources pipeline (string + locale qualifiers) to validate library-resource
 * packaging into the consuming apps (CMP-9547).
 */
@Composable
fun App() {
    MunicionTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(Res.string.app_name))
            }
        }
    }
}
