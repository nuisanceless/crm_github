package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.SaleRecordItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by ydwang on 2016/5/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleRecordListResponse extends BaseResponse {
    public List<SaleRecordItem> dataList;
    public int page;
    public int pageSize;
    public int totalPage;
}
