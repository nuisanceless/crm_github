package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ydwang on 2016/5/23.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerVisitCountResponse extends  BaseResponse{
    public String success;
    public int visitCount;
    public int customerCount;
    public int code;
}
