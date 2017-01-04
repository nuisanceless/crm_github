package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Lei on 2016/5/26.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadFileResponse extends BaseResponse {
    public List<Integer> data;
}
