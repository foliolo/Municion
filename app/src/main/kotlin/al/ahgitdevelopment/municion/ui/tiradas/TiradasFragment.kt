package al.ahgitdevelopment.municion.ui.tiradas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.forms.TiradaFormActivity
import al.ahgitdevelopment.municion.databinding.FragmentTiradasBinding
import al.ahgitdevelopment.municion.ui.viewmodel.TiradaViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para listar Tiradas con RecyclerView
 *
 * FASE 4: Migración UI a Kotlin
 * - RecyclerView moderno con ViewHolder pattern
 * - ViewBinding
 * - StateFlow observables
 * - Swipe to delete
 *
 * Reemplaza el tab de Tiradas en FragmentMainActivity
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@AndroidEntryPoint
class TiradasFragment : Fragment() {

    private var _binding: FragmentTiradasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TiradaViewModel by viewModels()

    private lateinit var adapter: TiradasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTiradasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeTiradas()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter = TiradasAdapter(
            onItemClick = { tirada ->
                // TODO: Navigate to edit form
                Snackbar.make(binding.root, "Editar tirada: ${tirada.descripcion}", Snackbar.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TiradasFragment.adapter
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
                val tirada = adapter.currentList[position]
                showDeleteDialog(tirada)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            // Launch legacy TiradaFormActivity
            startActivity(Intent(requireContext(), TiradaFormActivity::class.java))
        }
    }

    private fun observeTiradas() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tiradas.collect { tiradas ->
                    adapter.submitList(tiradas)

                    // Show/hide empty state
                    if (tiradas.isEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.emptyStateText.text = "No hay tiradas registradas"
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
                        is TiradaViewModel.TiradaUiState.Idle -> {
                            // Do nothing
                        }
                        is TiradaViewModel.TiradaUiState.Loading -> {
                            // Show loading indicator if needed
                        }
                        is TiradaViewModel.TiradaUiState.Success -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            viewModel.resetUiState()
                        }
                        is TiradaViewModel.TiradaUiState.Error -> {
                            Snackbar.make(binding.root, "Error: ${state.message}", Snackbar.LENGTH_LONG).show()
                            viewModel.resetUiState()
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(tirada: Tirada) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar tirada")
            .setMessage("¿Estás seguro de que deseas eliminar la tirada ${tirada.descripcion}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteTirada(tirada)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                // Restore item in RecyclerView
                adapter.notifyItemChanged(adapter.currentList.indexOf(tirada))
            }
            .setOnCancelListener {
                // Restore item in RecyclerView
                adapter.notifyItemChanged(adapter.currentList.indexOf(tirada))
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
