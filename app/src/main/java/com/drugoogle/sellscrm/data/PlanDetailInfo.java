package com.drugoogle.sellscrm.data;

import com.drugoogle.sellscrm.data.type.PlanStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by wgh on 2016/4/27.
 * 拜访详情信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanDetailInfo implements Serializable
{
    private static final long serialVersionUID = 3L;
//    public int id;
    public int status = PlanStatus.NONSTART;//状态
//    public String time;
//    public String name;
//    public int visitModel;
//    public int importantLevel;
//    public int visitType;



    public int is_admin;
    public int ruleValue;
    public int business;
    public int visitWay;
    public String customerId;
    public int rule;
    public String remark;
    public String beginTime;
    public int id;
    public String customerName;
}

