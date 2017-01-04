package com.drugoogle.sellscrm.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Lei on 2016/5/27.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditVisitRecordRequest {
    public int id;
    public String remark;
    public List<Integer> filePathIds;
    public List<Integer> deleteFileIds;
}
