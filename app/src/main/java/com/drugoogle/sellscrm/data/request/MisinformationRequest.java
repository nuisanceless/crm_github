package com.drugoogle.sellscrm.data.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.SerializableString;

import java.io.Serializable;

/**
 * Created by wgh on 2016/5/26.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MisinformationRequest implements Serializable
{
    public String cause;
    public String remark;
    public String longitude;
    public String latitude;
}
