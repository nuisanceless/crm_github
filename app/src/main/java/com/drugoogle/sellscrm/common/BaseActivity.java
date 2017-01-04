package com.drugoogle.sellscrm.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;

import com.drugoogle.sellscrm.rest.MyRestClient;

/**
 * Created by wgh on 2016/3/28.
 */
public class BaseActivity extends AppCompatActivity
{
    protected MyApplication mApp;
    protected ActivityHelper_ mActivityHelper;
    protected LayoutInflater mLayoutInflater;
    protected MyRestClient mRestClient;


    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mApp = MyApplication_.getInstance();
        mActivityHelper = ActivityHelper_.getInstance_(this);
        mLayoutInflater = this.getLayoutInflater();
        mRestClient = mApp.restClient();
    }
}
