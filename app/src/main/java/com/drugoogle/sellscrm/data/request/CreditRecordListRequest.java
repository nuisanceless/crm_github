package com.drugoogle.sellscrm.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ydwang on 2016/6/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditRecordListRequest {
    public int pageNumber;
    public int pageSize;
    public int customerId;
}
