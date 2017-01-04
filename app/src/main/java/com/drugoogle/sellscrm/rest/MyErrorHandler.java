package com.drugoogle.sellscrm.rest;

import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.api.RestErrorHandler;
import org.springframework.core.NestedRuntimeException;

/**
 * Created by wgh on 2016/3/31.
 */
@EBean
public class MyErrorHandler implements RestErrorHandler {

    final static String TAG = MyErrorHandler.class.getSimpleName();
    @Override
    public void onRestClientExceptionThrown(NestedRuntimeException e)
    {
        Log.d(TAG, e.toString());
        e.printStackTrace();
    }
}
