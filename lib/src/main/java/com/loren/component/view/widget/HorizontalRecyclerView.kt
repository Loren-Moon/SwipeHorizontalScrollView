package com.loren.component.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loren.component.view.R

/**
 * Created by Loren on 2021/12/31
 * Description -> 水平滚动的RecyclerView
 */
class HorizontalRecyclerView(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    val scrollViews by lazy { ArrayList<SwipeHorizontalScrollView>() }
    var recordX: Int = 0
    var needNotify = true
    var isShowLeft = false
    var needHideLeft = false
    var needShadow = true
    var needVibrate = true
    var extendThreshold = -1f
    var foldThreshold = -1f
    var needFixItemPosition = false
    var dingColumn: Int? = null

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        itemAnimator = null
        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.HorizontalRecyclerView)
        needHideLeft = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needHideLeft, needHideLeft)
        needShadow = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needShadow, needShadow)
        needVibrate = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needVibrate, needVibrate)
        extendThreshold = obtainStyledAttributes.getDimension(R.styleable.HorizontalRecyclerView_extendThreshold, extendThreshold)
        foldThreshold = obtainStyledAttributes.getDimension(R.styleable.HorizontalRecyclerView_foldThreshold, foldThreshold)
        needFixItemPosition = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needFixItemPosition, needFixItemPosition)
        obtainStyledAttributes.recycle()
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        needNotify = state == SCROLL_STATE_IDLE
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        val rightScroll = child?.findViewById<SwipeHorizontalScrollView>(R.id.swipeHorizontalView)
        rightScroll?.setRecyclerView(this)
        rightScroll?.tag = child
        rightScroll?.scrollTo(recordX, 0)
        decorateScrollView(rightScroll)
        rightScroll?.also { scrollViews.add(it) }
        super.addView(child, index, params)
    }

    private var originalIndex: Int = 0
    private fun decorateScrollView(scrollView: ViewGroup?) {
        val parent = scrollView?.parent as? ViewGroup?
        var dingWrapperLayout = parent?.findViewById<LinearLayoutCompat>(R.id.swipeHorizontalDingWrapperView)
        if (dingWrapperLayout == null) {
            dingWrapperLayout = LinearLayoutCompat(context).apply {
                id = R.id.swipeHorizontalDingWrapperView
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            parent?.removeView(scrollView)
            dingWrapperLayout.addView(scrollView)
            parent?.addView(dingWrapperLayout)
        }

        if (scrollView != null) {
            if (dingWrapperLayout.childCount > 1) {
                val dingView = dingWrapperLayout.getChildAt(0)
                dingWrapperLayout.removeViewAt(0)
                scrollView.addView(dingView, originalIndex)

                dingColumn?.let { column ->
                    if (column < scrollView.childCount) {
                        val needDingView = scrollView.getChildAt(column)
                        scrollView.removeViewAt(column)
                        dingWrapperLayout.addView(needDingView, 0)
                    }
                }
            } else {
                dingColumn?.let { column ->
                    if (column < scrollView.childCount) {
                        val needDingView = scrollView.getChildAt(column)
                        scrollView.removeViewAt(column)
                        dingWrapperLayout.addView(needDingView, 0)
                        originalIndex = column
                    }
                }
            }
        }

    }

    override fun onViewRemoved(child: View?) {
        val rightScroll = child?.findViewById<SwipeHorizontalScrollView>(R.id.swipeHorizontalView)
        rightScroll?.also { scrollViews.remove(it) }
        super.onViewRemoved(child)
    }

    /**
     * 将recyclerview与headScrollView进行绑定
     */
    fun bindHeadScrollView(view: View) {
        val rightScroll = view.findViewById<SwipeHorizontalScrollView>(R.id.swipeHorizontalView)
        rightScroll.setRecyclerView(this)
        decorateScrollView(rightScroll)
        if (scrollViews.contains(rightScroll)) scrollViews.remove(rightScroll)
        scrollViews.add(rightScroll)
    }

    fun resetScrollX(x: Int = 0) {
        recordX = x
        scrollViews.forEach {
            it.scrollTo(recordX, 0)
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
