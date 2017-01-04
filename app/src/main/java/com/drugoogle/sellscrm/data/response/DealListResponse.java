package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.DealInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by ydwang on 2016/5/25.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DealListResponse extends BaseResponse {
    public int totalPage;
    public List<DealInfo> dataList;
    public int pupageSize;
    public int page;
}
