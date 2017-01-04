package com.drugoogle.sellscrm.data.type;

import android.content.res.Resources;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.MyApplication_;

/**
 * Created by wgh on 2016/4/27.
 */
public class VisitPeriod
{
    /**仅这次*/
    public static final int ONLY_ONE_TIME = 1;

    /**每日*/
    public static final int EVERY_DAY = 2;

    /**每周*/
    public static final int EVERY_WEEK = 3;

    /**每月*/
    public static final int EVERY_MONTH = 4;

    /**到期之前拜访次数*/
    public static final int FIXED_TIME = 5;

    public static int getVisitPeriodStrRes (int type)
    {
        switch (type)
        {
            case ONLY_ONE_TIME:
                return R.string.only_one_time;
            case EVERY_DAY:
                return R.string.everyday;
            case EVERY_WEEK:
                return R.string.every_week;
            case EVERY_MONTH:
                return R.string.every_month;
            case FIXED_TIME:
                return R.string.fixed_time;
            default:
                return R.string.only_one_time;
        }
    }

    public static String[] getStringArray ()
    {
        Resources resources = MyApplication_.getInstance().getResources();
        String[] sa = new String[] {
                resources.getString(R.string.only_one_time),
                resources.getString(R.string.everyday),
                resources.getString(R.string.every_week),
                resources.getString(R.string.every_month)};
//                resources.getString(R.string.fixed_time)};//先去掉到期之前拜访固定次数选项
        return sa;
    }
}
