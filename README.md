## SwipeHorizontalScrollView
自定义的仿同花顺自选股控件，支持向右侧滑，联动滚动。

## 如何使用
[详细介绍](https://juejin.cn/post/7082759711824543752)
`app:needHideLeft="true"`设置是否隐藏firstView
`app:needShadow="true"`设置是否需要阴影

```xml
<com.loren.component.view.widget.SwipeHorizontalScrollView id="@+id/swipeHorizontalView">
...滚动的view
</com.loren.component.view.widget.SwipeHorizontalScrollView>
```

```xml
<com.loren.component.view.widget.HorizontalRecyclerView
        android:id="@+id/rvStock"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:needHideLeft="true"
        app:needShadow="true" />
```

## :camera_flash: Screenshots

<img src="/snapshot/screen.gif" width="260">
