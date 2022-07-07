package com.loren.component.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
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
    private var needHideLeft = false
    private var needShadow = true
    private var needVibrate = true
    private var extendThreshold = -1f
    private var foldThreshold = -1f
    private var defaultShowLeft = false

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        itemAnimator = null
        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.HorizontalRecyclerView)
        needHideLeft = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needHideLeft, needHideLeft)
        needShadow = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needShadow, needShadow)
        needVibrate = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_needVibrate, needVibrate)
        extendThreshold = obtainStyledAttributes.getDimension(R.styleable.HorizontalRecyclerView_extendThreshold, extendThreshold)
        foldThreshold = obtainStyledAttributes.getDimension(R.styleable.HorizontalRecyclerView_foldThreshold, foldThreshold)
        defaultShowLeft = obtainStyledAttributes.getBoolean(R.styleable.HorizontalRecyclerView_defaultShowLeft, defaultShowLeft)
        obtainStyledAttributes.recycle()
        isShowLeft = defaultShowLeft
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        needNotify = state == SCROLL_STATE_IDLE
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        val rightScroll = child?.findViewById<SwipeHorizontalScrollView>(R.id.swipeHorizontalView)
        rightScroll?.setRecyclerView(
            this, isNeedHideLeftView = needHideLeft, isNeedShowShadow = needShadow, isNeedVibrate = needVibrate,
            extendThreshold = if (extendThreshold == -1f) null else extendThreshold,
            foldThreshold = if (foldThreshold == -1f) null else foldThreshold,
            defaultShowLeft = defaultShowLeft
        )
        rightScroll?.tag = child
        super.addView(child, index, params)
        rightScroll?.scrollTo(recordX, 0)
    }

    override fun removeView(view: View?) {
        super.removeView(view)
    }

    /**
     * 将recyclerview与headScrollView进行绑定
     */
    fun bindHeadScrollView(view: View) {
        val rightScroll = view.findViewById<SwipeHorizontalScrollView>(R.id.swipeHorizontalView)
        rightScroll.setRecyclerView(
            this, isNeedHideLeftView = needHideLeft, isNeedShowShadow = needShadow, isNeedVibrate = needVibrate,
            extendThreshold = if (extendThreshold == -1f) null else extendThreshold,
            foldThreshold = if (foldThreshold == -1f) null else foldThreshold,
            defaultShowLeft = defaultShowLeft
        )
        if (scrollViews.contains(rightScroll)) scrollViews.remove(rightScroll)
        scrollViews.add(rightScroll)
    }

    fun resetScrollX() {
        recordX = 0
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
