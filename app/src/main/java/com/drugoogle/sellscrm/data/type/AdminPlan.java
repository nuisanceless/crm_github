package com.drugoogle.sellscrm.data.type;

import com.drugoogle.sellscrm.R;

/**
 * Created by wgh on 2016/5/9.
 */
public class AdminPlan
{

    //区分是系统添加计划还是用户添加计划 is_admin
    // 用户app添加
    public static final int IS_APP=1;
    // 系统添加
    public static final int IS_ADMIN=2;


    public static int getAdminPlanStrRes (int type)
    {
        switch (type)
        {
            case IS_APP:
                return R.string.user_plan;
            case IS_ADMIN:
                return R.string.admin_plan;
            default:
                return R.string.user_plan;
        }
    }
}
