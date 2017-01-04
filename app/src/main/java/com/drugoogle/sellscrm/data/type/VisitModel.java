package com.drugoogle.sellscrm.data.type;

import android.content.res.Resources;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.MyApplication_;

/**
 * Created by wgh on 2016/4/13.
 * 拜访方式类型：电话/见面
 */
public class VisitModel
{
    /**电话*/
    public static final int PHONE = 1;

    /**见面*/
    public static final int MEET = 2;

    /**不限*/
    public static final int UNLIMITED = 3;


    public static int getVisitModelStrRes (int model)
    {
        switch (model)
        {
            case PHONE:
                return R.string.visit_by_phone;
            case MEET:
                return R.string.visit_by_meet;
            case UNLIMITED:
                return R.string.unlimited;
            default:
                return R.string.visit_by_phone;
        }
    }

    public static int getVisitModelImgRes (int model)
    {
        switch (model)
        {
            case PHONE:
                return R.drawable.icon_visit_model_phone;
            case MEET:
                return R.drawable.icon_visit_model_meet;
            default:
                return R.drawable.icon_visit_model_phone;
        }
    }

    public static int getVisitModelYellowImgRes (int model)
    {
        switch (model)
        {
            case PHONE:
                return R.drawable.icon_visit_model_phone_yellow;
            case MEET:
                return R.drawable.icon_visit_model_meet_yellow;
            default:
                return R.drawable.icon_visit_model_phone_yellow;
        }
    }

    public static String[] getStringArray ()
    {
        Resources resources = MyApplication_.getInstance().getResources();
        String[] sa = new String[] {
                resources.getString(R.string.visit_by_phone),
                resources.getString(R.string.visit_by_meet)
                };
        return sa;
    }

    public static String[] getStringArrayWithUnlimited ()
    {
        Resources resources = MyApplication_.getInstance().getResources();
        String[] sa = new String[] {
                resources.getString(R.string.visit_by_phone),
                resources.getString(R.string.visit_by_meet),
                resources.getString(R.string.unlimited)
        };
        return sa;
    }
}
