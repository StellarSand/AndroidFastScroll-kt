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

class FastScrollerBuilder(private val mView: ViewGroup) {
    private var mViewHelper: FastScroller.ViewHelper? = null
    private var mPopupTextProvider: PopupTextProvider? = null
    private var mPadding: Rect? = null
    private var mTrackDrawable: Drawable? = null
    private var mThumbDrawable: Drawable? = null
    private var mPopupStyle: Consumer<TextView?>? = null
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
        mPadding !![left, top, right] = bottom
        return this
    }
    
    fun setPadding(padding: Rect?): FastScrollerBuilder {
        if (padding != null) {
            if (mPadding == null) {
                mPadding = Rect()
            }
            mPadding !!.set(padding)
        }
        else {
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
    
    fun setPopupStyle(popupStyle: Consumer<TextView?>): FastScrollerBuilder {
        mPopupStyle = popupStyle
        return this
    }
    
    fun useDefaultStyle(): FastScrollerBuilder {
        val context = mView.context
        mTrackDrawable = Utils.getGradientDrawableWithTintAttr(R.drawable.afs_track,
                                                               android.R.attr.colorControlNormal,
                                                               context) !!
        mThumbDrawable = Utils.getGradientDrawableWithTintAttr(R.drawable.afs_thumb,
                                                               android.R.attr.colorControlActivated,
                                                               context) !!
        mPopupStyle = PopupStyles.DEFAULT
        return this
    }
    
    fun useMd2Style(): FastScrollerBuilder {
        val context = mView.context
        mTrackDrawable = Utils.getGradientDrawableWithTintAttr(R.drawable.afs_md2_track,
                                                               android.R.attr.colorControlNormal,
                                                               context) !!
        mThumbDrawable = Utils.getGradientDrawableWithTintAttr(R.drawable.afs_md2_thumb,
                                                               android.R.attr.colorControlActivated,
                                                               context) !!
        mPopupStyle = PopupStyles.MD2
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
        return FastScroller(mView, orCreateViewHelper, mPadding, mTrackDrawable!!,
                            mThumbDrawable!!, mPopupStyle!!, orCreateAnimationHelper)
    }
    
    private val orCreateViewHelper: FastScroller.ViewHelper
        get() {
            if (mViewHelper != null) {
                return mViewHelper as FastScroller.ViewHelper
            }
            return when (mView) {
                is ViewHelperProvider -> {
                    (mView as ViewHelperProvider).viewHelper
                }
                
                is RecyclerView -> {
                    RecyclerViewHelper(mView, mPopupTextProvider)
                }
                
                is NestedScrollView -> {
                    throw UnsupportedOperationException("Please use "
                                                        + FastScrollNestedScrollView::class.java.simpleName + " instead of "
                                                        + NestedScrollView::class.java.simpleName + "for fast scroll")
                }
                
                is ScrollView -> {
                    throw UnsupportedOperationException("Please use "
                                                        + FastScrollScrollView::class.java.simpleName + " instead of "
                                                        + ScrollView::class.java.simpleName + "for fast scroll")
                }
                
                is WebView -> {
                    throw UnsupportedOperationException("Please use "
                                                        + FastScrollWebView::class.java.simpleName + " instead of "
                                                        + WebView::class.java.simpleName + "for fast scroll")
                }
                
                else -> {
                    throw UnsupportedOperationException(mView.javaClass.simpleName
                                                        + " is not supported for fast scroll")
                }
            }
        }
    private val orCreateAnimationHelper: AnimationHelper
        get() = mAnimationHelper ?: DefaultAnimationHelper(mView)
}