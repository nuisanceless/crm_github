package com.drugoogle.sellscrm.data.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by wgh on 2016/5/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentRecordInfo
{
    public String oldAddress;
    public String province;
    public String region;
    public String subaddr;
    public String address;
    public String city;
    public String customerId;
    public String customerName;
    public String customerLatitude;
    public String customerLongitude;
    public int id;
    public int is_admin;
    public String latitude;
    public String lengthStr;
    public String longitude;
    public String planRemark;
    public int status;
    public int visit_id;
    public String endDate;
    public String visitStartDate;
}
