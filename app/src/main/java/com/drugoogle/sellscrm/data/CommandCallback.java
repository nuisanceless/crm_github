package com.drugoogle.sellscrm.data;

import com.drugoogle.sellscrm.data.response.BaseResponse;

/**
 * Created by wgh on 2016/3/28.
 */
public interface CommandCallback {
    public  void onFinish(BaseResponse resp);
}
