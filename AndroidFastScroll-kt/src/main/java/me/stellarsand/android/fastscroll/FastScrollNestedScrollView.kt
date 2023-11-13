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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.AttrRes
import androidx.core.widget.NestedScrollView

@SuppressLint("MissingSuperCall")
abstract class FastScrollNestedScrollView (
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr), ViewHelperProvider {
    
    private val mViewHelper = ViewHelper()
    
    init {
        isScrollContainer = true
    }
    
    override fun draw(canvas: Canvas) {
        mViewHelper.draw(canvas)
    }
    
    override fun onScrollChanged(left: Int, top: Int, oldLeft: Int, oldTop: Int) {
        mViewHelper.onScrollChanged(left, top, oldLeft, oldTop)
    }
    
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return mViewHelper.onInterceptTouchEvent(event)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mViewHelper.onTouchEvent(event)
    }
    
    private inner class ViewHelper : SimpleViewHelper() {
        
        override fun addOnPreDrawListener(onPreDraw: Runnable) {
        }
    
        override fun addOnScrollChangedListener(onScrollChanged: Runnable) {
        }
    
        override fun addOnTouchEventListener(onTouchEvent: Predicate<MotionEvent>) {
        }
    
        override val scrollRange: Int
            get() = super.scrollRange + paddingTop + paddingBottom
        
        override fun superDraw(canvas: Canvas) {
            super@FastScrollNestedScrollView.draw(canvas)
        }
        
        override fun superOnScrollChanged(left: Int, top: Int, oldLeft: Int, oldTop: Int) {
            super@FastScrollNestedScrollView.onScrollChanged(left, top, oldLeft, oldTop)
        }
        
        override fun superOnInterceptTouchEvent(event: MotionEvent): Boolean {
            return super@FastScrollNestedScrollView.onInterceptTouchEvent(event)
        }
        
        override fun superOnTouchEvent(event: MotionEvent): Boolean {
            return super@FastScrollNestedScrollView.onTouchEvent(event)
        }
        
        @SuppressLint("RestrictedApi")
        override fun computeVerticalScrollRange(): Int {
            return this@FastScrollNestedScrollView.computeVerticalScrollRange()
        }
        
        @SuppressLint("RestrictedApi")
        override fun computeVerticalScrollOffset(): Int {
            return this@FastScrollNestedScrollView.computeVerticalScrollOffset()
        }
    
        override val scrollX: Int
            get() = this@FastScrollNestedScrollView.scrollX
        
        override fun scrollTo(x: Int, y: Int) {
            this@FastScrollNestedScrollView.scrollTo(x, y)
        }
    }
}