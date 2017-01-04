package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.ContactInfo;
import com.drugoogle.sellscrm.data.ContactParentInfo;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by ydwang on 2016/4/28.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDetailsResponse extends BaseResponse {

    public List<ContactInfo> linkmanList;
    public CustomerInfo ccCustomer;

}
