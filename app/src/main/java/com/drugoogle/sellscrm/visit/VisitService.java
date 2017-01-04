package com.drugoogle.sellscrm.visit;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.drugoogle.sellscrm.MainActivity_;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CacheUtils;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.Connectivities;
import com.drugoogle.sellscrm.Utils.ImageUtils;
import com.drugoogle.sellscrm.common.MyApplication;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.AutoVisitCustomerInfo;
import com.drugoogle.sellscrm.data.request.EndVisitRequest;
import com.drugoogle.sellscrm.data.response.AutoVisitCustomerListResponse;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.BeginAutoVisitResponse;
import com.drugoogle.sellscrm.data.response.CurrentVisitRecordResponse;
import com.drugoogle.sellscrm.data.response.UploadFileResponse;
import com.drugoogle.sellscrm.data.type.CurrentRecordInfo;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wgh on 2016/6/15.
 */
public class VisitService extends Service
{
    private final int mNotificationId = 123;
    private final int mEndVisitNotificationId = 124;
    private int mGpsNotificationId = 0;
    private int mNetworkNotificationId = 0;
//    public final static int GET_LOCATION_PERIOD = 30 * 1000;            //ms，间隔多久获取一次定位位置
//    public final static int AUTO_VISIT_STAY_DURATION = 1 * 10 * 1000;  //在一个地方x分钟不动判断为自动拜访。毫秒

    public final static int GET_LOCATION_PERIOD = 2 * 60 * 1000;            //ms，间隔多久获取一次定位位置
    public final static int AUTO_VISIT_STAY_DURATION = 5 * 60 * 1000;  //在一个地方x分钟不动判断为自动拜访。毫秒
    public final static int WORKING_TIMER_CHECK_PERIOD = 10 * 60 * 1000;//检查工作时间的timer间隔
    public final static int ALARM_POWER_INTERVAL = 5 * 60 * 1000;
    public final static double HOLD_STAY_DISTANCE = 300;                 //xx米方位内都算停留

    private static final int START_WORK_HOUR = 6;
    private static final int STOP_WORK_HOUR = 22;

    //状态
    private enum Status
    {
        Stopped,//停止工作状态
        Refreshing,//正在刷新当前状态
        Loctating,//正在定位
        CheckingCustomer,//正在检查是否有符合地点条件的客户
        WaitingStart,//等待开始(超时或者手动开始)
        Starting,//正在开始拜访
        Visiting,//正在拜访中
        EndingVisit,//正在结束拜访
    }

    //动作
    private enum Action
    {
        ConditionSatisfied,//工作时间,GPS,网络条件都满足isLogEnabled
        ConditionFailed,//工作时间,GPS,网络条件不满足
        Stayed,//原地停留超时
        Moved,//移动了位置
        WaitingTimeout,//等待开始拜访超时
        WaitingCancel,//取消等待开始
        StartVisit,//手动调用了开始拜访
        EndVisit,//手动结束拜访
        RequestFinished,//请求成功
    }

    private class RequestResult
    {
        Status requestFor;
        BaseResponse response;
        Object extra;

        RequestResult(Status requestFor, BaseResponse response)
        {
            this.requestFor = requestFor;
            this.response = response;
        }

        RequestResult(Status requestFor, BaseResponse response, Object extra)
        {
            this.requestFor = requestFor;
            this.response = response;
            this.extra = extra;
        }
    }

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private Notification mNotification;

    private Status curStatus = Status.Stopped;
    private LocationClient locationClient;
    private LocationListener locationListener;

    private BDLocation lastPosition;//最后一次的位置
    private BDLocation curCenter;//当前用于比较的中心点,null表示无效
    private long curCenterTimeStamp;//当前中心点的采样时间
    private BDLocation curVisitLocation;//正在拜访的客户的实际地址
    private int curVisitingId;//-1表示没有更新过,0表示没有正在拜访的

    private ServiceBinder mServiceBinder = new ServiceBinder();

    private LocationModeChangeReceiver mLocationModeChangeReceiver;
    private NetworkReceiver mNetworkReceiver;
    private TimerChangeReceiver mTimerChangeReceiver;
    private AlarmReceiver mAlarmReceiver;


    private Timer mTimer;


    AsyncTask<String, Integer, CurrentVisitRecordResponse> getCurrentVisitTask;
    AsyncTask<String, Integer, AutoVisitCustomerListResponse> getAroundClinetTask;
    AsyncTask<String, Integer, BeginAutoVisitResponse> startVisitTask;
    AsyncTask<String, Integer, BaseResponse> endVisitTask;

    private static final String TAG = VisitService.class.getSimpleName();

    private static void log(String message)
    {
        Log.i(TAG, message);
        MyApplication.logger.log(TAG, message);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        log("Service created========================================");

        mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this);

        mTimer = new Timer();

        scheduleWorkingTimer();
        registerReceivers();
        scheduleAlarm();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        log("onStartCommand========================================");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        log("onDestroy========================================");
        clearState();
        stopAllTasks();
        unregisterReceivers();

        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }
        releasePowerLock();
        cancelAlarm();

        super.onDestroy();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return mServiceBinder;
    }

    /**
     * 配置百度定位参数
     */
    private void logStatus()
    {
        boolean workingtime = isWorkingTime();
        boolean gpsOpen = CommonUtils.isLocationOpen(VisitService.this);
        boolean networkOK = Connectivities.isConnected(this);
        String center = "unknown location;";
        String location = "unknown location;";
        String visitLocation = "unknown location;";

        if (curCenter != null)
        {
            center = String.format("(%f : %f) timeStamp:%d", curCenter.getLongitude(), curCenter.getLatitude(), curCenterTimeStamp / 1000);
        }
        if (lastPosition != null)
        {
            location = String.format("(%f : %f)", lastPosition.getLongitude(), lastPosition.getLatitude());
        }
        if (curVisitLocation != null)
        {
            visitLocation = String.format("(%f : %f)", curVisitLocation.getLongitude(), curVisitLocation.getLatitude());
        }
        String stauts = String.format("++++++ Current Status : WorkingTime=%s; GPS=%s; Network=%s;WorkingStatus=%s;CurVisitID=%d", workingtime, gpsOpen, networkOK, curStatus, curVisitingId);
        log("--------------------------------------------------------------------------");
        log(stauts);
        log("\t\tCenter Location : " + center);
        log("\t\tLast Location : " + location);
        log("\t\tVisit Location : " + visitLocation);
        log("--------------------------------------------------------------------------");
    }

    private void onAction(Action action, @Nullable Object actionData)
    {
        String log = String.format("[ONACTION] %s/%s", curStatus.toString(), action.toString());
        if (action == Action.RequestFinished)
        {
            RequestResult res = (RequestResult) actionData;
            log = log + ", request = " + res.requestFor;
        }
        log(log);
        logStatus();

        boolean processed = false;
        switch (curStatus)
        {
            case Stopped:
                processed = onStoppedStatusAction(action, actionData);
                break;
            case Refreshing:
                processed = onRefreshingStatusAction(action, actionData);
                break;
            case Loctating:
                processed = onLoctatingStatusAction(action, actionData);
                break;
            case CheckingCustomer:
                processed = onCheckingCustomerStatusAction(action, actionData);
                break;
            case WaitingStart:
                processed = onWaitingStartStatusAction(action, actionData);
                break;
            case Starting:
                processed = onStartingStatusAction(action, actionData);
                break;
            case Visiting:
                processed = onVisitingStatusAction(action, actionData);
                break;
            case EndingVisit:
                processed = onEndingVisitStatusAction(action, actionData);
                break;
        }
        if (!processed)
        {
            //所有状态下,处理从外部来的action,比如检测到条件不满足, 外部调用结束拜访等
            if (action == Action.ConditionFailed)
            {
                if (curStatus == Status.EndingVisit)
                {
                    return;
                }
                if (!isWorkingTime() && curVisitingId > 0)
                {
                    EndVisitRequest request = new EndVisitRequest();
                    request.id = curVisitingId;
                    toStatusEndingVisit(request);
                }
                else
                {
                    toStatusStopped();
                }
                processed = true;
            }
            else if (action == Action.EndVisit)//有可能条件不满足,当前不在工作,但是用户调用了结束拜访
            {
                EndVisitRequest request = (EndVisitRequest) actionData;
                toStatusEndingVisit(request);
                processed = true;
            }
        }
        if (!processed)
        {
            log("onAction + action = " + action + " not processed !!!!!!!!!!!!!!!!!!!");
        }
    }

    private boolean onStoppedStatusAction(Action action, Object actionData)
    {
        /**
         * 条件满足->Refreshing
         */
        log("(ACTION/" + action + ") onStoppedStatusAction ==============");
        if (action == Action.ConditionSatisfied)
        {
            toStatusRefreshing();
            return true;
        }
        return false;
    }

    private boolean onRefreshingStatusAction(Action action, Object actionData)
    {
        /**
         * 成功->有拜访->拜访中
         * 成功->无拜访->定位中
         * 失败->停止
         */
        log("(ACTION/" + action + ") onRefreshingStatusAction ==============");
        if (action != Action.RequestFinished)
        {
            log("[ERROR]action != Action.RequestFinished");
            return false;
        }

        RequestResult result = (RequestResult) actionData;
        if (result.requestFor != Status.Refreshing)
        {
            log("[ERROR]result.requestFor != Status.Refreshing");
            return false;
        }


        workingLooper.sendEmptyMessageDelayed(MSG_DO_LOCATE, 0);

        CurrentVisitRecordResponse response = (CurrentVisitRecordResponse) result.response;
        if (BaseResponse.hasError(response))
        {
            log("onRefreshingStatusAction failed : " + BaseResponse.getErrorMessage(response));
            Toast.makeText(VisitService.this, "无法获取当前拜访状态:" + BaseResponse.getErrorMessage(response), Toast.LENGTH_SHORT).show();
            //等待下一次start work的通知
            toStatusStopped();
        }
        else if (response.data.id <= 0)//没有拜访
        {
            log("onRefreshingStatusAction No Visit");

            toStatusLocating();
        }
        else
        {
            double longitude = Double.parseDouble(response.data.customerLongitude);
            double latitude = Double.parseDouble(response.data.customerLatitude);
            log(String.format("onRefreshingStatusAction success : id=%d, location =(%f, %f)", response.data.id, longitude, latitude));
            if (isVisitExpired(response.data))//拜访已经过期
            {
                log("onRefreshingStatusAction Visit expired, start date = " + response.data.visitStartDate);
                EndVisitRequest request = new EndVisitRequest();
                request.id = response.data.id;
                toStatusEndingVisit(request);
            }
            else
            {
                BDLocation customerLocation = new BDLocation();
                customerLocation.setLongitude(longitude);
                customerLocation.setLatitude(latitude);
                toStatusVisiting(response.data.id, customerLocation, null);
            }
        }
        return true;
    }

    private boolean onLoctatingStatusAction(Action action, Object actionData)
    {
        /**
         * 停留->检查客户
         */
        log("(ACTION/" + action + ") onLoctatingStatusAction ==============");
        if (action == Action.Stayed)
        {
            toStatusCheckingCustomer();
            return true;
        }
        return false;
    }

    private boolean onCheckingCustomerStatusAction(Action action, Object actionData)
    {
        /**
         * 失败->定位中
         * 无客户->定位中
         * 有客户->等待开始
         */
        log("(ACTION/" + action + ") onCheckingCustomerStatusAction ==============");
        if (action != Action.RequestFinished)
        {
            log("[ERROR]action != Action.RequestFinished");
            return false;
        }

        RequestResult result = (RequestResult) actionData;
        if (result.requestFor != Status.CheckingCustomer)
        {
            log("[ERROR]result.requestFor != Status.CheckingCustomer");
            return false;
        }
        AutoVisitCustomerListResponse response = (AutoVisitCustomerListResponse) result.response;
        if (BaseResponse.hasError(response))
        {
            toStatusLocating();
        }
        else if (response.data.size() == 0)
        {
            toStatusLocating();
        }
        else
        {
            toStatusWatingStart(response.data);
        }
        return true;
    }

    private boolean onWaitingStartStatusAction(Action action, Object actionData)
    {
        /**
         * 移动->定位中
         * 超时->开始拜访
         * 手动开始->开始拜访
         */
        log("(ACTION/" + action + ") onWaitingStartStatusAction ==============");
        if (action == Action.Moved || action == Action.WaitingCancel)
        {
            curCenterTimeStamp = System.currentTimeMillis();
            workingLooper.removeMessages(MSG_VISIT_COUNT_DOWN);
            broadcastAboutToVisitCanceled();

            toStatusLocating();
            return true;
        }
        else if (action == Action.WaitingTimeout || action == Action.StartVisit)
        {
            workingLooper.removeMessages(MSG_VISIT_COUNT_DOWN);

            toStatusStartingVisit((Long) actionData, lastPosition.getLongitude(), lastPosition.getLatitude());
            return true;
        }
        return false;
    }

    private boolean onStartingStatusAction(Action action, Object actionData)
    {
        /**
         * 失败->刷新(因为有可能说网络慢超时了,但是请求已经发出去了,实际拜访已经成功,所以此时刷新状态最安全)
         * 成功->拜访中
         */
        log("(ACTION/" + action + ") onStartingStatusAction ==============");
        if (action != Action.RequestFinished)
        {
            log("[ERROR]action != Action.RequestFinished");
            return false;
        }

        RequestResult result = (RequestResult) actionData;
        if (result.requestFor != Status.Starting)
        {
            log("[ERROR]result.requestFor != Status.Starting");
            return false;
        }
        BeginAutoVisitResponse response = (BeginAutoVisitResponse) result.response;
        if (BaseResponse.hasError(response))
        {
            broadcastVisitStarted(0, false);
            //toStatusWatingStart(null);
            toStatusRefreshing();
        }
        else
        {
            double longitude = Double.parseDouble(response.data.longitude);
            double latitude = Double.parseDouble(response.data.latitude);

            BDLocation customerLocation = new BDLocation();
            customerLocation.setLongitude(longitude);
            customerLocation.setLatitude(latitude);
            toStatusVisiting(response.data.id, customerLocation, lastPosition);
        }
        return true;
    }

    private boolean onVisitingStatusAction(Action action, Object actionData)
    {
        /**
         * 移动->结束拜访
         * 手动结束->结束拜访
         */
        log("(ACTION/" + action + ") onVisitingStatusAction ==============");
        if (action == Action.Moved)
        {
            EndVisitRequest request = new EndVisitRequest();
            request.id = curVisitingId;
            //TODO: 检查磁盘保存的数据
            toStatusEndingVisit(request);
            return true;
        }
        else if (action == Action.EndVisit)
        {
            EndVisitRequest request = (EndVisitRequest) actionData;
            toStatusEndingVisit(request);
            return true;
        }
        return false;
    }

    private boolean onEndingVisitStatusAction(Action action, Object actionData)
    {
        /**
         * 失败->停止
         * 成功-条件满足->定位
         * 成功->条件不满足->停止
         */
        log("(ACTION/" + action + ") onEndingVisitStatusAction ==============");
        if (action != Action.RequestFinished)
        {
            log("[ERROR]action != Action.RequestFinished");
            return false;
        }
        RequestResult result = (RequestResult) actionData;
        if (result.requestFor != Status.EndingVisit)
        {
            log("[ERROR]result.requestFor != Status.EndingVisit");
            return false;
        }

        int visitId = (Integer) result.extra;
        BaseResponse response = result.response;
        if (!BaseResponse.hasError(response))
        {
            broadcastVisitEnded(visitId, true);
            endVisitNotification(visitId);
        }
        else
        {
            broadcastVisitEnded(visitId, false);
        }

        if (!isConditionsSatisfied(false))
        {
            toStatusStopped();
        }
        else
        {
            clearState();
            stopAllTasks();
            if (BaseResponse.hasError(response))
            {
                toStatusRefreshing();
            }
            else
            {
                toStatusLocating();
            }
        }
        return true;
    }

    //进入状态
    void toStatusStopped()
    {
        log("[CHANGE] toStatusStopped ~~~~~~~~~~~~~~~~~");
        curStatus = Status.Stopped;
        clearState();
        stopAllTasks();
    }

    void toStatusRefreshing()
    {
        log("[CHANGE] toStatusRefreshing ~~~~~~~~~~~~~~~~~");
        curStatus = Status.Refreshing;

        clearState();
        stopAllTasks();

        getCurrentVisitTask = new AsyncTask<String, Integer, CurrentVisitRecordResponse>()
        {
            @Override
            protected CurrentVisitRecordResponse doInBackground(String[] params)
            {
                log("toStatusRefreshing doing....");
                String token = params[0] + "";
                if (CommonUtils.IsNullOrEmpty(token))
                {
                    log("toStatusRefreshing null token");
                    return null;
                }
                return MyApplication_.getInstance().restClient().getCurrentVisitPlan(Account.getInstance().getToken());
            }

            @Override
            protected void onPostExecute(CurrentVisitRecordResponse resp)
            {
                log("toStatusRefreshing finish");
                onAction(Action.RequestFinished, new RequestResult(VisitService.Status.Refreshing, resp));
            }
        };
        getCurrentVisitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Account.getInstance().getToken());
    }

    void toStatusLocating()
    {
        log("[CHANGE] toStatusLocating ~~~~~~~~~~~~~~~~~");
        Status prevStatus = curStatus;
        curStatus = Status.Loctating;
        if (prevStatus != Status.CheckingCustomer)
        {
            locationNotification();
        }
        if (!workingLooper.hasMessages(MSG_DO_LOCATE))
        {
            workingLooper.sendEmptyMessageDelayed(MSG_DO_LOCATE, 0);
        }
    }

    void toStatusCheckingCustomer()
    {
        log("[CHANGE] toStatusCheckingCustomer ~~~~~~~~~~~~~~~~~");
        curStatus = Status.CheckingCustomer;
        stopAllTasks();
        getAroundClinetTask = new AsyncTask<String, Integer, AutoVisitCustomerListResponse>()
        {
            @Override
            protected AutoVisitCustomerListResponse doInBackground(String... params)
            {
                log("toStatusCheckingCustomer doing...");
                String token = params[0];
                if (CommonUtils.IsNullOrEmpty(token))
                {
                    log("toStatusCheckingCustomer  null token");
                    return null;
                }

                return MyApplication_.getInstance().restClient().getAroundCustomerList(lastPosition.getLongitude(), lastPosition.getLatitude(), token);

            }

            @Override
            protected void onPostExecute(AutoVisitCustomerListResponse resp)
            {
                log("toStatusCheckingCustomer finish...");
                if (BaseResponse.hasError(resp))
                {
                    log("toStatusCheckingCustomer failed : " + BaseResponse.getErrorMessage(resp));
                    Toast.makeText(VisitService.this, "无法获取周围客户列表:" + BaseResponse.getErrorMessage(resp), Toast.LENGTH_SHORT).show();
                }
                onAction(Action.RequestFinished, new RequestResult(VisitService.Status.CheckingCustomer, resp));
            }
        };
        getAroundClinetTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Account.getInstance().getToken());
    }

    void toStatusWatingStart(ArrayList<AutoVisitCustomerInfo> customers)
    {
        log("[CHANGE] toStatusWatingStart ~~~~~~~~~~~~~~~~~");
        curStatus = Status.WaitingStart;
        if (customers == null)
        {
            //只改变状态
            return;
        }

        log("Customers : ");
        for (AutoVisitCustomerInfo info : customers)
        {
            log("\t\t " + info.toString());
        }
        Intent intent = new Intent(VisitService.this, AutoVisitClientListActivity_.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_CUSTOMERS, customers);
        startActivity(intent);
        if (customers.size() == 1)
        {
            log("Only 1 customer, start to count down 20 ...");
            Message message = Message.obtain(workingLooper, MSG_VISIT_COUNT_DOWN, 20, 0, customers);
            workingLooper.sendMessageDelayed(message, 1000);
        }
        broadcastAboutToVisit();
    }

    void toStatusStartingVisit(final long customerId, final double longitude, final double latitude)
    {
        log("[CHANGE] toStatusStartingVisit ~~~~~~~~~~~~~~~~~");
        curStatus = Status.Starting;
        log(String.format("startVisit : customerId = %d, location = (%f, %f)", customerId, longitude, latitude));

        stopAllTasks();
        startVisitTask = new AsyncTask<String, Integer, BeginAutoVisitResponse>()
        {
            @Override
            protected BeginAutoVisitResponse doInBackground(String... params)
            {
                log("toStatusStartingVisit doing....");
                String token = params[0];
                if (CommonUtils.IsNullOrEmpty(token))
                {
                    log("toStatusStartingVisit null token");
                    return null;
                }

                return MyApplication_.getInstance().restClient().beginAutoVisit(customerId, longitude, latitude, token);
            }

            @Override
            protected void onPostExecute(BeginAutoVisitResponse resp)
            {
                log("toStatusStartingVisit finish");
                if (BaseResponse.hasError(resp))
                {
                    log("startVisitTask failed : " + BaseResponse.getErrorMessage(resp));
                    Toast.makeText(VisitService.this, "无法开始拜访:" + BaseResponse.getErrorMessage(resp), Toast.LENGTH_SHORT).show();
                }
                onAction(Action.RequestFinished, new RequestResult(VisitService.Status.Starting, resp));
            }
        };
        startVisitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Account.getInstance().getToken());
    }

    void toStatusVisiting(int visitId, BDLocation customerLocation, BDLocation lastLocation)
    {
        log("[CHANGE] toStatusVisiting ~~~~~~~~~~~~~~~~~");
        curStatus = Status.Visiting;
        curVisitingId = visitId;

        if (lastLocation == null)
        {
            lastLocation = customerLocation;
        }

        curVisitLocation = new BDLocation();
        curVisitLocation.setLongitude(customerLocation.getLongitude());
        curVisitLocation.setLatitude(customerLocation.getLatitude());

        curCenterTimeStamp = 0;
        curCenter = new BDLocation();
        curCenter.setLongitude(lastLocation.getLongitude());
        curCenter.setLatitude(lastLocation.getLatitude());

        lastPosition = new BDLocation();
        lastPosition.setLongitude(lastLocation.getLongitude());
        lastPosition.setLatitude(lastLocation.getLatitude());

        broadcastVisitStarted(0, true);
        visitingNotification(curVisitingId);
    }

    void toStatusEndingVisit(final EndVisitRequest request)
    {
        log("[CHANGE] toStatusEndingVisit ~~~~~~~~~~~~~~~~~");
        curStatus = Status.EndingVisit;
        stopAllTasks();
        endVisitTask = new AsyncTask<String, Integer, BaseResponse>()
        {
            @Override
            protected BaseResponse doInBackground(String... params)
            {
                log("toStatusEndingVisit doing");
                String token = params[0];
                if (CommonUtils.IsNullOrEmpty(token))
                {
                    log("toStatusEndingVisit null token");
                    return null;
                }

                CacheUtils.VisitAttachment attachment = CacheUtils.GetVisitSavedAttachment(request.id);
                if (attachment != null)
                {
                    if (attachment.file != null)
                    {
                        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
                        parts.add("file0", new FileSystemResource(ImageUtils.compressImg(VisitService.this, attachment.file)));
                        UploadFileResponse response = MyApplication_.getInstance().restClient().uploadVisitRecordFiles(parts, token);
                        if (BaseResponse.hasError(response))
                        {
                            return response;
                        }
                        else
                        {
                            request.filePathIds = response.data;
                        }
                    }
                    request.remark = attachment.text;
                }
                return MyApplication_.getInstance().restClient().endVisit(request, token);
            }

            @Override
            protected void onPostExecute(BaseResponse resp)
            {
                log("toStatusEndingVisit finish");
                if (BaseResponse.hasError(resp))
                {
                    log("toStatusEndingVisit failed : " + BaseResponse.getErrorMessage(resp));
                    Toast.makeText(VisitService.this, "无法结束拜访:" + BaseResponse.getErrorMessage(resp), Toast.LENGTH_SHORT).show();;
                }
                else
                {
                    CacheUtils.DeleteVisitSavedAttachment(request.id);
                }
                onAction(Action.RequestFinished, new RequestResult(VisitService.Status.EndingVisit, resp, request.id));
            }
        };
        endVisitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Account.getInstance().getToken());
    }

    private boolean isVisitExpired(CurrentRecordInfo info)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            Calendar visitDate = Calendar.getInstance();
            visitDate.setTime(format.parse(info.visitStartDate));
            Calendar today = Calendar.getInstance();
            if (today.get(Calendar.YEAR) != visitDate.get(Calendar.YEAR)
                    || today.get(Calendar.MONTH) != visitDate.get(Calendar.MONTH)
                    || today.get(Calendar.DAY_OF_MONTH) != visitDate.get(Calendar.DAY_OF_MONTH))
            {
                return true;
            }

        }
        catch (Exception e)
        {
            log("[ERROR] isVisitExpired" + e.getMessage());
            return true;
        }
        return false;
    }

    private boolean isConditionsSatisfied(boolean notification)
    {
        //获取各种前置状态gps, 网络, 当前时间
        boolean workingtime = isWorkingTime();
        boolean gpsOpen = CommonUtils.isLocationOpen(VisitService.this);
        boolean networkOK = Connectivities.isConnected(this);

        if (!networkOK)
        {
            if (notification)
            {
                networkNotification(true);
            }
        }
        else
        {
            networkNotification(false);
        }


        if (!gpsOpen)
        {
            if (notification)
            {
                gpsNotification(true);
            }
        }
        else
        {
            gpsNotification(false);
        }
        return (workingtime && gpsOpen && networkOK);
    }

    private void checkConditionSatisfied(boolean notification)
    {
        if (isConditionsSatisfied(notification))
        {
            onAction(Action.ConditionSatisfied, null);
        }
        else
        {

            onAction(Action.ConditionFailed, null);
        }
    }

    private void clearState()
    {
        //停止等待的timer
        workingLooper.removeCallbacksAndMessages(null);
        //停止定位
        stopLocationClient();
        //复位变量
        lastPosition = null;
        curCenter = null;
        curCenterTimeStamp = 0;
        curVisitingId = -1;
        curVisitLocation = null;
        //通知外界关闭等待界面
        broadcastAboutToVisitCanceled();
    }

    private void startLocationClient()
    {
        stopLocationClient();
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);                //可选，默认false,设置是否使用gps
//        option.setAddrType("all");              //返回的定位结果包含地址信息
        option.setCoorType("bd09ll");           //返回的定位结果是百度经纬度,默认值gcj02 可选,设置返回的定位结果坐标系
        option.setProdName(getPackageName());
        /**
         * Hight_Accuracy 高精度定位模式：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
         * Battery_Saving 低功耗定位模式：这种定位模式下，不会使用GPS，只会使用网络定位（Wi-Fi和基站定位）；
         * Device_Sensors 仅用设备定位模式：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。
         * */
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);       //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setScanSpan(1 * 1000);                //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
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

        locationClient = new LocationClient(getApplicationContext());
        locationListener = new LocationListener();
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(locationListener);
        locationClient.start();
    }

    private void stopLocationClient()
    {
        if (locationClient != null)
        {
            locationListener.canceled = true;
            locationClient.unRegisterLocationListener(locationListener);
            locationClient.stop();
            locationClient = null;
            locationListener = null;
        }
    }

    private void stopAllTasks()
    {
        //1. 结束各种异步任务
        if (startVisitTask != null)
        {
            log("Cancel startVisitTask");
            startVisitTask.cancel(true);
            startVisitTask = null;
        }
        if (endVisitTask != null)
        {
            log("Cancel endVisitTask");
            endVisitTask.cancel(true);
            endVisitTask = null;
        }
        if (getCurrentVisitTask != null)
        {
            log("Cancel getVisitIDTask");
            getCurrentVisitTask.cancel(true);
            getCurrentVisitTask = null;
        }
        if (getAroundClinetTask != null)
        {
            log("Cancel getAroundClinetTask");
            getAroundClinetTask.cancel(true);
            getAroundClinetTask = null;
        }
    }

    private void registerReceivers()
    {
        log("registerReceivers========================================");
        if (mNetworkReceiver == null)
        {
            mNetworkReceiver = new NetworkReceiver();
            IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mNetworkReceiver, networkFilter);
        }
        if (mLocationModeChangeReceiver == null)
        {
            mLocationModeChangeReceiver = new LocationModeChangeReceiver();
            IntentFilter intentFilter = new IntentFilter();
            if (CommonUtils.getSdkVersion() >= 19)
            {
                intentFilter.addAction(LocationManager.MODE_CHANGED_ACTION);
            }
            else
            {
                intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
            }
            registerReceiver(mLocationModeChangeReceiver, intentFilter);
        }
        if (mTimerChangeReceiver == null)
        {
            mTimerChangeReceiver = new TimerChangeReceiver();
            IntentFilter tcFilter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
            tcFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            registerReceiver(mTimerChangeReceiver, tcFilter);
        }
        if (mAlarmReceiver == null)
        {
            mAlarmReceiver = new AlarmReceiver();
            IntentFilter filter = new IntentFilter("VisitService.Alarm");
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            registerReceiver(mAlarmReceiver, filter);
        }
    }

    private void unregisterReceivers()
    {
        log("unregisterReceiver========================================");
        try
        {
            unregisterReceiver(mLocationModeChangeReceiver);
        }
        catch (Exception e)
        {

        }
        try
        {
            unregisterReceiver(mNetworkReceiver);
        }
        catch (Exception e)
        {

        }
        try
        {
            unregisterReceiver(mTimerChangeReceiver);
        }
        catch (Exception e)
        {

        }
        try
        {
            unregisterReceiver(mAlarmReceiver);
        }
        catch (Exception e)
        {

        }
        mNetworkReceiver = null;
        mLocationModeChangeReceiver = null;
        mTimerChangeReceiver = null;
        mAlarmReceiver = null;
    }

    private boolean isWorkingTime()
    {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour > START_WORK_HOUR && hour < STOP_WORK_HOUR);
    }


    /**
     * 判断坐标location是否还在center的半径范围内
     *
     * @return true 移动,并且更新了centerLocation, false 还在半径范围内
     */
    private boolean updateCenterIfMoved(BDLocation location)
    {
        log("updateCenterIfMoved========================================");
        if (curCenter != null)
        {
            LatLng latLngCenter = new LatLng(curCenter.getLatitude(), curCenter.getLongitude());
            LatLng latLngNow = new LatLng(location.getLatitude(), location.getLongitude());

            double distanceFromCenter = DistanceUtil.getDistance(latLngCenter, latLngNow);

            double distanceFromVisit = -1;
            if (curVisitLocation != null)
            {
                LatLng latLngVisit = new LatLng(curVisitLocation.getLatitude(), curVisitLocation.getLongitude());
                distanceFromVisit = DistanceUtil.getDistance(latLngVisit, latLngNow);
            }

            log("updateCenterIfMoved distanceCenter = " + distanceFromCenter + " distanceVisit = " + distanceFromVisit);
            //此处需要判断最后的位置和客户实际位置的距离, 有可能由于精度的原因客户刚好在半径边缘, 此时如果只判断最后站的位置有可能稍微有点移动就会超出客户范围,导致拜访结束。
            if ((distanceFromCenter >= 0 && distanceFromCenter <= HOLD_STAY_DISTANCE)
                    || (distanceFromVisit >= 0 && distanceFromVisit <= HOLD_STAY_DISTANCE))

            {
                log("updateCenterIfMoved : not moved");
                return false;
            }
        }
        log("updateCenterIfMoved : moved!!!!!! update to new center");
        curCenterTimeStamp = System.currentTimeMillis();
        curCenter = location;
        return true;
    }

    private void scheduleWorkingTimer()
    {
        log("scheduleWorkingTimer========================================---");
        //循环检测, 频率降低, 否则逻辑太复杂,尤其是考虑到启动timer的时候刚好在上下班交错的情况下
        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                workingLooper.sendEmptyMessageDelayed(isWorkingTime() ? MSG_START_WORKING : MSG_STOP_WORKING, 0);
            }
        }, 0, WORKING_TIMER_CHECK_PERIOD);//10 min
    }

    private void scheduleAlarm()
    {
        Intent intent = new Intent("VisitService.Alarm");
        PendingIntent sender = PendingIntent.getBroadcast(VisitService.this, 0, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ALARM_POWER_INTERVAL, ALARM_POWER_INTERVAL, sender);
    }
    private void cancelAlarm()
    {
        Intent intent = new Intent("VisitService.Alarm");
        PendingIntent sender = PendingIntent.getBroadcast(VisitService.this, 0, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }
    void checkLocation(BDLocation newLocation)
    {
        log("checkLocation========================================");
        lastPosition = newLocation;
        log(String.format("checkLocation location : (%f : %f)", newLocation.getLongitude(), newLocation.getLatitude()));
        logStatus();
        boolean moved = updateCenterIfMoved(newLocation);
        if (moved)
        {
            onAction(Action.Moved, null);
        }
        else
        {
            log("checkLocation time passed = " + (System.currentTimeMillis() - curCenterTimeStamp));
            if (curCenter == null || System.currentTimeMillis() - curCenterTimeStamp < AUTO_VISIT_STAY_DURATION)
            {
                return;
            }
            onAction(Action.Stayed, null);
        }
    }

    private static final int MSG_DO_LOCATE = 10;
    private static final int MSG_VISIT_COUNT_DOWN = 12;
    private static final int MSG_START_WORKING = 1;
    private static final int MSG_STOP_WORKING = 2;

    Handler workingLooper = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            removeMessages(msg.what);

            logStatus();
            if (msg.what == MSG_DO_LOCATE)
            {
                log("[LOOPER] : MSG_DO_LOCATE");
                if (locationClient != null)
                {
                    log("location timeout");
                    //轮了一圈结果还没有定位到,超时了,干掉location重新来
                    stopLocationClient();
                    sendEmptyMessageDelayed(MSG_DO_LOCATE, 2000);
                    return;

                }
                startLocationClient();
                sendEmptyMessageDelayed(MSG_DO_LOCATE, GET_LOCATION_PERIOD);
            }
            else if (msg.what == MSG_START_WORKING)
            {
                log("[LOOPER] : MSG_START_WORKING");
                checkConditionSatisfied(true);
            }
            else if (msg.what == MSG_STOP_WORKING)
            {
                log("[LOOPER] : MSG_STOP_WORKING");
                checkConditionSatisfied(false);
            }
            else if (msg.what == MSG_VISIT_COUNT_DOWN)
            {
                int count = msg.arg1;
                count = Math.max(count - 1, 0);
                broadcastAboutToVisitCountDown(count);
                log("[LOOPER] : MSG_VISIT_COUNT_DOWN " + count);
                if (count > 0)
                {
                    sendMessageDelayed(Message.obtain(this, MSG_VISIT_COUNT_DOWN, count, 0, msg.obj), 1000);
                    return;
                }
                List<AutoVisitCustomerInfo> infos = (List<AutoVisitCustomerInfo>) msg.obj;
                if (infos.size() == 1)
                {
                    log("MSG_VISIT_COUNT_DOWN  TIME OUT");
                    onAction(Action.WaitingTimeout, infos.get(0).ID);
                }
            }
        }
    };

    class LocationListener implements BDLocationListener
    {
        private boolean canceled = false;
        private int locTimes = 0;

        @Override
        public void onReceiveLocation(BDLocation bdLocation)
        {
            if (canceled)
            {
                return;
            }
            locTimes++;
            if (locTimes >= 15)
            {
                //最多定位15次,如果15次都失败,就休息,等下次再试
                log("BDLocationListener tried " + locTimes + " times failed, try next round!!!!!!!");
                locTimes = 0;
                stopLocationClient();
                return;
            }
            log("BDLocationListener==========================================");
            if (bdLocation == null)
            {
                log("BDLocationListener null bdLocation");
                return;
            }
            int locType = bdLocation.getLocType();

            log("BDLocationListener location Type = BDLocation.TypeNetWorkLocation");
            if (locType != BDLocation.TypeNetWorkLocation
                    && locType != BDLocation.TypeGpsLocation
                    && locType != BDLocation.TypeOffLineLocation)
            {
                log("BDLocationListener bad locaton type, do nothing");
                return;
            }

            if (locTimes < 2) //连续定位3次,取最后的值
            {
                return;
            }
            stopLocationClient();
            checkLocation(bdLocation);
        }
    }

    private class LocationModeChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            log("LocationModeChangeReceiver==========================");
            checkConditionSatisfied(true);
        }
    }

    private class NetworkReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            log("NetworkReceiver==========================");
            checkConditionSatisfied(true);
        }
    }

    private class TimerChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction() == Intent.ACTION_TIME_CHANGED || intent.getAction() == Intent.ACTION_TIMEZONE_CHANGED)
            {
                log("TimerChangeReceiver==========================");
                //checkConditionSatisfied(true);
                scheduleWorkingTimer();
            }
        }
    }

    private class AlarmReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            log("Alarm Received******************************");
            requirePowerLock();
        }
    }
//    private void requireStrongPowerLock()
//    {
//        releaseStrongPowerLock();
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, VisitService.class.getName());
//        mWakeLock.acquire(600 * 1000);
//    }
//    private void releasePowerLock()
//    {
//        try
//        {
//            mWakeLock.release();
//        }
//        catch (Exception e)
//        {
//
//        }
//        finally
//        {
//            mWakeLock = null;
//        }
//    }

    private void requirePowerLock()
    {
        releasePowerLock();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, VisitService.class.getName());
        mWakeLock.acquire(600 * 1000);
    }
    private void releasePowerLock()
    {
        try
        {
            mWakeLock.release();
        }
        catch (Exception e)
        {

        }
        finally
        {
            mWakeLock = null;
        }
    }

    private PowerManager.WakeLock mWakeLock = null;
    private PowerManager.WakeLock mStrongWakeLock = null;
    public static final String ACTION_VISIT_ENDED = "com.drugoogle.sellscrm.visit.VisitService.ACTION_VISIT_ENDED";
    public static final String ACTION_VISIT_STARTED = "com.drugoogle.sellscrm.visit.VisitService.ACTION_VISIT_STARTED";
    public static final String ACTION_CANCEL_ABOUT_TO_VISIT = "com.drugoogle.sellscrm.visit.VisitService.ACTION_CANCEL_ABOUNT_TO_VISIT";
    public static final String ACTION_ABOUT_TO_VISIT = "com.drugoogle.sellscrm.visit.VisitService.ACTION_ABOUNT_TO_VISIT";
    public static final String ACTION_ABOUT_TO_VISIT_COUNT_DOWN = "com.drugoogle.sellscrm.visit.VisitService.ACTION_ABOUNT_TO_VISIT_COUNT_DOWN";

    public static final String EXTRA_VISIT_ID = "com.drugoogle.sellscrm.visit.VisitService.EXTRA_VISIT_ID";
    public static final String EXTRA_COUNT_DOWN = "com.drugoogle.sellscrm.visit.VisitService.EXTRA_COUNT_DOWN";
    public static final String EXTRA_CUSTOMERS = "com.drugoogle.sellscrm.visit.VisitService.EXTRA_CUSTOMERS";
    public static final String EXTRA_CURRENT_POSITION = "com.drugoogle.sellscrm.visit.VisitService.EXTRA_CURRENT_POSITION";
    public static final String EXTRA_MISMATCH_REASON = "com.drugoogle.sellscrm.visit.VisitService.EXTRA_MISMATCH_REASON";
    public static final String EXTRA_RESULT = "com.drugoogle.sellscrm.visit.VisitService.EXTRA_RESULT";


    private void broadcastVisitEnded(int visitingId, boolean success)
    {
        Intent intent = new Intent(ACTION_VISIT_ENDED);
        intent.putExtra(EXTRA_VISIT_ID, visitingId);
        intent.putExtra(EXTRA_RESULT, success);
        sendBroadcast(intent);
    }

    private void broadcastVisitStarted(int visitingId, boolean success)
    {
        Intent intent = new Intent(ACTION_VISIT_STARTED);
        intent.putExtra(EXTRA_VISIT_ID, visitingId);
        intent.putExtra(EXTRA_RESULT, success);
        sendBroadcast(intent);
    }

    private void broadcastAboutToVisit()
    {
        sendBroadcast(new Intent(ACTION_ABOUT_TO_VISIT));
    }

    private void broadcastAboutToVisitCanceled()
    {
        sendBroadcast(new Intent(ACTION_CANCEL_ABOUT_TO_VISIT));
    }

    private void broadcastAboutToVisitCountDown(int countdown)
    {
        Intent intent = new Intent(ACTION_ABOUT_TO_VISIT_COUNT_DOWN);
        intent.putExtra(EXTRA_COUNT_DOWN, countdown);
        sendBroadcast(intent);
    }


    /**
     * 拜访中notification，驻留
     */
    void visitingNotification(int vistingId)
    {
        Intent intent = new Intent(this, VisitRecordDetailActivity_.class).putExtra("mRecordId", vistingId).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder.setSmallIcon(R.drawable.app_statusbar_icon)
                .setTicker(getString(R.string.visiting_noti_content_title))
                .setContentTitle(getString(R.string.visiting_noti_content_title))
                .setContentText(getString(R.string.visiting_noti_content_text))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        mNotification = mNotificationBuilder.build();
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        //mNotificationManager.notify(mNotificationId, mNotification);
        startForeground(mNotificationId, mNotification);
    }

    /**
     * 自动拜访定位中notification
     */
    void locationNotification()
    {
        Intent intent = new Intent(this, MainActivity_.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setSmallIcon(R.drawable.app_statusbar_icon)
                .setTicker(getString(R.string.location_ongoing))
                .setContentTitle(getString(R.string.location_ongoing))
                .setContentText(getString(R.string.auto_visit_location_service_working))
                .setDefaults(Notification.DEFAULT_ALL)              //震动，提示音等全部采用默认
                .setContentIntent(pendingIntent);

        mNotification = mNotificationBuilder.build();
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        //mNotificationManager.notify(mNotificationId, mNotification);
        startForeground(mNotificationId, mNotification);
    }

    /**
     * 拜访结束notification
     */
    void endVisitNotification(int vistingId)
    {
        Intent intent = new Intent(this, VisitRecordDetailActivity_.class).putExtra("mRecordId", vistingId).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder.setSmallIcon(R.drawable.app_statusbar_icon)
//                .setLargeIcon(mIconBitmap)
                .setTicker(getString(R.string.visit_finish))
                .setContentTitle(getString(R.string.visit_finish))
                .setContentText(getString(R.string.visit_finish_when_leave_customer))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        Notification notification = mNotificationBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(mEndVisitNotificationId, notification);
        //mNotificationManager.cancel(mNotificationId);
    }

    /**
     * 提示打开GPS notification，驻留
     */
    void gpsNotification(boolean open)
    {
        if (!open)
        {
            if (mGpsNotificationId > 0)
            {
                mNotificationManager.cancel(mGpsNotificationId);
                mGpsNotificationId = 0;
            }
        }
        else
        {
            if (mGpsNotificationId > 0)
            {
                return;
            }
            mGpsNotificationId = 125;
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            mNotificationBuilder.setSmallIcon(R.drawable.app_statusbar_icon)
                    .setTicker(getString(R.string.gps_permission_request))
                    .setContentTitle(getString(R.string.gps_permission_request))
                    .setContentText(getString(R.string.open_gps_for_auto_visit))
                    .setVibrate(new long[]{0, 200, 200, 200})
                    .setContentIntent(pendingIntent);

            Notification notification = mNotificationBuilder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            mNotificationManager.notify(mGpsNotificationId, notification);
        }
    }

    /**
     * 检查网络连接 notification，驻留
     */
    void networkNotification(boolean open)
    {

        if (!open)
        {
            if (mNetworkNotificationId > 0)
            {
                mNotificationManager.cancel(mNetworkNotificationId);
                mNetworkNotificationId = 0;
            }
        }
        else
        {
            if (mNetworkNotificationId > 0)
            {
                return;
            }
            mNetworkNotificationId = 126;
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            mNotificationBuilder.setSmallIcon(R.drawable.app_statusbar_icon)
                    .setTicker(getString(R.string.please_check_network))
                    .setContentTitle(getString(R.string.please_check_network))
                    .setContentText(getString(R.string.please_confirm_connected_network))
                    .setVibrate(new long[]{0, 200, 200, 200})
                    .setContentIntent(pendingIntent);

            Notification notification = mNotificationBuilder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            mNotificationManager.notify(mNetworkNotificationId, notification);
        }


    }


    private Handler binderHandler = new Handler();

    public class ServiceBinder extends Binder
    {
        public void startVisit(long customerId)
        {
            if (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId())
            {
                throw new Error("Call binder function from non-main thread");
            }
            onAction(Action.StartVisit, customerId);
        }

        public void endVisit(EndVisitRequest request)
        {
            if (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId())
            {
                throw new Error("Call binder function from non-main thread");
            }
            onAction(Action.EndVisit, request);
        }

        public void cancelAboutToStart(String reason)
        {
            if (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId())
            {
                throw new Error("Call binder function from non-main thread");
            }
            onAction(Action.WaitingCancel, reason);
        }
    }


}
