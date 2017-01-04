package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by ydwang on 2016/6/8.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerInfoItem implements Serializable{

    private static final long serialVersionUID = 178L;
    public String date;
    public String distance;
    public int grade;
    public String erpCode;
    public String count;
    public String name;
    public String id;
    public int sort;
}
