package com.drugoogle.sellscrm.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import com.drugoogle.sellscrm.R;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by wuguohao on 17/1/7.
 */

public class CustomCalendar extends LinearLayout
{
    private int touchSlop;

    public CustomCalendar(Context context) {
        super(context);
        init(context);
    }

    public CustomCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init (Context context)
    {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        inflate(context, R.layout.view_custom_calendar, this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private float mDownX;
    private float mDownY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //return super.onInterceptTouchEvent(ev);
        boolean intercepted = false;
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                intercepted = false;
                Log.e("ACTION_DOWN", "OUT_intercepted_DOWN_EVENT mDownX=" + mDownX + " mDownY=" + mDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                float disX = ev.getX() - mDownX;
                float disY = ev.getY() - mDownY;
                if ((Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop) || Math.abs(disX) > Math.abs(disY))
                {
                    intercepted = false;
                }
                else
                {
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:
                break;
        }
        Log.e("intercepted", intercepted ? "true" : "false");
        return intercepted;
    }

    float mLastX, mLastY;
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.e("11111", "x=" + event.getX() + "  y=" + event.getY());
        mLastX = event.getRawX();
        mLastY = event.getRawY();
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                Log.e("ACTION_DOWN", "OUT_DOWN_EVENT mDownX=" + mDownX + " mDownY=" + mDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                float disX = event.getX() - mDownX;
                float disY = event.getY() - mDownY;
                Log.e("1111111111", "disX=" + disX + "  disY=" + disY + "mDownX=" + mDownX + " mDownY=" + mDownY);

                //int translationX = (int) (ViewHelper.getTranslationX(this) + disX);
                int translationY = (int) (ViewHelper.getTranslationY(this) + disY);
                //ViewHelper.setTranslationX(this,translationX);
                ViewHelper.setTranslationY(this, translationY);

                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }
        return true;
//        float x = event.getX();
//        float y = event.getY();
//        switch (event.getAction())
//        {
//            case MotionEvent.ACTION_DOWN:
//
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float disX = x - mLastX;
//                float disY = y - mLastY;
//                //int translationX = (int) (ViewHelper.getTranslationX(this) + disX);
//                int translationY = (int) (ViewHelper.getTranslationY(this) + disY);
//                //ViewHelper.setTranslationX(this, translationX);
//                ViewHelper.setTranslationY(this, translationY);
//
//                break;
//            case MotionEvent.ACTION_UP:
//
//                break;
//            default:
//                break;
//        }
//        mLastX = x;
//        mLastY = y;
//        return true;
    }
}
