package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.di.initKoin
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() =
    ComposeUIViewController(
        configure = { initKoin() },
    ) {
        App()
    }
