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

import android.view.View
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

class DefaultAnimationHelper(private val mView: View) : FastScroller.AnimationHelper {
    
    companion object {
        private const val SHOW_DURATION_MILLIS = 150L
        private const val HIDE_DURATION_MILLIS = 200L
        private val SHOW_SCROLLBAR_INTERPOLATOR = LinearOutSlowInInterpolator()
        private val HIDE_SCROLLBAR_INTERPOLATOR = FastOutLinearInInterpolator()
        private const val AUTO_HIDE_SCROLLBAR_DELAY_MILLIS = 1500
    }
    
    private var mScrollbarAutoHideEnabled = true
    private var mShowingScrollbar = true
    private var mShowingPopup = false
    
    override val isScrollbarAutoHideEnabled: Boolean
        get() = mScrollbarAutoHideEnabled
    
    override val scrollbarAutoHideDelayMillis: Int
        get() = AUTO_HIDE_SCROLLBAR_DELAY_MILLIS
    
    override fun showScrollbar(trackView: View, thumbView: View) {
        if (mShowingScrollbar) {
            return
        }
        mShowingScrollbar = true
        
        trackView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(SHOW_DURATION_MILLIS)
            .setInterpolator(SHOW_SCROLLBAR_INTERPOLATOR)
            .start()
        thumbView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(SHOW_DURATION_MILLIS)
            .setInterpolator(SHOW_SCROLLBAR_INTERPOLATOR)
            .start()
    }
    
    override fun hideScrollbar(trackView: View, thumbView: View) {
        if (!mShowingScrollbar) {
            return
        }
        mShowingScrollbar = false
        
        val isLayoutRtl = mView.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val width = trackView.width.coerceAtLeast(thumbView.width).toFloat()
        val translationX: Float =
            if (isLayoutRtl) {
                if (trackView.left == 0) -width else 0f
            }
            else {
                if (trackView.right == mView.width) width else 0f
            }
        trackView.animate()
            .alpha(0f)
            .translationX(translationX)
            .setDuration(HIDE_DURATION_MILLIS)
            .setInterpolator(HIDE_SCROLLBAR_INTERPOLATOR)
            .start()
        thumbView.animate()
            .alpha(0f)
            .translationX(translationX)
            .setDuration(HIDE_DURATION_MILLIS)
            .setInterpolator(HIDE_SCROLLBAR_INTERPOLATOR)
            .start()
    }
    
    fun setScrollbarAutoHideEnabled(enabled: Boolean) {
        mScrollbarAutoHideEnabled = enabled
    }
    
    override fun showPopup(popupView: View) {
        if (mShowingPopup) {
            return
        }
        mShowingPopup = true
        
        popupView.animate()
            .alpha(1f)
            .setDuration(SHOW_DURATION_MILLIS)
            .start()
    }
    
    override fun hidePopup(popupView: View) {
        if (!mShowingPopup) {
            return
        }
        mShowingPopup = false
        
        popupView.animate()
            .alpha(0f)
            .setDuration(HIDE_DURATION_MILLIS)
            .start()
    }
}
