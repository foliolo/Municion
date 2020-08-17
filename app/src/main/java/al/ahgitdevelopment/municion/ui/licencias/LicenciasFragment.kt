package al.ahgitdevelopment.municion.ui.licencias

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.SettingsFragment
import al.ahgitdevelopment.municion.databinding.LicenciasFragmentBinding
import al.ahgitdevelopment.municion.di.AppComponent
import al.ahgitdevelopment.municion.sandbox.Utils
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class LicenciasFragment : Fragment() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LicenciasViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        AppComponent.create(requireContext()).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: LicenciasFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.licencias_fragment, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onCreatedView()
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
                Utils.showImage(requireContext(), bitmap, "table")
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

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                recordLogoutEvent()
                clearUserData()
                findNavController().navigate(R.id.loginPasswordFragment)
            }
    }

    private fun recordLogoutEvent() {
        firebaseAnalytics.logEvent(EVENT_LOGOUT, null)
    }

    private fun clearUserData() {
        firebaseAnalytics.setUserId(null)
        firebaseCrashlytics.setUserId("")
    }

    companion object {
        private const val TAG = "LicenciasFragment"
        private const val EVENT_LOGOUT = "logout"
    }
}
