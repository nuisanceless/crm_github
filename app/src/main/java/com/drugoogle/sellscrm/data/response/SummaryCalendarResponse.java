package com.drugoogle.sellscrm.data.response;

import com.drugoogle.sellscrm.data.CalendarCellInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by wgh on 2016/5/4.
 * 自定义日历控件
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SummaryCalendarResponse extends BaseResponse
{
    public List<CalendarCellInfo> dateArrayList;
    public UserResponse user;

}
