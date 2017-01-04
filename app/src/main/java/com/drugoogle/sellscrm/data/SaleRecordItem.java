package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by ydwang on 2016/5/25.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleRecordItem implements Serializable{
    private static final long serialVersionUID = 8L;

    public String amount;
    public String quantity;
    public String create_time;
    public String sell_order_id;
    public String yd_status;
}
