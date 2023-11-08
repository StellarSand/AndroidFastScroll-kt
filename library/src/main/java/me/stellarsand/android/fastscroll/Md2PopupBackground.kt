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

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.math.sqrt

internal class Md2PopupBackground(context: Context) : Drawable() {
    private val mPaint: Paint = Paint()
    private val mPaddingStart: Int
    private val mPaddingEnd: Int
    private val mPath = Path()
    private val mTempMatrix = Matrix()
    
    init {
        mPaint.isAntiAlias = true
        mPaint.color = Utils.getColorFromAttrRes(android.R.attr.colorControlActivated, context)
        mPaint.style = Paint.Style.FILL
        val resources = context.resources
        mPaddingStart = resources.getDimensionPixelOffset(R.dimen.afs_md2_popup_padding_start)
        mPaddingEnd = resources.getDimensionPixelOffset(R.dimen.afs_md2_popup_padding_end)
    }
    
    override fun draw(canvas: Canvas) {
        canvas.drawPath(mPath, mPaint)
    }
    
    override fun onLayoutDirectionChanged(layoutDirection: Int): Boolean {
        updatePath()
        return true
    }
    
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun isAutoMirrored(): Boolean {
        return true
    }
    
    private fun needMirroring(): Boolean {
        return DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_RTL
    }
    
    @Deprecated("Deprecated in Java",
                ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
    
    override fun onBoundsChange(bounds: Rect) {
        updatePath()
    }
    
    private fun updatePath() {
        mPath.reset()
        val bounds = bounds
        var width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        val r = height / 2
        val sqrt2 = sqrt(2.0).toFloat()
        // Ensure we are convex.
        width = (r + sqrt2 * r).coerceAtLeast(width)
        pathArcTo(mPath, r, r, r, 90f, 180f)
        val o1X = width - sqrt2 * r
        pathArcTo(mPath, o1X, r, r, - 90f, 45f)
        val r2 = r / 5
        val o2X = width - sqrt2 * r2
        pathArcTo(mPath, o2X, r, r2, - 45f, 90f)
        pathArcTo(mPath, o1X, r, r, 45f, 45f)
        mPath.close()
        if (needMirroring()) {
            mTempMatrix.setScale(- 1f, 1f, width / 2, 0f)
        }
        else {
            mTempMatrix.reset()
        }
        mTempMatrix.postTranslate(bounds.left.toFloat(), bounds.top.toFloat())
        mPath.transform(mTempMatrix)
    }
    
    companion object {
        private fun pathArcTo(path: Path, centerX: Float, centerY: Float, radius: Float,
                              startAngle: Float, sweepAngle: Float) {
            path.arcTo(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
                       startAngle, sweepAngle, false)
        }
    }
    
    override fun getPadding(padding: Rect): Boolean {
        if (needMirroring()) {
            padding[mPaddingEnd, 0, mPaddingStart] = 0
        }
        else {
            padding[mPaddingStart, 0, mPaddingEnd] = 0
        }
        return true
    }
    
    override fun getOutline(outline: Outline) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ! mPath.isConvex) {
            // The outline path must be convex before Q, but we may run into floating point error
            // caused by calculation involving sqrt(2) or OEM implementation difference, so in this
            // case we just omit the shadow instead of crashing.
            super.getOutline(outline)
            return
        }
        outline.setConvexPath(mPath)
    }
    
}