package com.drugoogle.sellscrm.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TimePicker;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by wgh on 2016/4/29.
 */
public class DateTimePickPopupWindow
        extends PopupWindow
        implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener
{
    private Context mContext;
    private View mContentView;
    private DatePicker datePicker;
    private TimePicker timePicker;

    private int mWidth;
    private int mHeight;
    private int mScreenWidth;
    private static String mInitDateTime;
    public static ChangeTimeCallback mChangeTimeCallback;

    public interface ChangeTimeCallback {
        void onChange(Date date);
    }

    public DateTimePickPopupWindow(Context context) {
        super(context);
        this.mContext = context;
        this.initPopupWindow();
    }

    private void initPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContentView = inflater.inflate(R.layout.date_time_picker_popup_window, null);

        mScreenWidth = CommonUtils.getScreenWidth(mContext);

        datePicker = (DatePicker) mContentView.findViewById(R.id.date_picker);
        timePicker = (TimePicker) mContentView.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        try
        {
            fixPickerPosition(datePicker, timePicker);
        }
        catch (Exception e)
        {
            //ignore
        }

        init(datePicker, timePicker);



        this.setContentView(mContentView);
        // 设置弹出窗体的宽
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        // 设置弹出窗体的高
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        // 设置弹出窗体可点击
        this.setTouchable(true);
        this.setFocusable(true);
        // 设置点击是否消失
        this.setOutsideTouchable(true);
        //设置弹出窗体动画效果
        //this.setAnimationStyle(R.style.PopupAnimation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable background = new ColorDrawable(0x4f000000);
        //设置弹出窗体的背景
        this.setBackgroundDrawable(new BitmapDrawable());
        // 绘制
        this.mandatoryDraw();
    }

    private void fixPickerPosition(DatePicker datePicker, TimePicker timePicker)
    {
        //获取内部子视图，然后编辑他的宽度
        LinearLayout dpContainer = (LinearLayout)datePicker.getChildAt(0)   ;   // LinearLayout
//        for(int i = 0; i < dpContainer.getChildCount(); i ++)
//        {
//            View v = dpContainer.getChildAt(i);     // 0-2 : NumberPicker
//            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)v.getLayoutParams();
//            params1.weight = 1;
//            v.setLayoutParams(params1);
//        }
        LinearLayout dpSpinner = (LinearLayout)dpContainer.getChildAt(0);       // 0 : LinearLayout; 1 : CalendarView
        for(int i = 0; i < dpSpinner.getChildCount(); i ++)
        {
            View numPicker = dpSpinner.getChildAt(i);     // 0-2 : NumberPicker
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(mScreenWidth / 7, LinearLayout.LayoutParams.WRAP_CONTENT);
            params1.leftMargin = 15;
            params1.rightMargin = 15;
            numPicker.setLayoutParams(params1);

//            EditText cusET = (EditText)numPicker.getChildAt(0);     // CustomEditText
//            cusET.setTextSize(14);
//            cusET.setWidth(70);
        }


        //5.0之前的系统和之后的系统的timePicker布局不一样。
        if (CommonUtils.getRomVersion() >= Build.VERSION_CODES.LOLLIPOP)
        {
            // Change TimePicker layout
            LinearLayout tpContainer = (LinearLayout)timePicker.getChildAt(0);
            LinearLayout tpSpinner = (LinearLayout)tpContainer.getChildAt(0);
            LinearLayout tpSpinner2 = (LinearLayout)tpContainer.getChildAt(1);
            for(int i = 0; i < tpSpinner2.getChildCount(); i ++)
            {
                // child(1) is a TextView ( : ) 小时和分钟之间有冒号
                if (i == 1) {
                    tpSpinner2.getChildAt(i).setVisibility(View.GONE);
                    continue;
                }
                View numPicker = tpSpinner2.getChildAt(i);     // 0 : NumberPicker; 1 : TextView; 2 : NumberPicker
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(mScreenWidth / 7, LinearLayout.LayoutParams.WRAP_CONTENT);
                params2.leftMargin = 15;
                params2.rightMargin = 15;
                numPicker.setLayoutParams(params2);

//            EditText cusET = (EditText)numPicker.getChildAt(0);     // CustomEditText
//            cusET.setTextSize(14);
//            cusET.setWidth(70);
            }
        }
        else
        {
            // Change TimePicker layout
            LinearLayout tpContainer = (LinearLayout)timePicker.getChildAt(0);
            LinearLayout tpSpinner2 = (LinearLayout)tpContainer.getChildAt(0);
            LinearLayout tpSpinner = (LinearLayout)tpContainer.getChildAt(1);
            for(int i = 0; i < tpSpinner2.getChildCount(); i ++)
            {
                // child(1) is a TextView ( : ) 小时和分钟之间有冒号
                if (i == 1) {
                    tpSpinner2.getChildAt(i).setVisibility(View.GONE);
                    continue;
                }
                View numPicker = tpSpinner2.getChildAt(i);     // 0 : NumberPicker; 1 : TextView; 2 : NumberPicker
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(mScreenWidth / 7, LinearLayout.LayoutParams.WRAP_CONTENT);
                params2.leftMargin = 15;
                params2.rightMargin = 15;
                numPicker.setLayoutParams(params2);

//            EditText cusET = (EditText)numPicker.getChildAt(0);     // CustomEditText
//            cusET.setTextSize(14);
//            cusET.setWidth(70);
            }
        }
    }
    /**
     * 强制绘制popupWindowView，并且初始化popupWindowView的尺寸
     */
    private void mandatoryDraw() {
        this.mContentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        /**
         * 强制刷新后拿到PopupWindow的宽高
         */
        this.mWidth = this.mContentView.getMeasuredWidth();
        this.mHeight = this.mContentView.getMeasuredHeight();
    }




    public void init(DatePicker datePicker, TimePicker timePicker) {

        Calendar calendar = Calendar.getInstance();
        if (null == mInitDateTime || "".equals(mInitDateTime))
        {
            mInitDateTime = calendar.get(Calendar.YEAR) + "/"
                    + calendar.get(Calendar.MONTH) + "/"
                    + calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                    + calendar.get(Calendar.MINUTE);
        } else {
            calendar = this.getCalenderFromString(mInitDateTime);
        }

        datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), this);
        timePicker.setOnTimeChangedListener(this);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }



    public static class PopupWindowBuilder
    {
        private static String activityHashCode;
        private static DateTimePickPopupWindow popupWindow;
        public static PopupWindowBuilder ourInstance;

        public static PopupWindowBuilder getInstance(Activity activity, String date,  ChangeTimeCallback callback) {
            mChangeTimeCallback = callback;
            if (ourInstance == null) ourInstance = new PopupWindowBuilder();
            String hashCode = String.valueOf(activity.hashCode());
            /**
             * 不同一个Activity
             */
            if (!hashCode.equals(String.valueOf(activityHashCode))) {
                activityHashCode = hashCode;
                mInitDateTime = date;
                popupWindow = new DateTimePickPopupWindow(activity);
            }
            return ourInstance;
        }

        public PopupWindowBuilder setTouchable(boolean touchable) {
            popupWindow.setTouchable(touchable);
            return this;
        }

        public PopupWindowBuilder setAnimationStyle(int animationStyle) {
            popupWindow.setAnimationStyle(animationStyle);
            return this;
        }

        public PopupWindowBuilder setBackgroundDrawable(Drawable background) {
            popupWindow.setBackgroundDrawable(background);
            return this;
        }

        public DateTimePickPopupWindow getPopupWindow(String initDateTime) {
            mInitDateTime = initDateTime;
//            mChangeTimeCallback = callback;
            popupWindow.update();
            return popupWindow;
        }

        public PopupWindowBuilder setOnDismissListener (PopupWindow.OnDismissListener listener)
        {
            popupWindow.setOnDismissListener(listener);
            return this;
        }

    }






    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        onDateChanged(null, 0, 0, 0);
    }

    public void onDateChanged(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
        // 获得日历实例
        Calendar calendar = Calendar.getInstance();

        calendar.set(datePicker.getYear(), datePicker.getMonth(),
                datePicker.getDayOfMonth(), timePicker.getCurrentHour(),
                timePicker.getCurrentMinute());
        SimpleDateFormat sdf = new SimpleDateFormat(Consts.DATE_FORMAT);

        //dateTime = sdf.format(calendar.getTime());
        mChangeTimeCallback.onChange(calendar.getTime());
    }




    private Calendar getCalenderFromString (String initDateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat(Consts.DATE_FORMAT);
        Date date = null;
        try {
            date = sdf.parse(initDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}
