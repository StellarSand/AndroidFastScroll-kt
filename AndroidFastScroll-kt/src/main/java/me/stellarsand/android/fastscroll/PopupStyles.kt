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
        
        private fun TextView.applyCommonStyles() {
            val context = this.context
            this.apply {
                ellipsize = TextUtils.TruncateAt.MIDDLE
                gravity = Gravity.CENTER
                includeFontPadding = false
                isSingleLine = true
                setTextColor(Utils.getColorFromAttrRes(android.R.attr.textColorPrimaryInverse, context))
            }
        }
        
        private fun TextView.applyLayoutParams(gravity: Int, marginEnd: Int) {
            val layoutParams = this.layoutParams as FrameLayout.LayoutParams
            layoutParams.apply {
                this.gravity = gravity
                this.marginEnd = marginEnd
            }
            this.layoutParams = layoutParams
        }
        
        val MD1 =
            Consumer { popupView: TextView ->
                val resources = popupView.resources
                val minimumSize = resources.getDimensionPixelSize(R.dimen.afs_md1_popup_min_size)
                popupView.apply {
                    applyLayoutParams(Gravity.RIGHT or Gravity.CENTER_VERTICAL, resources.getDimensionPixelOffset(R.dimen.afs_md1_popup_margin_end))
                    minimumWidth = minimumSize
                    minimumHeight = minimumSize
                    background = AutoMirrorDrawable(Utils.getGradientDrawableWithTintAttr(R.drawable.afs_md1_popup_background,
                                                                                          android.R.attr.colorControlActivated, context)!!)
                    applyCommonStyles()
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                        R.dimen.afs_md1_popup_text_size).toFloat())
                }
            }
        
        val DEFAULT =
            Consumer { popupView: TextView ->
                val resources = popupView.resources
                popupView.apply {
                    applyLayoutParams(Gravity.CENTER_HORIZONTAL or Gravity.TOP, resources.getDimensionPixelOffset(R.dimen.afs_default_popup_margin_end))
                    minimumWidth = resources.getDimensionPixelSize(R.dimen.afs_default_popup_min_width)
                    minimumHeight = resources.getDimensionPixelSize(R.dimen.afs_default_popup_min_height)
                    background = DefaultPopupBackground(context)
                    elevation = resources.getDimensionPixelOffset(R.dimen.afs_default_popup_elevation).toFloat()
                    applyCommonStyles()
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                        R.dimen.afs_default_popup_text_size).toFloat())
                }
            }
        
    }
}