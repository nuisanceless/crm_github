package com.drugoogle.sellscrm.data.type;

import android.content.res.Resources;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.MyApplication_;

/**
 * Created by wgh on 2016/4/13.
 * 拜访计划类型：财务/物流/业务/其他
 */
public class VisitType
{
    /**财务*/
    public static final int FINANCE = 1;

    /**物流*/
    public static final int LOGISTICS = 2;

    /**业务*/
    public static final int BUSINESS = 3;

    /**其他*/
    public static final int OTHER = 4;

    /**不限*/
    public static final int UNLIMITED = 5;

    public static int getVisitTypeStrRes (int type)
    {
        switch (type)
        {
            case FINANCE:
                return R.string.visit_type_finance;
            case LOGISTICS:
                return R.string.visit_type_logistics;
            case BUSINESS:
                return R.string.visit_type_business;
            case OTHER:
                return R.string.visit_type_other;
            case UNLIMITED:
                return R.string.unlimited;
            default:
                return R.string.visit_type_other;
        }
    }

    public static int getVisitTypeImgRes (int type)
    {
        switch (type)
        {
            case FINANCE:
                return R.drawable.type_finance;
            case LOGISTICS:
                return R.drawable.type_logistics;
            case BUSINESS:
                return R.drawable.type_business;
            case OTHER:
                return R.drawable.type_others;
            default:
                return R.drawable.type_others;
        }
    }

    public static int getVisitTypeYellowImgRes (int type)
    {
        switch (type)
        {
            case FINANCE:
                return R.drawable.type_finance_yellow;
            case LOGISTICS:
                return R.drawable.type_logistics_yellow;
            case BUSINESS:
                return R.drawable.type_business_yellow;
            case OTHER:
                return R.drawable.type_others_yellow;
            default:
                return R.drawable.type_others_yellow;
        }
    }


    public static String[] getStringArray ()
    {
        Resources resources = MyApplication_.getInstance().getResources();
        String[] sa = new String[] {
                resources.getString(R.string.visit_type_finance),
                resources.getString(R.string.visit_type_logistics),
                resources.getString(R.string.visit_type_business),
                resources.getString(R.string.visit_type_other)};
        return sa;
    }

    public static String[] getStringArrayWithUnlimited ()
    {
        Resources resources = MyApplication_.getInstance().getResources();
        String[] sa = new String[] {
                resources.getString(R.string.visit_type_finance),
                resources.getString(R.string.visit_type_logistics),
                resources.getString(R.string.visit_type_business),
                resources.getString(R.string.visit_type_other),
                resources.getString(R.string.unlimited)};
        return sa;
    }
}
