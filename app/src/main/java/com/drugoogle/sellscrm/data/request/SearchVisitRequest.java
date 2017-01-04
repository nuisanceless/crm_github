package com.drugoogle.sellscrm.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wuguohao on 16/5/22.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchVisitRequest {
    public String beginTime;
    public String endDate;
    public String customerId = "";
    public String business = "";
    public String visitWay = "";
}
