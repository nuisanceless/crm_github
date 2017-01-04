package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.PlanRecordListInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by wgh on 2016/4/13.
 * 计划记录列表接口数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisitPlanListResponse extends BaseResponse
{
    public int flagId;
    public UserResponse user;
    public List<PlanRecordListInfo> dataList;
}
