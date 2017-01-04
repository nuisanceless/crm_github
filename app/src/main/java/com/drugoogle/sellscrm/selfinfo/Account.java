package com.drugoogle.sellscrm.selfinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.EncodeUtils;
import com.drugoogle.sellscrm.Utils.PrefUtils_;
import com.drugoogle.sellscrm.common.MyApplication;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.ChangePasswordResponse;
import com.drugoogle.sellscrm.data.response.CustomerVisitCountResponse;
import com.drugoogle.sellscrm.data.response.LoginResponse;
import com.drugoogle.sellscrm.data.response.SendVerifyResponse;
import com.drugoogle.sellscrm.data.response.VerifyResponse;
import com.drugoogle.sellscrm.rest.MyRestClient;

/**
 * Created by ydwang on 2016/3/24.
 */
public class Account {

    private MyApplication mApp = MyApplication_.getInstance();
    private MyRestClient mRestClient = mApp.restClient();

    private Account() {
    }

    //在类初始化时，已经自行实例化
    private static Account account = new Account();

    public static Account getInstance() {
        return account;
    }

    private String id;//账号
    private String pass;//密码
    private String passEncrypt;//加密后密码
    private String passChEncrypt;//修改密码加密
    private static boolean LOGIN_STATE = false;//登录状态
    public void setLoginState (boolean status)
    {
        LOGIN_STATE = status;
    }
    public void loadSavedStates(Context context)
    {
        PrefUtils_ prefUtils = new PrefUtils_(context);

        if (!prefUtils.withoutLogin().getOr(false))
        {
            return;
        }

        setLogTag(prefUtils.logTag().get());
        setPhone(prefUtils.phone().get());
        setName(prefUtils.userName().get());//姓名
        setBirth(prefUtils.birth().get());
        setAvatar(prefUtils.avatar().get());
        setUserId(prefUtils.userId().get());
        setEmail(prefUtils.email().get());
        setGender(prefUtils.gender().get());
        setToken(prefUtils.token().get());
        setCustomerCount(prefUtils.customerCount().get());
        setVisitCount(prefUtils.visitCount().get());
        account.setLoginState(true);
    }
    private int lastRequestId = 1;

    /**
     * 登录
     *
     * @param listener
     */
    protected void login(String account, String password, final LoginCallback listener) {
        id = account;
        pass = password;
        passEncrypt = EncodeUtils.encodePassword( pass, "md5" );
        new Thread( new Runnable() {
            @Override
            public void run() {
                loginBackground( lastRequestId, id, passEncrypt, listener );
            }
        } ).start();
    }

    protected void cancelLogin() {
        ++lastRequestId;//让之前的请求无效
    }

    /**
     * 传入账号密码信息请求服务器返回账号信息并解析
     *
     * @param id
     * @param passEncrypt
     * @param listener
     */
    private void loginBackground(int requestId, String id, String passEncrypt, LoginCallback listener) {
        if (requestId != lastRequestId)//过期的请求
        {
            return;
        }
        //调用登录接口
        LoginResponse loginResponse = mRestClient.login( id, passEncrypt );
        Person person = new Person();
        person.setLoginResp( loginResponse );
        person.setListener( listener );
        Message message = new Message();
        message.obj = person;
        message.arg1 = requestId;
        accountHandler.sendMessage( message );
    }

    Handler accountHandler = new Handler( Looper.getMainLooper() ) {
        public void handleMessage(Message message) {
            if (message.arg1 != lastRequestId)//过期的请求
            {
                return;
            }
            Person person = (Person) message.obj;
            loginResp = person.getLoginResp();
            if (person.getListener() != null) {
                if (loginResp != null && !BaseResponse.hasError( loginResp )) {
                    //登录成功
                    loginFinish( account, person.getListener() );
                } else {
                    //网络中断或登录失败
                    person.getListener().onFinish( account );
                }
            }
        }
    };

    /**
     * 登录结束
     *
     * @param account
     * @param listener
     */
    private void loginFinish(Account account, LoginCallback listener) {

        if (account.getLoginResp().info != null) {
            MyApplication.logger.checkLogEnable();
            LOGIN_STATE = true;
            //获取剩余拜访数/客户数
            requestCustomerVisitCount( listener );
    }
    }

    /**
     * 获取客户数/剩余拜访数
     */
    private void requestCustomerVisitCount(final LoginCallback listener) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                getVisitCustomerCount( listener );
            }
        } ).start();
    }

    private void getVisitCustomerCount(LoginCallback listener) {
        CustomerVisitCountResponse response =
                mRestClient.getCustomerAndVisitCount( account.getLoginResp().token );
        Person person = new Person();
        person.setCusVisitCountResp( response );
        person.setListener( listener );
        Message message = new Message();
        message.obj = person;
        visitCusCountHandler.sendMessage( message );
    }

    Handler visitCusCountHandler = new Handler( Looper.getMainLooper() ) {
        public void handleMessage(Message message) {
            Person person = (Person) message.obj;
            cusVisitResp = person.getCusVisitCountResp();
            //回调
            person.getListener().onFinish( account );
        }
    };

    /**
     * 获取重置密码验证码
     *
     * @param phone
     * @param listener
     */
    protected void requestVerifyCode(final String phone, final LoginCallback listener) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                requestVerifyBackground( phone, listener );
            }
        } ).start();
    }

    private void requestVerifyBackground(String phone, LoginCallback listener) {

        VerifyResponse verifyResponse = mRestClient.getForgetPasswordVerifyCode( phone );
        Person person = new Person();
        person.setVerifyResp( verifyResponse );
        person.setListener( listener );
        Message message = new Message();
        message.obj = person;
        verifyHandler.sendMessage( message );
    }

    Handler verifyHandler = new Handler( Looper.getMainLooper() ) {
        public void handleMessage(Message message) {
            Person person = (Person) message.obj;
            verifyResp = person.getVerifyResp();
            if (person.getListener() != null) {
                person.getListener().onFinish( account );
            }
        }
    };

    /**
     * 向服务器发送手机号和验证码并返回验证信息
     *
     * @param phone
     * @param verifyCode
     * @param listener
     */
    protected void sendVerifyCode(final String phone, final String verifyCode, final LoginCallback listener) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                sendVerifyBackgroud( phone, verifyCode, listener );
            }
        } ).start();
    }

    private void sendVerifyBackgroud(String phone, String verifyCode, LoginCallback listener) {

        SendVerifyResponse sendVerifyResponse = mRestClient.verifyForgetPasswordAuthCode( phone, verifyCode );
        Message message = new Message();
        Person person = new Person();
        person.setSendVerifyResp( sendVerifyResponse );
        person.setListener( listener );
        message.obj = person;
        sendHandler.sendMessage( message );
    }

    Handler sendHandler = new Handler( Looper.getMainLooper() ) {
        @Override
        public void handleMessage(Message message) {
            Person person = (Person) message.obj;
            sendVerifyResp = person.getSendVerifyResp();
            if (person.getListener() != null) {
                person.getListener().onFinish( account );
            }
        }
    };

    /**
     * 修改密码
     *
     * @param phone
     * @param pass
     * @param listener
     */
    protected void changePass(final String phone, final String resetToken, final String pass, final LoginCallback listener) {
        passChEncrypt = EncodeUtils.encodePassword( pass, "md5" );
        try {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    changePassBackground( phone, resetToken, passChEncrypt, listener );
                }
            } ).start();
        } catch (Exception e) {
        }
    }

    private void changePassBackground(String phone, String resetToken, String passChEncrypt, LoginCallback listener) {

        ChangePasswordResponse changeResponse = mRestClient.ChangePasswordRequest( phone, resetToken, passChEncrypt );
        Message message = new Message();
        Person person = new Person();
        person.setChangeResp( changeResponse );
        person.setListener( listener );
        message.obj = person;
        changeHandler.sendMessage( message );
    }

    Handler changeHandler = new Handler( Looper.getMainLooper() ) {
        @Override
        public void handleMessage(Message message) {
            Person person = (Person) message.obj;
            changeResp = person.getChangeResp();
            if (person.getListener() != null) {
                person.getListener().onFinish( account );
            }
        }
    };

    /**
     * 注销登录
     */
    public void logout() {
        //登录状态设置未登录
        LOGIN_STATE = false;
    }

    /**
     * 判断是否登录
     *
     * @return
     */
    public boolean isLogin() {
        return LOGIN_STATE;
    }


    private int logTag;
    private String phone;
    private String name;
    private String birth;
    private String avatar;
    private int userId;
    private String email;
    private int gender;
    private String token;
    private int customerCount;
    public int visitCount;
    private LoginResponse loginResp;
    private VerifyResponse verifyResp;
    private SendVerifyResponse sendVerifyResp;
    private ChangePasswordResponse changeResp;
    private CustomerVisitCountResponse cusVisitResp;

    public void setLogTag(int logTag) {
        this.logTag = logTag;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setCustomerCount(int customerCount) {
        this.customerCount = customerCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }


    public int getLogTag() {
        return logTag;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getBirth() {
        return birth;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public int getGender() {
        return gender;
    }

    public String getToken() {
        return token;
    }

    public int getCustomerCount() {
        return customerCount;
    }

    public int getVisitCount() {
        return visitCount;
    }


    protected SendVerifyResponse getSendVerifyResp() {
        return sendVerifyResp;
    }

    protected VerifyResponse getVerifyResp() {
        return verifyResp;
    }

    protected LoginResponse getLoginResp() {
        return loginResp;
    }

    protected ChangePasswordResponse getChangeResp() {
        return changeResp;
    }

    protected CustomerVisitCountResponse getCusVisitCountResp() {
        return cusVisitResp;
    }


}
