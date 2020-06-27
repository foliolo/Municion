package al.ahgitdevelopment.municion

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_navigation.*
import javax.inject.Inject
import javax.inject.Provider

class NavigationActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.comprasFragment, R.id.licenciasFragment
                )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view?.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = getString(R.string.app_name)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            when (destination.id) {
                R.id.loginPasswordFragment -> {
                    setToolbarSubtitle(getString(R.string.login))
                    nav_view.visibility = View.GONE
                }
                R.id.comprasFragment -> {
                    setToolbarSubtitle(getString(R.string.section_compras_title))
                    nav_view.visibility = View.VISIBLE
                }
                R.id.licenciasFragment -> {
                    setToolbarSubtitle(getString(R.string.section_licencias_title))
                    nav_view.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setToolbarSubtitle(subtitle: String) {
        supportActionBar?.subtitle = subtitle
    }
}

//https://developer.android.com/guide/navigation/navigation-migrate#integrate