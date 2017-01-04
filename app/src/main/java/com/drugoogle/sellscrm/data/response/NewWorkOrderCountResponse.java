package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ydwang on 2016/6/6.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewWorkOrderCountResponse extends BaseResponse{
    public int data;
}
