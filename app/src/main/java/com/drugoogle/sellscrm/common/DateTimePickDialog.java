package com.drugoogle.sellscrm.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by wuguohao on 16/3/25.
 */
public class DateTimePickDialog implements DatePicker.OnDateChangedListener,
        TimePicker.OnTimeChangedListener
{
    private DatePicker datePicker;
    private TimePicker timePicker;
    private AlertDialog ad;
    private String dateTime;
    private String initDateTime;
    private Activity activity;

    /**
     * 日期时间弹出选择框构造函数
     *
     * @param activity
     *            ：调用的父activity
     * @param initDateTime
     *            初始日期时间值，作为弹出窗口的标题和日期时间初始值
     */
    public DateTimePickDialog(Activity activity, String initDateTime) {
        this.activity = activity;
        this.initDateTime = initDateTime;

    }

    public void init(DatePicker datePicker, TimePicker timePicker) {
        Calendar calendar = Calendar.getInstance();
        if (!(null == initDateTime || "".equals(initDateTime))) {
            calendar = this.getCalenderFromString(initDateTime);
        } else {
            initDateTime = calendar.get(Calendar.YEAR) + "/"
                    + calendar.get(Calendar.MONTH) + "/"
                    + calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                    + calendar.get(Calendar.MINUTE);
        }

        datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), this);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }


    public interface ConfirmCallback {
          void onFinish(String dateTime);
    }

    /**
     * 弹出日期时间选择框方法
     *
     * @param inputDate
     *            :为需要设置的日期时间文本编辑框
     * @return
     */
    public AlertDialog dateTimePicKDialog(final TextView inputDate, final ConfirmCallback callback)
    {
        LinearLayout dateTimeLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.date_time_picker_popup_window, null);
        datePicker = (DatePicker) dateTimeLayout.findViewById(R.id.date_picker);
        timePicker = (TimePicker) dateTimeLayout.findViewById(R.id.time_picker);

        init(datePicker, timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(this);

        ad = new AlertDialog.Builder(activity)
                .setTitle(initDateTime)
                .setView(dateTimeLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Calendar calendar = Calendar.getInstance();

                        calendar.set(datePicker.getYear(), datePicker.getMonth(),
                                datePicker.getDayOfMonth(), timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute());
                        int weekNum = calendar.get(Calendar.DAY_OF_WEEK);
                        String weekStr = CommonUtils.GetWeekStr(weekNum - 1);

                        inputDate.setText(dateTime + " " + weekStr);
                        callback.onFinish(dateTime);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();

        onDateChanged(null, 0, 0, 0);
        return ad;
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

        dateTime = sdf.format(calendar.getTime());
        int weekNum = calendar.get(Calendar.DAY_OF_WEEK);
        String weekStr = CommonUtils.GetWeekStr(weekNum - 1);
        ad.setTitle(dateTime + " " + weekStr);
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
