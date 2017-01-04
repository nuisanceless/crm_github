package com.drugoogle.sellscrm.rest;

import com.drugoogle.sellscrm.common.MyApplication;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;



/**
 * Created by wgh on 2016/3/31.
 */

@EBean
public class MyRestTemplate extends RestTemplate {

    @App
    MyApplication mApp;

//    @Pref
//    MyPrefs_ mPrefs;

    @Override
    protected <T> T doExecute(URI url,
                              HttpMethod method,
                              RequestCallback requestCallback,
                              ResponseExtractor<T> responseExtractor) throws RestClientException
    {
        Assert.notNull(url, "'url' must not be null");
        Assert.notNull(method, "'method' must not be null");
        ClientHttpResponse response = null;
        try {
            ClientHttpRequest request = createRequest(url, method);
            if (requestCallback != null) {
                requestCallback.doWithRequest(request);
            }
            response = request.execute();
            if (getErrorHandler().hasError(response))
            {
                if(response.getStatusCode() == HttpStatus.UNAUTHORIZED)
                {
                    response.close();
//                    autoLogin();
                    request = createRequest(url, method);
                    if (requestCallback != null) {
                        requestCallback.doWithRequest(request);
                    }

                    StringBuilder cookiesValue = new StringBuilder();
                    cookiesValue.append(String.format("ss-id=%s;",
                            mApp.restClient().getCookie("ss-id")));
                    request.getHeaders().set("Cookie", cookiesValue.toString());

//                    mPrefs.edit().cookie().put(mApp.restClient().getCookie("ss-id")).apply();
//                    ShutterbugManager manager = ShutterbugManager.getSharedImageManager(mApp.getBaseContext());
//                    manager.setRequestProperty("Cookie", cookiesValue.toString());
                    response = request.execute();
                }
            }

            if (getErrorHandler().hasError(response))
            {
                getErrorHandler().handleError(response);
            }

            if (responseExtractor != null) {
                return responseExtractor.extractData(response);
            }
            else {
                return null;
            }
        }
        catch (IOException ex) {
            throw new ResourceAccessException("I/O error on " + method.name() +
                    " request for \"" + url + "\": " + ex.getMessage(), ex);
        }
        finally {
            if (response != null) {
                response.close();
            }
        }
    }

//    LoginResponse autoLogin() {
//
//        Log.d(MyRestTemplate.class.getSimpleName(), "AutoLogin");
//
//        LoginResponse resp = null;
//        int loginType = mPrefs.loginType().get();
//        switch (loginType) {
//            case Consts.LOGIN_TYPE_NORMAL: {
//                LoginRequest request = new LoginRequest();
//                request.OS = CommonUtils.getOsName();
//                request.UserName = mPrefs.account().get();
//                request.Password = EncodeHandler.encode(mPrefs.password().get());//EncodeHandler.encode("SHA1", "111111");
//                request.Version = mApp.getVersion();
//                request.DeviceId = CommonUtils.getLocalMacAddress(mApp);
//                resp = mApp.restClient().login(request);
//            }
//            break;
//            case Consts.LOGIN_TYPE_QQ: {
//                ThirdPartyLoginRequest request = new ThirdPartyLoginRequest();
//                request.Token = mPrefs.thirdPartyToken().get();
//                request.UserId = mPrefs.thridPartyUserId().get();
//                request.DeviceId = CommonUtils.getLocalMacAddress(mApp);
//                request.OS = CommonUtils.getOsName();
//                request.Version = mApp.getVersion();
//                resp = mApp.restClient().qqLogin(request);
//            }
//            break;
//            case Consts.LOGIN_TYPE_WEIBO: {
//                ThirdPartyLoginRequest request = new ThirdPartyLoginRequest();
//                request.Token = mPrefs.thirdPartyToken().get();
//                request.UserId = mPrefs.thridPartyUserId().get();
//                request.DeviceId = CommonUtils.getLocalMacAddress(mApp);
//                request.OS = CommonUtils.getOsName();
//                request.Version = mApp.getVersion();
//                resp = mApp.restClient().weiboLogin(request);
//            }
//            break;
//            case Consts.LOGIN_TYPE_WEIXIN: {
//                ThirdPartyLoginRequest request = new ThirdPartyLoginRequest();
//                request.Token = mPrefs.thirdPartyToken().get();
//                request.UserId = mPrefs.thridPartyUserId().get();
//                request.DeviceId = CommonUtils.getLocalMacAddress(mApp);
//                request.OS = CommonUtils.getOsName();
//                request.Version = mApp.getVersion();
//                request.RefreshToken = mPrefs.wechatRefreshToken().get();
//                resp = mApp.restClient().wechatLogin(request);
//            }
//            break;
//            default:
//                break;
//        }
//
//        return resp;
//    }
}
