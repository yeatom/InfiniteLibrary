package com.yeatom.infinitelibrary

import android.animation.*
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Property
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import kotlin.math.roundToInt

class InfiniteLibrary @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private var views: List<ImageView> = listOf()
    private var urls: List<String> = listOf()

    private var diameter = 50.dpToPx
    private var radius = 25.dpToPx

    private var visibleNum: Int = 3
    private val overlapRatio: Float

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.InfiniteLibrary)

        visibleNum = typedArray.getInt(R.styleable.InfiniteLibrary_visibleNum, 3)
        overlapRatio = typedArray.getFloat(R.styleable.InfiniteLibrary_overlapRatio, 0.4f)

        preview()
        typedArray.recycle()
    }

    fun exhibit(urls: List<String>?) {
        urls?.run {
            if (size < visibleNum) {
                visibleNum = size
            }
            initChildViews()

            take(visibleNum).zip(views).forEachIndexed { index, pair ->
                val view = pair.second as SimpleDraweeView
                val url = pair.first

                view loadImage url
            }
        }
    }

    private val loopRunnable = object : Runnable {
        val animators = arrayListOf<Animator>()

        val interpolator = AccelerateDecelerateInterpolator()
        val property = object : Property<View, Float>(Float::class.java, "translationX") {
            override fun get(`object`: View): Float {
                return `object`.translationX
            }

            override fun set(`object`: View, value: Float) {
                `object`.translationX = value
            }
        }

        override fun run() {
            views.forEachIndexed { index, view ->
                addTranslateAnimator(index, view)
                addFadeAnimator(index, view)
            }

            AnimatorSet().apply {
                playTogether(animators)
                duration = 4000
                start()
            }
        }

        private fun addFadeAnimator(index: Int, view: ImageView) {
            val size: Float = views.size * 1f

            val fadeValues =
                if (index != views.size - 1)
                    PropertyValuesHolder.ofKeyframe(
                        ALPHA,
                        Keyframe.ofFloat(0f, 1f),
                        Keyframe.ofFloat(index / size, 1f),
                        Keyframe.ofFloat((index + 1f) / size, 0f),
                        Keyframe.ofFloat((index + 2f) / size, 1f),
                        Keyframe.ofFloat(1f, 1f),
                    )
                else
                    PropertyValuesHolder.ofKeyframe(
                        ALPHA,
                        Keyframe.ofFloat(0f, 0f),
                        Keyframe.ofFloat(1f / size, 1f),
                        Keyframe.ofFloat(index / size, 1f),
                        Keyframe.ofFloat(1f, 0f),
                    )

            val overlayValues =
                PropertyValuesHolder.ofKeyframe(
                    TRANSLATION_Z,
                    Keyframe.ofFloat(0f, (index + 1f) / size),
                    Keyframe.ofFloat((index + 1f) / size, 0f),
                    Keyframe.ofFloat(((index + 1f) / size) + 0.001f, 1f),
                    Keyframe.ofFloat(1f, (index + 1f) / size),
                )

            val fade =
                ObjectAnimator.ofPropertyValuesHolder(view, fadeValues, overlayValues).apply {
                    interpolator = LinearInterpolator()
                    repeatCount = ObjectAnimator.INFINITE
                }
            animators.add(fade)
        }

        private fun addTranslateAnimator(index: Int, view: ImageView) {
            val offset = (index + 1) * diameter * (1 - overlapRatio)

            val evaluator = object : TypeEvaluator<Float> {
                override fun evaluate(
                    fraction: Float,
                    startValue: Float?,
                    endValue: Float?
                ): Float {
                    val process = (fraction * views.size) % 1
                    val moved = (fraction * views.size).toInt()

                    val segmentDistance = diameter * (overlapRatio - 1)
                    val wholeDistance = (views.size) * diameter * (overlapRatio - 1)
                    val currentPoint = (wholeDistance / views.size) * moved +
                            interpolator.getInterpolation(process) * segmentDistance

                    if (currentPoint + offset <= 0) {
                        return currentPoint - wholeDistance
                    }
                    return currentPoint
                }
            }

            val translateX = ObjectAnimator.ofObject(view, property, evaluator, 0f, 0f)
                .apply {
                    interpolator = LinearInterpolator()
                    repeatCount = ObjectAnimator.INFINITE
                }
            animators.add(translateX)
        }
    }

    fun loop(urls: List<String>) {
        if (urls.size <= visibleNum || visibleNum <= 2) {
            exhibit(urls)
            return
        }

        urls.run {
            initChildViews(isStartOffset = true, isEndOffset = true)

            take(visibleNum + 1).zip(views).forEachIndexed { index, pair ->
                val url = pair.first
                val view = pair.second as SimpleDraweeView

                view loadImage url
            }
        }

        this.urls = urls
        removeCallbacks(loopRunnable)
        postDelayed(loopRunnable, 1000)
    }

    private fun preview() {
        initChildViews(true)

        views.forEachIndexed { index, view ->
            if (index == visibleNum) {
                view.alpha = 0f
            }
            view as ImageFilterView loadImage drawableId
        }
    }

    private fun initChildViews(
        isPreview: Boolean = false,
        isStartOffset: Boolean = false,
        isEndOffset: Boolean = false,
    ) {
        removeAllViews()

        val offset = if (!isStartOffset) 0 else (diameter * (1 - overlapRatio)).roundToInt()
        val viewsNum = if (isEndOffset) visibleNum + 1 else visibleNum

        views = List(viewsNum) { index ->
            val imageView =
                if (isPreview)
                    ImageFilterView(context).apply {
                        roundPercent = 1f
                    }
                else SimpleDraweeView(context).apply {
                    hierarchy.roundingParams =
                        RoundingParams.fromCornersRadius(radius.toFloat()).apply {
                            borderColor = Color.WHITE
                            roundAsCircle = true
                            borderWidth = 1f.dpToPx
                        }
                }

            val layoutParams = LayoutParams(diameter, diameter).apply {
                marginStart = offset + (diameter * (1 - overlapRatio) * index).roundToInt()
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
            }

            imageView.apply {
                addView(this, childCount, layoutParams)

                if (index == visibleNum) {
                    alpha = 0f
                }
            }
        }
    }

    private infix fun SimpleDraweeView.loadImage(url: String) {
        setImageURI(url)
    }

    private infix fun ImageFilterView.loadImage(id: Int) {
        setImageResource(id)
    }

    companion object {
        private const val drawableId = R.mipmap.gengar

        inline val displayMetrics: DisplayMetrics
            get() = Resources.getSystem().displayMetrics

        inline val Int.dpToPx: Int
            get() = (this * displayMetrics.density).toInt()

        inline val Float.dpToPx: Float
            get() = this * displayMetrics.density
    }
}