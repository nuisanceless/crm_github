package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by ydwang on 2016/4/28.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactParentInfo {
    public String name;
    public String tittle;
    public List<ContactChildrenInfo> contactChildrenInfos;
}
