package com.drugoogle.sellscrm.selfinfo;

import com.drugoogle.sellscrm.data.response.ChangePasswordResponse;
import com.drugoogle.sellscrm.data.response.CustomerVisitCountResponse;
import com.drugoogle.sellscrm.data.response.LoginResponse;
import com.drugoogle.sellscrm.data.response.SendVerifyResponse;
import com.drugoogle.sellscrm.data.response.VerifyResponse;

/**
 * Created by ydwang on 2016/3/25.
 */
public class Person {
    //登录操作返回
    public LoginResponse loginResp;
    //客户数/剩余拜访数量
    public CustomerVisitCountResponse cusVisitCountResp;
    //请求验证码返回
    public VerifyResponse verifyResp;
    //发送验证码返回
    public SendVerifyResponse sendVerifyResp;
    //改变密码返回
    public ChangePasswordResponse changeResp;
    //监听器
    public LoginCallback listener;

    public VerifyResponse getVerifyResp() {
        return verifyResp;
    }

    public void setVerifyResp(VerifyResponse verifyResp) {
        this.verifyResp = verifyResp;
    }

    public SendVerifyResponse getSendVerifyResp() {
        return sendVerifyResp;
    }

    public void setSendVerifyResp(SendVerifyResponse sendVerifyResp) {
        this.sendVerifyResp = sendVerifyResp;
    }

    public ChangePasswordResponse getChangeResp() {
        return changeResp;
    }

    public void setChangeResp(ChangePasswordResponse changeResp) {
        this.changeResp = changeResp;
    }

    public LoginResponse getLoginResp() {
        return loginResp;
    }

    public void setLoginResp(LoginResponse loginResp) {
        this.loginResp = loginResp;
    }

    public CustomerVisitCountResponse getCusVisitCountResp() {
        return cusVisitCountResp;
    }

    public void setCusVisitCountResp(CustomerVisitCountResponse cusVisitCountResp) {
        this.cusVisitCountResp = cusVisitCountResp;
    }

    public LoginCallback getListener() {
        return listener;
    }

    public void setListener(LoginCallback listener) {
        this.listener = listener;
    }
}
