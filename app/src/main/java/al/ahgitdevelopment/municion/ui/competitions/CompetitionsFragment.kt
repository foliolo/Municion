package al.ahgitdevelopment.municion.ui.competitions

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.CompetitionsFragmentBinding
import al.ahgitdevelopment.municion.di.AppComponent
import al.ahgitdevelopment.municion.ui.DeleteItemOnSwipe
import al.ahgitdevelopment.municion.ui.RecyclerInterface
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.competitions_fragment.*
import javax.inject.Inject

class CompetitionsFragment : Fragment(), RecyclerInterface {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var competitionAdapter: CompetitionAdapter

    private val viewModel: CompetitionsViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        AppComponent.create(requireContext()).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: CompetitionsFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.competitions_fragment, container, false)

        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addCompetition.observe(viewLifecycleOwner, {
            findNavController().navigate(R.id.competitionFormFragment)
        })

        viewModel.competitions.observe(viewLifecycleOwner) {
            competitionAdapter = CompetitionAdapter().apply {
                submitList(it)
                setHasStableIds(true)
            }

            competitions_recycler_view.apply {
                adapter = competitionAdapter
                layoutManager = LinearLayoutManager(requireContext())

                ItemTouchHelper(
                    DeleteItemOnSwipe(object : DeleteItemOnSwipe.DeleteCallback {
                        override fun deleteOnSwipe(viewHolder: RecyclerView.ViewHolder) {
                            viewModel.deletePurchase(viewHolder.itemId)

                            undoDelete(viewHolder)

                            adapter?.notifyDataSetChanged()
                        }
                    })
                ).attachToRecyclerView(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getPurchases()

        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
            it.hideSoftInputFromWindow(view?.rootView?.windowToken, 0)
        }
    }

    override fun RecyclerView?.undoDelete(viewHolder: RecyclerView.ViewHolder) {
        competitionAdapter.currentList[(viewHolder as CompetitionAdapter.CompetitionViewHolder).adapterPosition]?.let { competition ->
            Snackbar.make(
                competitions_layout,
                R.string.snackbar_undo_delete_message,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.snackbar_undo_delete) {
                viewModel.addPurchase(competition)
                this?.adapter?.notifyDataSetChanged()
            }.show()
        }
    }
}