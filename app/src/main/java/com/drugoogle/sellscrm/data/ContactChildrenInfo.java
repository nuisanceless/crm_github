package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ydwang on 2016/4/28.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactChildrenInfo {
    public String phone;
    public String email;
}
