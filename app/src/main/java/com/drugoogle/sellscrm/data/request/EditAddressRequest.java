package com.drugoogle.sellscrm.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wuguohao on 15-6-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditAddressRequest {
    public String id;
    public String province;
    public String city;
    public String region;
    public String addr;
    public String subaddr;
    public String longtitude;
    public String latitude;
    public String addressName;

}
