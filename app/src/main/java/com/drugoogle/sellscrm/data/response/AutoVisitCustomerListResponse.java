package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.AutoVisitCustomerInfo;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wgh on 2016/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoVisitCustomerListResponse extends BaseResponse
{
    public ArrayList<AutoVisitCustomerInfo> data;
}
