package com.drugoogle.sellscrm.visit;

import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by wgh on 2016/5/4.
 */
public class OverlayKey
{
    int year;
    int month;
    int day;

    public  OverlayKey(int year, int month, int day)
    {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public OverlayKey dayPlusOne (OverlayKey olk)
    {
        String olkStr = toString();
        Date date = CommonUtils.String2Date(olkStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        OverlayKey overlayKey = new OverlayKey(year, month, day);
        return overlayKey;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof  OverlayKey))
        {
            return false;
        }
        OverlayKey key = (OverlayKey)o;
        boolean eq = key.year == year && key.month == month && key.day == day;
        return  eq;
    }

    @Override
    public int hashCode()
    {
        return  toString().hashCode();
    }

    @Override
    public String toString()
    {
        return  String.format("%04d-%02d-%02d", year, month, day);
    }
}
