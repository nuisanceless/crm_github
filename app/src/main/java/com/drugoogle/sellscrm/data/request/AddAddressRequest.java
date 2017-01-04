package com.drugoogle.sellscrm.data.request;

import com.drugoogle.sellscrm.data.AddressDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wuguohao on 15-6-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddAddressRequest {
    public String companyId;
    public AddressDetail addressDetail;
}
