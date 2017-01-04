package com.drugoogle.sellscrm.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.MyApplication;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.type.CommonType;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wgh on 2016/3/23.
 */
public class CommonUtils
{
    public static int getRomVersion ()
    {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static boolean IsNullOrEmpty (String str)
    {
        if (str == null || str.isEmpty())
            return true;
        return false;
    }

    /**
     * date format yyyy-MM-dd
     * */
    public static String DateFormate (Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * date format yyyy-MM-dd HH:mm:ss
     * */
    public static String DateFormatTwo (Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static Date String2Date (String dateStr)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date String2DateTwo (String dateStr)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public static String GetWeekStr (int weekNum)
    {
        return GetStringArrayValue(R.array.day_of_week, weekNum);
    }

    public static String GetStringArrayValue (int array, int num)
    {
        String[] strArray = MyApplication_.getInstance().getResources().getStringArray(array);
        return strArray[num];
    }
    public static int GetIntArrayValue (int array, int num)
    {
        int[] intArray = MyApplication_.getInstance().getResources().getIntArray(array);
        return intArray[num];
    }

    public static int getScreenWidth (Context context)
    {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        return width;
    }

    public static int getScreenHeight (Context context)
    {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.widthPixels;
        return height;
    }

    private     static final String PREFERENCE_CHECKUPDTETIME = "CheckUpdateTime";
    public static long lastCheckUpdateTime(final Context context)
    {
        return  PreferenceManager.getDefaultSharedPreferences(context).getLong(PREFERENCE_CHECKUPDTETIME, 0);
    }

    public static void checkUpdateIfNeed(final Activity activity, final boolean showNoUpdate)
    {
        long past = System.currentTimeMillis() - lastCheckUpdateTime(activity);
        if (past > 1000 * 60 * 60 * 8)
        {
            checkUpdate(activity, showNoUpdate);
        }
    }
    public static void checkUpdate(final Activity activity, final boolean showNoUpdate)
    {
        PgyUpdateManager.register(activity,
                new UpdateManagerListener()
                {
                    @Override
                    public void onUpdateAvailable(final String result)
                    {
                        PreferenceManager.getDefaultSharedPreferences(activity).edit().putLong(PREFERENCE_CHECKUPDTETIME, System.currentTimeMillis());
                        final AppBean appBean = getAppBeanFromString(result);
                        String version = appBean.getVersionName();

                        new AlertDialog.Builder(activity)
                                .setTitle("更新")
                                .setMessage("有新版本 " + version + "可用，是否更新")
                                .setNegativeButton("不，谢谢", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {

                                    }
                                })
                                .setPositiveButton(
                                        "现在更新",
                                        new DialogInterface.OnClickListener()
                                        {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which)
                                            {
                                                startDownloadTask(
                                                        activity,
                                                        appBean.getDownloadURL());
                                            }
                                        }).show();

                    }

                    @Override
                    public void onNoUpdateAvailable()
                    {
                        if (showNoUpdate)
                        {
                            Toast.makeText(activity, "已经是最新版本",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * 判断设备位置服务是否打开
     * @return true 打开， false 没有打开
     * */
    public static boolean isLocationOpen (Context context)
    {
        LocationManager mLocationManager = (LocationManager)context.getSystemService(Service.LOCATION_SERVICE);
        if (CommonUtils.getSdkVersion() >= 19)
        {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
        else
        {
            boolean gpsOpen = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkOpen = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gpsOpen || networkOpen)
                return true;
            else
                return false;
        }
    }


    public static void writeToFile (String tag, String content)
    {
        if (MyApplication.getDebug())
        {
            String filePath = Environment.getExternalStorageDirectory().getPath() + "/crm_log.txt";
            //如果filePath是传递过来的参数，可以做一个后缀名称判断； 没有指定的文件名没有后缀，则自动保存为.txt格式
            if(!filePath.endsWith(".txt") && !filePath.endsWith(".log"))
                filePath += ".txt";
            //保存文件
            File file = new File(filePath);
            String str = CommonUtils.DateFormatTwo(new Date())
                    + " -- TAG:" + tag
                    + " -- " + content + "\n";
            try
            {
                OutputStream outstream = new FileOutputStream(file, true);
                OutputStreamWriter out = new OutputStreamWriter(outstream);
                out.write(str);
                out.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        Log.e(tag, content);
    }


    /**
     * 获取设备厂商名
     * */
    public static String getDeviceBrand ()
    {
        return Build.BRAND;
    }

    /**
     * 获取设备型号
     * */
    public static String getDeviceModel ()
    {
        return Build.MODEL;
    }

    /**
     * 获取设备SDK版本号
     * */
    public static int getSdkVersion ()
    {
        return Build.VERSION.SDK_INT;
    }
}
