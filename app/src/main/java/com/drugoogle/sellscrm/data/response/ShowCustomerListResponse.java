package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.CustomerInfoItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by ydwang on 2016/4/7.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShowCustomerListResponse extends BaseResponse{
    public List<CustomerInfoItem> ccCustomerList;
    public UserResponse user;
    public int totalPage;
}
