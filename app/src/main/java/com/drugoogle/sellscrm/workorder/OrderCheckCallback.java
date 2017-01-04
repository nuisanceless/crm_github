package com.drugoogle.sellscrm.workorder;

import com.drugoogle.sellscrm.data.response.NewWorkOrderCountResponse;

/**
 * Created by ydwang on 2016/3/24.
 */
public interface OrderCheckCallback {
    public void onFinish(NewWorkOrderCountResponse resp);
}
