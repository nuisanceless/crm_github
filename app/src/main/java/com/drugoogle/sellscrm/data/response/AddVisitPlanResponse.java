package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/3/28.
 * 暂时没用
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddVisitPlanResponse extends BaseResponse
{
    public int planId;
}
