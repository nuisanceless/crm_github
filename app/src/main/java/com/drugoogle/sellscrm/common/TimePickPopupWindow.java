package com.drugoogle.sellscrm.common;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TimePicker;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;

import java.util.Date;

/**
 * Created by wuguohao on 16/5/2.
 */
public class TimePickPopupWindow extends PopupWindow implements TimePicker.OnTimeChangedListener
{
    private Context mContext;
    private View mContentView;
    private TimePicker mTimePicker;

    private ChangeTimeCallback mChangeTimeCallback;

    public interface ChangeTimeCallback {
        void onChange(int hourOfDay, int minute);
    }

    /**
     * @param duration 时长分钟数
     * */
    public TimePickPopupWindow (Context context, ChangeTimeCallback callback, int duration)
    {
        super(context);
        this.mContext = context;
        this.mChangeTimeCallback = callback;
        initPopupWindow(duration);
    }

    void initPopupWindow (int duration)
    {
        //LayoutInflater inflater = LayoutInflater.from(mContext);
        this.mContentView = View.inflate(mContext, R.layout.time_picker_layout, null);
        mTimePicker = (TimePicker)mContentView.findViewById(R.id.time_picker);
        mTimePicker.setIs24HourView(true);
        mTimePicker.setCurrentHour(duration / 60);
        mTimePicker.setCurrentMinute(duration % 60);
        mTimePicker.setOnTimeChangedListener(this);
        this.setContentView(mContentView);
        // 设置弹出窗体的宽
        this.setWidth(CommonUtils.getScreenWidth(mContext) / 2);
        // 设置弹出窗体的高
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        // 设置弹出窗体可点击
        this.setTouchable(true);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        //设置弹出窗体的背景
        this.setBackgroundDrawable(new BitmapDrawable());
    }

    public void setChangeTimeCallback (ChangeTimeCallback callback)
    {
        this.mChangeTimeCallback = callback;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
    {
        mChangeTimeCallback.onChange(hourOfDay, minute);
    }
}
