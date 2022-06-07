package com.loren.component.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loren.component.view.R

/**
 * Created by Loren on 2021/12/28
 * Description -> 水平滚动的RecyclerView
 */
class HorizontalRecyclerView(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    val scrollViews by lazy { ArrayList<SwipeHorizontalScrollView>() }
    var recordX: Int = 0
    var needNotify = true
    private var needHideLeft = false
    private var needShadow = true
    private var needVibrate = true
    private var extendThreshold = -1f
    private var foldThreshold = -1f

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        itemAnimator = null
        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.HorizontalRecyclerView)
        needHideLeft = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needHideLeft, needHideLeft)
        needShadow = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needShadow, needShadow)
        needVibrate = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needVibrate, needVibrate)
        extendThreshold = obtainStyledAttributes.getDimension(R.styleable.HorizontalRecyclerView_extendThreshold, extendThreshold)
        foldThreshold = obtainStyledAttributes.getDimension(R.styleable.HorizontalRecyclerView_foldThreshold, foldThreshold)
        obtainStyledAttributes.recycle()
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        needNotify = state == SCROLL_STATE_IDLE
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        val rightScroll = child?.findViewById<SwipeHorizontalScrollView>(R.id.swipeHorizontalView)
        rightScroll?.setRecyclerView(
            this,
            isNeedHideLeftView = needHideLeft,
            isNeedShowShadow = needShadow,
            isNeedVibrate = needVibrate,
            extendThreshold = if (extendThreshold == -1f) null else extendThreshold,
            foldThreshold = if (foldThreshold == -1f) null else foldThreshold
        )
        rightScroll?.tag = child

        decorateScrollView(rightScroll)

        super.addView(child, index, params)
        rightScroll?.scrollTo(recordX, 0)
    }

    /**
     * 将recyclerview与headScrollView进行绑定
     */
    fun bindHeadScrollView(view: View) {
        val rightScroll = view.findViewById<SwipeHorizontalScrollView>(R.id.swipeHorizontalView)
        rightScroll.setRecyclerView(
            this,
            isNeedHideLeftView = needHideLeft,
            isNeedShowShadow = needShadow,
            isNeedVibrate = needVibrate,
            extendThreshold = if (extendThreshold == -1f) null else extendThreshold,
            foldThreshold = if (foldThreshold == -1f) null else foldThreshold
        )
        rightScroll?.tag = decorateScrollView(rightScroll)
        if (scrollViews.contains(rightScroll)) scrollViews.remove(rightScroll)
        scrollViews.add(rightScroll)
    }

    private fun decorateScrollView(scrollView: View?): FrameLayout {
        val frameLayout = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        val shadowView = getShadowView()
        val parent = scrollView?.parent as? ViewGroup?
        parent?.removeView(scrollView)
        scrollView?.let {
            frameLayout.addView(it)
        }
        frameLayout.addView(shadowView)
        parent?.addView(frameLayout)
        return frameLayout
    }

    private fun getShadowView(): View {
        return View(context).apply {
            id = R.id.swipeHorizontalShadowView
            setBackgroundResource(R.drawable.view_shadow)
            layoutParams = MarginLayoutParams(36, ViewGroup.LayoutParams.MATCH_PARENT)
            visibility = GONE
        }
    }

    fun resetScrollX() {
        recordX = 0
        scrollViews.forEach {
            it.scrollTo(recordX, 0)
            it.setShadow(recordX)
        }
    }

    fun updateState(state: SwipeHorizontalScrollView.ViewState) {
        when (state) {
            SwipeHorizontalScrollView.ViewState.EXTEND -> mListener?.extend()
            SwipeHorizontalScrollView.ViewState.FOLD -> mListener?.fold()
        }
    }

    interface OnHorizontalRecyclerViewStateListener {
        fun extend()
        fun fold()
    }

    private var mListener: OnHorizontalRecyclerViewStateListener? = null

    fun setOnHorizontalRecyclerViewStateListener(listener: OnHorizontalRecyclerViewStateListener?) {
        this.mListener = listener
    }
}
