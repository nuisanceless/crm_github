package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by ydwang on 2016/5/25.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkOrderInfo implements Serializable {
    private static final long serialVersionUID = 9L;

    public String publishTime;
    public String description;
    public String type;
    public String customerName;
    public String status;
    public String result;
    public String title;
    public String linkerPhone;
    public String id;
    public String linkerName;

}
