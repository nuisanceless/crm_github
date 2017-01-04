package com.drugoogle.sellscrm.selfinfo;

import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.ChangePasswordResponse;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by ydwang on 2016/3/22.
 */
@EActivity(R.layout.selfinfo_change)
public class Selfinfo_change_Activity extends AppCompatActivity {

    public static final int SUCCESSCODE = 1;//修改密码成功
    public static final int ERRCODE = 6;//验证码过期

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.self_changePassTittle)
    TextView self_changePassTittle;
    @ViewById(R.id.self_changePass)
    EditText self_changePass;
    @ViewById(R.id.self_changeClear)
    ImageView self_changeClear;
    @ViewById(R.id.self_changePassCon)
    EditText self_changePassCon;
    @ViewById(R.id.self_changeCon)
    Button self_changeCon;

    private String phone;
    private String resetToken;
    private String pass;
    private String passCon;
    Account account;

    @AfterViews
    void init() {

        account = Account.getInstance();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //根据输入框内容有误设置登录按钮的颜色
        if (TextUtils.isEmpty(self_changePassCon.getText()) || TextUtils.isEmpty(self_changePass.getText())) {
            self_changeCon.setTextColor(getResources().getColor(R.color.yellowButtonClick));
        } else {
            self_changeCon.setTextColor(getResources().getColor(R.color.yellowButton));
        }
        //监听id以改变清除按钮，登录按钮状态
        self_changePass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (TextUtils.isEmpty(self_changePass.getText())) {
                    self_changeClear.setVisibility(View.GONE);
                } else {
                    self_changeClear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                self_changeClear.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(self_changePass.getText())) {
                    self_changeClear.setVisibility(View.GONE);
                    self_changeCon.setTextColor(getResources().getColor(R.color.yellowButtonClick));
                } else {
                    self_changeClear.setVisibility(View.VISIBLE);
                    if (TextUtils.isEmpty(self_changePassCon.getText())) {
                        self_changeCon.setTextColor(getResources().getColor(R.color.yellowButtonClick));
                    } else {
                        self_changeCon.setTextColor(getResources().getColor(R.color.yellowButton));
                    }
                }
            }
        });
        //监听id以改变清除按钮，登录按钮状态
        self_changePassCon.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                self_changeCon.setTextColor(getResources().getColor(R.color.yellowButtonClick));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                self_changeCon.setTextColor(getResources().getColor(R.color.yellowButton));
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(self_changePassCon.getText()) || TextUtils.isEmpty(self_changePass.getText())) {
                    self_changeCon.setTextColor(getResources().getColor(R.color.yellowButtonClick));
                } else {
                    self_changeCon.setTextColor(getResources().getColor(R.color.yellowButton));
                }
            }
        });
    }

    /**
     * 清除内容
     */
    @Click(R.id.self_changeClear)
    void clear() {
        self_changePass.setText("");
        self_changePassCon.setText("");
    }

    /**
     * 确认修改密码
     */
    @Click(R.id.self_changeCon)
    void changePassword() {
        if (!TextUtils.isEmpty(self_changePass.getText()) && !TextUtils.isEmpty(self_changePassCon.getText())) {

            phone = getIntent().getStringExtra("phone");
            resetToken = getIntent().getStringExtra("resetToken");
            pass = self_changePass.getText().toString();
            passCon = self_changePassCon.getText().toString();
            if (pass.equals(passCon)) {
                if (validate(pass) && validate(passCon)) {
                    account.changePass(phone, resetToken, pass, new LoginCallback() {
                        @Override
                        public void onFinish(Account account) {
                            if (account.getChangeResp() == null || BaseResponse.hasError(account.getChangeResp())) {
                                //网络故障或者验证码过期
                                changePassErr(account);
                            } else {
                                //密码修改成功
                                afterChangePass(account.getChangeResp());
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, R.string.pass_format_err, Toast.LENGTH_SHORT).show();
                }
            } else {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.toast_message, null);
                TextView textView = (TextView) layout.findViewById(R.id.dialog_message);
                textView.setText(R.string.pass_not_equals);
                Toast toast = new Toast(this);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }
        } else {
            Toast.makeText(this, R.string.pass_isNull, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 密码修改成功
     *
     * @param
     */
    void afterChangePass(ChangePasswordResponse resp) {
        if (resp.code == SUCCESSCODE) {
            Toast.makeText(this, R.string.pass_changed, Toast.LENGTH_SHORT).show();
            Selfinfo_login_Activity_.intent(this).start();
            finish();
        }
    }

    /**
     * 修改失败
     */
    void changePassErr(Account account) {
        if (account.getChangeResp() == null) {
            //网络故障
            Toast.makeText(this, R.string.network_err, Toast.LENGTH_SHORT).show();
        } else if (account.getChangeResp().code == ERRCODE) {
            //验证码错误
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_message, null);
            TextView textView = (TextView) layout.findViewById(R.id.dialog_message);
            textView.setText(R.string.verify_code_err);
            Toast toast = new Toast(this);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();

        } else {
            Toast.makeText(this, account.getChangeResp().message, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 输入非空验证
     *
     * @return
     */
    boolean validate(String password) {
        //只能是6-20位的字母数字下划线
        String passRegex = "\\w{6,20}";
        if (!TextUtils.isEmpty(password)) {
            if (password.matches(passRegex)) {
                return true;
            }
        }
        return false;
    }


}
