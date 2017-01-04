package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.deser.std.StackTraceElementDeserializer;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by ydwang on 2016/6/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditRecordItem implements Serializable {
    private static final long serialVersionUID = 501L;

    public BigDecimal amount;
    public String buildDate;
    public int id;
    public String outDate;
    public String erpSettlementId;
}
