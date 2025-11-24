package al.ahgitdevelopment.municion.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.ActivityMainBinding
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainActivity moderna con Navigation Component
 *
 * FASE 4: Migración UI a Kotlin
 * - Single Activity Architecture
 * - Navigation Component
 * - ViewBinding
 * - Hilt DI
 * - Zero static fields
 *
 * Reemplaza a FragmentMainActivity.java (1516 líneas de código legacy)
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // ViewBinding (reemplaza findViewById)
    private lateinit var binding: ActivityMainBinding

    // Navigation
    private lateinit var navController: NavController

    // ViewModels inyectados (reemplaza static fields)
    private val mainViewModel: MainViewModel by viewModels()

    // Firebase inyectado con Hilt
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var crashlytics: FirebaseCrashlytics

    // Calendar permission launcher
    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Snackbar.make(binding.root, "Permiso de calendario concedido", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "Permiso de calendario denegado", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setIcon(R.drawable.ic_bullseye)
        }

        // Setup Navigation
        setupNavigation()

        // Observe UI state
        observeUiState()

        // Check calendar permissions
        checkCalendarPermission()

        // Trigger initial sync
        mainViewModel.syncFromFirebase()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup bottom navigation with NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // Setup ActionBar with NavController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.guiasFragment,
                R.id.comprasFragment,
                R.id.licenciasFragment,
                R.id.tiradasFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe main UI state
                launch {
                    mainViewModel.uiState.collect { state ->
                        when (state) {
                            is MainViewModel.MainUiState.Loading -> {
                                // Show loading indicator if needed
                            }
                            is MainViewModel.MainUiState.Authenticated -> {
                                // User authenticated
                            }
                            is MainViewModel.MainUiState.Unauthenticated -> {
                                // Redirect to login
                                navigateToLogin()
                            }
                        }
                    }
                }

                // Observe sync state
                launch {
                    mainViewModel.syncState.collect { state ->
                        when (state) {
                            is MainViewModel.SyncState.Idle -> {
                                // Do nothing
                            }
                            is MainViewModel.SyncState.Syncing -> {
                                Snackbar.make(binding.root, "Sincronizando con Firebase...", Snackbar.LENGTH_SHORT).show()
                            }
                            is MainViewModel.SyncState.Success -> {
                                Snackbar.make(binding.root, "Sincronización exitosa (${state.count} items)", Snackbar.LENGTH_SHORT).show()
                            }
                            is MainViewModel.SyncState.Error -> {
                                Snackbar.make(binding.root, "Error de sincronización: ${state.message}", Snackbar.LENGTH_LONG).show()
                            }

                            is MainViewModel.SyncState.PartialSuccess -> {
                                // TODO
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkCalendarPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Mostrar explicación si es necesario
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)) {
                AlertDialog.Builder(this)
                    .setTitle("Permiso de calendario")
                    .setMessage("La app necesita acceso al calendario para crear recordatorios de caducidad de licencias")
                    .setPositiveButton("Permitir") { _, _ ->
                        calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
            }
        }
    }

    private fun navigateToLogin() {
        // TODO: Implement navigation to LoginActivity
        firebaseAuth.signOut()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                mainViewModel.syncFromFirebase()
                true
            }
            R.id.action_settings -> {
                // TODO: Navigate to settings
                Snackbar.make(binding.root, "Settings (TODO)", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                firebaseAuth.signOut()
                navigateToLogin()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
