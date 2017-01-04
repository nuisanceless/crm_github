package com.drugoogle.sellscrm.selfinfo;

import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.SendVerifyResponse;
import com.drugoogle.sellscrm.data.response.VerifyResponse;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by ydwang on 2016/3/22.
 */
@EActivity(R.layout.selfinfo_verify)
public class Selfinfo_verify_Activity extends AppCompatActivity {

    //请求验证码code
    public static final int VERIFYSENFEDCODE = 1;
    public static final int NOTSIGNINCODE = 3;
    //发送验证码code
    public static final int VERIFIEDCODE = 1;
    public static final int WRONGCODE = 4;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.self_verifyTittle)
    TextView self_verifyTittle;

    @ViewById(R.id.self_phone)
    EditText self_phone;
    @ViewById(R.id.self_verifyClear)
    ImageView self_verifyClear;

    @ViewById(R.id.self_verifyCode)
    EditText self_verifyCode;
    @ViewById(R.id.self_verifySend)
    TextView self_verifySend;

    @ViewById(R.id.self_verifyNext)
    Button self_verifyNext;

    //登录id
    private String loginid;
    //手机号码
    private String phone;
    //验证码
    private String verifyCode;
    //账号管理
    Account account;
    //验证码倒计时
    CountDownTimer countDownTimer;
    LayoutInflater inflater;

    @AfterViews
    protected void init() {

        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );
        toolbar.setNavigationIcon( R.drawable.back_white );
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );

        //下一步按钮初始时为浅色
        self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );

        self_phone.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (TextUtils.isEmpty( self_phone.getText() )) {
                    self_verifyClear.setVisibility( View.GONE );
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty( self_phone.getText() )) {
                    self_verifyClear.setVisibility( View.GONE );
                    self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );
                } else {
                    self_verifyClear.setVisibility( View.VISIBLE );
                    if (TextUtils.isEmpty( self_verifyCode.getText() )) {
                        self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );
                    } else {
                        self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButton ) );
                    }
                }
                //手机号344格式
                String contents = s.toString();
                int length = contents.length();
                if (length == 4) {
                    if (contents.substring( 3 ).equals( new String( " " ) )) { // -
                        contents = contents.substring( 0, 3 );
                        self_phone.setText( contents );
                        self_phone.setSelection( contents.length() );
                    } else { // +
                        contents = contents.substring( 0, 3 ) + " " + contents.substring( 3 );
                        self_phone.setText( contents );
                        self_phone.setSelection( contents.length() );
                    }
                } else if (length == 9) {
                    if (contents.substring( 8 ).equals( new String( " " ) )) { // -
                        contents = contents.substring( 0, 8 );
                        self_phone.setText( contents );
                        self_phone.setSelection( contents.length() );
                    } else {// +
                        contents = contents.substring( 0, 8 ) + " " + contents.substring( 8 );
                        self_phone.setText( contents );
                        self_phone.setSelection( contents.length() );
                    }
                } else if (length == 11) {
                    //直接粘贴手机号时344格式显示
                    String phoneCopy = self_phone.getText().toString();
                    boolean isCopy = true;//是否是直接粘贴过来的号码
                    for (int i = 0; i < phoneCopy.length(); i++) {
                        if (phoneCopy.substring( i, i + 1 ).equals( new String( " " ) )) {
                            isCopy = false;
                            break;
                        } else {
                            isCopy = true;
                        }
                    }
                    if (isCopy) {
                        self_phone.setText( phoneCopy.substring( 0, 3 ) + " " + phoneCopy.substring( 3, 7 )
                                + " " + phoneCopy.substring( 7 ) );
                        self_phone.setSelection( self_phone.getText().toString().length() );
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty( self_phone.getText() )) {
                    self_verifyClear.setVisibility( View.GONE );
                } else {
                    self_verifyClear.setVisibility( View.VISIBLE );
                }
            }
        } );

        self_verifyCode.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButton ) );
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty( self_phone.getText() ) || TextUtils.isEmpty( self_verifyCode.getText() )) {
                    self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );
                } else {
                    self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButton ) );
                }
            }
        } );
        if (TextUtils.isEmpty( self_phone.getText() )) {
            self_verifyClear.setVisibility( View.GONE );
        }
        //得到账号管理类的实例
        account = Account.getInstance();
        inflater = getLayoutInflater();
    }

    /**
     * 清除输入内容
     */
    @Click(R.id.self_verifyClear)
    void verifyClear() {
        self_phone.setText( "" );
    }

    /**
     * 获取验证码
     */
    @Click(R.id.self_verifySend)
    void verifyRequest() {
        String phone344 = self_phone.getText().toString();
        if (isPhoneNo( phone344 )) {
            phone = phone344.substring( 0, 3 ) + phone344.substring( 4, 8 ) + phone344.substring( 9 );
            //开始倒计时
            if (countDownTimer == null) {
                //每隔60请求一次验证码
                countDownTimer = new CountDownTimer( 60000, 1000 ) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (millisUntilFinished > 0 && millisUntilFinished < 10000) {
                            self_verifySend.setText( "0" + millisUntilFinished / 1000 + "s" );
                        } else {
                            self_verifySend.setText( millisUntilFinished / 1000 + "s" );
                        }
                        self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButton ) );
                    }

                    @Override
                    public void onFinish() {
                        self_verifySend.setText( R.string.tv_vcode_request );
                        self_verifyNext.setTextColor( getResources().getColor( R.color.yellowButtonClick ) );

                        countDownTimer = null;
                    }
                };
                countDownTimer.start();
                //获取重置密码验证码
                account.requestVerifyCode( phone, new LoginCallback() {
                    @Override
                    public void onFinish(Account account) {
                        if (account.getVerifyResp() == null || BaseResponse.hasError( account.getVerifyResp() )) {
                            //网络故障或者该手机未注册
                            requestVerifyErr( account );
                        } else {
                            //验证码发送成功
                            afterRequestVerify( account.getVerifyResp() );
                        }
                    }
                } );
            }
        } else {
            Toast.makeText( this, R.string.phone_wrong, Toast.LENGTH_SHORT ).show();
        }
    }

    /**
     * 验证码已发送
     *
     * @param verifyResp
     */
    private void afterRequestVerify(VerifyResponse verifyResp) {
        //提示用户验证码已经发送
        if (verifyResp.code == VERIFYSENFEDCODE) {
            View layout = inflater.inflate( R.layout.dialog_verify_send, null );
            final AlertDialog.Builder builder = new AlertDialog.Builder( this );
            builder.setTitle( "" );
            builder.setCancelable( true );
            final AlertDialog dialog = builder.show();
            dialog.getWindow().setContentView( layout );
            TextView alertPhone = (TextView) layout.findViewById( R.id.alert_phone );
            LinearLayout alertCancel = (LinearLayout) layout.findViewById( R.id.alert_cancel_lay );
            alertPhone.setText( self_phone.getText() );
            alertCancel.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            } );
        }
    }

    /**
     * 请求验证码出错
     *
     * @param account
     */
    private void requestVerifyErr(Account account) {
        //停止倒计时
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer.onFinish();
        }
        if (account.getVerifyResp() == null) {
            //网络故障
            Toast.makeText( this, R.string.network_err, Toast.LENGTH_SHORT ).show();

        } else if (account.getVerifyResp().code == NOTSIGNINCODE) {
            //手机号未绑定账号
            View layout = inflater.inflate( R.layout.toast_message, null );
            TextView textView = (TextView) layout.findViewById( R.id.dialog_message );
            textView.setText( R.string.phone_not_find );
            Toast toast = new Toast( this );
            toast.setGravity( Gravity.CENTER, 0, 0 );
            toast.setDuration( Toast.LENGTH_SHORT );
            toast.setView( layout );
            toast.show();
        } else {
            Toast.makeText( this, account.getVerifyResp().message, Toast.LENGTH_SHORT ).show();
        }

    }

    /**
     * 下一步,验证重置密码验证码
     */
    @Click(R.id.self_verifyNext)
    void verifyNext() {
//        if (countDownTimer != null) {
//            //点击下一步时候取消倒计时
//            countDownTimer.cancel();
//            countDownTimer.onFinish();
//        }
        String phone344 = self_phone.getText().toString();
        verifyCode = self_verifyCode.getText().toString();
        if (isPhoneNo( phone344 )) {
            phone = phone344.substring( 0, 3 ) + phone344.substring( 4, 8 ) + phone344.substring( 9 );
            if (validate( verifyCode )) {
                //向服务器发送验证码和手机号并核对
                account.sendVerifyCode( phone, verifyCode, new LoginCallback() {
                    @Override
                    public void onFinish(Account account) {
                        if (account.getSendVerifyResp() == null || BaseResponse.hasError( account.getSendVerifyResp() )) {
                            nextErr( account );
                        } else {
                            //验证通过
                            afterNext( account.getSendVerifyResp() );
                        }
                    }
                } );

            } else {
                Toast.makeText( this, R.string.verify_length_err, Toast.LENGTH_SHORT ).show();
            }
        } else {
            Toast.makeText( this, R.string.phone_wrong, Toast.LENGTH_SHORT ).show();
        }
    }

    private void afterNext(SendVerifyResponse resp) {
        //跳转到修改密码页面
        if (resp.code == VERIFIEDCODE) {
            if (resp.data != null) {
                Selfinfo_change_Activity_.intent( this ).extra( "phone", phone ).extra( "resetToken", resp.data.resetToken ).start();
                finish();
            }
        }
    }

    private void nextErr(Account account) {
        if (account.getSendVerifyResp() == null) {
            //网络故障
            Toast.makeText( this, R.string.network_err, Toast.LENGTH_SHORT ).show();
        } else if (account.getSendVerifyResp().code == WRONGCODE) {
            //验证码不正确
            View layout = inflater.inflate( R.layout.toast_message, null );
            TextView textView = (TextView) layout.findViewById( R.id.dialog_message );
            textView.setText( R.string.verify_code_err );
            Toast toast = new Toast( this );
            toast.setGravity( Gravity.CENTER, 0, 0 );
            toast.setDuration( Toast.LENGTH_SHORT );
            toast.setView( layout );
            toast.show();
        } else {
            Toast.makeText( this, account.getSendVerifyResp().message, Toast.LENGTH_SHORT ).show();
        }
    }

    /**
     * 验证码格式
     *
     * @param verifyCode
     * @return
     */
    boolean validate(String verifyCode) {
        String verifyRegex = "\\d{4}";
        if (!TextUtils.isEmpty( verifyCode )) {
            if (verifyCode.matches( verifyRegex )) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证手机格式
     *
     * @return
     */
    boolean isPhoneNo(String phone344) {
        //第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
        if (phone344.length() == 13) {
            String phone = phone344.substring( 0, 3 ) + phone344.substring( 4, 8 ) + phone344.substring( 9 );
            String telRegex = "[1][358]\\d{9}";
            if (!TextUtils.isEmpty( phone )) {
                if (phone.matches( telRegex )) {
                    return true;
                }
            }
        }
        return false;
    }
}
