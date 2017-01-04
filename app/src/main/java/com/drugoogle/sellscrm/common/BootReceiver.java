package com.drugoogle.sellscrm.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.drugoogle.sellscrm.visit.VisitService;

/**
 * Created by wuguohao on 16/7/5.
 */
public class BootReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Intent serviceIntent = new Intent(context, VisitService.class);
            context.startService(serviceIntent);

//            Intent serviceIntent = new Intent(context, Selfinfo_login_Activity_.class);
//            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
//            context.startActivity(serviceIntent);
        }
    }
}
