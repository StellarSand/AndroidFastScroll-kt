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
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat
import androidx.core.graphics.drawable.DrawableCompat

internal class AutoMirrorDrawable(drawable: Drawable) : DrawableWrapperCompat(drawable) {
    
    override fun draw(canvas: Canvas) {
        if (isMirroringNeeded()) {
            val centerX = bounds.exactCenterX()
            canvas.scale(-1f, 1f, centerX, 0f)
            super.draw(canvas)
            canvas.scale(-1f, 1f, centerX, 0f)
        }
        else {
            super.draw(canvas)
        }
    }
    
    override fun onLayoutDirectionChanged(layoutDirection: Int): Boolean {
        super.onLayoutDirectionChanged(layoutDirection)
        return true
    }
    
    override fun isAutoMirrored(): Boolean {
        return true
    }
    
    private fun isMirroringNeeded(): Boolean {
        return DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_RTL
    }
    
    override fun getPadding(padding: Rect): Boolean {
        val hasPadding = super.getPadding(padding)
        if (isMirroringNeeded()) {
            val paddingStart = padding.left
            val paddingEnd = padding.right
            padding.left = paddingEnd
            padding.right = paddingStart
        }
        return hasPadding
    }
}