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
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.RecyclerView

class FixItemDecorationRecyclerView(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : RecyclerView (context, attrs, defStyleAttr) {
    
    override fun dispatchDraw(canvas: Canvas) {
        for (i in 0 until itemDecorationCount) {
            val decor = super.getItemDecorationAt(i) as FixItemDecoration
            decor.getState()?.let { state ->
                decor.itemDecoration.onDraw(canvas, this, state)
            }
        }
        super.dispatchDraw(canvas)
        for (i in 0 until itemDecorationCount) {
            val decor = super.getItemDecorationAt(i) as FixItemDecoration
            decor.getState()?.let { state ->
                decor.itemDecoration.onDrawOver(canvas, this, state)
            }
        }
    }
    
    
    override fun addItemDecoration(decor: ItemDecoration, index: Int) {
        super.addItemDecoration(FixItemDecoration(decor), index)
    }
    
    override fun getItemDecorationAt(index: Int): ItemDecoration {
        return (super.getItemDecorationAt(index) as FixItemDecoration).itemDecoration
    }
    
    override fun removeItemDecoration(decor: ItemDecoration) {
        var decorToBeRemoved = decor
        if (decor !is FixItemDecoration) {
            for (i in 0 until itemDecorationCount) {
                val fixDecor = super.getItemDecorationAt(i) as FixItemDecoration
                if (fixDecor.itemDecoration == decor) {
                    decorToBeRemoved = fixDecor
                    break
                }
            }
        }
        super.removeItemDecoration(decorToBeRemoved)
    }
    
    companion object {
        private class FixItemDecoration(val itemDecoration: ItemDecoration) : ItemDecoration() {
    
            private var _state: State? = null
            
            fun getState(): State? = _state
            
            override fun onDraw(c: Canvas, parent: RecyclerView, state: State) {
                _state = state
            }
            
            @Deprecated("Deprecated in Java")
            override fun onDraw(c: Canvas, parent: RecyclerView) {}
            
            override fun onDrawOver(c: Canvas, parent: RecyclerView, state: State) {}
            
            @Deprecated("Deprecated in Java")
            override fun onDrawOver(c: Canvas, parent: RecyclerView) {}
            
            override fun getItemOffsets(outRect: Rect, view: View,
                                        parent: RecyclerView, state: State) {
                itemDecoration.getItemOffsets(outRect, view, parent, state)
            }
        }
    }
}