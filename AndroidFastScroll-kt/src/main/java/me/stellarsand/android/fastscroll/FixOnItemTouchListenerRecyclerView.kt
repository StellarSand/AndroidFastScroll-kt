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
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.RecyclerView

class FixOnItemTouchListenerRecyclerView(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private val mOnItemTouchDispatcher = OnItemTouchDispatcher()
    
    init {
        super.addOnItemTouchListener(mOnItemTouchDispatcher)
    }
    
    override fun addOnItemTouchListener(listener: OnItemTouchListener) {
        mOnItemTouchDispatcher.addListener(listener)
    }
    
    override fun removeOnItemTouchListener(listener: OnItemTouchListener) {
        mOnItemTouchDispatcher.removeListener(listener)
    }
    
    companion object {
        private class OnItemTouchDispatcher : OnItemTouchListener {
            
            private val mListeners: MutableList<OnItemTouchListener> = ArrayList()
            private val mTrackingListeners: MutableSet<OnItemTouchListener> = LinkedHashSet()
            private var mInterceptingListener: OnItemTouchListener? = null
            
            fun addListener(listener: OnItemTouchListener) {
                mListeners.add(listener)
            }
        
            fun removeListener(listener: OnItemTouchListener) {
                mListeners.remove(listener)
                mTrackingListeners.remove(listener)
                if (mInterceptingListener == listener) {
                    mInterceptingListener = null
                }
            }
        
            // @see RecyclerView#findInterceptingOnItemTouchListener
            override fun onInterceptTouchEvent(recyclerView: RecyclerView,
                                               event: MotionEvent): Boolean {
                val action = event.action
                for (listener in mListeners) {
                    val intercepted = listener.onInterceptTouchEvent(recyclerView, event)
                    if (action == MotionEvent.ACTION_CANCEL) {
                        mTrackingListeners.remove(listener)
                        continue
                    }
                    if (intercepted) {
                        mTrackingListeners.remove(listener)
                        event.action = MotionEvent.ACTION_CANCEL
                        for (trackingListener in mTrackingListeners) {
                            trackingListener.onInterceptTouchEvent(recyclerView, event)
                        }
                        event.action = action
                        mTrackingListeners.clear()
                        mInterceptingListener = listener
                        return true
                    }
                    else {
                        mTrackingListeners.add(listener)
                    }
                }
                return false
            }
        
            override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
                if (mInterceptingListener == null) {
                    return
                }
                mInterceptingListener !!.onTouchEvent(recyclerView, event)
                val action = event.action
                if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                    mInterceptingListener = null
                }
            }
        
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                for (listener in mListeners) {
                    listener.onRequestDisallowInterceptTouchEvent(disallowIntercept)
                }
            }
        }
    }
    
}