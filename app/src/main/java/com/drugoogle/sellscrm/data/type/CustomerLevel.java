package com.drugoogle.sellscrm.data.type;

import android.content.res.Resources;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.MyApplication_;

/**
 * Created by ydwang on 2016/4/19.
 */
public class CustomerLevel {

    //所有级别
    public static final int CUSTOMER_LEVEL_DEFAULT = 0;
    //非常重要
    public static final int CUSTOMER_LEVEL_HIGH = 3;
    //重要
    public static final int CUSTOMER_LEVEL_MIDDLE = 2;
    //普通
    public static final int CUSTOMER_LEVEL_LOW = 1;

    public static int getCustomerLevel(int customerLevwel) {
        switch (customerLevwel) {
            case CUSTOMER_LEVEL_HIGH:
                return R.string.level_high;
            case CUSTOMER_LEVEL_MIDDLE:
                return R.string.level_middle;
            case CUSTOMER_LEVEL_LOW:
                return R.string.level_low;
            default:
                return R.string.level_all;
        }
    }
    public static int getCustomerImRes (int customerLevwel)
    {
        switch (customerLevwel)
        {
            case CUSTOMER_LEVEL_HIGH:
                return R.drawable.icon_important_level_high;
            case CUSTOMER_LEVEL_MIDDLE:
                return R.drawable.icon_important_level_middle;
            case CUSTOMER_LEVEL_LOW:
                return R.drawable.icon_important_level_low;
            case CUSTOMER_LEVEL_DEFAULT:
                return R.drawable.icon_important_level_high;
            default:
                return R.drawable.icon_important_level_high;
        }
    }

    public static String[] getStringArray ()
    {
        Resources resources = MyApplication_.getInstance().getResources();
        String[] sa = new String[] {
                resources.getString(R.string.level_low),
                resources.getString(R.string.level_middle),
                resources.getString(R.string.level_high)
        };
        return sa;
    }
}
