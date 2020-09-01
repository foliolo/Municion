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
                R.id.propertiesFragment, R.id.purchasesFragment, R.id.licensesFragment, R.id.competitionsFragment
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
                R.id.propertiesFragment -> {
                    nav_view.visibility = View.VISIBLE
                }
                R.id.purchasesFragment -> {
                    nav_view.visibility = View.VISIBLE
                }
                R.id.licensesFragment -> {
                    nav_view.visibility = View.VISIBLE
                }
                R.id.competitionsFragment -> {
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