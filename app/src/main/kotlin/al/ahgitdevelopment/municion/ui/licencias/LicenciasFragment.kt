package al.ahgitdevelopment.municion.ui.licencias

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.forms.LicenciaFormActivity
import al.ahgitdevelopment.municion.databinding.FragmentLicenciasBinding
import al.ahgitdevelopment.municion.managers.CalendarManager
import al.ahgitdevelopment.municion.ui.viewmodel.LicenciaViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import al.ahgitdevelopment.municion.datamodel.Licencia as LegacyLicencia

/**
 * Fragment para listar Licencias con RecyclerView
 *
 * FASE 4: Migración UI a Kotlin
 * - RecyclerView moderno con ViewHolder pattern
 * - ViewBinding
 * - StateFlow observables
 * - Integración con CalendarManager
 *
 * Reemplaza el tab de Licencias en FragmentMainActivity
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@AndroidEntryPoint
class LicenciasFragment : Fragment() {

    companion object {
        private const val LICENCIA_COMPLETED = 300
    }

    private var _binding: FragmentLicenciasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LicenciaViewModel by viewModels()

    @Inject
    lateinit var calendarManager: CalendarManager

    private lateinit var adapter: LicenciasAdapter

    // ActivityResultLauncher para capturar resultado del formulario legacy
    private val licenciaFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleLicenciaFormResult(result.resultCode, result.data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLicenciasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeLicencias()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter = LicenciasAdapter(
            onItemClick = { licencia ->
                // TODO: Navigate to edit form
                Snackbar.make(binding.root, "Editar licencia: ${licencia.numLicencia}", Snackbar.LENGTH_SHORT).show()
            },
            onCalendarClick = { licencia ->
                // Create calendar events for license expiration
                viewLifecycleOwner.lifecycleScope.launch {
                    if (calendarManager.hasCalendarPermission()) {
                        calendarManager.createLicenseExpirationEvents(licencia)
                            .onSuccess {
                                Snackbar.make(
                                    binding.root,
                                    "Eventos de calendario creados",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                            .onFailure { error ->
                                Snackbar.make(
                                    binding.root,
                                    "Error creando eventos: ${error.message}",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Se necesitan permisos de calendario",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LicenciasFragment.adapter
            setHasFixedSize(true)
        }

        // Swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val licencia = adapter.currentList[position]
                showDeleteDialog(licencia)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            // Launch legacy LicenciaFormActivity usando launcher para capturar resultado
            licenciaFormLauncher.launch(Intent(requireContext(), LicenciaFormActivity::class.java))
        }
    }

    /**
     * Maneja el resultado del formulario legacy de Licencia
     */
    private fun handleLicenciaFormResult(resultCode: Int, data: Intent?) {
        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            // Obtener Licencia legacy del Intent
            val legacyLicencia = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableExtra("modify_licencia", LegacyLicencia::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableExtra("modify_licencia") as? LegacyLicencia
            }

            legacyLicencia?.let { legacy ->
                // Convertir legacy Licencia a Room Licencia y guardar
                val roomLicencia = convertLegacyToRoom(legacy)
                viewModel.saveLicencia(roomLicencia)
            }
        }
    }

    /**
     * Convierte una Licencia legacy (Java) a Licencia Room (Kotlin)
     */
    private fun convertLegacyToRoom(legacy: LegacyLicencia): Licencia {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = dateFormat.format(Date())

        return Licencia(
            id = 0,  // Room auto-generará el ID
            tipo = legacy.tipo.coerceAtLeast(0),
            nombre = legacy.nombre,
            tipoPermisoConduccion = legacy.tipoPermisoConduccion,
            edad = legacy.edad.coerceAtLeast(1),
            fechaExpedicion = legacy.fechaExpedicion.ifBlank { today },
            fechaCaducidad = legacy.fechaCaducidad.ifBlank { today },
            numLicencia = legacy.numLicencia.ifBlank { "SIN-NUMERO" },
            numAbonado = legacy.numAbonado,
            numSeguro = legacy.numSeguro,
            autonomia = legacy.autonomia,
            escala = legacy.escala,
            categoria = legacy.categoria
        )
    }

    private fun observeLicencias() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.licencias.collect { licencias ->
                    adapter.submitList(licencias)

                    // Show/hide empty state
                    if (licencias.isEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.emptyStateText.text = "No hay licencias registradas"
                    } else {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.emptyStateText.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LicenciaViewModel.LicenciaUiState.Idle -> {
                            // Do nothing
                        }
                        is LicenciaViewModel.LicenciaUiState.Loading -> {
                            // Show loading indicator if needed
                        }
                        is LicenciaViewModel.LicenciaUiState.Success -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            viewModel.resetUiState()
                        }
                        is LicenciaViewModel.LicenciaUiState.Error -> {
                            Snackbar.make(binding.root, "Error: ${state.message}", Snackbar.LENGTH_LONG).show()
                            viewModel.resetUiState()
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(licencia: Licencia) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar licencia")
            .setMessage("¿Estás seguro de que deseas eliminar la licencia ${licencia.numLicencia}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    // Delete calendar events first
                    if (calendarManager.hasCalendarPermission()) {
                        calendarManager.deleteLicenseCalendarEvents(licencia)
                    }
                    // Then delete licencia
                    viewModel.deleteLicencia(licencia)
                }
            }
            .setNegativeButton("Cancelar") { _, _ ->
                // Restore item in RecyclerView
                adapter.notifyItemChanged(adapter.currentList.indexOf(licencia))
            }
            .setOnCancelListener {
                // Restore item in RecyclerView
                adapter.notifyItemChanged(adapter.currentList.indexOf(licencia))
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
