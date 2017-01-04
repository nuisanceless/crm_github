package com.drugoogle.sellscrm.common;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.baidu.mapapi.SDKInitializer;
import com.drugoogle.sellscrm.Utils.CrmLogger;
import com.drugoogle.sellscrm.Utils.FileUtils;
import com.drugoogle.sellscrm.Utils.LogUtils;
import com.drugoogle.sellscrm.Utils.PrefUtils_;
import com.drugoogle.sellscrm.rest.MyErrorHandler;
import com.drugoogle.sellscrm.rest.MyInterceptor;
import com.drugoogle.sellscrm.rest.MyResponseErrorHandler;
import com.drugoogle.sellscrm.rest.MyRestClient;
import com.drugoogle.sellscrm.rest.MyRestTemplate;
import com.drugoogle.sellscrm.selfinfo.Account;
import com.drugoogle.sellscrm.visit.VisitService;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.pgyersdk.crash.PgyCrashManager;
import com.tmindtech.log.Logger;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.rest.spring.annotations.RestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * Created by wuguohao on 16/3/25.
 */
@EApplication
public class MyApplication extends Application implements MyResponseErrorHandler.ErrorListener
{
//    @RestService
//    MyRestClient mRestClient;
//
//    public MyRestClient restClient()
//    {
//        return mRestClient;
//    }

    public static CrmLogger logger;
    private static final boolean DEBUG = false;
    /**
     * 获取APP是否开启debug的状态
     * */
    public static boolean getDebug ()
    {
        return DEBUG;
    }

    public void onCreate() {
        super.onCreate();
        logger =  new CrmLogger(this, "CRM");//Use CRMLogger for upload

        FileUtils.mkdir(LogUtils.LOG_FILE_DIR);
        SDKInitializer.initialize(this);

        PgyCrashManager.register(this);

    }

    public String getVersion()
    {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getVersionCode()
    {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public MyRestClient restClient()
    {
        return mRestClient;
    }


    @RestService
    MyRestClient mRestClient;

    @Bean
    MyErrorHandler mErrorHandler;

    @Bean
    MyInterceptor mInterceptor;

    @Bean
    MyRestTemplate mMyRestTemplate;

    MyResponseErrorHandler mErrHandler = new MyResponseErrorHandler();

    @Pref
    PrefUtils_ prefUtils;

    @AfterInject
    void afterInject()
    {
        Account account = Account.getInstance();
        account.loadSavedStates(this);

        mMyRestTemplate.setMessageConverters(mRestClient.getRestTemplate().getMessageConverters());
        mRestClient.setRestTemplate(mMyRestTemplate);
        mRestClient.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        mRestClient.setRestErrorHandler(mErrorHandler);
        mRestClient.getRestTemplate().setErrorHandler(mErrHandler);
        mInterceptor.setRestClient(mRestClient);
        mRestClient.getRestTemplate().getInterceptors().add(mInterceptor);
        mErrHandler.setErrorListener(this);

        Fresco.initialize(getBaseContext());

        if (account.isLogin())
        {
            Intent serviceIntent = new Intent( this, VisitService.class );
            startService( serviceIntent );
        }
    }


    @Override
    public void onError(HttpStatus status) {
//        if(status == HttpStatus.UNAUTHORIZED) {
//            LoginActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    .start();
//        }
    }
}
