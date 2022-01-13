package com.loren.component.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
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
    private var viewWidth = 0

    private var velocityTracker: VelocityTracker? = null
    private val mMinimumVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val mMaximumVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val mScroller by lazy { OverScroller(context) }
    private val downPoint = PointF()
    private var moveX = 0f
    private var isShowLeft = false
    private var needFix = false
    private var mDirection = Direction.DIRECTION_LEFT // 内容滚动方向

    fun setRecyclerView(recyclerView: HorizontalRecyclerView, isNeedHideLeftView: Boolean = false, isNeedShowShadow: Boolean = true) {
        this.recyclerView = recyclerView
        this.isNeedHideLeftView = isNeedHideLeftView
        this.isNeedShowShadow = isNeedShowShadow
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            postInvalidate()
        } else {
            if (needFix) {
                fixScrollX()
            }
            postInvalidate()
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
        val h = (parent as? View?)?.measuredHeight ?: 0

        setMeasuredDimension(contentWidth + paddingStart + paddingEnd, max(contentHeight + paddingTop + paddingBottom, h))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var layoutLeft = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            val childViewWidth = childView.measuredWidth
            val childViewHeight = childView.measuredHeight
            // 需要隐藏左边第一个view&&是第一个元素的时候
            if (isNeedHideLeftView && i == 0) {
                layoutLeft = -childViewWidth
            }
            childView.layout(layoutLeft, paddingTop, layoutLeft + childViewWidth, paddingTop + childViewHeight)
            layoutLeft += childViewWidth
        }
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!monitorScrollViews().contains(this))
            monitorScrollViews().add(this)
        scrollTo(getRecordX(), 0)
        setShadow(getRecordX())
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
                recyclerView?.needNotify = true
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
                    val firstViewWidth = getChildAt(0).measuredWidth
                    if (afterScrollX >= -firstViewWidth && afterScrollX <= measuredWidth - viewWidth - firstViewWidth) {
                        if ((afterScrollX >= -firstViewWidth && afterScrollX < 0) || afterScrollX == 0 && deltaX < 0) {
                            scrollBy(deltaX / 2, 0)
                        } else {
                            scrollBy(deltaX, 0)
                        }
                    }
                } else {
                    if (afterScrollX >= 0 && afterScrollX <= measuredWidth - viewWidth) {
                        scrollBy(deltaX, 0)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (abs(downPoint.x - event.x) < touchSlop && abs(downPoint.y - event.y) < touchSlop) {
                    (tag as? View)?.onTouchEvent(event)
                }
                // 释放
                velocityTracker?.run {
                    computeCurrentVelocity(1000)
                    val firstViewWidth = getChildAt(0).measuredWidth
                    if (abs(xVelocity) > mMinimumVelocity) {
                        needFix = true
                        if (isShowLeft) {
                            fixScrollX()
                        } else {
                            if (mDirection == Direction.DIRECTION_RIGHT && scrollX < 0) {
                                fixScrollX()
                            } else {
                                val maxX = if (measuredWidth < viewWidth) 0 else measuredWidth - viewWidth
                                if (isNeedHideLeftView) {
                                    monitorScrollViews().forEach {
                                        it.mScroller.fling(
                                            scrollX,
                                            0,
                                            (-xVelocity.toInt() * 1.5).toInt(),
                                            0,
                                            -(firstViewWidth * 0.2).toInt(),
                                            maxX - firstViewWidth,
                                            0,
                                            0
                                        )
                                    }
                                } else {
                                    monitorScrollViews().forEach {
                                        it.mScroller.fling(
                                            scrollX,
                                            0,
                                            (-xVelocity.toInt() * 1.5).toInt(),
                                            0,
                                            0,
                                            maxX,
                                            0,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        if (isNeedHideLeftView) {
                            fixScrollX()
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

    private fun allViewsScrollX(x: Int) {
        monitorScrollViews().forEach {
            if (it != this) {
                it.setShadow(x)
                it.scrollTo(x, 0)
            }
        }
        setRecordX(x)
    }

    /**
     * 设置阴影布局
     */
    fun setShadow(x: Int) {
        if (isNeedShowShadow) {
            if (x > 0) {
                (tag as? View)?.findViewById<View>(R.id.swipeHorizontalShadowView)?.visibility = View.VISIBLE
            } else {
                (tag as? View)?.findViewById<View>(R.id.swipeHorizontalShadowView)?.visibility = View.GONE
            }
        }
    }

    /**
     * 修正x位置
     */
    private fun fixScrollX() {
        needFix = false
        if (isNeedHideLeftView) {
            val firstViewWidth = getChildAt(0).measuredWidth
            val threshold = firstViewWidth * 0.3 // [-firstViewWidth  -firstViewWidth+threshold    -threshold  0]
            if (isShowLeft) { // 展开状态
                if (scrollX >= -firstViewWidth && scrollX <= -firstViewWidth + threshold) {
                    extend()
                } else if (scrollX > -firstViewWidth + threshold) {
                    fold()
                }
            } else { // 收起状态
                if (scrollX <= -threshold) {
                    extend()
                } else if (scrollX > -threshold && scrollX <= 0) {
                    fold()
                }
            }
        }
    }

    /**
     * 展开view
     */
    private fun extend() {
        val left = getChildAt(0).measuredWidth
        monitorScrollViews().forEach {
            it.mScroller.startScroll(scrollX, 0, -left - scrollX, 0, 300)
        }
        isShowLeft = true
    }

    /**
     * 折叠view
     */
    private fun fold() {
        monitorScrollViews().forEach {
            it.mScroller.startScroll(scrollX, 0, -scrollX, 0, 300)
        }
        isShowLeft = false
    }

}