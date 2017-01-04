package com.drugoogle.sellscrm.data.Model;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.DateUtils;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.PlanDetailInfo;
import com.drugoogle.sellscrm.data.type.VisitModel;
import com.drugoogle.sellscrm.data.type.VisitPeriod;
import com.drugoogle.sellscrm.data.type.VisitType;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by wgh on 2016/3/30.
 */
public class VisitPlanModel
{
    //拜访周期默认值
    public static final int DEFAULT_VISIT_PERIOD = VisitPeriod.ONLY_ONE_TIME;
    //拜访类型默认值
    public static final int DEFAULT_VISIT_TYPE = VisitType.BUSINESS;
    //拜访方式默认值
    public static final int DEFAULT_VISIT_MODEL = VisitModel.MEET;




    //计划的id
    public int id;
    //拜访周期，仅这次，每日、每周、每月、到期固定次数。
    public int visitPeriod;
    //拜访日期
    public Date dateTime;
    //到期前拜访次数
    public int visitCount;
    //客户ID
    public String clientId;
    //客户名字
    public String clientName;
    //拜访类型（财务、物流、业务、其他（默认））
    public int visitType;
    //拜访方式（电话、见面（默认））
    public int visitModel;
    //备注
    public String remark;



    public VisitPlanModel ()
    {
        visitPeriod = DEFAULT_VISIT_PERIOD;//默认值为（仅这次）
        dateTime = Calendar.getInstance().getTime();
        visitType = DEFAULT_VISIT_TYPE;//默认值为（业务）
        visitModel = DEFAULT_VISIT_MODEL;//默认值为（见面）
    }

    public String checkValidity ()
    {
        String msg = "";

        if (visitPeriod == VisitPeriod.FIXED_TIME && visitCount == 0)
        {
            msg = MyApplication_.getInstance().getString(R.string.hint_total_visit_count);
            return msg;
        }
        if (CommonUtils.IsNullOrEmpty(clientId))
        {
            msg = MyApplication_.getInstance().getString(R.string.please_select_client);
            return msg;
        }
//        if (CommonUtils.IsNullOrEmpty(remark))
//        {
//            msg = MyApplication_.getInstance().getString(R.string.please_enter_remark);
//            return msg;
//        }


//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        Date aaDate = calendar.getTime();
        if (dateTime.before(new Date()))
        {
            msg = "请勿添加今日之前的计划。";
            return msg;
        }

        
        int dateGap = DateUtils.getGapCount(new Date(), dateTime);
        if (visitPeriod == VisitPeriod.FIXED_TIME && visitCount > dateGap * 2)
        {
            msg = "到期前拜访次数过多，请确认拜访频率。";
            return msg;
        }
        return msg;
    }

    public void inflate (PlanDetailInfo mPlanDetailInfo)
    {
        id = mPlanDetailInfo.id;
        visitPeriod = mPlanDetailInfo.rule;
        dateTime = CommonUtils.String2DateTwo(mPlanDetailInfo.beginTime);
        visitCount = mPlanDetailInfo.ruleValue;
        clientId = mPlanDetailInfo.customerId;
        clientName = mPlanDetailInfo.customerName;
        visitType = mPlanDetailInfo.business;
        visitModel = mPlanDetailInfo.visitWay;
        remark = mPlanDetailInfo.remark;
    }
}
