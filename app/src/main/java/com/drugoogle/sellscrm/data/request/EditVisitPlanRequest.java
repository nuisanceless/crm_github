package com.drugoogle.sellscrm.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/5/12.
 * 编辑拜访计划
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditVisitPlanRequest
{
    public int id;
    public String beginTime;

    public String customerId;

    //拜访类型（财务、物流、业务、其他（默认））
    public int business;

    //拜访方式（电话、见面（默认））
    public int visitWay;

    //拜访周期，仅这次，每日、每周、每月、到期固定次数。
    public int rule;

    //String?
    public String ruleValue;
    public String remark;
}
