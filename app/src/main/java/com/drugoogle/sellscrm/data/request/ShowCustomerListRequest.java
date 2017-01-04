package com.drugoogle.sellscrm.data.request;

/**
 * Created by ydwang on 2016/4/7.
 */
public class ShowCustomerListRequest {
    public int pageNumber;
    public int pageSize;
    public String nameOrNum;
    public int sort;
    public int condition;
    public double longitude;
    public double latitude;
    public String token;
}
