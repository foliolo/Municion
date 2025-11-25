package al.ahgitdevelopment.municion.ui

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.ActivityMainBinding
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel
import android.Manifest
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
            showSnackbar("Permiso de calendario concedido")
        } else {
            showSnackbar("Permiso de calendario denegado")
        }
    }

    // Track active dialogs to prevent WindowLeaked
    private var activeDialog: AlertDialog? = null

    // Track if calendar permission was already requested this session
    private var calendarPermissionRequested = false

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

        // Observe UI state - this handles auth and triggers other actions
        observeUiState()
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
                                // User authenticated - NOW it's safe to:
                                // 1. Request permissions (user will stay in this activity)
                                // 2. Sync data
                                onUserAuthenticated()
                            }

                            is MainViewModel.MainUiState.Unauthenticated -> {
                                // Redirect to login - dismiss any dialogs first
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
                                showSnackbar("Sincronizando con Firebase...")
                            }

                            is MainViewModel.SyncState.Success -> {
                                showSnackbar("Sincronización exitosa (${state.count} items)")
                            }

                            is MainViewModel.SyncState.Error -> {
                                showSnackbar("Error de sincronización: ${state.message}")
                            }

                            is MainViewModel.SyncState.PartialSuccess -> {
                                showSnackbar("Sincronización parcial (${state.count} items)")
                            }

                            is MainViewModel.SyncState.SuccessWithParseErrors ->
                                showSnackbar("Sincronización con errores en ${state.count} campos)")
                        }
                    }
                }
            }
        }
    }

    /**
     * Called when user is confirmed to be authenticated.
     * This is the safe place to request permissions and sync data.
     */
    private fun onUserAuthenticated() {
        // Check calendar permissions only once per session and only when authenticated
        if (!calendarPermissionRequested) {
            calendarPermissionRequested = true
            checkCalendarPermission()
        }

        // Trigger sync
        mainViewModel.syncFromFirebase()
    }

    /**
     * Request calendar permission if needed.
     * Only called when user is authenticated to avoid WindowLeaked issues.
     */
    private fun checkCalendarPermission() {
        // Safety check - don't show dialogs if activity is finishing
        if (isFinishing || isDestroyed) return

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Mostrar explicación si es necesario
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)) {
                showCalendarPermissionDialog()
            } else {
                calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
            }
        }
    }

    private fun showCalendarPermissionDialog() {
        // Safety check
        if (isFinishing || isDestroyed) return

        dismissActiveDialog()
        activeDialog = AlertDialog.Builder(this)
            .setTitle("Permiso de calendario")
            .setMessage("La app necesita acceso al calendario para crear recordatorios de caducidad de licencias")
            .setPositiveButton("Permitir") { _, _ ->
                calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
            }
            .setNegativeButton("Cancelar", null)
            .setOnDismissListener { activeDialog = null }
            .create()
        activeDialog?.show()
    }

    private fun navigateToLogin() {
        // CRITICAL: Dismiss any dialogs BEFORE finishing the activity
        dismissActiveDialog()

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
                navController.navigate(R.id.accountSettingsFragment)
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
        if (isFinishing || isDestroyed) return

        dismissActiveDialog()
        activeDialog = AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                navigateToLogin()
            }
            .setNegativeButton("No", null)
            .setOnDismissListener { activeDialog = null }
            .create()
        activeDialog?.show()
    }

    /**
     * Helper to show snackbar safely
     */
    private fun showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        if (!isFinishing && !isDestroyed) {
            Snackbar.make(binding.root, message, duration).show()
        }
    }

    /**
     * Dismiss any active dialog to prevent WindowLeaked
     */
    private fun dismissActiveDialog() {
        activeDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        activeDialog = null
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        // Dismiss any active dialog to prevent WindowLeaked
        dismissActiveDialog()
        super.onDestroy()
    }
}
