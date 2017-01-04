package com.drugoogle.sellscrm.data.type;

import com.drugoogle.sellscrm.R;

/**
 * Created by wgh on 2016/4/27.
 */
public class PlanStatus
{
    /**未开始*/
    public static final int NONSTART = 0;

    /**进行中*/
    public static final int UNDERWAY = 1;

    /**已完成*/
    public static final int FINISHED = 2;


    public static int getPlanStatusStrRes (int status)
    {
        switch (status)
        {
            case NONSTART:
                return R.string.visit_status_nonstart;
            case UNDERWAY:
                return R.string.visit_status_underway;
            case FINISHED:
                return R.string.visit_status_finished;
            default:
                return R.string.visit_status_nonstart;
        }
    }

    public static int getPlanStatusImgRes (int status)
    {
        switch (status)
        {
            case NONSTART:
                return R.drawable.icon_status_nonstart;
            case UNDERWAY:
                return R.drawable.icon_status_underway;
            case FINISHED:
                return R.drawable.icon_status_finished;
            default:
                return R.drawable.icon_alert;
        }
    }

    /**
     * 时间轴上面的状态图标
     * */
    public static int getPlanStatusTimelineImgRes (int status)
    {
        switch (status)
        {
            case NONSTART:
                return R.drawable.icon_prismatic_nonstart;
            case UNDERWAY:
                return R.drawable.icon_prismatic_nonstart;
            case FINISHED:
                return R.drawable.icon_prismatic_finished;
            default:
                return R.drawable.icon_prismatic_nonstart;
        }
    }

    public static int getPlanStatusColorRes (int status)
    {
        switch (status)
        {
            case NONSTART:
                return R.color.status_unstart;
            case UNDERWAY:
                return R.color.status_underway;
            case FINISHED:
                return R.color.status_finished;
            default:
                return R.color.dark_gray;
        }
    }
}
