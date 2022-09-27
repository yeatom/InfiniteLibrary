package com.yeatom.infinitelibrary

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.Space
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * @author yeatom 9/27/2022
 */

class InfiniteLibrary @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : LinearLayoutCompat(context, attrs) {

    fun display(urlList: List<String>) {
        visibility = INVISIBLE
        urls = urlList

        removeAllViews()
        initChildren()
        resetState()

        preloadForDisplay()
        autoScroll()
    }

    private lateinit var views: List<ImageView>
    private lateinit var urls: List<String>

    private var diameter = 50.dpToPx
    private var radius = 25.dpToPx

    private val overlapRatio: Float
    private val visibleNum: Int

    private var currentHead = 0
    private var currentTail = 0
    private var currentUrl = 0

    //create ImageViews
    private fun initChildren() {
        val spaceForDisappear = (diameter * (1 - overlapRatio)).roundToInt()
        addView(Space(context), LayoutParams(spaceForDisappear, diameter))

        views = List(visibleNum + 1) { index ->
            ShapeableImageView(context).apply {
                //border
                strokeWidth = diameter / 25f
                strokeColor = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_enabled)),
                    intArrayOf(Color.WHITE)
                )
                val paddingOffset = (strokeWidth / 2).roundToInt()
                setPadding(paddingOffset, paddingOffset, paddingOffset, paddingOffset)

                //shape
                shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                    .setAllCorners(RoundedCornerTreatment())
                    .setAllCornerSizes(RelativeCornerSize(0.5f))
                    .build()

                addView(this, childCount, LayoutParams(diameter, diameter).apply {
                    if (index != visibleNum)
                        marginEnd = (-diameter * overlapRatio).roundToInt()
                })
            }
        }
    }

    //make some items visible first
    private fun preloadForDisplay() {
        MainScope().launch {
            urls.take(visibleNum + 1).zip(views).forEachIndexed { index, pair ->
                val view = pair.second
                val url = pair.first

                if (index == visibleNum) {
                    view.alpha = 0f
                }
                view loadImage url
            }
        }
        visibility = VISIBLE
    }

    //translate views to the left and load next image
    private fun moveViews() {
        views.forEachIndexed { index, view ->

            view.run {
                val animate = animate()
                if (index == currentHead) {
                    animate.alpha(0f)
                        .withEndAction {
                            view loadImage urls[currentUrl]
                            translationX += width * (1 - overlapRatio) * (visibleNum + 1)
                        }
                }
                if (index == currentTail) {
                    animate.alpha(1f)
                }

                val offsetX = width * (overlapRatio - 1) + translationX
                val offsetZ =
                    if (index - currentHead >= 0) index - currentHead
                    else views.size - currentHead + index

                z = offsetZ.toFloat()

                animate
                    .setDuration(1000)
                    .translationX(offsetX)
                    .start()
            }
        }


        if (++currentHead == views.size) {
            currentHead = 0
        }

        if (++currentTail == views.size) {
            currentTail = 0
        }

        if (++currentUrl == urls.size) {
            currentUrl = 0
        }
    }

    init {
        orientation = HORIZONTAL

        val height = attrs?.getAttributeValue("android", "layout_height")
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.InfiniteLibrary)

        if (!height.equals(WRAP_CONTENT.toString()) &&
            !height.equals(MATCH_PARENT.toString())) {

            context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.layout_height)).run {
                diameter = getDimensionPixelOffset(0, 50.dpToPx)
                radius = diameter / 2
                recycle()
            }
        }

        visibleNum = typedArray.getInt(R.styleable.InfiniteLibrary_visibleNum, 3)
        overlapRatio = typedArray.getFloat(R.styleable.InfiniteLibrary_overlapRatio, 0.4f)

        typedArray.recycle()
        resetState()
        preview()
    }

    //only for ide preview
    private fun preview() {
        initChildren()
        val drawable = ContextCompat.getDrawable(context, R.mipmap.gengar)
        views.forEachIndexed { index, view ->
            if (index == visibleNum) {
                view.alpha = 0f
            }
            view.setImageDrawable(drawable)
        }
    }

    private fun resetState() {
        currentHead = 0
        currentTail = visibleNum
        currentUrl = visibleNum
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        layoutParams?.run {
            width = WRAP_CONTENT
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        executor.shutdown()
        executor = Executors.newSingleThreadScheduledExecutor()
    }

    private fun autoScroll() {
        executor.scheduleAtFixedRate({
            MainScope().launch(Dispatchers.Main) {
                moveViews()
            }
        }, 3000, 3000, TimeUnit.MILLISECONDS)
    }

    companion object {
        private infix fun ImageView.loadImage(url: String) {
            visibility = INVISIBLE

            val callback = object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    setImageDrawable(resource)
                    visibility = VISIBLE
                    return true
                }
            }

            Glide.with(this)
                .load(url)
                .centerCrop()
                .addListener(callback)
                .into(this)
        }

        private val Int.dpToPx: Int
            get() = (this * Resources.getSystem().displayMetrics.density).toInt()

        private var executor = Executors.newSingleThreadScheduledExecutor()
    }
}