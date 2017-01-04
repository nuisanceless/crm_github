package com.drugoogle.sellscrm.selfinfo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.drugoogle.sellscrm.MainActivity_;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.PrefUtils_;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.visit.VisitService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by ydwang on 2016/4/14.
 */
@EActivity(R.layout.selfinfo_setting)
public class Selfinfo_setting_Activity extends BaseActivity {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.title)
    TextView title;
    @ViewById(R.id.self_setting_check)
    ToggleButton selfSettingCheck;
    @ViewById(R.id.self_setting_sync)
    ToggleButton selfSettingSync;
    @ViewById(R.id.self_setting_wifi)
    ToggleButton selfSettingWifi;
    @ViewById(R.id.txt_version)
    TextView mVersionTv;

    Account account;

    @Pref
    PrefUtils_ prefUtils;

    /**
     * 退出登录
     */
    @Click(R.id.self_setting_logout)
    void selfSettingLogout() {
        account.logout();

        Intent serviceIntent = new Intent(this, VisitService.class);
        stopService(serviceIntent);
        finish();
        MainActivity_.finishInstance();
        //跳转到登录界面
        prefUtils.edit().withoutLogin().put( false ).apply();
        Selfinfo_login_Activity_.intent( this ).start();

    }

    @Click(R.id.btn_checkupdate)
    void checkUpdate() {
        CommonUtils.checkUpdate( this, true );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @AfterViews
    void init()
    {
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );
        toolbar.setNavigationIcon( R.drawable.back_white );
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );

        title.setText( getString( R.string.setting ) );
        mVersionTv.setText( getResources().getString( R.string.version_name ) + mApp.getVersion() );

        account = Account.getInstance();
        selfSettingCheck.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //TODO
                } else {
                    //TODO
                }
            }
        } );
        selfSettingSync.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                } else {

                }
            }
        } );
        selfSettingWifi.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                } else {

                }
            }
        } );

        try {
            // ---get the package info---
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo( getPackageName(), 0 );
            mVersionTv.setText( "(当前版本 " + pi.versionName + ")" );
        } catch (Exception e) {
        }
        findViewById( R.id.self_setting_logout ).setVisibility( prefUtils.withoutLogin().getOr(false) ? View.VISIBLE : View.INVISIBLE );
    }
}
