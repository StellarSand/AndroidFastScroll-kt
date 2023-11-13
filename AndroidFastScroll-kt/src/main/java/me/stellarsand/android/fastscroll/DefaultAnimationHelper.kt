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

import android.animation.TimeInterpolator
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
    
    override fun showScrollbar(trackView: View, thumbView: View) {
        if (!mShowingScrollbar) {
            mShowingScrollbar = true
            
            animateView(view = trackView,
                        alpha = 1f,
                        translationX = 0f,
                        duration = SHOW_DURATION_MILLIS,
                        interpolator = SHOW_SCROLLBAR_INTERPOLATOR)
            
            animateView(view = thumbView,
                        alpha = 1f,
                        translationX = 0f,
                        duration = SHOW_DURATION_MILLIS,
                        interpolator = SHOW_SCROLLBAR_INTERPOLATOR)
        }
    }
    
    override fun hideScrollbar(trackView: View, thumbView: View) {
        if (mShowingScrollbar) {
            mShowingScrollbar = false
            val isLayoutRtl = mView.layoutDirection == View.LAYOUT_DIRECTION_RTL
            val width = trackView.width.coerceAtLeast(thumbView.width).toFloat()
            val translationX: Float =
                if (isLayoutRtl) {
                    if (trackView.left == 0) - width else 0f
                }
                else {
                    if (trackView.right == mView.width) width else 0f
                }
            
            animateView(view = trackView,
                        alpha = 0f,
                        translationX = translationX,
                        duration = HIDE_DURATION_MILLIS,
                        interpolator = HIDE_SCROLLBAR_INTERPOLATOR)
            
            animateView(view = thumbView,
                        alpha = 0f,
                        translationX = translationX,
                        duration = HIDE_DURATION_MILLIS,
                        interpolator = HIDE_SCROLLBAR_INTERPOLATOR)
        }
    }
    
    override val isScrollbarAutoHideEnabled: Boolean
        get() = mScrollbarAutoHideEnabled
    
    fun setScrollbarAutoHideEnabled(enabled: Boolean) {
        mScrollbarAutoHideEnabled = enabled
    }
    
    override val scrollbarAutoHideDelayMillis: Int
        get() = AUTO_HIDE_SCROLLBAR_DELAY_MILLIS
    
    override fun showPopup(popupView: View) {
        if (!mShowingPopup) {
            mShowingPopup = true
            animatePopupView(popupView = popupView,
                             alpha = 1f,
                             duration = SHOW_DURATION_MILLIS)
        }
    }
    
    override fun hidePopup(popupView: View) {
        if (mShowingPopup) {
            mShowingPopup = false
            animatePopupView(popupView = popupView,
                             alpha = 0f,
                             duration = HIDE_DURATION_MILLIS)
        }
    }
    
    private fun animateView(view: View,
                            alpha: Float,
                            translationX: Float,
                            duration: Long,
                            interpolator: TimeInterpolator) {
        view.animate()
            .alpha(alpha)
            .translationX(translationX)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .start()
    }
    
    private fun animatePopupView(popupView: View,
                                 alpha: Float,
                                 duration: Long) {
        popupView.animate()
            .alpha(alpha)
            .setDuration(duration)
            .start()
    }
}