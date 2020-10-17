package al.ahgitdevelopment.municion.ui.tutorial

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.di.AppComponent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.fragment_tutorial_viewpager.*
import java.io.File
import javax.inject.Inject

class TutorialViewPagerFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: TutorialViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        AppComponent.create(requireContext()).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_FULLSCREEN
        activity?.window?.decorView?.systemUiVisibility = uiOptions

        requireActivity().toolbar.visibility = View.GONE

        return inflater.inflate(R.layout.fragment_tutorial_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tutorialViewPager.requestDisallowInterceptTouchEvent(true)
        tutorialViewPager.setPageTransformer(TutorialPageTransformer())

        viewModel.images.observe(viewLifecycleOwner) { files ->
            tutorialViewPager.adapter = SectionsPagerAdapter(files, requireActivity())
        }

        viewModel.progressBar.observe(viewLifecycleOwner) {
            when (it) {
                true -> screenshotProgressBar.show()
                false -> screenshotProgressBar.hide()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)

        hiddenDrawerNavigationMenu()
    }

    private fun hiddenDrawerNavigationMenu() {
        requireActivity().nav_view.visibility = View.GONE
    }

    class SectionsPagerAdapter(
        private val images: List<File>,
        fragmentActivity: FragmentActivity
    ) : FragmentStateAdapter(fragmentActivity) {

        override fun createFragment(position: Int): Fragment =
            TutorialScreenshotFragment.newInstance(images[position])

        override fun getItemCount(): Int = images.count()
    }

    companion object {
        private val TAG = TutorialViewPagerFragment::class.java.name
    }
}