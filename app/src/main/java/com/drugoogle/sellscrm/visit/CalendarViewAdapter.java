package com.drugoogle.sellscrm.visit;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

/**
 * Created by wuguohao on 4/7/16.
 */
public class CalendarViewAdapter<V extends View> extends PagerAdapter
{

    private V[] views;

    public CalendarViewAdapter (V[] views)
    {
        super();
        this.views = views;
    }


    @Override
    public int getCount()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == ((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        if (((ViewPager)container).getChildCount() == views.length)
        {
            ((ViewPager) container).removeView(views[position % views.length]);
        }

        ((ViewPager) container).addView(views[position % views.length], 0);
        return views[position % views.length];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        ((ViewPager) container).removeView((View) container);
    }

    public V[] getAllItems()
    {
        return views;
    }

    public V getItem (int position)
    {
        return views[position % views.length];
    }
}
