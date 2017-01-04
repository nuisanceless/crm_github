package com.drugoogle.sellscrm.visit;

import com.drugoogle.sellscrm.Utils.DateUtils;

import java.io.Serializable;

/**
 * Created by wuguohao on 4/6/16.
 */
public class CustomCalendarDate implements Serializable
{
    public static final long serialVersionUID = 1L;

    public int year, month, day, week;


    public CustomCalendarDate ()
    {
        this.year = DateUtils.getCurrentYear();
        this.month = DateUtils.getCurrentMonth();
        this.day = DateUtils.getCurrentMonthDay();
    }


    public CustomCalendarDate(int year,int month,int day){
        if(month > 12){
            month = 1;
            year++;
        }else if(month <1){
            month = 12;
            year--;
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }



    @Override
    public String toString() {
        return year+"-"+month+"-"+day;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }
}
