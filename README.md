# AndroidFastScroll-kt

Fast scroll for Android `RecyclerView`.

- This is a Kotlin rewrite of [AndroidFastScroll](https://github.com/zhanghai/AndroidFastScroll) with better popup position.
- This is **not** an officially supported Google product.


## Contents
- [Why AndroidFastScroll-kt?](#why-androidfastscroll-kt)
- [Preview](#preview)
- [Integration](#integration)
- [Usage](#usage)
- [License](#license)



## Why AndroidFastScroll-kt?
- Fully customizable: Override track, thumb, popup, animation and scrolling.
- Easy-to-use defaults: Predefined default style, Material Design 1 style and animation.
- Extensive view support: Out-of-box support for `RecyclerView`, `ScrollView`, `NestedScrollView` and `WebView`, plus any view with a `ViewHelper` implementation.
- Window insets friendly: Support setting a separate padding for scrollbar.
- Clean implementation: Decoupled touch handling, animation and scrolling logic.



## Preview

https://github.com/StellarSand/AndroidFastScroll-kt/assets/68168200/c9fb4ce0-5c0a-43ad-b9bc-06a611b1e1c4


| Default style | Material Design 1 style |
| - | - |
| <img src="assets/default_style.png" width="250" /> | <img src="assets/MD1_style.png" width="250" /> |



## Integration

- **Gradle (Kotlin):**
```gradle
dependencies {
    implementation("com.github.StellarSand:AndroidFastScroll-kt:v1.0.0")
}
```

- **Gradle (Groovy):**
```gradle
dependencies {
    implementation 'com.github.StellarSand:AndroidFastScroll-kt:v1.0.0'
}
```



## Usage
- Simply create a `FastScroller` as follows:
```kotlin
FastScrollerBuilder(recyclerView).build()
```

- You can also implement [`PopupTextProvider`](library/src/main/java/me/stellarsand/android/fastscroll/PopupTextProvider.kt) in your `RecyclerView.Adapter` to show a popup.
```kotlin
class RvAdapter(
    private val aListViewItems: ArrayList<RvData>
): RecyclerView.Adapter<RvAdapter.ListViewHolder>(), PopupTextProvider {
    
    // .... other code ....
    
    override fun getPopupText(view: View, position: Int): CharSequence {
        return aListViewItems[position].title.substring(0, 1)
    }

}
```

- For more customization, please use the methods on [`FastScrollerBuilder`](library/src/main/java/me/stellarsand/android/fastscroll/FastScrollerBuilder.kt). Namely:
    - `setViewHelper()` - allows providing a custom `ViewHelper` to support more views.
    - `setPopupTextProvider()` - allows providing a custom `PopupTextProvider` if your `RecyclerView.Adapter` cannot implement that interface.
    - `setPadding()` - allows setting a custom padding for the scrollbar, instead of the padding of the view.
    - `setTrackDrawable()` and `setThumbDrawable()` - allow setting custom drawables for the scrollbar. The `android:state_pressed` state will be updated for them so you can use a selector. The track drawable needs to have an intrinsic width and the thumb drawable needs to have an intrinsic size, in order to allow proper touch event handling.
    - `setPopupStyle()` - allows customizing the popup view with a lambda that will receive the view.
    - `setAnimationHelper()` - allows providing a custom `AnimationHelper` to use an alternative scrollbar animation.
    - `disableScrollbarAutoHide()` - allows disabling the auto hide animation for scrollbar. This implies using a `DefaultAnimationHelper`.
    - `useDefaultStyle()` and `useMd1Style()` - allow using the predefined styles, which sets the drawables and popup style. `useDefaultStyle()`, as its name suggests, is the default style when a `FastScrollerBuilder` is created.

- The default `ViewHelper` implementation for `RecyclerView` supports both `LinearLayoutManager` and `GridLayoutManager`, but assumes that each item has the same height when calculating scroll, as there's no common way to deal with variable item height. If you know how to measure for scrolling in your specific case, you can provide your own `ViewHelper` implementation and fast scroll will work correctly again.

- If you are using any `RecyclerView.ItemDecoration` that implements `onDrawOver()`, you might be interested in [`FixItemDecorationRecyclerView`](library/src/main/java/me/stellarsand/android/fastscroll/FixItemDecorationRecyclerView.kt) which can fix the drawing order.

- If you are using any other library that makes use of `RecyclerView.OnItemTouchListener` (e.g. `recyclerview-selection`), you might be interested in [`FixOnItemTouchListenerRecyclerView`](library/src/main/java/me/stellarsand/android/fastscroll/FixOnItemTouchListenerRecyclerView.kt) which can correctly handle cancellations when dispatching touch events to listeners. You may also want to configure this library before others so that this library can take precedence in touch event handling.



## License
This project is licensed under the terms of [Apache v2.0 license](https://github.com/StellarSand/AndroidFastScroll-kt/blob/main/LICENSE).
