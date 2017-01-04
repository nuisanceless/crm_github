package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.RecordDetailInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/5/9.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordDetailResponse extends BaseResponse
{
    public RecordDetailInfo visitRecord;
}
