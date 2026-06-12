package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.ads.AdsBootstrap
import al.ahgitdevelopment.municion.auth.SocialAuthActivityHolder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        SocialAuthActivityHolder.set(this)

        // UMP consent + MobileAds init + native ad preload (needs an Activity). No-op if ads removed.
        AdsBootstrap.gatherConsentAndInitializeAds(this)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        SocialAuthActivityHolder.clear(this)
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
