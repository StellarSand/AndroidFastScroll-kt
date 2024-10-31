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

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.util.Consumer
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import me.stellarsand.android.fastscroll.FastScroller.AnimationHelper
import me.stellarsand.android.fastscroll.Utils.Companion.getGradientDrawableWithTintAttr

class FastScrollerBuilder(private val mView: ViewGroup) {
    
    private var mViewHelper: FastScroller.ViewHelper? = null
    private var mPopupTextProvider: PopupTextProvider? = null
    private var mPadding: Rect? = null
    private var mTrackDrawable: Drawable? = null
    private var mThumbDrawable: Drawable? = null
    private var mPopupStyle = Consumer<TextView>{}
    private var mAnimationHelper: AnimationHelper? = null
    
    init {
        useDefaultStyle()
    }
    
    fun setViewHelper(viewHelper: FastScroller.ViewHelper?): FastScrollerBuilder {
        mViewHelper = viewHelper
        return this
    }
    
    fun setPopupTextProvider(popupTextProvider: PopupTextProvider?): FastScrollerBuilder {
        mPopupTextProvider = popupTextProvider
        return this
    }
    
    fun setPadding(left: Int, top: Int, right: Int, bottom: Int): FastScrollerBuilder {
        if (mPadding == null) {
            mPadding = Rect()
        }
        mPadding!![left, top, right] = bottom
        return this
    }
    
    fun setPadding(padding: Rect?): FastScrollerBuilder {
        padding?.let {
            if (mPadding == null) {
                mPadding = Rect()
            }
            mPadding!!.set(it)
        } ?: run {
            mPadding = null
        }
        
        return this
    }
    
    fun setTrackDrawable(trackDrawable: Drawable): FastScrollerBuilder {
        mTrackDrawable = trackDrawable
        return this
    }
    
    fun setThumbDrawable(thumbDrawable: Drawable): FastScrollerBuilder {
        mThumbDrawable = thumbDrawable
        return this
    }
    
    fun setPopupStyle(popupStyle: Consumer<TextView>): FastScrollerBuilder {
        mPopupStyle = popupStyle
        return this
    }
    
    fun useMd1Style(): FastScrollerBuilder {
        val context = mView.context
        mTrackDrawable = getGradientDrawableWithTintAttr(R.drawable.afs_md1_track,
                                                               android.R.attr.colorControlNormal,
                                                               context)!!
        mThumbDrawable = getGradientDrawableWithTintAttr(R.drawable.afs_md1_thumb,
                                                               android.R.attr.colorControlActivated,
                                                               context)!!
        mPopupStyle = PopupStyles.MD1
        return this
    }
    
    fun useDefaultStyle(): FastScrollerBuilder {
        val context = mView.context
        mTrackDrawable = getGradientDrawableWithTintAttr(R.drawable.afs_default_track,
                                                               android.R.attr.colorControlNormal,
                                                               context)!!
        mThumbDrawable = getGradientDrawableWithTintAttr(R.drawable.afs_default_thumb,
                                                               android.R.attr.colorControlActivated,
                                                               context)!!
        mPopupStyle = PopupStyles.DEFAULT
        return this
    }
    
    fun setAnimationHelper(animationHelper: AnimationHelper?) {
        mAnimationHelper = animationHelper
    }
    
    fun disableScrollbarAutoHide() {
        val animationHelper = DefaultAnimationHelper(mView)
        animationHelper.setScrollbarAutoHideEnabled(false)
        mAnimationHelper = animationHelper
    }
    
    fun build(): FastScroller {
        return FastScroller(mView, getOrCreateViewHelper(), mPadding, mTrackDrawable!!,
                            mThumbDrawable!!, mPopupStyle, getOrCreateAnimationHelper())
    }
    
    private fun getOrCreateViewHelper(): FastScroller.ViewHelper {
        return mViewHelper ?: when (mView) {
            is ViewHelperProvider -> (mView as ViewHelperProvider).viewHelper
            is RecyclerView -> RecyclerViewHelper(mView, mPopupTextProvider)
            is NestedScrollView -> throwUnsupportedException(NestedScrollView::class.java)
            is ScrollView -> throwUnsupportedException(ScrollView::class.java)
            is WebView -> throwUnsupportedException(WebView::class.java)
            else -> throwUnsupportedException(mView.javaClass)
        }
    }
    
    private fun throwUnsupportedException(currentClass: Class<*>): Nothing {
        val replacementClassName =
            when (currentClass) {
                NestedScrollView::class.java -> FastScrollNestedScrollView::class.java.simpleName
                ScrollView::class.java -> FastScrollScrollView::class.java.simpleName
                WebView::class.java -> FastScrollWebView::class.java.simpleName
                else -> throw UnsupportedOperationException("${currentClass.simpleName} is not supported for fast scroll")
            }
        
        throw UnsupportedOperationException("Please use " +
                                            "$replacementClassName " +
                                            "instead of " +
                                            "${currentClass.simpleName} " +
                                            "for fast scroll")
    }
    
    private fun getOrCreateAnimationHelper(): AnimationHelper {
        return mAnimationHelper ?: DefaultAnimationHelper(mView)
    }
    
}