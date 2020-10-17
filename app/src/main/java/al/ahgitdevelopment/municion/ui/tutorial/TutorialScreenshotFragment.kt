package al.ahgitdevelopment.municion.ui.tutorial

import al.ahgitdevelopment.municion.R
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_tutorial_screenshots.*
import java.io.File

class TutorialScreenshotFragment : Fragment() {

    private val fadeInAnimation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.fade_in)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_tutorial_screenshots, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, true) {
            findNavController().navigate(R.id.licensesFragment)
        }

        arguments?.let { args ->

            val file = args.getSerializable(ARG_IMAGE) as File
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            tutorialScreenshotImageView.setImageBitmap(bitmap)
        }

        tutorialScreenshotImageView.startAnimation(fadeInAnimation)
    }

    companion object {
        private const val ARG_IMAGE = "image"

        @JvmStatic
        fun newInstance(image: File): TutorialScreenshotFragment {
            return TutorialScreenshotFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_IMAGE, image)
                }
            }
        }
    }
}
