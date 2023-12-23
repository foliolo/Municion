package al.ahgitdevelopment.municion.ui.tutorial

import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class TutorialPageTransformer : ViewPager2.PageTransformer {

    private val interpolator = DecelerateInterpolator(INTERPOLATOR_FACTOR)

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            when {
                position < -1 -> {
                    alpha = 0f
                }

                position <= 1 -> {
                    val transformFactor = interpolator
                        .getInterpolation(
                            abs((position / SWIPE_FACTOR))
                                .coerceIn(0f, 1f),
                        )
                    val scale = 1 - transformFactor * SCALE_FACTOR
                    val verticalMargin = pageHeight * (1 - scale) / 2
                    val horizontalMargin = pageWidth * (1 - scale) / 2

                    translationX =
                        if (position < 0) {
                            horizontalMargin - verticalMargin / 2
                        } else {
                            horizontalMargin + verticalMargin / 2
                        }

                    scaleX = scale
                    scaleY = scale

                    alpha = MIN_ALPHA + (1 - transformFactor) * (1 - MIN_ALPHA)
                }

                else -> {
                    alpha = 0f
                }
            }
        }
    }

    companion object {
        private const val INTERPOLATOR_FACTOR = 1.5f
        private const val SWIPE_FACTOR = 0.4f
        private const val SCALE_FACTOR = 0.1f
        private const val MIN_ALPHA = 0.8f
    }
}
