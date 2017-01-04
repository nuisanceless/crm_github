package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.type.CurrentRecordInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/5/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentVisitRecordResponse extends BaseResponse {
    public CurrentRecordInfo data;
}
