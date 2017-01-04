package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by wgh on 2016/5/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoVisitCustomerInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    public String ADDRESS;
    public String AREA;
    public String CATEGORY;
    public String CATEGORY_ITEM;
    public String CITY;

    public String COMPANY_NAME;
    public String CONTACTOR;
    public String EB_BUYER_ID;
    public String EMAIL;
    public String ERP_CODE;
    public int GRADE;

    public long ID;//客户id
    public MODIFY_TIME MODIFY_TIME;
    public String MODIFY_USER;
    public String NAME;//客户姓名
    public String OPT_CODE;

    public String PHONE;
    public String PREREGISTERED;
    public String PROVINCE;
    public String SHIPPING_ADDRESS;
    public String SOURCE_FLAG;
    public String STS;

    public class MODIFY_TIME implements Serializable{
        public Stream stream;
        public Bytes bytes;
        public long length;
        public class Stream implements Serializable{
        }
        public class Bytes implements Serializable{
        }
    }

    @Override
    public String toString()
    {
        return String.format("(%d - %s) Address:%s %s", ID, NAME, CITY, ADDRESS);
    }
}
