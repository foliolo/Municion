package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.BaseFragment
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.CompetitionsFragmentBinding
import al.ahgitdevelopment.municion.ui.DeleteItemOnSwipe
import al.ahgitdevelopment.municion.ui.RecyclerInterface
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompetitionsFragment : BaseFragment(), RecyclerInterface {

    private lateinit var binding: CompetitionsFragmentBinding
    private lateinit var competitionAdapter: CompetitionAdapter

    private val viewModel: CompetitionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = CompetitionsFragmentBinding.inflate(layoutInflater, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToForm.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.competitionFormFragment)
        }

        viewModel.message.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { message ->
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(message),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }

        viewModel.exception.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { exception ->
                Toast.makeText(
                    requireContext(),
                    exception.message,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }

        viewModel.progressBar.observe(viewLifecycleOwner) {
            when (it.getContentIfNotHandled()) {
                true -> activity?.findViewById<ContentLoadingProgressBar>(R.id.progressBar)?.show()
                else -> activity?.findViewById<ContentLoadingProgressBar>(R.id.progressBar)?.hide()
            }
        }

        viewModel.competitions.observe(viewLifecycleOwner) {
            competitionAdapter = CompetitionAdapter().apply {
                submitList(it.sortedBy { it.description })
                setHasStableIds(true)
            }

            binding.competitionsRecyclerView.apply {
                adapter = competitionAdapter
                layoutManager = LinearLayoutManager(requireContext())

                ItemTouchHelper(
                    DeleteItemOnSwipe(
                        object : DeleteItemOnSwipe.DeleteCallback {
                            override fun deleteOnSwipe(viewHolder: RecyclerView.ViewHolder) {
                                competitionAdapter.currentList[viewHolder.adapterPosition]?.let {
                                    viewModel.deleteCompetition(it.id)
                                }

                                undoDelete(viewHolder)

                                adapter?.notifyDataSetChanged()
                            }
                        },
                    ),
                ).attachToRecyclerView(this)
            }

            viewModel.hideProgressBar()
        }
    }

    override fun onResume() {
        super.onResume()

        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view?.rootView?.windowToken,
            0,
        )
    }

    override fun signOut() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                viewModel.recordLogoutEvent(analytics)
                viewModel.clearUserData(analytics)
                findNavController().navigate(R.id.loginPasswordFragment)
            }
    }

    override fun settings() {
        Toast.makeText(requireContext(), "Settings click", Toast.LENGTH_SHORT).show()
    }

    override fun tutorial() {
        findNavController().navigate(R.id.tutorialViewPagerFragment)
    }

    override fun finish() {
        viewModel.closeApp(analytics)
        requireActivity().finish()
    }

    override fun RecyclerView?.undoDelete(viewHolder: RecyclerView.ViewHolder) {
        competitionAdapter.currentList[
            (viewHolder as CompetitionAdapter.CompetitionViewHolder).adapterPosition,
        ]?.let { competition ->
            Snackbar.make(
                binding.competitionsLayout,
                R.string.snackbar_undo_delete_message,
                Snackbar.LENGTH_LONG,
            ).setAction(R.string.snackbar_undo_delete) {
                viewModel.addCompetition(competition)
            }.show()
        }
    }

    companion object {
        val TAG: String = CompetitionsFragment::class.java.name
    }
}
