package com.drugoogle.sellscrm.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ydwang on 2016/5/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressListRequest {
    public int page;
    public int pageSize;
    public String companyId;
}
