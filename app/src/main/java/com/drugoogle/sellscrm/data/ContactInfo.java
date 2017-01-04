package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by ydwang on 2016/5/4.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactInfo implements Serializable {
    private static final long serialVersionUID = 4L;

    public String ADDRESS;
    public String AREA;
    public String CITY;
    public String CREATE_TIME;
    public String CUSTOMER_ID;

    public String EMAIL;
    public String EXTENSION;
    public String ID;
    public String IS_MAIN;
    public String MODIFY_TIME;

    public String MODIFY_USER;
    public String NAME;
    public String NATURE;
    public String PARTNERSHIP;
    public String PHONE;

    public String PHONE1;
    public String PHONE2;
    public String POSITION;
    public String PROVINCE;
    public String QQ;

    public String REMARK;
    public String SEX;
    public String STS;
    public String TEL;
    public String TYPE;

    public String WEBCHAT;
    public String WEIBO;
    public String WEIXIN;
}
