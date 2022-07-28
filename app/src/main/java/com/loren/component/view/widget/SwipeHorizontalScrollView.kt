package com.loren.component.view.widget

import android.app.Service
import android.content.Context
import android.graphics.*
import android.os.Vibrator
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.OverScroller
import kotlin.math.abs
import kotlin.math.max

/**
 * Created by Loren on 2021/12/31
 * Description -> 水平滚动的scrollview
 */
class SwipeHorizontalScrollView(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs), View.OnTouchListener {

    /**
     * 方向
     */
    enum class Direction {
        DIRECTION_LEFT, DIRECTION_RIGHT
    }

    private var mDirection = Direction.DIRECTION_LEFT // 内容滚动方向

    private var recyclerView: HorizontalRecyclerView? = null
    private val mScrollViews = mutableListOf<SwipeHorizontalScrollView>()
    private var mRecordX = 0
    private var isNeedHideLeftView = false
    private var isNeedShowShadow = true
    private var isNeedVibrate = true
    private var extendThreshold: Float? = null
    private var foldThreshold: Float? = null
    private var needFixItemPosition = false

    private var viewWidth = 0
    private var needFix = false
    private val mScroller by lazy { OverScroller(context) }
    private val firstViewWidth by lazy {
        getChildAt(0).measuredWidth
    }

    private val gradientMatrix by lazy { Matrix() }
    private val linearGradient by lazy { LinearGradient(0f, 0f, 36f, 0f, Color.parseColor("#1A000000"), Color.TRANSPARENT, Shader.TileMode.CLAMP) }
    private val shadowPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            shader = linearGradient
        }
    }

    private val helper = GestureDetector(context, GestureDetectorHelper())
    private var isDrag = false

    init {
        setWillNotDraw(false)
        setOnTouchListener(this)
    }

    fun setRecyclerView(
        recyclerView: HorizontalRecyclerView
    ) {
        this.recyclerView = recyclerView
        this.isNeedHideLeftView = recyclerView.needHideLeft
        this.isNeedShowShadow = recyclerView.needShadow
        this.isNeedVibrate = recyclerView.needVibrate
        this.extendThreshold = recyclerView.extendThreshold
        this.foldThreshold = recyclerView.foldThreshold
        this.needFixItemPosition = recyclerView.needFixItemPosition
    }

    inner class GestureDetectorHelper : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            monitorScrollViews().forEach {
                if (!it.mScroller.isFinished) {
                    it.mScroller.abortAnimation()
                }
            }
            setRecordX(scrollX)
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            (tag as? View)?.performClick()
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            (tag as? View)?.performLongClick()
            super.onLongPress(e)
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            if (isDrag || abs(distanceX) > abs(distanceY)) {
                isDrag = true
                parent?.requestDisallowInterceptTouchEvent(true)

                val deltaX = distanceX.toInt()
                mDirection = if (deltaX > 0) {
                    Direction.DIRECTION_LEFT
                } else {
                    Direction.DIRECTION_RIGHT
                }
                val afterScrollX = scrollX + deltaX

                if (isNeedHideLeftView) {
                    if (scrollX < 0 || (afterScrollX == 0 && deltaX < 0)) {
                        allViewsScrollX(
                            (scrollX + deltaX / 2).coerceAtLeast(-firstViewWidth).coerceAtMost(measuredWidth - viewWidth - firstViewWidth)
                        )
                    } else {
                        allViewsScrollX((scrollX + deltaX).coerceAtLeast(-firstViewWidth).coerceAtMost(measuredWidth - viewWidth - firstViewWidth))
                    }
                } else {
                    allViewsScrollX((scrollX + deltaX).coerceAtLeast(0).coerceAtMost(measuredWidth - viewWidth))
                }
            }
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            needFix = true
            if (recyclerView?.isShowLeft == true) {
                // 展开的情况下最多只能快速滑动到0位置
                if (scrollX != -firstViewWidth) {
                    monitorScrollViews().forEach {
                        it.mScroller.fling(
                            scrollX,
                            0,
                            -velocityX.toInt(),
                            0,
                            -firstViewWidth,
                            0,
                            0,
                            0
                        )
                        postInvalidateOnAnimation()
                    }
                }
            } else {
                if (scrollX < 0) {
                    monitorScrollViews().forEach {
                        it.mScroller.fling(
                            scrollX,
                            0,
                            -velocityX.toInt(),
                            0,
                            -firstViewWidth,
                            0,
                            0,
                            0
                        )
                        postInvalidateOnAnimation()
                    }
                } else {
                    val maxX = if (isNeedHideLeftView) {
                        if (measuredWidth - firstViewWidth < viewWidth) 0 else measuredWidth - viewWidth - firstViewWidth
                    } else {
                        if (measuredWidth < viewWidth) 0 else measuredWidth - viewWidth
                    }
                    val overX = if (isNeedHideLeftView && velocityX > 0) {
                        ((extendThreshold ?: (firstViewWidth * 0.3f))).toInt()
                    } else {
                        0
                    }
                    monitorScrollViews().forEach {
                        it.mScroller.fling(
                            scrollX,
                            0,
                            -velocityX.toInt(),
                            0,
                            0,
                            maxX,
                            0,
                            0,
                            overX,
                            0
                        )
                        postInvalidateOnAnimation()
                    }
                }
            }
            return true
        }
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
            if (childView.visibility != View.GONE) {
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
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isNeedShowShadow && getRecordX() > 0) {
            linearGradient.setLocalMatrix(gradientMatrix.apply { setTranslate(getRecordX().toFloat(), 0f) })
            canvas?.drawRect(getRecordX().toFloat(), 0f, getRecordX() + 36f, measuredHeight.toFloat(), shadowPaint)
        }
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset() && !mScroller.isFinished) {
            allViewsScrollX(mScroller.currX)
            postInvalidateOnAnimation()
        } else {
            if (needFix) {
                fixScrollX()
                recyclerView?.needNotify = true
                needFix = false
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val onTouchEvent = helper.onTouchEvent(event)
        if (event?.action == MotionEvent.ACTION_UP || event?.action == MotionEvent.ACTION_CANCEL) {
            isDrag = false
            parent?.requestDisallowInterceptTouchEvent(false)

            if (isNeedHideLeftView && !needFix) {
                fixScrollX(true)
            }
        }
        return onTouchEvent
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                recyclerView?.needNotify = false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                recyclerView?.needNotify = true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                // 解决SwipeHorizontalScrollView嵌套RecyclerView的奇怪用法
                helper.onTouchEvent(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                return true
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            recyclerView?.isShowLeft = getRecordX() < 0
            scrollTo(getRecordX(), 0)
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
            it.scrollTo(x, 0)
        }
        setRecordX(x)
    }

    /**
     * 修正x位置
     */
    private fun fixScrollX(interceptCancelEvent: Boolean = false) {
        recyclerView?.needNotify = false
        if (isNeedHideLeftView) {
            val threshold: Float
            if (recyclerView?.isShowLeft == true) { // 展开状态
                threshold = foldThreshold ?: (firstViewWidth * 0.3f)
                if (scrollX >= -firstViewWidth && scrollX < -firstViewWidth + threshold) {
                    extend(interceptCancelEvent)
                } else if (scrollX >= -firstViewWidth + threshold) {
                    if (!interceptCancelEvent) {
                        vibrate()
                    }
                    fold(interceptCancelEvent)
                }
            } else { // 收起状态
                threshold = extendThreshold ?: (firstViewWidth * 0.3f)
                if (scrollX < 0 - threshold) {
                    if (!interceptCancelEvent) {
                        vibrate()
                    }
                    extend(interceptCancelEvent)
                } else if (scrollX >= 0 - threshold && scrollX <= 0) {
                    fold(interceptCancelEvent)
                }
            }
        }

        offsetPosition()
    }

    /**
     * 偏移tab位置
     */
    private fun offsetPosition() {
        if (needFixItemPosition && recyclerView?.isShowLeft != true && scrollX != measuredWidth - viewWidth - firstViewWidth) {
            var sumX = 0
            var stopIndexHeader = 0
            var stopIndexFooter = 0
            for (i in 1 until childCount) {
                if (sumX < scrollX) {
                    stopIndexHeader = sumX
                    sumX += getChildAt(i).measuredWidth
                } else {
                    stopIndexFooter = sumX
                    break
                }
            }
            val middle = (stopIndexFooter - stopIndexHeader) / 2
            monitorScrollViews().forEach {
                it.mScroller.startScroll(
                    scrollX,
                    0,
                    if (scrollX - stopIndexHeader > middle) (stopIndexFooter - scrollX).coerceAtMost(measuredWidth - firstViewWidth - viewWidth - scrollX) else (stopIndexHeader - scrollX),
                    0,
                    600
                )
                postInvalidateOnAnimation()
            }
        }
    }

    /**
     * 展开view
     */
    private fun extend(interceptCancelEvent: Boolean = false) {
        monitorScrollViews().forEach {
            it.mScroller.startScroll(scrollX, 0, -firstViewWidth - scrollX, 0, 500)
            postInvalidateOnAnimation()
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
            it.mScroller.startScroll(scrollX, 0, 0 - scrollX, 0, duration.toInt())
            postInvalidateOnAnimation()
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