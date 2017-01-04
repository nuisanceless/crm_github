package com.drugoogle.sellscrm.customviews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by fanzhang on 2016/3/17.
 */
public class UndraggableViewPager extends ViewPager
{
    public UndraggableViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        return true;
    }
}
