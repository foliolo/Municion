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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import al.ahgitdevelopment.municion.datamodel.Compra as LegacyCompra
import al.ahgitdevelopment.municion.datamodel.Guia as LegacyGuia
import al.ahgitdevelopment.municion.data.local.room.entities.Guia as RoomGuia

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

    // Variable para trackear si estamos editando o creando
    private var editingCompra: Compra? = null

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
                // Click simple: mostrar info breve
                Snackbar.make(binding.root, "${compra.marca} - ${compra.unidades} uds.", Snackbar.LENGTH_SHORT).show()
            },
            onItemLongClick = { compra ->
                // Long-press: abrir formulario de edición
                launchEditCompraForm(compra)
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
            showGuiaSelectionDialog()
        }
    }

    /**
     * Muestra un diálogo para seleccionar la guía antes de crear una compra.
     * CompraFormActivity requiere saber la guía asociada para pre-cargar calibres.
     *
     * Usa coroutine con timeout para esperar a que las guías se carguen,
     * resolviendo el race condition donde guias.value está vacío antes del sync.
     */
    private fun showGuiaSelectionDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Esperar hasta 2 segundos para que se carguen las guías
            // Si hay timeout, usar el valor actual (fallback)
            val guias = withTimeoutOrNull(2000L) {
                guiaViewModel.guias.first { it.isNotEmpty() }
            } ?: guiaViewModel.guias.value

            if (guias.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    "No hay guías disponibles. Primero debes crear una guía.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@launch
            }

            // Crear array de nombres de guías para el diálogo
            val guiaNames = guias.mapIndexed { index, guia ->
                "${guia.marca} ${guia.modelo} (${guia.calibre1})"
            }.toTypedArray()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Selecciona una guía")
                .setItems(guiaNames) { _, which ->
                    // Lanzar CompraFormActivity con la guía seleccionada
                    val selectedGuia = guias[which]
                    val legacyGuia = convertRoomToLegacyGuia(selectedGuia)

                    val intent = Intent(requireContext(), CompraFormActivity::class.java).apply {
                        // IMPORTANTE: Pasar el ID de Room, NO el índice de la lista
                        // CreateCompraUseCase busca por getGuiaById(idPosGuia)
                        putExtra("position_guia", selectedGuia.id)
                        putExtra("guia", legacyGuia)
                        // Pasar información de cupo para validación client-side
                        putExtra("cupo_disponible", selectedGuia.disponible())
                        putExtra("cupo_total", selectedGuia.cupo)
                    }
                    compraFormLauncher.launch(intent)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    /**
     * Lanza el formulario de edición para una compra existente.
     * El formulario legacy detecta modo edición cuando NO recibe "position_guia".
     */
    private fun launchEditCompraForm(compra: Compra) {
        editingCompra = compra
        val legacyCompra = convertRoomToLegacyCompra(compra)

        // Obtener la guía asociada para pasar info de cupo
        val guia = guiaViewModel.guias.value.find { it.id == compra.idPosGuia }

        val intent = Intent(requireContext(), CompraFormActivity::class.java).apply {
            // NO pasar "position_guia" para que detecte modo edición
            putExtra("modify_compra", legacyCompra)
            putExtra("position", compra.id)  // Usar ID de Room

            // Pasar cupo para validación (añadir unidades actuales porque se "devuelven")
            guia?.let {
                putExtra("cupo_disponible", it.disponible() + compra.unidades)
                putExtra("cupo_total", it.cupo)
            }
        }
        compraFormLauncher.launch(intent)
    }

    /**
     * Convierte una Compra Room (Kotlin) a Compra legacy (Java) para pasarla a CompraFormActivity
     */
    private fun convertRoomToLegacyCompra(room: Compra): LegacyCompra {
        val legacy = LegacyCompra()
        legacy.idPosGuia = room.idPosGuia
        legacy.calibre1 = room.calibre1
        legacy.calibre2 = room.calibre2 ?: ""
        legacy.unidades = room.unidades
        legacy.precio = room.precio
        legacy.fecha = room.fecha
        legacy.tipo = room.tipo
        legacy.peso = room.peso
        legacy.marca = room.marca
        legacy.tienda = room.tienda ?: ""
        legacy.valoracion = room.valoracion
        legacy.imagePath = room.imagePath
        return legacy
    }

    /**
     * Convierte una Guia Room (Kotlin) a Guia legacy (Java) para pasarla a CompraFormActivity
     */
    private fun convertRoomToLegacyGuia(roomGuia: RoomGuia): LegacyGuia {
        val legacyGuia = LegacyGuia()
        legacyGuia.id = roomGuia.id
        legacyGuia.idCompra = roomGuia.idCompra
        legacyGuia.tipoLicencia = roomGuia.tipoLicencia
        legacyGuia.marca = roomGuia.marca
        legacyGuia.modelo = roomGuia.modelo
        legacyGuia.apodo = roomGuia.apodo
        legacyGuia.tipoArma = roomGuia.tipoArma
        legacyGuia.calibre1 = roomGuia.calibre1
        legacyGuia.calibre2 = roomGuia.calibre2 ?: ""
        legacyGuia.numGuia = roomGuia.numGuia
        legacyGuia.numArma = roomGuia.numArma
        legacyGuia.cupo = roomGuia.cupo
        legacyGuia.gastado = roomGuia.gastado
        legacyGuia.imagePath = roomGuia.imagePath
        return legacyGuia
    }

    /**
     * Maneja el resultado del formulario legacy de Compra.
     * Distingue entre crear (editingCompra == null) y actualizar (editingCompra != null).
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
                val roomCompra = convertLegacyToRoom(legacy)

                if (editingCompra != null) {
                    // EDICIÓN: mantener ID original y llamar update
                    val updatedCompra = roomCompra.copy(id = editingCompra!!.id)
                    compraViewModel.updateCompra(updatedCompra)
                } else {
                    // CREACIÓN: nuevo item
                    compraViewModel.createCompra(roomCompra)
                }
            }
        }
        // Limpiar siempre el estado de edición
        editingCompra = null
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
                    // Protección: verificar que binding no sea null
                    val rootView = _binding?.root ?: return@collect

                    when (state) {
                        is CompraViewModel.CompraUiState.Idle -> {
                            // Do nothing
                        }
                        is CompraViewModel.CompraUiState.Loading -> {
                            // Show loading indicator if needed
                        }
                        is CompraViewModel.CompraUiState.Success -> {
                            Snackbar.make(rootView, state.message, Snackbar.LENGTH_SHORT).show()
                            compraViewModel.resetUiState()
                        }
                        is CompraViewModel.CompraUiState.Error -> {
                            // Mostrar mensaje de error más amigable
                            val userFriendlyMessage = formatErrorMessage(state.message)
                            Snackbar.make(rootView, userFriendlyMessage, Snackbar.LENGTH_LONG).show()
                            compraViewModel.resetUiState()
                        }
                    }
                }
            }
        }
    }

    /**
     * Convierte mensajes de error técnicos a mensajes amigables para el usuario
     */
    private fun formatErrorMessage(message: String): String {
        return when {
            message.contains("Cupo insuficiente") -> {
                // Extraer números del mensaje técnico
                val regex = Regex("Disponible: (\\d+), Requerido: (\\d+)")
                val match = regex.find(message)
                if (match != null) {
                    val disponible = match.groupValues[1]
                    val requerido = match.groupValues[2]
                    "No puedes comprar $requerido unidades. Solo tienes $disponible de cupo disponible."
                } else {
                    "Cupo insuficiente para esta compra."
                }
            }
            message.contains("Guía no encontrada") -> {
                "Error: La guía seleccionada ya no existe."
            }
            else -> "Error: $message"
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
