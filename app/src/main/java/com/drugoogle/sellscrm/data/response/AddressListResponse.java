package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.type.AddressInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


/**
 * Created by wuguohao on 15-6-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressListResponse extends BaseResponse
{
    public int totalPage;
    public int pageSize;
    public int page;

    public List<AddressInfo> dataList;
}
