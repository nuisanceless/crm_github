package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by fanzhang on 16/10/12.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class StartVisitInfo implements Serializable
{
    public int id;
    public String longitude;
    public String latitude;
}
