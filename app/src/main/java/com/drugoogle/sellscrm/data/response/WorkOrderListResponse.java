package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.WorkOrderInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by ydwang on 2016/5/25.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkOrderListResponse extends BaseResponse {
    public int pageSize;
    public int page;
    public int totalPage;
    public List<WorkOrderInfo> dataList;
}
