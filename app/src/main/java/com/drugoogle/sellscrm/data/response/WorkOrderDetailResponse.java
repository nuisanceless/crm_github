package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.WorkOrderInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ydwang on 2016/5/25.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkOrderDetailResponse extends BaseResponse{

public WorkOrderInfo data;
}
