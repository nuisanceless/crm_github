package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by ydwang on 2016/5/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressDetail implements Serializable {
    public String province;
    public String city;
    public String region;
    public String addr;
    public String subaddr;
    public String longtitude;
    public String latitude;
    public String addressName;
}
