## SwipeHorizontalScrollView
自定义的仿同花顺自选股控件，支持向右侧滑，联动滚动。

## 如何使用
[详细介绍](https://juejin.cn/post/7082759711824543752)
- `app:needHideLeft="true|false"`设置是否隐藏firstView
- `app:needShadow="true|false"`设置是否需要阴影
- `app:needVibrate="true|false"`设置是否需要在折叠or展开触发震动效果
- `app:extendThreshold="70dp"`设置展开的阈值
- `app:foldThreshold="60dp"`设置折叠的阈值
- `app:needFixItemPosition="true|false"`设置是否需要自动修正item的位置
- `HorizontalRecyclerView.recordX=-折叠view的width`设置默认进入页面的时候折叠的部分展开

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
        app:extendThreshold="70dp"
        app:foldThreshold="60dp"
        app:needVibrate="true"
        app:needHideLeft="true"
        app:needShadow="true" />
```

## :camera_flash: Screenshots

<img src="/snapshot/screen.gif" width="260">
<img src="/snapshot/fixItemPosition.gif" width="260">
