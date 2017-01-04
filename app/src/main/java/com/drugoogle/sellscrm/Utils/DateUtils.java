package com.drugoogle.sellscrm.Utils;

import android.annotation.SuppressLint;

import com.drugoogle.sellscrm.visit.CustomCalendarDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by wuguohao on 4/6/16.
 */
public class DateUtils
{
    public static String[] weekName = { "日", "一", "二", "三", "四", "五","六" };
    public static String[] weekNameTwo = { "周日", "周一", "周二", "周三", "周四", "周五","周六" };

    /**
     * 获取指定月份的天数
     * */
    public static int getMonthDays(int year, int month)
    {
        if (month > 12)
        {
            month = 1;
            year += 1;
        }
        else if (month < 1)
        {
            month = 12;
            year -= 1;
        }
        int[] arr = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        int days = 0;

        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
        {
            arr[1] = 29; // 闰年2月29天
        }

        try
        {
            days = arr[month - 1];
        } catch (Exception e) {
            e.getStackTrace();
        }

        return days;
    }


    /**
     * 获取当前日期的年份
     * */
    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * 获取当前日期的月份
     * */
    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    /**
     * 获取当前日期是当月第几天
     * */
    public static int getCurrentMonthDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前日期是当周的第几天
     * 以周日为一周的第一天
     * 第一天索引为0，第七天索引为6
     * */
    public static int getWeekDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * 获取指定日期是当周的第几天
     * 以周日为一周的第一天
     * 第一天索引为0，第七天索引为6
     * */
    public static int getWeekDay(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static int getHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public static int getWeekDayFromDate(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getFirstDayOfMonth(year, month));
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return week_index;
    }



    /**
     * 获取当月的第一天
     * */
    public static Date getFirstDayOfMonth (int year, int month)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getTime();
    }


    /**
     * 获取当月的第一天
     * */
    public static Date getFirstDayOfMonth (Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    /**
     * 获取当月的最后一天
     * */
    public static Date getEndDayOfMonth (Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }


    public static boolean isToday(CustomCalendarDate date){
        return(date.year == getCurrentYear() &&
                date.month == getCurrentMonth()
                && date.day == getCurrentMonthDay());
    }


    public static boolean isCurrentMonth(CustomCalendarDate date){
        return(date.year == getCurrentYear() &&
                date.month == getCurrentMonth());
    }

    public static boolean isCurrentMonth(int year, int month){
        return(year == DateUtils.getCurrentYear() &&
                month == DateUtils.getCurrentMonth());
    }

    /**
     * 判断指定日期是否在今天所在的周里面
     * */
    public static boolean inSameWeekWithToday(Date date)
    {
        return inSameWeek(new Date(), date);
    }

    /**
     * 判断date2是否在date1所在的周里面
     * */
    public static boolean inSameWeek (Date date1, Date date2)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);

        calendar.add(Calendar.DAY_OF_MONTH, (1-dayInWeek));
        Date firstDay = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 7);
        Date endDay = calendar.getTime();

        if (date2.after(firstDay) && date2.before(endDay))
            return true;
        else
            return false;
    }

    /**
     * 判断date1和date2是否在同一个月里面
     * */
    public static boolean inSameMonth (Date date1, Date date2)
    {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date1);
        int year1 = calendar.get(Calendar.YEAR);
        int month1 = calendar.get(Calendar.MONTH);

        calendar.setTime(date2);
        int year2 = calendar.get(Calendar.YEAR);
        int month2 = calendar.get(Calendar.MONTH);

        return (year1 == year2 && month1 == month2);
    }

    /**
     * 获取指定日期的下一周（指定日期下一周的第一天）
     * */
    public static Date getNextWeek(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_MONTH, 7 - dayOfWeek + 1);
        return calendar.getTime();
    }

    /**
     * 获取指定日期的上一周（指定日期上一周的第一天）
     * */
    public static Date getLastWeek(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_MONTH, 1 - dayOfWeek - 7);
        return calendar.getTime();
    }


    /**
     * 获取指定日期所在周的第一天
     * 周日为第一天
     * */
    public static Date getFirstDayOfThisWeek (Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_MONTH, 1 - dayOfWeek);
        return calendar.getTime();
    }


    /**
     * 获取指定日期所在周的最后一天
     * 周日为第一天，周六为最后一天
     * */
    public static Date getEndDayOfThisWeek (Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_MONTH, 7 - dayOfWeek);
        return calendar.getTime();
    }


    public static int getGapCount(Date startDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        int result = (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));

        return result < 1 ? 1 : result;
    }

    public static Date getTomorrow (Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public static Date getYesterday (Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public static boolean isSameDay (Date date1, Date date2)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int year1 = calendar.get(Calendar.YEAR);
        int month1 = calendar.get(Calendar.MONTH);
        int day1 = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTime(date2);
        int year2 = calendar.get(Calendar.YEAR);
        int month2 = calendar.get(Calendar.MONTH);
        int day2 = calendar.get(Calendar.DAY_OF_MONTH);

        if (year1 == year2 && month1 == month2 && day1 == day2)
            return true;
        else
            return false;
    }


}
