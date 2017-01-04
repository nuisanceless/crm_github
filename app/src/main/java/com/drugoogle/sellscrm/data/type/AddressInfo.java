package com.drugoogle.sellscrm.data.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by Lei on 2015/6/8.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressInfo implements Serializable {

    public String ADDRESS;
    public String ADDRESS_OLD;
    public String CITY;
    public String CITY_ID;
    public String COMPANYID;

    public String DELFLAG;
    public String ID;
    public String IS_DEFAULT;
    public String LATITUDE;
    public String LONGITUDE ;

    public String PROVINCE;
    public String PROVINCE_ID;
    public String REGION;
    public String REGION_ID;
    public int ROWNUM_;

    public String SUBADDRESS;
    public String ADDRESSNAME;
}
