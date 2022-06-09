package com.loren.component.view.widget

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.PointF
import android.os.Vibrator
import android.util.AttributeSet
import android.view.*
import android.widget.OverScroller
import com.loren.component.view.R
import kotlin.math.abs
import kotlin.math.max

/**
 * Created by Loren on 2021/12/31
 * Description -> 水平滚动的scrollview
 */
class SwipeHorizontalScrollView(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    /**
     * 方向
     */
    enum class Direction {
        DIRECTION_LEFT, DIRECTION_RIGHT
    }

    private var recyclerView: HorizontalRecyclerView? = null
    private var mRecordX = 0
    private val mScrollViews = mutableListOf<SwipeHorizontalScrollView>()
    private var isNeedHideLeftView = false
    private var isNeedShowShadow = true
    private var isNeedVibrate = true
    private var extendThreshold: Float? = null
    private var foldThreshold: Float? = null
    private var defaultShowLeft = false
    private var viewWidth = 0

    private var velocityTracker: VelocityTracker? = null
    private val mMinimumVelocity = 1000
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val mScroller by lazy { OverScroller(context) }
    private val downPoint = PointF()
    private var moveX = 0f
    private var needFix = false
    private var mDirection = Direction.DIRECTION_LEFT // 内容滚动方向

    private val firstViewWidth by lazy {
        getChildAt(0).measuredWidth
    }

    private val triggerThreshold by lazy {
        if (defaultShowLeft) firstViewWidth else 0
    }

    fun setRecyclerView(
        recyclerView: HorizontalRecyclerView,
        isNeedHideLeftView: Boolean = false,
        isNeedShowShadow: Boolean = true,
        isNeedVibrate: Boolean = true,
        extendThreshold: Float? = null,
        foldThreshold: Float? = null,
        defaultShowLeft: Boolean = false
    ) {
        this.recyclerView = recyclerView
        this.isNeedHideLeftView = isNeedHideLeftView
        this.isNeedShowShadow = isNeedShowShadow
        this.isNeedVibrate = isNeedVibrate
        this.extendThreshold = extendThreshold
        this.foldThreshold = foldThreshold
        this.defaultShowLeft = defaultShowLeft
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            if (mScroller.currX != mScroller.startX) {
                scrollTo(mScroller.currX, mScroller.currY)
            }
            postInvalidate()
        } else {
            recyclerView?.needNotify = true
            if (needFix) {
                fixScrollX()
                postInvalidate()
            }
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        allViewsScrollX(l)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (viewWidth == 0)
            viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        var contentWidth = 0
        var contentHeight = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView.visibility != View.GONE) {
                measureChildWithMargins(childView, 0, 0, heightMeasureSpec, 0)
                contentWidth += childView.measuredWidth
                contentHeight = max(contentHeight, childView.measuredHeight)
            }
        }

        setMeasuredDimension(contentWidth + paddingStart + paddingEnd, contentHeight + paddingTop + paddingBottom)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var layoutLeft = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            val childViewWidth = childView.measuredWidth
            val childViewHeight = childView.measuredHeight
            // 需要隐藏左边第一个view&&是第一个元素的时候
            if (isNeedHideLeftView && i == 0) {
                layoutLeft = if (defaultShowLeft) {
                    0
                } else {
                    -childViewWidth
                }
            }
            childView.layout(layoutLeft, paddingTop, layoutLeft + childViewWidth, paddingTop + childViewHeight)
            layoutLeft += childViewWidth
        }
        setShadow(getRecordX())
    }

    private fun getRecordX(): Int {
        return recyclerView?.recordX ?: mRecordX
    }

    private fun setRecordX(x: Int) {
        recyclerView?.recordX = x
        mRecordX = x
    }

    private fun monitorScrollViews(): MutableList<SwipeHorizontalScrollView> {
        return recyclerView?.scrollViews ?: mScrollViews
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        scrollTo(getRecordX(), 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!monitorScrollViews().contains(this))
            monitorScrollViews().add(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        monitorScrollViews().remove(this)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                downPoint.set(ev.x, ev.y)
                moveX = ev.x
                monitorScrollViews().forEach {
                    if (!it.mScroller.isFinished) {
                        it.mScroller.abortAnimation()
                    }
                }
                setRecordX(scrollX)
                recyclerView?.needNotify = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(downPoint.x - ev.x) > abs(downPoint.y - ev.y)) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                if (abs(downPoint.x - ev.x) >= touchSlop || abs(downPoint.y - ev.y) >= touchSlop) {
                    (tag as? View)?.cancelLongPress()
                }
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_CANCEL -> {
                (tag as? View)?.cancelLongPress()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_MOVE && abs(downPoint.x - ev.x) > abs(downPoint.y - ev.y)) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("Recycle", "ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        (velocityTracker ?: VelocityTracker.obtain()).also { velocityTracker = it }.addMovement(event)

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                (tag as? View)?.onTouchEvent(event)
                downPoint.set(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                (tag as? View)?.onTouchEvent(event)
                val deltaX = (moveX - event.x).toInt()
                mDirection = if (deltaX > 0) {
                    Direction.DIRECTION_LEFT
                } else {
                    Direction.DIRECTION_RIGHT
                }
                val afterScrollX = scrollX + deltaX

                if (isNeedHideLeftView) {
                    if (afterScrollX >= triggerThreshold - firstViewWidth) {
                        if (afterScrollX <= measuredWidth - viewWidth - (firstViewWidth - triggerThreshold)) {
                            if (inFirstViewWidthRange(afterScrollX) || afterScrollX == triggerThreshold && deltaX < 0) {
                                scrollBy(deltaX / 2, 0)
                            } else {
                                scrollBy(deltaX, 0)
                            }
                        } else if (afterScrollX <= triggerThreshold || deltaX < 0) { // view的条目很窄的情况下，不能向左滑动
                            scrollBy(deltaX / 2, 0)
                        }
                    }
                } else {
                    if (afterScrollX >= triggerThreshold && afterScrollX <= measuredWidth - viewWidth) {
                        scrollBy(deltaX, 0)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (abs(downPoint.x - event.x) < touchSlop && abs(downPoint.y - event.y) < touchSlop) {
                    (tag as? View)?.onTouchEvent(event)
                }
                // 释放
                velocityTracker?.run {
                    computeCurrentVelocity(1000)
                    if (abs(xVelocity) > mMinimumVelocity) {
                        needFix = true
                        if (recyclerView?.isShowLeft == true) {
                            fixScrollX()
                        } else {
                            if (mDirection == Direction.DIRECTION_RIGHT && scrollX < triggerThreshold) {
                                fixScrollX()
                            } else {
                                if (isNeedHideLeftView) {
                                    val maxX = if (measuredWidth - firstViewWidth < viewWidth) triggerThreshold else measuredWidth - viewWidth
                                    val minX = triggerThreshold - ((extendThreshold ?: (firstViewWidth * 0.3f))).toInt()
                                    monitorScrollViews().forEach {
                                        it.mScroller.fling(
                                            scrollX,
                                            0,
                                            -xVelocity.toInt(),
                                            0,
                                            minX,
                                            maxX + (triggerThreshold - firstViewWidth),
                                            0,
                                            0
                                        )
                                    }
                                } else {
                                    val maxX = if (measuredWidth < viewWidth) triggerThreshold else measuredWidth - viewWidth
                                    monitorScrollViews().forEach {
                                        it.mScroller.fling(scrollX, 0, -xVelocity.toInt(), 0, 0, maxX, 0, 0)
                                    }
                                }
                            }
                        }
                    } else {
                        if (isNeedHideLeftView) {
                            fixScrollX(event.action == MotionEvent.ACTION_CANCEL)
                        }
                    }
                    postInvalidate()
                    recycle()
                    velocityTracker = null
                }
            }
        }
        event?.let {
            moveX = it.x
        }
        return true
    }

    private fun inFirstViewWidthRange(afterScrollX: Int): Boolean {
        return afterScrollX >= triggerThreshold - firstViewWidth && afterScrollX < triggerThreshold
    }

    private fun vibrate() {
        if (isNeedVibrate) {
            (context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator).let {
                if (it.hasVibrator()) {
                    it.vibrate(30)
                }
            }
        }
    }

    private fun allViewsScrollX(x: Int) {
        monitorScrollViews().forEach {
//            if (it != this) {
            it.setShadow(x)
            it.scrollTo(x, 0)
//            }
        }
        setRecordX(x)
    }

    /**
     * 设置阴影布局
     */
    fun setShadow(x: Int) {
        if (isNeedShowShadow) {
            if ((defaultShowLeft && x > getChildAt(0).measuredWidth) || (!defaultShowLeft && x > 0)) {
                (tag as? View)?.findViewById<View>(R.id.swipeHorizontalShadowView)?.visibility = View.VISIBLE
            } else {
                (tag as? View)?.findViewById<View>(R.id.swipeHorizontalShadowView)?.visibility = View.GONE
            }
        }
    }

    /**
     * 修正x位置
     */
    private fun fixScrollX(interceptCancelEvent: Boolean = false) {
        needFix = false
        if (isNeedHideLeftView) {
            val threshold: Float
            if (recyclerView?.isShowLeft == true) { // 展开状态
                threshold = foldThreshold ?: (firstViewWidth * 0.3f)
                if (scrollX >= triggerThreshold - firstViewWidth && scrollX < triggerThreshold - firstViewWidth + threshold) {
                    extend(interceptCancelEvent)
                } else if (scrollX > triggerThreshold - firstViewWidth + threshold) {
                    if (!interceptCancelEvent) {
                        vibrate()
                    }
                    fold(interceptCancelEvent)
                }
            } else { // 收起状态
                threshold = extendThreshold ?: (firstViewWidth * 0.3f)
                if (scrollX < triggerThreshold - threshold) {
                    if (!interceptCancelEvent) {
                        vibrate()
                    }
                    extend(interceptCancelEvent)
                } else if (scrollX >= triggerThreshold - threshold && scrollX < triggerThreshold) {
                    fold(interceptCancelEvent)
                }
            }
        }
    }

    /**
     * 展开view
     */
    private fun extend(interceptCancelEvent: Boolean = false) {
        monitorScrollViews().forEach {
            it.mScroller.startScroll(scrollX, 0, if (defaultShowLeft) -scrollX else -firstViewWidth - scrollX, 0, 500)
        }
        recyclerView?.isShowLeft = true
        if (!interceptCancelEvent) {
            recyclerView?.updateState(ViewState.EXTEND)
        }
    }

    /**
     * 折叠view
     */
    private fun fold(interceptCancelEvent: Boolean = false) {
        // 优化：左面被隐藏的view在展开的时候，view宽度的完整动画时长为800，要根据比例算出剩余view被隐藏的时长
        val duration = if (recyclerView?.isShowLeft == true) abs(1f * scrollX) / firstViewWidth * 1000 else 800
        monitorScrollViews().forEach {
            it.mScroller.startScroll(scrollX, 0, if (defaultShowLeft) firstViewWidth - scrollX else -scrollX, 0, duration.toInt())
        }
        recyclerView?.isShowLeft = false
        if (!interceptCancelEvent) {
            recyclerView?.updateState(ViewState.FOLD)
        }
    }

    enum class ViewState {
        EXTEND, FOLD
    }

}