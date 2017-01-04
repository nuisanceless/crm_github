package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Created by ydwang on 2016/6/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditRecordResponse extends BaseResponse {
    public String outDate;
    public BigDecimal amount;
    public String invoiceNumber;
    public BigDecimal notReturnAmount;
    public String buildDate;
    public int id;
    public String erpSettlementId;
    public BigDecimal noTaxAmount;
    public BigDecimal taxAmount;
    public BigDecimal returnAmount;
}
