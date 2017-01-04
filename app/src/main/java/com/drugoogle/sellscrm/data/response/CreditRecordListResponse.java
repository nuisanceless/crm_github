package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.CreditRecordItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by ydwang on 2016/6/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditRecordListResponse extends BaseResponse{

    public BigDecimal totalAmount;
    public BigDecimal residualAmount;
    public int totalPage;
    public List<CreditRecordItem> list;
}
