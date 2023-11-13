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
        get() =
            itemHeight.takeIf {
                it != 0 && itemCount != 0
            }?.let {
                mView.paddingTop + itemCount * it + mView.paddingBottom
            } ?: 0
    
    override val scrollOffset: Int
        get() =
            firstItemPosition.takeIf {
                it != RecyclerView.NO_POSITION
            }?.let {
                mView.paddingTop + it * itemHeight - firstItemOffset
            } ?: 0
    
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
            val popupTextProvider =
                mPopupTextProvider ?: mView.adapter.takeIf {
                    it is PopupTextProvider
                } as? PopupTextProvider
            
            val position = getPopupTextPosition()
            
            return popupTextProvider?.takeIf {
                position != RecyclerView.NO_POSITION
            }?.getPopupText(mView, position)
        }
    
    private val itemCount: Int
        get() =
            verticalLinearLayoutManager?.let { layoutManager ->
                var itemCount = layoutManager.itemCount
                if (itemCount != 0 && layoutManager is GridLayoutManager) {
                    itemCount = (itemCount - 1) / layoutManager.spanCount + 1
                }
                itemCount
            } ?: 0
    
    private val itemHeight: Int
        get() =
            mView.getChildAt(0)?.let { itemView ->
                mView.getDecoratedBoundsWithMargins(itemView, mTempRect)
                mTempRect.height()
            } ?: 0
    
    private val firstItemPosition: Int
        get() =
            verticalLinearLayoutManager?.let { layoutManager ->
                var position = firstItemAdapterPosition
                if (layoutManager is GridLayoutManager) {
                    position /= layoutManager.spanCount
                }
                position
            } ?: RecyclerView.NO_POSITION
    
    private val firstItemAdapterPosition: Int
        get() =
            mView.getChildAt(0)?.let { itemView ->
                verticalLinearLayoutManager?.getPosition(itemView)
            } ?: RecyclerView.NO_POSITION
    
    private val firstItemOffset: Int
        get() =
            mView.getChildAt(0)?.let { itemView ->
                mView.getDecoratedBoundsWithMargins(itemView, mTempRect)
                mTempRect.top
            } ?: RecyclerView.NO_POSITION
    
    private fun scrollToPositionWithOffset(position: Int, offset: Int) {
        verticalLinearLayoutManager?.let { layoutManager ->
            var tempPosition = position
            var tempOffset = offset
            if (layoutManager is GridLayoutManager) {
                tempPosition *= layoutManager.spanCount
            }
            // LinearLayoutManager actually takes offset from paddingTop instead of top of RecyclerView.
            tempOffset -= mView.paddingTop
            layoutManager.scrollToPositionWithOffset(tempPosition, tempOffset)
        }
    }
    
    private val verticalLinearLayoutManager: LinearLayoutManager?
        get() =
            mView.layoutManager.let {
                it as? LinearLayoutManager
            }?.takeIf {
                it.orientation == RecyclerView.VERTICAL
            }
    
    // Better popup position
    private fun getPopupTextPosition(): Int {
        val position = firstItemAdapterPosition
        val range = (scrollRange - mView.height).coerceAtLeast(1)
        val offset = scrollOffset.coerceAtMost(range)
        
        return verticalLinearLayoutManager?.let { layoutManager ->
            if (position == RecyclerView.NO_POSITION) {
                return@let RecyclerView.NO_POSITION
            }
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            if (firstVisibleItemPosition == RecyclerView.NO_POSITION
                || lastVisibleItemPosition == RecyclerView.NO_POSITION) {
                return@let position
            }
            val positionOffset = (lastVisibleItemPosition - firstVisibleItemPosition + 1) * offset / range
            (position + positionOffset).coerceAtMost((mView.adapter?.itemCount?.minus(1)) ?: RecyclerView.NO_POSITION)
        } ?: RecyclerView.NO_POSITION
    }
    
}