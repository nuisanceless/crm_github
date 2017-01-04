package com.drugoogle.sellscrm.data.type;

import com.drugoogle.sellscrm.R;

/**
 * Created by ydwang on 2016/4/19.
 */
public class CustomnerSortBy {
    //按按名称首字母
    public static final int CUSTOMER_SORTTYPE_DEFAULT = 1;
    //按级别
    public static final int CUSTOMER_SORTTYPE_LEVEL = 2;
    //按离我最近
    public static final int CUSTOMER_SORTTYPE_NEAREST = 3;
    //按最近拜访记录
    public static final int CUSTOMER_SORTTYPE_LATEST = 4;
    //按拜访次数
    public static final int CUSTOMER_SORTTYPE_TIMES = 5;

    public static int getCustomerSortBy(int SortType){

        switch (SortType){
            case CUSTOMER_SORTTYPE_LEVEL:
                return R.string.sort_level;
            case CUSTOMER_SORTTYPE_NEAREST:
                return R.string.sort_nearest;
            case CUSTOMER_SORTTYPE_LATEST:
                return R.string.sort_latest;
            case CUSTOMER_SORTTYPE_TIMES:
                return R.string.sort_times;
            case CUSTOMER_SORTTYPE_DEFAULT:
                return R.string.sort_first_letter;
            default:
                return R.string.sort_first_letter;
        }
    }
}
