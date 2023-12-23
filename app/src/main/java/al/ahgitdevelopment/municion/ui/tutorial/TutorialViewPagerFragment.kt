package al.ahgitdevelopment.municion.ui.tutorial

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentTutorialViewpagerBinding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

@AndroidEntryPoint
class TutorialViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentTutorialViewpagerBinding
    private val viewModel: TutorialViewModel by viewModels()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_FULLSCREEN
        activity?.window?.decorView?.systemUiVisibility = uiOptions

        // (requireActivity() as NavigationActivity).findViewById<Toolbar>(R.id.toolbar)?.isVisible = false
        (requireActivity() as NavigationActivity).supportActionBar?.hide()

        binding = FragmentTutorialViewpagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tutorialViewPager.requestDisallowInterceptTouchEvent(true)
        binding.tutorialViewPager.setPageTransformer(TutorialPageTransformer())

        viewModel.images.observe(viewLifecycleOwner) { files ->
            binding.tutorialViewPager.adapter = SectionsPagerAdapter(files, requireActivity())
        }

        viewModel.progressBar.observe(viewLifecycleOwner) {
            when (it) {
                true -> binding.screenshotProgressBar.show()
                false -> binding.screenshotProgressBar.hide()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)

        binding.tutorialScreenshotButton.setOnClickListener {
            findNavController().navigate(
                TutorialViewPagerFragmentDirections.actionTutorialViewPagerFragmentToLicensesFragment(),
            )
        }

        hiddenDrawerNavigationMenu()
    }

    private fun hiddenDrawerNavigationMenu() {
        (requireActivity() as NavigationActivity).findViewById<BottomNavigationView>(R.id.nav_view)?.isVisible = false
    }

    class SectionsPagerAdapter(
        private val images: List<File>,
        fragmentActivity: FragmentActivity,
    ) : FragmentStateAdapter(fragmentActivity) {

        override fun createFragment(position: Int): Fragment =
            TutorialScreenshotFragment.newInstance(images[position])

        override fun getItemCount(): Int = images.count()
    }
}
