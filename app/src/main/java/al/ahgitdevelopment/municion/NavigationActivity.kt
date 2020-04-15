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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.compras_fragment, R.id.licencias_fragment
//                        ,
//                        R.id.listViewFragmentLicencias, R.id.listViewFragmentTiradas
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
                R.id.compras_fragment -> {
                    setToolbarSubtitle(getString(R.string.section_compras_title))
                    nav_view.visibility = View.VISIBLE
                }
                R.id.licencias_fragment -> {
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