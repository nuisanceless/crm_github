package com.drugoogle.sellscrm.visit;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

/**
 * Created by wgh on 2016/3/28.
 */
@EBean
public class VisitManager
{
    public VisitManager ()
    {

    }


    /**
     * 添加拜访计划
     * */
    public void AddVisitPlan ()
    {

    }

    /**
     * 删除拜访计划/纪录
     * @param flag 标记是计划还是纪录
     * @param id 纪录或者计划的id
     * */
    @Background
    public void DeleteVisitPlan (int flag, int id)
    {

    }


    /**
     * 修改拜访计划
     * */
    public void ModifyVisitPlan (int id)
    {

    }


    /**
     * 查询单个拜访计划
     * */
    public void GetVisitPlan (int id)
    {

    }


    /**
     * 查询拜访计划列表
     * */
    public void GetVisitPlanList (int userId)
    {

    }

}
