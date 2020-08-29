package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.SettingsFragment
import al.ahgitdevelopment.municion.databinding.LicensesFragmentBinding
import al.ahgitdevelopment.municion.di.AppComponent
import al.ahgitdevelopment.municion.ui.DeleteItemOnSwipe
import al.ahgitdevelopment.municion.ui.RecyclerInterface
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.licenses_fragment.*
import javax.inject.Inject

class LicensesFragment : Fragment(), RecyclerInterface {

    @Inject
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var licensesAdapter: LicenseAdapter? = null

    private val viewModel: LicensesViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        AppComponent.create(requireContext()).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: LicensesFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.licenses_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addLicense.observe(viewLifecycleOwner, {
            findNavController().navigate(R.id.licenciaFormFragment)
        })

        viewModel.licenses.observe(viewLifecycleOwner) {
            licensesAdapter = LicenseAdapter().apply {
                submitList(it)
                setHasStableIds(true)
            }

            licenses_recycler_view.apply {
                adapter = licensesAdapter
                layoutManager = LinearLayoutManager(requireContext())

                ItemTouchHelper(
                    DeleteItemOnSwipe(object : DeleteItemOnSwipe.DeleteLicenseCallback {
                        override fun deleteLicenseOnSwipe(viewHolder: LicenseAdapter.LicenseViewHolder) {
                            viewModel.deleteLicense(viewHolder.itemId)

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
        viewModel.getLicenses()

        (requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).let {
            it.hideSoftInputFromWindow(view?.rootView?.windowToken, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(requireContext(), SettingsFragment::class.java)
                startActivity(intent)
            }
            R.id.tabla_tiradas -> try {
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.image_table)
                // Utils.showImage(requireContext(), bitmap, "table")
            } catch (ex: Exception) {
                Log.e(TAG, "Error mostrando la tabla de tiradas")
                firebaseCrashlytics.recordException(ex)
            }
            R.id.log_out -> {
                signOut()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun RecyclerView?.undoDelete(viewHolder: LicenseAdapter.LicenseViewHolder) {
        licensesAdapter?.currentList?.get(viewHolder.adapterPosition)?.let { license ->
            Snackbar.make(
                licenses_layout,
                R.string.snackbar_undo_delete_message,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.snackbar_undo_delete) {
                viewModel.addLicense(license)
                this?.adapter?.notifyDataSetChanged()
            }.show()
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                viewModel.recordLogoutEvent()
                viewModel.clearUserData()
                findNavController().navigate(R.id.loginPasswordFragment)
            }
    }

    companion object {
        private const val TAG = "LicensesFragment"
    }
}
