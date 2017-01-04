package com.drugoogle.sellscrm.data.Model;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.type.VisitModel;
import com.drugoogle.sellscrm.data.type.VisitType;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by wgh on 2016/3/30.
 */
public class VisitRecordModel
{
    //拜访类型默认值
    public static final int DEFAULT_VISIT_TYPE = VisitType.BUSINESS;
    //拜访方式默认值
    public static final int DEFAULT_VISIT_MODEL = VisitModel.MEET;

    //拜访开始时间
    public Date visitTime;
    //拜访时长(分钟)
    public int visitDuration = 120;
    //客户号
    public String clientId;
    public String erpCode;
    //客户名称
    public String clientName;
    //拜访类型（财务、物流、业务、其他（默认））
    public int visitType;
    //拜访方式（电话、见面（默认））
    public int visitModel;
    //备注
    public String remark;
    //图片ID
    public List<Integer> filePathIds;
    //删除图片ID
    public List<Integer> deleteFileIds;

    //客户地址
    public String addressId;
    //实际地址
    public String logitude;
    public String latitude;


    public VisitRecordModel ()
    {
        visitTime = Calendar.getInstance().getTime();
        visitType = DEFAULT_VISIT_TYPE;//默认值为（业务）
        visitModel = DEFAULT_VISIT_MODEL;//默认为（见面）
    }

    public String checkValidity ()
    {
        String msg = "";

        if (visitDuration <= 0)
        {
            msg = MyApplication_.getInstance().getString(R.string.please_enter_visit_duration);
        }
        else if (CommonUtils.IsNullOrEmpty(clientId))
        {
            msg = MyApplication_.getInstance().getString(R.string.please_select_client);
        }
        else if (addressId == null)
        {
            msg = MyApplication_.getInstance().getString(R.string.please_select_client_addr);
        }
//        if (CommonUtils.IsNullOrEmpty(remark))
//        {
//            msg = MyApplication_.getInstance().getString(R.string.please_enter_visit_record_remark);
//            return msg;
//        }
        if (visitTime.after(new Date()))
        {
            msg = "请勿添加当前时间之后的记录。";
            return msg;
        }
        return msg;
    }
}
