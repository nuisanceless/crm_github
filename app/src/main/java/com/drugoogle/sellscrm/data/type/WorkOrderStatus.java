package com.drugoogle.sellscrm.data.type;

import com.drugoogle.sellscrm.R;

/**
 * Created by ydwang on 2016/5/25.
 */
public class WorkOrderStatus {

    //所有状态
    public static final int All = 5;
    //已处理
    public static final int FINISHED = 4;
    //处理中
    public static final int PROCESSING = 3;
    //待处理
    public static final int WAIT_PROCESSING = 2;
    //未查看
    public static final int NOT_VIEWED = 1;

    public static int getWorkOrderStatus(int status) {
        switch (status) {
            case NOT_VIEWED:
                return R.string.state_not;
            case WAIT_PROCESSING:
                return R.string.state_wait;
            case PROCESSING:
                return R.string.state_resolving;
            case FINISHED:
                return R.string.state_resolved;
            case All:
                return R.string.state_all;
            default:
                return R.string.state_all;
        }
    }
}
