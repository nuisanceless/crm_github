package com.drugoogle.sellscrm.data.response;

import android.content.Context;
import android.content.Intent;

import com.drugoogle.sellscrm.common.ActivityHelper_;
import com.drugoogle.sellscrm.selfinfo.Account;
import com.drugoogle.sellscrm.selfinfo.Selfinfo_login_Activity;
import com.drugoogle.sellscrm.selfinfo.Selfinfo_login_Activity_;
import com.drugoogle.sellscrm.visit.VisitService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/3/28.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class  BaseResponse
{
    private static final String TAG = BaseResponse.class.getSimpleName();
    public static final int REQUEST_CODE_EXPIRED = 999;
    public static final int RESULT_CODE_EXPIRED = 998;



    /**成功*/
    public static final int SUCCESS_CODE = 1;
    /**登陆失效*/
    public static final int EXPIRED_CODE = 422;



    /**
     * code = 1 success
     * code != 1 fail
     * */
    public int code;
    public String message;

    public static String getErrorMessage(BaseResponse resp)
    {
        if (resp == null)
        {
            return "网络不给力，请稍后再试。";
        }
        return resp.message;
    }

    public static boolean hasError(BaseResponse resp)
    {
        return resp == null || (resp.code != SUCCESS_CODE);
    }

    public static boolean hasErrorWithOperation(BaseResponse resp, Context context)
    {
        boolean hasError = false;
        String errorMsg = "";
        if (resp == null)
        {
            hasError = true;
            errorMsg = "网络不给力，请稍后再试。";
            //判断是否联网
//            if (!Connectivities.isConnected(context))
//            {
//                Intent serviceIntent = new Intent(context, LocationService_.class);
//                context.stopService(serviceIntent);
//                if (Selfinfo_login_Activity.isExist != true){
//                    Selfinfo_login_Activity_.intent(context).isExpired(true).startForResult(REQUEST_CODE_EXPIRED);
//                }
//                errorMsg = "网络不给力，请检查网络连接。";
//            }
        }
        else
        {
            if (resp.code != SUCCESS_CODE)
            {
                hasError = true;
                errorMsg = resp.message;
            }
        }
        if (hasError)
        {
            if (resp != null && resp.code == EXPIRED_CODE)
            {
                ActivityHelper_.getInstance_(context).showNormalToast("登录过期，请重新登陆");
                Account.getInstance().logout();
                Intent serviceIntent = new Intent(context, VisitService.class);
                context.stopService(serviceIntent);
                if (Selfinfo_login_Activity.isExist != true){
                    Selfinfo_login_Activity_.intent(context).isExpired(true).startForResult(REQUEST_CODE_EXPIRED);
                }
            }else {
                ActivityHelper_.getInstance_(context).showNormalToast(errorMsg);
            }
        }



        return hasError;
    }

}
