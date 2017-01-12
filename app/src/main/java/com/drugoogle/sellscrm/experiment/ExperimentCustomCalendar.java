package com.drugoogle.sellscrm.experiment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.drugoogle.sellscrm.R;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by wuguohao on 17/1/7.
 * 在日历滑动实验页面中模拟日历
 */

public class ExperimentCustomCalendar extends LinearLayout
{

    public ExperimentCustomCalendar(Context context) {
        super(context);
        init(context);
    }

    public ExperimentCustomCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExperimentCustomCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init (Context context)
    {
        inflate(context, R.layout.view_experiment_custom_calendar, this);
    }

    private float mDownX;
    private float mDownY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float disX = event.getX() - mDownX;
                float disY = event.getY() - mDownY;

//                int translationX = (int) (ViewHelper.getTranslationX(this) + mDisX);
                int translationY = (int) (ViewHelper.getTranslationY(this) + disY);
//                ViewHelper.setTranslationX(this,translationX);
                ViewHelper.setTranslationY(this,translationY);
                break;
            case MotionEvent.ACTION_UP:

                break;
            default:

                break;
        }
        return true;
    }
}
