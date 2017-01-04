package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.SaleRecordDetailInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ydwang on 2016/5/25.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleRecordDetailResponse extends BaseResponse {
    public SaleRecordDetailInfo data;
}
