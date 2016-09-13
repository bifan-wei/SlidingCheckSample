
## GitHub
- [GitHub SlidingCheckSample](https://github.com/chendongMarch/SlidingCheckSample)

## Gradle
> compile 'com.march.slidingselect:slidingselect:0.0.1'

## Usage
- xml中使用

```xml
	<com.march.slidingselect.SlidingSelectLayout
        android:id="@+id/scl"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/recyclerview"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
            
    </com.march.slidingselect.SlidingSelectLayout>
```

- java代码中配置,将pos和data与view进行绑定以便内部获取

```java
private SlidingSelectLayout mScl;
mScl = getView(R.id.scl);

class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    	mScl.markView(holder.itemView,position,demos.get(position));
    }
}
```

- 监听回调，使用范型获得手指触摸到的位置和当前位置对应的数据进行更新adapter

```java
mScl.setOnSlidingSelectListener(new SlidingSelectLayout.OnSlidingSelectListener<Demo>() {
            @Override
            public void onSlidingSelect(int pos, View parentView, Demo data) {
                demos.get(pos).isChanged = !demos.get(pos).isChanged;
                adapter.notifyItemChanged(pos);
            }
        });
```

## 推荐阅读

- 原先的思路是根据手指滑动的距离计算行列值，后来使用`findViewUnder(x,y)`方法做了优化[原先的设计](http://blog.csdn.net/chendong_/article/details/52454805)

- [仿微信QQ显示手机相册](https://github.com/chendongMarch/CommonLib/blob/master/baselib/src/main/java/com/march/baselib/ui/activity/SelectImageActivity.java)

- [RecyclerView快速适配器，支持单类型多类型数据适配，预加载更多，添加Header和Footer,九宫格模式显示。。。](http://blog.csdn.net/chendong_/article/details/50897581)

## 演示视频
- [普通模式演示视频](http://7xtjec.com1.z0.glb.clouddn.com/gallery.mp4)

- [九宫格模式演示视频](http://7xtjec.com1.z0.glb.clouddn.com/item_header_sliding_select.mp4)

## 前言
- 显示相册在app中是一个比较常见的操作，大致的操作就是通过ContentProvider获取多媒体资源进行展示，我综合了一下QQ 的和微信的显示效果，实现了一下,[仿微信QQ显示手机相册](https://github.com/chendongMarch/CommonLib/blob/master/baselib/src/main/java/com/march/baselib/ui/activity/SelectImageActivity.java)，在QQ的相册选择时是支持滑动选中的，即手指碰到哪个就选中哪张照片，正好公司的项目中用到了这个功能，在网上找了找没有很好的解决方案，所以通过自定义控件处理事件，这篇文章主要介绍这个功能的实现。


----------
![](http://7xtjec.com1.z0.glb.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202016-09-06%2023.27.04.png)


##  大体思路

- 打算继承`FramLayout`实现，当然继承别的也可以，习惯继承`FramLayout`

- 当手指竖向滑动时，`RecyclerView`处理事件，进行滑动。当手指横向滑动达到阈值自定义控件截断事件自己进行处理。

- 根据手指的滑动获取x,y坐标，使用`RecycelrView`的`findViewUnder(float x,float y)` 的方法，可以直接获取制定位置的View，再使用tag从view中拿到之前使用`mScl.markView()`方法绑定的pos和data数据

- 使用该方法就不会因为动态计算距离而局限于RecyclerView的布局，九宫格模式下仍然可以很好的支持。


## 对外封闭

- 遍历所有的childView获取RecyclerView,获得GridLayoutManager的列数，初始化一些值，旨在尽量简化使用方法，

- 此方法想获得RecyclerView必须将RecyclerView作为该控件的直接childView,为了兼容特殊情况，开放一个API用来设置RecyclerView

```java
private void setTargetRv(RecyclerView mTargetRv) {
        this.mTargetRv = mTargetRv;
}
```

- 遍历子控件获取RecyclerView

```java
	private void ensureTarget() {
        if (mTargetRv != null)
            return;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof RecyclerView) {
                mTargetRv = (RecyclerView) childAt;
                return;
            }
        }
    }
```

- 处理LayoutManager，初始化xTouchSlop，这个值是滑动多大距离触发水平滑动，根据GridLayoutManager的列数来动态设置，当一次水平滑动超过一个item宽度的0.25时触发。

```java
   private void ensureLayoutManager() {
        if (mTargetRv == null || itemSpanCount != INVALID_PARAM)
            return;
        RecyclerView.LayoutManager lm = mTargetRv.getLayoutManager();
        if (lm == null)
            return;
        if (lm instanceof GridLayoutManager) {
            GridLayoutManager glm = (GridLayoutManager) lm;
            itemSpanCount = glm.getSpanCount();
        } else {
            itemSpanCount = 4;
        }
        int size = (int) (getResources().getDisplayMetrics().widthPixels / (itemSpanCount * 1.0f));
        xTouchSlop = yTouchSlop = size * TOUCH_SLOP_RATE;
    }
```

## 拦截事件

```java
private boolean isReadyToIntercept() {
    return mTargetRv != null 
    && mTargetRv.getAdapter() != null 
    && itemSpanCount != INVALID_PARAM;
}

@Override
public boolean onInterceptTouchEvent(MotionEvent ev) {
    
    if (!isEnabled())
        return super.onInterceptTouchEvent(ev);
        
    ensureTarget();
    ensureLayoutManager();
       
    if (!isReadyToIntercept())
        return super.onInterceptTouchEvent(ev);
        
    int action = MotionEventCompat.getActionMasked(ev);
    switch (action) {
        case MotionEvent.ACTION_DOWN:
            // init
            mInitialDownX = ev.getX();
            mInitialDownY = ev.getY();
            break;
        case MotionEvent.ACTION_UP:
            // stop
            isBeingSlide = false;
            break;
        case MotionEvent.ACTION_MOVE:
            // handle
            // 水平滑动超过阈值，垂直滑动没有超过阈值时拦截事件
            float xDiff = Math.abs(ev.getX() - mInitialDownX);
            float yDiff = Math.abs(ev.getY() - mInitialDownY);
            if (yDiff < xTouchSlop && xDiff > yTouchSlop) {
                isBeingSlide = true;
            }
            break;
    }
    return isBeingSlide;
}
```

## 触摸事件

- 重点是up事件时重新初始化一些值

- move事件时处理位置的移动

```java
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                // re init
                isBeingSlide = false;
                preViewPos = INVALID_PARAM;
                break;
            case MotionEvent.ACTION_MOVE:
                // 使用监听发布事件
                publishSlidingCheck(ev);
                break;
        }
        return isBeingSlide;
    }
```

## 处理Move事件
- 从Tag中获取pos

```java
private int getPos(View parentView) {
        int pos = INVALID_PARAM;
        Object tag = parentView.getTag(tagPosKey);
        if (tag != null)
            pos = (int) tag;
        return pos;
    }
```

- 从tag中获取data

```java
private Object getData(View parentView) {
        return parentView.getTag(tagDataKey);
    }
```

- 使用监听向外发布事件,将获取的pos和data通过监听发布

```java
private void publishSlidingCheck(MotionEvent event) {
        float x = generateX(event.getX());
        float y = generateY(event.getY());
        View childViewUnder = mTargetRv.findChildViewUnder(x, y);
        // fast stop
        if (onSlidingSelectListener == null || childViewUnder == null)
            return;
        int pos = getPos(childViewUnder);
        Object data = getData(childViewUnder);
        // fast stop
        if (pos == INVALID_PARAM || preViewPos == pos || data == null)
            return;
        
        try {
            onSlidingSelectListener.onSlidingSelect(pos, childViewUnder, data);
            preViewPos = pos;
        } catch (ClassCastException e) {
            Log.e("SlidingSelect", "ClassCastException:填写的范型有误，无法转换");
        }
    }
```