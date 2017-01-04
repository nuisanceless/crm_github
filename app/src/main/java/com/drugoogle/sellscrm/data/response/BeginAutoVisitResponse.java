package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.StartVisitInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/5/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BeginAutoVisitResponse extends BaseResponse
{
    //生成的记录的ID
    public StartVisitInfo data;
}
