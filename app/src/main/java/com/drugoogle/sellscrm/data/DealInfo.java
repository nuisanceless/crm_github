package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by ydwang on 2016/5/23.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DealInfo implements Serializable {
    private static final long serialVersionUID = 5L;
    public String MEDICINE_NAME;//货品名称
    public String STANDARD;//规格
    public String BASE_UNIT;//基本单位
    public int QUANTITY;//数量
    public BigDecimal PRICE;//单价
    public BigDecimal AMOUNT;//金额
    public String REMARK;//备注
}
