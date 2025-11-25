package al.ahgitdevelopment.municion.ui.guias

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
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.forms.GuiaFormActivity
import al.ahgitdevelopment.municion.databinding.FragmentGuiasBinding
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import al.ahgitdevelopment.municion.datamodel.Guia as LegacyGuia

/**
 * Fragment para listar Guías con RecyclerView
 *
 * FASE 4: Migración UI a Kotlin
 * - RecyclerView moderno con ViewHolder pattern
 * - ViewBinding
 * - StateFlow observables
 * - Swipe to delete
 *
 * Reemplaza el tab de Guías en FragmentMainActivity
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@AndroidEntryPoint
class GuiasFragment : Fragment() {

    companion object {
        private const val GUIA_COMPLETED = 200
    }

    private var _binding: FragmentGuiasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GuiaViewModel by viewModels()

    private lateinit var adapter: GuiasAdapter

    // ActivityResultLauncher para capturar resultado del formulario legacy
    private val guiaFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleGuiaFormResult(result.resultCode, result.data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuiasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeGuias()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter = GuiasAdapter(
            onItemClick = { guia ->
                // TODO: Navigate to edit form
                Snackbar.make(binding.root, "Editar guía: ${guia.numGuia}", Snackbar.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GuiasFragment.adapter
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
                val guia = adapter.currentList[position]
                showDeleteDialog(guia)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            // Launch legacy GuiaFormActivity usando launcher para capturar resultado
            guiaFormLauncher.launch(Intent(requireContext(), GuiaFormActivity::class.java))
        }
    }

    /**
     * Maneja el resultado del formulario legacy de Guia
     */
    private fun handleGuiaFormResult(resultCode: Int, data: Intent?) {
        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            // Obtener Guia legacy del Intent
            val legacyGuia = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableExtra("modify_guia", LegacyGuia::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableExtra("modify_guia") as? LegacyGuia
            }

            legacyGuia?.let { legacy ->
                // Convertir legacy Guia a Room Guia y guardar
                val roomGuia = convertLegacyToRoom(legacy)
                viewModel.saveGuia(roomGuia)
            }
        }
    }

    /**
     * Convierte una Guia legacy (Java) a Guia Room (Kotlin)
     */
    private fun convertLegacyToRoom(legacy: LegacyGuia): Guia {
        return Guia(
            id = 0,  // Room auto-generará el ID
            idCompra = legacy.idCompra,
            tipoLicencia = legacy.tipoLicencia,
            marca = legacy.marca.ifBlank { "Sin marca" },
            modelo = legacy.modelo.ifBlank { "Sin modelo" },
            apodo = legacy.apodo.ifBlank { "Sin apodo" },
            tipoArma = legacy.tipoArma,
            calibre1 = legacy.calibre1.ifBlank { "Sin calibre" },
            calibre2 = legacy.calibre2,
            numGuia = legacy.numGuia.ifBlank { "SIN-GUIA" },
            numArma = legacy.numArma.ifBlank { "SIN-ARMA" },
            cupo = legacy.cupo.coerceAtLeast(1),
            gastado = legacy.gastado.coerceAtLeast(0),
            imagePath = legacy.imagePath
        )
    }

    private fun observeGuias() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.guias.collect { guias ->
                    adapter.submitList(guias)

                    // Show/hide empty state
                    if (guias.isEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.emptyStateText.setText(R.string.guia_empty_list)
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
                        is GuiaViewModel.GuiaUiState.Idle -> {
                            // Do nothing
                        }
                        is GuiaViewModel.GuiaUiState.Loading -> {
                            // Show loading indicator if needed
                        }
                        is GuiaViewModel.GuiaUiState.Success -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            viewModel.resetUiState()
                        }
                        is GuiaViewModel.GuiaUiState.Error -> {
                            Snackbar.make(binding.root, "Error: ${state.message}", Snackbar.LENGTH_LONG).show()
                            viewModel.resetUiState()
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(guia: Guia) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar guía")
            .setMessage("¿Estás seguro de que deseas eliminar la guía ${guia.numGuia}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteGuia(guia)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                // Restore item in RecyclerView
                adapter.notifyItemChanged(adapter.currentList.indexOf(guia))
            }
            .setOnCancelListener {
                // Restore item in RecyclerView
                adapter.notifyItemChanged(adapter.currentList.indexOf(guia))
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
