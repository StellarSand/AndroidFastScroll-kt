/*
 * Copyright 2023-present StellarSand
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

import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener
import java.util.Objects

internal class RecyclerViewHelper(
    private val mView: RecyclerView,
    private val mPopupTextProvider: PopupTextProvider?
) : FastScroller.ViewHelper {
    
    private val mTempRect = Rect()
    
    override fun addOnPreDrawListener(onPreDraw: Runnable) {
        mView.addItemDecoration(object : ItemDecoration() {
            override fun onDraw(canvas: Canvas, parent: RecyclerView,
                                state: RecyclerView.State) {
                onPreDraw.run()
            }
        })
    }
    
    override fun addOnScrollChangedListener(onScrollChanged: Runnable) {
        mView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                onScrollChanged.run()
            }
        })
    }
    
    override fun addOnTouchEventListener(onTouchEvent: Predicate<MotionEvent>) {
        mView.addOnItemTouchListener(object : SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(recyclerView: RecyclerView,
                                               event: MotionEvent): Boolean {
                return onTouchEvent.test(event)
            }
            
            override fun onTouchEvent(recyclerView: RecyclerView,
                                      event: MotionEvent) {
                onTouchEvent.test(event)
            }
        })
    }
    
    override val scrollRange: Int
        get() {
            val itemCount = itemCount
            if (itemCount == 0) {
                return 0
            }
            val itemHeight = itemHeight
            return if (itemHeight == 0) {
                0
            }
            else mView.paddingTop + itemCount * itemHeight + mView.paddingBottom
        }
    override val scrollOffset: Int
        get() {
            val firstItemPosition = firstItemPosition
            if (firstItemPosition == RecyclerView.NO_POSITION) {
                return 0
            }
            val itemHeight = itemHeight
            val firstItemTop = firstItemOffset
            return mView.paddingTop + firstItemPosition * itemHeight - firstItemTop
        }
    
    override fun scrollTo(offset: Int) {
        // Stop any scroll in progress for RecyclerView.
        var tempOffset = offset
        mView.stopScroll()
        tempOffset -= mView.paddingTop
        val itemHeight = itemHeight
        // firstItemPosition should be non-negative even if paddingTop is greater than item height.
        val firstItemPosition = 0.coerceAtLeast(tempOffset / itemHeight)
        val firstItemTop = firstItemPosition * itemHeight - tempOffset
        scrollToPositionWithOffset(firstItemPosition, firstItemTop)
    }
    
    override val popupText: CharSequence?
        get() {
            var popupTextProvider = mPopupTextProvider
            if (popupTextProvider == null) {
                val adapter = mView.adapter
                if (adapter is PopupTextProvider) {
                    popupTextProvider = adapter
                }
            }
            if (popupTextProvider == null) {
                return null
            }
            val position = getPopupTextPosition()
            return if (position == RecyclerView.NO_POSITION) {
                null
            }
            else popupTextProvider.getPopupText(mView, position)
        }
    private val itemCount: Int
        get() {
            val linearLayoutManager = verticalLinearLayoutManager ?: return 0
            var itemCount = linearLayoutManager.itemCount
            if (itemCount == 0) {
                return 0
            }
            if (linearLayoutManager is GridLayoutManager) {
                itemCount = (itemCount - 1) / linearLayoutManager.spanCount + 1
            }
            return itemCount
        }
    private val itemHeight: Int
        get() {
            if (mView.childCount == 0) {
                return 0
            }
            val itemView = mView.getChildAt(0)
            mView.getDecoratedBoundsWithMargins(itemView, mTempRect)
            return mTempRect.height()
        }
    private val firstItemPosition: Int
        get() {
            var position = firstItemAdapterPosition
            val linearLayoutManager = verticalLinearLayoutManager ?: return RecyclerView.NO_POSITION
            if (linearLayoutManager is GridLayoutManager) {
                position /= linearLayoutManager.spanCount
            }
            return position
        }
    private val firstItemAdapterPosition: Int
        get() {
            if (mView.childCount == 0) {
                return RecyclerView.NO_POSITION
            }
            val itemView = mView.getChildAt(0)
            val linearLayoutManager = verticalLinearLayoutManager ?: return RecyclerView.NO_POSITION
            return linearLayoutManager.getPosition(itemView)
        }
    private val firstItemOffset: Int
        get() {
            if (mView.childCount == 0) {
                return RecyclerView.NO_POSITION
            }
            val itemView = mView.getChildAt(0)
            mView.getDecoratedBoundsWithMargins(itemView, mTempRect)
            return mTempRect.top
        }
    
    private fun scrollToPositionWithOffset(position: Int, offset: Int) {
        var tempPosition = position
        var tempOffset = offset
        val linearLayoutManager = verticalLinearLayoutManager ?: return
        if (linearLayoutManager is GridLayoutManager) {
            tempPosition *= linearLayoutManager.spanCount
        }
        // LinearLayoutManager actually takes offset from paddingTop instead of top of RecyclerView.
        tempOffset -= mView.paddingTop
        linearLayoutManager.scrollToPositionWithOffset(tempPosition, tempOffset)
    }
    
    private val verticalLinearLayoutManager: LinearLayoutManager?
        get() {
            val layoutManager = mView.layoutManager as? LinearLayoutManager ?: return null
            return if (layoutManager.orientation != RecyclerView.VERTICAL) {
                null
            }
            else layoutManager
        }
    
    // Fixes wrong popup position
    private fun getPopupTextPosition(): Int {
        val position: Int = firstItemAdapterPosition
        val range = (scrollRange - mView.height).coerceAtLeast(1)
        val offset = scrollOffset.coerceAtMost(range)
        val linearLayoutManager = verticalLinearLayoutManager
        if (position == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }
        val firstVisibleItemPosition = linearLayoutManager!!.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
        if (firstVisibleItemPosition == RecyclerView.NO_POSITION
            || lastVisibleItemPosition == RecyclerView.NO_POSITION) {
            return position
        }
        val positionOffset =
            (lastVisibleItemPosition - firstVisibleItemPosition + 1) * offset / range
        return (position + positionOffset).coerceAtMost(Objects.requireNonNull(mView.adapter).itemCount - 1)
    }
}