package com.drugoogle.sellscrm.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by wgh on 2016/5/4.
 * 添加拜访记录
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddVisitRecordRequest
{
    public String visitStartDate;
    public String customerId;
    public int business;
    public int visitWay;
    /**秒*/
    public int length;
    public String remark;

    public String addressId;
    public String logitude;
    public String latitude;
    public List<Integer> filePathIds;
}
