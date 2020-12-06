package al.ahgitdevelopment.municion

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.propertiesFragment,
                R.id.purchasesFragment,
                R.id.licensesFragment,
                R.id.competitionsFragment
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
                    setToolbarSubtitle(getString(R.string.section_properties_title))
                    nav_view.visibility = View.VISIBLE
                }
                R.id.purchasesFragment -> {
                    setToolbarSubtitle(getString(R.string.section_purchases_title))
                    nav_view.visibility = View.VISIBLE
                }
                R.id.licensesFragment -> {
                    setToolbarSubtitle(getString(R.string.section_licenses_title))
                    nav_view.visibility = View.VISIBLE
                }
                R.id.competitionsFragment -> {
                    setToolbarSubtitle(getString(R.string.section_competitions_title))
                    nav_view.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setToolbarSubtitle(subtitle: String) {
        supportActionBar?.subtitle = subtitle
    }
}
