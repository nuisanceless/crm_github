package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Created by ydwang on 2016/6/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemainingCreditResponse  extends  BaseResponse{
    public BigDecimal data;
}
