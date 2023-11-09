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

import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.util.Consumer

class PopupStyles {
    
    companion object {
    
        val MD1 = Consumer { popupView: TextView ->
            val resources = popupView.resources
            val minimumSize = resources.getDimensionPixelSize(R.dimen.afs_popup_min_size)
            popupView.minimumWidth = minimumSize
            popupView.minimumHeight = minimumSize
            val layoutParams = popupView.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            layoutParams.marginEnd = resources.getDimensionPixelOffset(R.dimen.afs_popup_margin_end)
            popupView.layoutParams = layoutParams
            val context = popupView.context
            popupView.background = AutoMirrorDrawable(Utils.getGradientDrawableWithTintAttr(R.drawable.afs_md1_popup_background,
                                                                                            android.R.attr.colorControlActivated,
                                                                                            context) !!)
            popupView.ellipsize = TextUtils.TruncateAt.MIDDLE
            popupView.gravity = Gravity.CENTER
            popupView.includeFontPadding = false
            popupView.isSingleLine = true
            popupView.setTextColor(Utils.getColorFromAttrRes(android.R.attr.textColorPrimaryInverse,
                                                             context))
            popupView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                R.dimen.afs_popup_text_size).toFloat())
        }
    
        val DEFAULT = Consumer { popupView: TextView ->
            val resources = popupView.resources
            popupView.minimumWidth = resources.getDimensionPixelSize(
                R.dimen.afs_md2_popup_min_width)
            popupView.minimumHeight = resources.getDimensionPixelSize(
                R.dimen.afs_md2_popup_min_height)
            val layoutParams = popupView.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            layoutParams.marginEnd = resources.getDimensionPixelOffset(
                R.dimen.afs_md2_popup_margin_end)
            popupView.layoutParams = layoutParams
            val context = popupView.context
            popupView.background = DefaultPopupBackground(context)
            popupView.elevation = resources.getDimensionPixelOffset(R.dimen.afs_md2_popup_elevation).toFloat()
            popupView.ellipsize = TextUtils.TruncateAt.MIDDLE
            popupView.gravity = Gravity.CENTER
            popupView.includeFontPadding = false
            popupView.isSingleLine = true
            popupView.setTextColor(Utils.getColorFromAttrRes(android.R.attr.textColorPrimaryInverse,
                                                             context))
            popupView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                R.dimen.afs_md2_popup_text_size).toFloat())
        }
    
    }
}