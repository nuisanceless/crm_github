package com.drugoogle.sellscrm.data.type;

import com.drugoogle.sellscrm.R;

/**
 * Created by ydwang on 2016/5/25.
 */
public class WorkOrderType {

    public static final int ALL= 0;
    public static final int TOUSU = 1;
    public static final int JIANJU = 2;
    public static final int ZIXUN = 3;
    public static final int BAOPI = 4;
    public static final int WEIHU = 5;
    public static final int DINGHUO = 6;

    public static int getWorkOrderType(int type) {
        switch (type) {
            case TOUSU:
                return R.string.type_tousu;
            case JIANJU:
                return R.string.type_jianju;
            case ZIXUN:
                return R.string.type_zixun;
            case BAOPI:
                return R.string.type_zixun;
            case WEIHU:
                return R.string.type_weihu;
            case DINGHUO:
                return R.string.type_dinghuo;
            case ALL:
                return R.string.type_all;
            default:
                return R.string.type_all;
        }
    }
}
