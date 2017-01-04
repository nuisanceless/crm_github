package com.drugoogle.sellscrm.selfinfo;

import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.data.response.LoginResponse;
import com.drugoogle.sellscrm.data.type.Gender;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ydwang on 2016/4/14.
 */
@EActivity(R.layout.selfinfo_info)
public class Selfinfo_info_Activity extends BaseActivity {

    private Account account;

    @ViewById(R.id.self_info_toolbar)
    Toolbar selfInfoToolbar;

    @ViewById(R.id.self_info_title)
    TextView selfInfoTitle;
    @ViewById(R.id.self_info_portrait)
    ImageView selfInfoPortrait;
    @ViewById(R.id.self_info_name)
    TextView selfInfoName;
    @ViewById(R.id.self_info_age)
    TextView selfInfoAge;
    @ViewById(R.id.self_info_mail)
    TextView selfInfoMail;
    @ViewById(R.id.self_info_phone)
    TextView selfInfoPhone;

    @AfterViews
    void init() {

        account = Account.getInstance();
        setSupportActionBar( selfInfoToolbar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );
        selfInfoToolbar.setNavigationIcon( R.drawable.back_white );
        selfInfoToolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );

        selfInfoTitle.setText( account.getName() );
        selfInfoPortrait.setImageResource(
                Gender.getPortraitImgRes( account.getGender() ) );
        selfInfoName.setText( account.getName() );
        selfInfoAge.setText( getAge( account.getBirth() ) );
        if (!TextUtils.isEmpty( account.getEmail() )) {
            selfInfoMail.setText( account.getEmail() );
        } else {
            selfInfoMail.setText( getString( R.string.no_email ) );
        }
        if (!TextUtils.isEmpty( account.getPhone() )) {
            selfInfoPhone.setText( account.getPhone() );
        } else {
            selfInfoPhone.setText( getString( R.string.no_phone ) );
        }
    }

    private String getAge(String birth) {

        if (!TextUtils.isEmpty( birth )) {
            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            String birthYear = sdf.format( Long.parseLong( birth) ).substring( 0, 4 );
            Calendar mCalendar = Calendar.getInstance();
            int yearNow = mCalendar.get( Calendar.YEAR );
            int age = yearNow - Integer.valueOf( birthYear );
            return String.valueOf( age ) + "岁";
        } else {
            return getString( R.string.no_age );
        }
    }
}
