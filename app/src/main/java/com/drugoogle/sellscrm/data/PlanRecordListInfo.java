package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/5/6.
 * 当日计划列表里面的列表item info
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanRecordListInfo
{
    public int is_admin;
    public int visitRecordId;
    public int business;
    public int visitPlanId;
    public int visitWay;
    public String time;
    public String customerName;
    public int status;

    //暂时没有
    public int importantLevel;
}
