package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/5/5.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarCellInfo {
    public int time;
    public int tag;
    public String planDate;
}
