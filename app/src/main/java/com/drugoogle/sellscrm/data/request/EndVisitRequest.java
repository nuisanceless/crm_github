package com.drugoogle.sellscrm.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by wgh on 2016/5/19.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndVisitRequest
{
    public int id;
    public String remark;
    public List<Integer> filePathIds;
}
