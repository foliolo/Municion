package al.ahgitdevelopment.municion

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.guiasFragment, R.id.comprasFragment, R.id.licenciasFragment, R.id.tiradasFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view?.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = getString(R.string.app_name)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            setToolbarSubtitle("")

            when (destination.id) {
                R.id.loginPasswordFragment -> {
                    setToolbarSubtitle(getString(R.string.login))
                    nav_view.visibility = View.GONE
                }
                R.id.guiasFragment -> {
                    nav_view.visibility = View.VISIBLE
                }
                R.id.comprasFragment -> {
                    nav_view.visibility = View.VISIBLE
                }
                R.id.licenciasFragment -> {
                    nav_view.visibility = View.VISIBLE
                }
                R.id.tiradasFragment -> {
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