package com.drugoogle.sellscrm.selfinfo;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.MainActivity_;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.PrefUtils_;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.visit.VisitService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by ydwang on 2016/3/22.
 */
@EActivity(R.layout.selfinfo_login)
public class Selfinfo_login_Activity extends BaseActivity {
    private static final String TAG = Selfinfo_login_Activity.class.getSimpleName();

    public static final int SUCCESS = 1;//登录成功
    public static final int NONEEXIST = 3;//账号未注册
    public static final int PASSERR = 5;//密码错误

    @Pref
    PrefUtils_ prefUtils;

    @Extra
    boolean isExpired = false;

    @ViewById(R.id.self_loginId)
    EditText self_loginId;
    @ViewById(R.id.self_loginClear)
    ImageView self_loginClear;
    @ViewById(R.id.self_loginPass)
    EditText self_loginPass;
    @ViewById(R.id.self_login)
    Button self_login;

    public static boolean isExist = false;
    //账号密码
    private String id = null;
    private String pass = null;
    public String token;
    private Account account = Account.getInstance();

    @AfterViews
    protected void init() {
        if (isExist == true) {
            finish();
            return;
        }
        isExist = true;

        if (isExpired) {
            prefUtils.edit().withoutLogin().put( false ).apply();
        }

        if (Account.getInstance().isLogin()) {
            MainActivity_.intent( this ).start();
            finish();
            return;
        }

        //将账号和密码都设置到文本框中
        String name = prefUtils.name().get();
        String password = prefUtils.password().get();
        if (!TextUtils.isEmpty( name ))
            self_loginId.setText( name );
        if (!TextUtils.isEmpty( password ))
            self_loginPass.setText( password );
        //设置光标位置
        self_loginId.setSelection( self_loginId.getText().length() );

        self_loginId.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (TextUtils.isEmpty( self_loginId.getText() )) {
                        self_loginClear.setVisibility( View.GONE );
                        return;
                    }
                    self_loginClear.setVisibility( View.VISIBLE );
                }
            }
        } );
        //根据输入框内容有误设置登录按钮的颜色
        if (TextUtils.isEmpty( self_loginPass.getText() ) || TextUtils.isEmpty( self_loginId.getText() )) {
            self_login.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );
        } else {
            self_login.setTextColor( getResources().getColor( R.color.yellowButton ) );
        }
        //监听id以改变清除按钮，登录按钮状态
        self_loginId.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (TextUtils.isEmpty( self_loginId.getText() )) {
                    self_loginClear.setVisibility( View.GONE );
                } else {
                    self_loginClear.setVisibility( View.VISIBLE );
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                self_loginClear.setVisibility( View.VISIBLE );
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty( self_loginId.getText() )) {
                    self_loginClear.setVisibility( View.GONE );
                    self_login.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );
                } else {
                    self_loginClear.setVisibility( View.VISIBLE );
                    if (TextUtils.isEmpty( self_loginPass.getText() )) {
                        self_login.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );
                    } else {
                        self_login.setTextColor( getResources().getColor( R.color.yellowButton ) );
                    }
                }
            }
        } );
        //监听id以改变清除按钮，登录按钮状态
        self_loginPass.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                self_login.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                self_login.setTextColor( getResources().getColor( R.color.yellowButton ) );
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty( self_loginPass.getText() ) || TextUtils.isEmpty( self_loginId.getText() )) {
                    self_login.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );
                } else {
                    self_login.setTextColor( getResources().getColor( R.color.yellowButton ) );
                }
            }
        } );

        if (prefUtils.withoutLogin().getOr(false)) {
            MainActivity_.intent( this ).start();
            finish();
        }
//          autoLogin( name, password );

    }

//    void autoLogin(String name, String password) {
//        if (TextUtils.isEmpty( name ) || TextUtils.isEmpty( password ))
//            return;
//
//        id = name;
//        pass = password;
//
//        mActivityHelper.showLoadingDialog( getString( R.string.loging ) );
//        Account.getInstance().login( name, password, new LoginCallback() {
//            @Override
//            public void onFinish(Account account) {
//                if (BaseResponse.hasError( account.getLoginResp() )) {
//                    //登录出错
//                    loginError( account );
//                } else {
//                    //登录成功
//                    afterLogin( account.getLoginResp() );
//                }
//            }
//        } );
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isExist = false;
    }

    /**
     * 清除内容
     */
    @Click(R.id.self_loginClear)
    void clear() {
        self_loginId.setText( "" );
    }

    /**
     * 忘记密码
     */
    @Click(R.id.self_loginForget)
    void passForget() {
        Selfinfo_verify_Activity_.intent( this ).start();
    }

    @Click(R.id.setting)
    void onSetting() {
        Selfinfo_setting_Activity_.intent( this ).start();
    }

    /**
     * 登录
     */
    @Click(R.id.self_login)
    void login() {
        if (validate()) {
//            final Account account = Account.getInstance();
            id = self_loginId.getText().toString();
            pass = self_loginPass.getText().toString();
            mActivityHelper.showLoadingDialog( getString( R.string.loging )
                    , new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            account.cancelLogin();
                        }
                    } );
            account.login( id, pass, new LoginCallback() {
                @Override
                public void onFinish(Account account) {
                    if (BaseResponse.hasError( account.getLoginResp() )) {
                        //登录出错
                        loginError( account );
                    } else {
                        //登录成功
                        afterLogin( account );
                    }
                }
            } );
//            mActivityHelper.dismissLoadingDialog();
        } else {
            Toast.makeText( this, getString( R.string.idOrPassIsNull ), Toast.LENGTH_SHORT ).show();
        }
    }


    /**
     * 成功登录
     */
    void afterLogin(Account account) {
        mActivityHelper.dismissLoadingDialog();
        //记住账号信息
        saveAccountInfo( id, pass, account );
        //为account设值
        setAccountInfo(account);
        Toast.makeText( this, getString( R.string.logined ), Toast.LENGTH_SHORT ).show();
        if (!isExpired) {
            //退出登录后再次登陆后
            MainActivity_.intent( this ).start();
        }
        //登录过期再次登陆后
        setResult( BaseResponse.RESULT_CODE_EXPIRED );

        //启动停留检测
        Intent serviceIntent = new Intent( this, VisitService.class );
        startService( serviceIntent );


        finish();
    }

    /**
     * 登录失败提示
     *
     * @param
     */
    void loginError(Account account) {
        mActivityHelper.dismissLoadingDialog();
        if (account.getLoginResp() == null) {
            Toast.makeText( this, getString( R.string.network_err ), Toast.LENGTH_SHORT ).show();
        } else if (account.getLoginResp().code == NONEEXIST || account.getLoginResp().code == PASSERR) {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate( R.layout.toast_message, null );
            TextView textView = (TextView) layout.findViewById( R.id.dialog_message );
            textView.setText( R.string.idOrPassErr );
            Toast toast = new Toast( this );
            toast.setGravity( Gravity.CENTER, 0, 0 );
            toast.setDuration( Toast.LENGTH_SHORT );
            toast.setView( layout );
            toast.show();
        } else {
            Toast.makeText( this, account.getLoginResp().message, Toast.LENGTH_SHORT ).show();
        }
    }


    /**
     * 非空
     *
     * @return
     */
    boolean validate() {
        //非空判断
        if (!TextUtils.isEmpty( self_loginId.getText() ) && !TextUtils.isEmpty( self_loginPass.getText() )) {
            return true;
        }
        return false;
    }

    /**
     * 记住账号信息
     *
     * @param id
     * @param pass
     */
    void saveAccountInfo(String id, String pass, Account account) {
        prefUtils.edit().name().put( id ).apply();
        prefUtils.edit().password().put( pass ).apply();
        prefUtils.edit().withoutLogin().put( true ).apply();

        prefUtils.edit().logTag().put( account.getLoginResp().info.logTag ).apply();
        prefUtils.edit().phone().put( account.getLoginResp().info.phone ).apply();
        prefUtils.edit().userName().put( account.getLoginResp().info.name ).apply();
        prefUtils.edit().birth().put( account.getLoginResp().info.birth ).apply();
        prefUtils.edit().avatar().put( account.getLoginResp().info.avatar ).apply();
        if (account.getLoginResp().info.userId != null)
            prefUtils.edit().userId().put( Integer.parseInt( account.getLoginResp().info.userId ) ).apply();
        prefUtils.edit().email().put( account.getLoginResp().info.email ).apply();
        if (account.getLoginResp().info.sex != null)
            prefUtils.edit().gender().put( Integer.parseInt( account.getLoginResp().info.sex ) ).apply();
        prefUtils.edit().token().put( account.getLoginResp().token ).apply();
        prefUtils.edit().customerCount().put( account.getCusVisitCountResp().customerCount ).apply();
        prefUtils.edit().visitCount().put( account.getCusVisitCountResp().customerCount ).apply();
    }

    /**
     * 为account设值
     */
    void setAccountInfo(Account account) {
        account.setLogTag( account.getLoginResp().info.logTag );
        account.setPhone( account.getLoginResp().info.phone );
        account.setName( account.getLoginResp().info.name );//姓名
        account.setBirth( account.getLoginResp().info.birth );
        account.setAvatar( account.getLoginResp().info.avatar );
        if (!TextUtils.isEmpty(account.getLoginResp().info.userId))
            account.setUserId( Integer.parseInt(account.getLoginResp().info.userId) );
        account.setEmail( account.getLoginResp().info.email );
        if (!TextUtils.isEmpty(account.getLoginResp().info.sex))
            account.setGender( Integer.parseInt(account.getLoginResp().info.sex) );
        account.setToken( account.getLoginResp().token );
        account.setCustomerCount( account.getCusVisitCountResp().customerCount );
        account.setVisitCount(account.getCusVisitCountResp().visitCount);
    }
}
