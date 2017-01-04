package com.drugoogle.sellscrm.customer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.customviews.ClearEditText;
import com.drugoogle.sellscrm.data.AddressDetail;
import com.drugoogle.sellscrm.data.request.AddAddressRequest;
import com.drugoogle.sellscrm.data.request.EditAddressRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.type.AddressInfo;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;


/**
 * Created by wuguohao on 15-6-5.
 */
@EActivity(R.layout.activity_set_address)
public class SetAddressActivity extends BaseActivity {
    public static final int REQUEST = 1;
    public AddAddressRequest addRequest;
    public EditAddressRequest editRequest;
    public String token;
    public Account account;
    public String companyId;
    public AddressInfo info;

    @Extra
    boolean isAddAddress = false;

    @Extra
    boolean isEditAddress = false;

    @Extra
    AddressInfo _addressInfo;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    TextView tittle;

    @ViewById(R.id.hint)
    TextView hint;

    @ViewById(R.id.addressName)
    TextView addressName;

    @ViewById(R.id.address)
    TextView address;

    @ViewById(R.id.detail)
    ClearEditText mDetail;

    @ViewById(R.id.affirm)
    Button affirm;

    @Click(R.id.address_layout)
    void toSelectAddress() {
        Intent intent = new Intent(this, AddressSelectActivity.class);
        if (_addressInfo != null) {
            //修改地址时进入addressSelect,传进已存在地址
            intent.putExtra("address", _addressInfo);
        } else if (info != null) {
            //地图选点后未进行添加/修改，再次选点，传进已选在地址
            intent.putExtra("address", info);
        }
        //添加地址时进入addressSelect，无地
        startActivityForResult(intent, REQUEST);
    }

    @AfterViews
    void init() {
        account = Account.getInstance();
        companyId = getIntent().getStringExtra("companyId");
        addRequest = new AddAddressRequest();
        editRequest = new EditAddressRequest();
        token = account.getToken();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tittle = ((TextView) toolbar.findViewById(R.id.title));
        if (isAddAddress) {
            tittle.setText("添加地址");
            affirm.setText("确认添加");
            affirm.setOnClickListener(onClickListener);
        } else if (isEditAddress) {
            tittle.setText("修改地址");
            affirm.setText("保存");
            setAddress(_addressInfo);
            if (_addressInfo.SUBADDRESS != null)
                mDetail.setText(_addressInfo.SUBADDRESS);
            affirm.setOnClickListener(onClickListener);
        }
    }

    private void setAddress(AddressInfo info)
    {
        if (!TextUtils.isEmpty(info.ADDRESS))//使用详细地址的格式
        {
            addressName.setText(info.ADDRESSNAME);
            address.setText(info.ADDRESS);

            addressName.setVisibility(View.VISIBLE);
            address.setVisibility(View.VISIBLE);

            hint.setText("");
            hint.setVisibility(View.INVISIBLE);
        }
        else
        {

            addressName.setText("");
            address.setText("");

            addressName.setVisibility(View.INVISIBLE);
            address.setVisibility(View.INVISIBLE);

            hint.setVisibility(View.VISIBLE);
            hint.setText(info.ADDRESS_OLD);
        }
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onAddOrEditAddress();
            affirm.setClickable( false );
        }
    };

    void onAddOrEditAddress() {
        if (address.getText().toString() == null || address.getText().toString().equals("")) {
            mActivityHelper.showToast(getString( R.string.please_select_address ));
            return;
        }
        if (isAddAddress) {
            addAddress();
        } else if (isEditAddress) {
            editAddress();
        }
    }

    @Background
    void addAddress() {
        //添加新地址
        addRequest.companyId = companyId; //客户id
        addRequest.addressDetail = new AddressDetail();
        addRequest.addressDetail.province = info.PROVINCE;
        addRequest.addressDetail.city = info.CITY;
        addRequest.addressDetail.region = info.REGION;
        addRequest.addressDetail.addr = info.ADDRESS;
        if (mDetail.getText() != null) {
            addRequest.addressDetail.subaddr = mDetail.getText().toString();//详细地址
        }
        addRequest.addressDetail.latitude = info.LATITUDE;
        addRequest.addressDetail.longtitude = info.LONGITUDE;
        addRequest.addressDetail.addressName = info.ADDRESSNAME;
        BaseResponse resp = mRestClient.addAddress(addRequest, token);
        uiAfterAddAddress(resp);
    }

    @UiThread
    void uiAfterAddAddress(BaseResponse resp) {
        affirm.setClickable( true );
        //将新添加的地址返回地址列表
        if (!BaseResponse.hasErrorWithOperation(resp, this)) {
            mActivityHelper.showToast(getString(R.string.add_addr_suc));
            setResult(RESULT_OK);
            finish();
        }
    }

    @Background
    void editAddress() {
        //修改地址
        initEditRequest();
        BaseResponse resp = mRestClient.editAddress(editRequest, token);
        uiAfterEditAddress(resp);
    }

    /**
     * 初始化修改地址Request
     */
    private void initEditRequest() {
        if (info != null) {
            //地图选点地址信息
            editRequest.id = _addressInfo.ID;//地址id
            editRequest.province = info.PROVINCE;
            editRequest.city = info.CITY;
            editRequest.region = info.REGION;
            editRequest.addr = info.ADDRESS;
            editRequest.subaddr = mDetail.getText().toString();//详细地址
            editRequest.latitude = info.LATITUDE;
            editRequest.longtitude = info.LONGITUDE;
            editRequest.addressName = info.ADDRESSNAME;
        } else {
            //未地图选点时候仍是已存在的地址
            editRequest.id = _addressInfo.ID;//地址id
            editRequest.province = _addressInfo.PROVINCE;
            editRequest.city = _addressInfo.CITY;
            editRequest.region = _addressInfo.REGION;
            editRequest.addr = _addressInfo.ADDRESS;
            editRequest.subaddr = mDetail.getText().toString();//详细地址
            editRequest.latitude = _addressInfo.LATITUDE;
            editRequest.longtitude = _addressInfo.LONGITUDE;
            editRequest.addressName = _addressInfo.ADDRESSNAME;
        }
    }

    @UiThread
    void uiAfterEditAddress(BaseResponse resp) {
        affirm.setClickable( true );
        //修改地址结束返回地址列表
        if (!BaseResponse.hasErrorWithOperation(resp, this)) {
            mActivityHelper.showToast(getString(R.string.set_addr_suc));
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //返回地图选址
        switch (resultCode) {
            case RESULT_OK:
                Bundle b = data.getExtras();
                info = (AddressInfo) b.get("address");
                setAddress(info);
                break;
            default:
                break;
        }
    }
}
