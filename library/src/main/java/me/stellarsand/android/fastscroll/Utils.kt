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

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat

internal object Utils {
    @ColorInt
    fun getColorFromAttrRes(@AttrRes attrRes: Int, context: Context): Int {
        val colorStateList = getColorStateListFromAttrRes(attrRes, context)
        return colorStateList?.defaultColor ?: 0
    }
    
    fun getColorStateListFromAttrRes(@AttrRes attrRes: Int,
                                     context: Context): ColorStateList? {
        val a = context.obtainStyledAttributes(intArrayOf(attrRes))
        val resId: Int
        return try {
            resId = a.getResourceId(0, 0)
            if (resId != 0) {
                AppCompatResources.getColorStateList(context, resId)
            }
            else a.getColorStateList(0)
        }
        finally {
            a.recycle()
        }
    }
    
    // Work around the bug that GradientDrawable didn't actually implement tinting until
    // Lollipop MR1 (API 22).
    fun getGradientDrawableWithTintAttr(@DrawableRes drawableRes: Int,
                                        @AttrRes tintAttrRes: Int,
                                        context: Context): Drawable? {
        var drawable = AppCompatResources.getDrawable(context, drawableRes)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1
            && drawable is GradientDrawable) {
            drawable = DrawableCompat.wrap(drawable)
            drawable.setTintList(getColorStateListFromAttrRes(tintAttrRes, context))
        }
        return drawable
    }
}