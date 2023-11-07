/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.stellarsand.android.fastscroll

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.math.MathUtils
import androidx.core.util.Consumer
import kotlin.math.abs

class FastScroller(view: ViewGroup, viewHelper: ViewHelper,
                   padding: Rect?, trackDrawable: Drawable,
                   thumbDrawable: Drawable, popupStyle: Consumer<TextView?>,
                   animationHelper: AnimationHelper) {
    private val mMinTouchTargetSize: Int
    private val mTouchSlop: Int
    private val mView: ViewGroup
    private val mViewHelper: ViewHelper
    private var mUserPadding: Rect?
    private val mAnimationHelper: AnimationHelper
    private val mTrackWidth: Int
    private val mThumbWidth: Int
    private val mThumbHeight: Int
    private val mTrackView: View
    private val mThumbView: View
    private val mPopupView: TextView
    private var mScrollbarEnabled = false
    private var mThumbOffset = 0
    private var mDownX = 0f
    private var mDownY = 0f
    private var mLastY = 0f
    private var mDragStartY = 0f
    private var mDragStartThumbOffset = 0
    private var mDragging = false
    private val mAutoHideScrollbarRunnable = Runnable { autoHideScrollbar() }
    private val mTempRect = Rect()
    
    init {
        mMinTouchTargetSize = view.resources.getDimensionPixelSize(
            R.dimen.afs_min_touch_target_size)
        val context = view.context
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        mView = view
        mViewHelper = viewHelper
        mUserPadding = padding
        mAnimationHelper = animationHelper
        mTrackWidth = requireNonNegative(trackDrawable.intrinsicWidth,
                                         "trackDrawable.getIntrinsicWidth() < 0")
        mThumbWidth = requireNonNegative(thumbDrawable.intrinsicWidth,
                                         "thumbDrawable.getIntrinsicWidth() < 0")
        mThumbHeight = requireNonNegative(thumbDrawable.intrinsicHeight,
                                          "thumbDrawable.getIntrinsicHeight() < 0")
        mTrackView = View(context)
        mTrackView.background = trackDrawable
        mThumbView = View(context)
        mThumbView.background = thumbDrawable
        mPopupView = AppCompatTextView(context)
        mPopupView.setLayoutParams(FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT))
        popupStyle.accept(mPopupView)
        val overlay = mView.overlay
        overlay.add(mTrackView)
        overlay.add(mThumbView)
        overlay.add(mPopupView)
        postAutoHideScrollbar()
        mPopupView.setAlpha(0f)
        mViewHelper.addOnPreDrawListener { onPreDraw() }
        mViewHelper.addOnScrollChangedListener { onScrollChanged() }
        mViewHelper.addOnTouchEventListener { event: MotionEvent -> onTouchEvent(event) }
    }
    
    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        if (mUserPadding != null && mUserPadding !!.left == left && mUserPadding !!.top == top && mUserPadding !!.right == right && mUserPadding !!.bottom == bottom) {
            return
        }
        if (mUserPadding == null) {
            mUserPadding = Rect()
        }
        mUserPadding !![left, top, right] = bottom
        mView.invalidate()
    }
    
    private var padding: Rect?
        get() {
            if (mUserPadding != null) {
                mTempRect.set(mUserPadding !!)
            }
            else {
                mTempRect[mView.paddingLeft, mView.paddingTop, mView.paddingRight] =
                    mView.paddingBottom
            }
            return mTempRect
        }
        set(padding) {
            if (mUserPadding == padding) {
                return
            }
            if (padding != null) {
                if (mUserPadding == null) {
                    mUserPadding = Rect()
                }
                mUserPadding !!.set(padding)
            }
            else {
                mUserPadding = null
            }
            mView.invalidate()
        }
    
    private fun onPreDraw() {
        updateScrollbarState()
        mTrackView.visibility =
            if (mScrollbarEnabled) View.VISIBLE else View.INVISIBLE
        mThumbView.visibility =
            if (mScrollbarEnabled) View.VISIBLE else View.INVISIBLE
        if (! mScrollbarEnabled) {
            mPopupView.visibility = View.INVISIBLE
            return
        }
        val layoutDirection = mView.layoutDirection
        mTrackView.layoutDirection = layoutDirection
        mThumbView.layoutDirection = layoutDirection
        mPopupView.layoutDirection = layoutDirection
        val isLayoutRtl = layoutDirection == View.LAYOUT_DIRECTION_RTL
        val viewWidth = mView.width
        val viewHeight = mView.height
        val padding = padding !!
        val trackLeft = if (isLayoutRtl) padding.left else viewWidth - padding.right - mTrackWidth
        layoutView(mTrackView, trackLeft, padding.top, trackLeft + mTrackWidth,
                   (viewHeight - padding.bottom).coerceAtLeast(padding.top))
        val thumbLeft = if (isLayoutRtl) padding.left else viewWidth - padding.right - mThumbWidth
        val thumbTop = padding.top + mThumbOffset
        layoutView(mThumbView, thumbLeft, thumbTop, thumbLeft + mThumbWidth,
                   thumbTop + mThumbHeight)
        val popupText = mViewHelper.popupText
        val hasPopup = ! TextUtils.isEmpty(popupText)
        mPopupView.visibility = if (hasPopup) View.VISIBLE else View.INVISIBLE
        if (hasPopup) {
            val popupLayoutParams = mPopupView.layoutParams as FrameLayout.LayoutParams
            if (mPopupView.text != popupText) {
                mPopupView.text = popupText
                val widthMeasureSpec = ViewGroup.getChildMeasureSpec(
                    View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY),
                    padding.left + padding.right + mThumbWidth + popupLayoutParams.leftMargin
                    + popupLayoutParams.rightMargin, popupLayoutParams.width)
                val heightMeasureSpec = ViewGroup.getChildMeasureSpec(
                    View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY),
                    padding.top + padding.bottom + popupLayoutParams.topMargin
                    + popupLayoutParams.bottomMargin, popupLayoutParams.height)
                mPopupView.measure(widthMeasureSpec, heightMeasureSpec)
            }
            val popupWidth = mPopupView.measuredWidth
            val popupHeight = mPopupView.measuredHeight
            val popupLeft =
                if (isLayoutRtl) padding.left + mThumbWidth + popupLayoutParams.leftMargin
                else (viewWidth - padding.right - mThumbWidth - popupLayoutParams.rightMargin
                      - popupWidth)
            val popupAnchorY: Int = when (popupLayoutParams.gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT -> 0
                Gravity.CENTER_HORIZONTAL -> popupHeight / 2
                Gravity.RIGHT -> popupHeight
                else -> 0
            }
            val thumbAnchorY: Int = when (popupLayoutParams.gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> mThumbView.paddingTop
                Gravity.CENTER_VERTICAL -> {
                    val thumbPaddingTop = mThumbView.paddingTop
                    thumbPaddingTop + (mThumbHeight - thumbPaddingTop
                                       - mThumbView.paddingBottom) / 2
                }
                
                Gravity.BOTTOM -> mThumbHeight - mThumbView.paddingBottom
                else -> mThumbView.paddingTop
            }
            val popupTop = MathUtils.clamp(thumbTop + thumbAnchorY - popupAnchorY,
                                           padding.top + popupLayoutParams.topMargin,
                                           viewHeight - padding.bottom - popupLayoutParams.bottomMargin - popupHeight)
            layoutView(mPopupView, popupLeft, popupTop, popupLeft + popupWidth,
                       popupTop + popupHeight)
        }
    }
    
    private fun updateScrollbarState() {
        val scrollOffsetRange = scrollOffsetRange
        mScrollbarEnabled = scrollOffsetRange > 0
        mThumbOffset =
            if (mScrollbarEnabled) (thumbOffsetRange.toLong() * mViewHelper.scrollOffset / scrollOffsetRange).toInt() else 0
    }
    
    private fun layoutView(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        val scrollX = mView.scrollX
        val scrollY = mView.scrollY
        view.layout(scrollX + left, scrollY + top, scrollX + right, scrollY + bottom)
    }
    
    private fun onScrollChanged() {
        updateScrollbarState()
        if (! mScrollbarEnabled) {
            return
        }
        mAnimationHelper.showScrollbar(mTrackView, mThumbView)
        postAutoHideScrollbar()
    }
    
    private fun onTouchEvent(event: MotionEvent): Boolean {
        if (! mScrollbarEnabled) {
            return false
        }
        val eventX = event.x
        val eventY = event.y
        val padding = padding !!
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = eventX
                mDownY = eventY
                if (mThumbView.alpha > 0 && isInViewTouchTarget(mThumbView, eventX, eventY)) {
                    mDragStartY = eventY
                    mDragStartThumbOffset = mThumbOffset
                    setDragging(true)
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (! mDragging && isInViewTouchTarget(mTrackView, mDownX, mDownY) && abs(
                        eventY - mDownY) > mTouchSlop) {
                    if (isInViewTouchTarget(mThumbView, mDownX, mDownY)) {
                        mDragStartY = mLastY
                        mDragStartThumbOffset = mThumbOffset
                    }
                    else {
                        mDragStartY = eventY
                        mDragStartThumbOffset = (eventY - padding.top - mThumbHeight / 2f).toInt()
                        scrollToThumbOffset(mDragStartThumbOffset)
                    }
                    setDragging(true)
                }
                if (mDragging) {
                    val thumbOffset = mDragStartThumbOffset + (eventY - mDragStartY).toInt()
                    scrollToThumbOffset(thumbOffset)
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> setDragging(false)
        }
        mLastY = eventY
        return mDragging
    }
    
    private fun isInView(view: View, x: Float, y: Float): Boolean {
        val scrollX = mView.scrollX
        val scrollY = mView.scrollY
        return x >= view.left - scrollX && x < view.right - scrollX && y >= view.top - scrollY && y < view.bottom - scrollY
    }
    
    private fun isInViewTouchTarget(view: View, x: Float, y: Float): Boolean {
        val scrollX = mView.scrollX
        val scrollY = mView.scrollY
        return (isInTouchTarget(x, view.left - scrollX, view.right - scrollX, 0,
                                mView.width)
                && isInTouchTarget(y, view.top - scrollY, view.bottom - scrollY, 0,
                                   mView.height))
    }
    
    private fun isInTouchTarget(position: Float, viewStart: Int, viewEnd: Int, parentStart: Int,
                                parentEnd: Int): Boolean {
        val viewSize = viewEnd - viewStart
        if (viewSize >= mMinTouchTargetSize) {
            return position >= viewStart && position < viewEnd
        }
        var touchTargetStart = viewStart - (mMinTouchTargetSize - viewSize) / 2
        if (touchTargetStart < parentStart) {
            touchTargetStart = parentStart
        }
        var touchTargetEnd = touchTargetStart + mMinTouchTargetSize
        if (touchTargetEnd > parentEnd) {
            touchTargetEnd = parentEnd
            touchTargetStart = touchTargetEnd - mMinTouchTargetSize
            if (touchTargetStart < parentStart) {
                touchTargetStart = parentStart
            }
        }
        return position >= touchTargetStart && position < touchTargetEnd
    }
    
    private fun scrollToThumbOffset(thumbOffset: Int) {
        var tempThumbOffset = thumbOffset
        val thumbOffsetRange = thumbOffsetRange
        tempThumbOffset = MathUtils.clamp(tempThumbOffset, 0, thumbOffsetRange)
        val scrollOffset = (scrollOffsetRange.toLong() * tempThumbOffset / thumbOffsetRange).toInt()
        mViewHelper.scrollTo(scrollOffset)
    }
    
    private val scrollOffsetRange: Int
        get() = mViewHelper.scrollRange - mView.height
    private val thumbOffsetRange: Int
        get() {
            val padding = padding !!
            return mView.height - padding.top - padding.bottom - mThumbHeight
        }
    
    private fun setDragging(dragging: Boolean) {
        if (mDragging == dragging) {
            return
        }
        mDragging = dragging
        if (mDragging) {
            mView.parent.requestDisallowInterceptTouchEvent(true)
        }
        mTrackView.isPressed = mDragging
        mThumbView.isPressed = mDragging
        if (mDragging) {
            cancelAutoHideScrollbar()
            mAnimationHelper.showScrollbar(mTrackView, mThumbView)
            mAnimationHelper.showPopup(mPopupView)
        }
        else {
            postAutoHideScrollbar()
            mAnimationHelper.hidePopup(mPopupView)
        }
    }
    
    private fun postAutoHideScrollbar() {
        cancelAutoHideScrollbar()
        if (mAnimationHelper.isScrollbarAutoHideEnabled) {
            mView.postDelayed(mAutoHideScrollbarRunnable,
                              mAnimationHelper.scrollbarAutoHideDelayMillis.toLong())
        }
    }
    
    private fun autoHideScrollbar() {
        if (mDragging) {
            return
        }
        mAnimationHelper.hideScrollbar(mTrackView, mThumbView)
    }
    
    private fun cancelAutoHideScrollbar() {
        mView.removeCallbacks(mAutoHideScrollbarRunnable)
    }
    
    interface ViewHelper {
        fun addOnPreDrawListener(onPreDraw: Runnable)
        fun addOnScrollChangedListener(onScrollChanged: Runnable)
        fun addOnTouchEventListener(onTouchEvent: Predicate<MotionEvent>)
        val scrollRange: Int
        val scrollOffset: Int
        fun scrollTo(offset: Int)
        val popupText: CharSequence?
            get() = null
    }
    
    interface AnimationHelper {
        fun showScrollbar(trackView: View, thumbView: View)
        fun hideScrollbar(trackView: View, thumbView: View)
        val isScrollbarAutoHideEnabled: Boolean
        val scrollbarAutoHideDelayMillis: Int
        fun showPopup(popupView: View)
        fun hidePopup(popupView: View)
    }
    
    companion object {
        private fun requireNonNegative(value: Int, message: String): Int {
            require(value >= 0) { message }
            return value
        }
    }
}