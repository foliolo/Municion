package al.ahgitdevelopment.municion.ui.compras

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
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.forms.CompraFormActivity
import al.ahgitdevelopment.municion.databinding.FragmentComprasBinding
import al.ahgitdevelopment.municion.ui.viewmodel.CompraViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import al.ahgitdevelopment.municion.datamodel.Compra as LegacyCompra

/**
 * Fragment para listar Compras con RecyclerView
 *
 * FASE 4: Migración UI a Kotlin
 * - RecyclerView moderno con ViewHolder pattern
 * - ViewBinding
 * - StateFlow observables
 * - Swipe to delete
 *
 * Reemplaza el tab de Compras en FragmentMainActivity
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@AndroidEntryPoint
class ComprasFragment : Fragment() {

    companion object {
        private const val COMPRA_COMPLETED = 100
    }

    private var _binding: FragmentComprasBinding? = null
    private val binding get() = _binding!!

    private val compraViewModel: CompraViewModel by viewModels()
    private val guiaViewModel: GuiaViewModel by viewModels()

    private lateinit var adapter: ComprasAdapter

    // ActivityResultLauncher para capturar resultado del formulario legacy
    private val compraFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleCompraFormResult(result.resultCode, result.data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComprasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeCompras()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter = ComprasAdapter(
            onItemClick = { compra ->
                // TODO: Navigate to edit form
                Snackbar.make(binding.root, "Editar compra de ${compra.marca}", Snackbar.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ComprasFragment.adapter
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
                val compra = adapter.currentList[position]
                showDeleteDialog(compra)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                // Check if user has guías first
                guiaViewModel.guias.value.let { guias ->
                    if (guias.isEmpty()) {
                        Snackbar.make(
                            binding.root,
                            R.string.compra_empty_list,
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        // Launch legacy CompraFormActivity usando launcher para capturar resultado
                        compraFormLauncher.launch(Intent(requireContext(), CompraFormActivity::class.java))
                    }
                }
            }
        }
    }

    /**
     * Maneja el resultado del formulario legacy de Compra
     */
    private fun handleCompraFormResult(resultCode: Int, data: Intent?) {
        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            // Obtener Compra legacy del Intent
            val legacyCompra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableExtra("modify_compra", LegacyCompra::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableExtra("modify_compra") as? LegacyCompra
            }

            legacyCompra?.let { legacy: LegacyCompra ->
                // Convertir legacy Compra a Room Compra y guardar
                val roomCompra = convertLegacyToRoom(legacy)
                compraViewModel.createCompra(roomCompra)
            }
        }
    }

    /**
     * Convierte una Compra legacy (Java) a Compra Room (Kotlin)
     */
    private fun convertLegacyToRoom(legacy: LegacyCompra): Compra {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = dateFormat.format(Date())

        return Compra(
            id = 0,  // Room auto-generará el ID
            idPosGuia = legacy.idPosGuia,
            calibre1 = legacy.calibre1.ifBlank { "Sin calibre" },
            calibre2 = legacy.calibre2,
            unidades = legacy.unidades.coerceAtLeast(1),
            precio = legacy.precio.coerceAtLeast(0.0),
            fecha = legacy.fecha.ifBlank { today },
            tipo = legacy.tipo.ifBlank { "Sin tipo" },
            peso = legacy.peso.coerceAtLeast(1),
            marca = legacy.marca.ifBlank { "Sin marca" },
            tienda = legacy.tienda,
            valoracion = legacy.valoracion.coerceIn(0f, 5f),
            imagePath = legacy.imagePath
        )
    }

    private fun observeCompras() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                compraViewModel.compras.collect { compras ->
                    adapter.submitList(compras)

                    // Show/hide empty state
                    if (compras.isEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.emptyStateText.text = "No hay compras registradas"
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
                compraViewModel.uiState.collect { state ->
                    when (state) {
                        is CompraViewModel.CompraUiState.Idle -> {
                            // Do nothing
                        }
                        is CompraViewModel.CompraUiState.Loading -> {
                            // Show loading indicator if needed
                        }
                        is CompraViewModel.CompraUiState.Success -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            compraViewModel.resetUiState()
                        }
                        is CompraViewModel.CompraUiState.Error -> {
                            Snackbar.make(binding.root, "Error: ${state.message}", Snackbar.LENGTH_LONG).show()
                            compraViewModel.resetUiState()
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(compra: Compra) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar compra")
            .setMessage("¿Estás seguro de que deseas eliminar esta compra de ${compra.marca}?")
            .setPositiveButton("Eliminar") { _, _ ->
                compraViewModel.deleteCompra(compra)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                // Restore item in RecyclerView
                adapter.notifyItemChanged(adapter.currentList.indexOf(compra))
            }
            .setOnCancelListener {
                // Restore item in RecyclerView
                adapter.notifyItemChanged(adapter.currentList.indexOf(compra))
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
