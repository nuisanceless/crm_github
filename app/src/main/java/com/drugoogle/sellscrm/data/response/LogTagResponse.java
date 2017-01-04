package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/7/6.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogTagResponse extends BaseResponse
{
    //0不用上传，1需要上传
    public int data;
}
