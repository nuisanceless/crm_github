package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.PlanDetailInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/4/27.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanDetailResponse extends BaseResponse
{
    public PlanDetailInfo visitPlan;
}
