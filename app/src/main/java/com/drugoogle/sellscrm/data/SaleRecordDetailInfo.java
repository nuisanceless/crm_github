package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by ydwang on 2016/5/25.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleRecordDetailInfo implements Serializable {
    private static final long serialVersionUID = 6L;

    public String BUYER_NAME;
    public String BUYER_CODE;
    public int YD_STATUS;
    public String SELL_ORDER_ID;
    public String CREATE_TIME;
    public int QUANTITY;
    public BigDecimal AMOUNT;
    public String OPERATOR;
    public String CHECK_CELL;
    public String REMARK;
}
