package com.drugoogle.sellscrm.common;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.Date;

/**
 * Created by wgh on 2016/5/16.
 */
public class LocationUtil
{
    private final static boolean DEBUG = true;
    private final static String TAG = "LocationUtil";

    public final static int GET_LOCATION_PERIOD = 60 * 1000;            //ms，间隔多久获取一次定位位置
    public final static int AUTO_VISIT_STAY_DURATION = 10 * 60 * 1000;  //在一个地方10分钟不动判断为自动拜访。毫秒
    public final static double HOLD_STAY_DISTANCE = 50;                 //50米方位内都算停留

    Context mContext;
    String mProvider;
    private BDLocation mLocation = null;

    private MLocation  mBaseLocation = new MLocation();
    public BDLocationListener myListener;
    private LocationClient mLocationClient;




    public LocationUtil(Context context, BDLocationListener listener)
    {
        mContext = context;
        mLocationClient = new LocationClient(context.getApplicationContext());
        initParams();
        myListener = listener;
    }


    public void setLocationListener (BDLocationListener listener)
    {
        myListener = listener;
    }

    public void startMonitor() {
        if (DEBUG) Log.e(TAG, "start monitor location " + myListener);
        mLocationClient.registerLocationListener(myListener);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }


    public void stopMonitor() {
        if (DEBUG) Log.e(TAG, "stop monitor location " + myListener);
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(myListener);

            if (mLocationClient.isStarted())
                mLocationClient.stop();
        }
    }

    public void requestLocation ()
    {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.requestLocation();
        } else {
            Log.d("LocSDK3", "locClient is null or not started");
        }
    }


    public BDLocation getLocation() {
        if (DEBUG) Log.d(TAG, "get BDLocation");
        return mLocation;
    }

    public MLocation getBaseLocation() {
        if (DEBUG) Log.d(TAG, "get mBaseLocation");
        return mBaseLocation;
    }

    private void initParams() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);                //可选，默认false,设置是否使用gps
        option.setAddrType("all");              //返回的定位结果包含地址信息
        option.setCoorType("bd09ll");           //返回的定位结果是百度经纬度,默认值gcj02 可选,设置返回的定位结果坐标系
        /**
         * Hight_Accuracy 高精度定位模式：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
         * Battery_Saving 低功耗定位模式：这种定位模式下，不会使用GPS，只会使用网络定位（Wi-Fi和基站定位）；
         * Device_Sensors 仅用设备定位模式：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。
         * */
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);       //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setScanSpan(100);                //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.disableCache(true);              //禁止启用缓存定位
        option.setPriority(LocationClientOption.GpsFirst);      //设置GPS优先
        option.setIgnoreKillProcess(false);     //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

//        option.setIsNeedAddress(true);          //可选，设置是否需要地址信息，默认不需要
//        option.setLocationNotify(true);         //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//        option.setPoiNumber(5);                 //最多返回POI个数，POI是“Point of Interest”的缩写，中文可以翻译为“兴趣点”。商户什么的
//        option.setPoiDistance(1000);            //poi查询距离
//        option.setPoiExtraInfo(true);           //是否需要POI的电话和地址等详细信息
//        option.setIsNeedLocationDescribe(true);     //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        option.setIsNeedLocationPoiList(true);      //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//        option.SetIgnoreCacheException(false);      //可选，默认false，设置是否收集CRASH信息，默认收集
//        option.setEnableSimulateGps(false);         //可选，默认false，设置是否需要过滤gps仿真结果，默认需要

        mLocationClient.setLocOption(option);
//        mLocationClient.requestLocation();
    }




    public class MLocation {
        public double latitude;
        public double longitude;
    }



    /**
     * 获取两个时间的时间差
     * */
    public static long getTimeDifference (Date date1, Date date2)
    {
        long dateLong1 = date1.getTime();
        long dateLong2 = date2.getTime();
        return dateLong2 - dateLong1;
    }


    /**
     * 判断坐标2是否还在以坐标1为圆心的指定半径之内
     * @return true 保持停留，false 移动
     * */
    public static boolean isHoldStay (BDLocation location1, BDLocation location2)
    {
        LatLng latLng1 = new LatLng(location1.getLatitude(), location1.getLongitude());
        LatLng latLng2 = new LatLng(location2.getLatitude(), location2.getLongitude());
        double distance = DistanceUtil.getDistance(latLng1, latLng2);
        if (distance == -1)
            return false;
        else
        {
            if (distance > HOLD_STAY_DISTANCE)
                return false;
            else
                return true;
        }
    }
}
