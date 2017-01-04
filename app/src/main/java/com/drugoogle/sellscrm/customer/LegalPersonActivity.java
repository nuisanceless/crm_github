package com.drugoogle.sellscrm.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.request.LegalPersonRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.LegalPersonDetailsResponse;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by ydwang on 2016/4/28.
 */
@EActivity(R.layout.activity_legal_person_details)
public class LegalPersonActivity extends BaseActivity {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.title)
    TextView titleTv;
    @ViewById(R.id.content_lay)
    View contentView;
    @ViewById(R.id.name_tv)
    TextView nameTv;
    @ViewById(R.id.name_short_tv)
    TextView nameShortTv;

    @ViewById(R.id.legal_person_tv)
    TextView legalPersonTv;
    @ViewById(R.id.person_in_charge_tv)
    TextView personInChargeTv;
    @ViewById(R.id.contact_number_tv)
    TextView contactNumberTv;
    @ViewById(R.id.company_type_tv)
    TextView companyTypeTv;

    @ViewById(R.id.area_tv)
    TextView areaTv;
    @ViewById(R.id.sign_address_tv)
    TextView signAddressTv;
    @ViewById(R.id.tax_code_tv)
    TextView taxCodeTv;
    @ViewById(R.id.license_number_tv)
    TextView licenseNumberTv;
    @ViewById(R.id.organization_code_tv)
    TextView organizationCodeTv;

    @ViewById(R.id.operation_code_tv)
    TextView operationCodeTv;
    @ViewById(R.id.inspector_tv)
    TextView inspectorTv;

    @ViewById(R.id.remarks_tv)
    TextView remarksTv;

    private Account account;
    private String ccCustomerId;
    private String token;
    private LegalPersonRequest request;
    private LegalPersonDetailsResponse resp;
    private ProgressDialog pdLoading;

    @AfterViews
    void init() {

        account = Account.getInstance();
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );
        toolbar.setNavigationIcon( R.drawable.back_white );
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );
        request = new LegalPersonRequest();
        CustomerInfo customerInfo = (CustomerInfo) getIntent().getSerializableExtra( "ccCustomer" );
        request.ccCustomerId = customerInfo.ID;
        request.token = account.getToken();
        showProgressDialog();
        initView( request );
    }


    @Background
    void initView(LegalPersonRequest request) {

        ccCustomerId = request.ccCustomerId;
        token = request.token;
        resp = mRestClient.getLegalPersonDetailsInfo( ccCustomerId, token );
        afterInitView( resp );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == BaseResponse.REQUEST_CODE_EXPIRED && resultCode == BaseResponse.RESULT_CODE_EXPIRED) {
            request.token = Account.getInstance().getToken();
            showProgressDialog();
            initView( request );
        }
    }

    @UiThread
    void afterInitView(LegalPersonDetailsResponse resp) {
        dismissProgressDialog();
        if (!BaseResponse.hasErrorWithOperation( resp, this )) {
            //请求成功
            if (resp.code == BaseResponse.SUCCESS_CODE) {
                if (resp.data == null){
                    Toast.makeText( this, R.string.no_legal_person, Toast.LENGTH_SHORT ).show();
                }else {

                    if (!TextUtils.isEmpty( resp.data.GRCUSTOMNAME )) {
                        titleTv.setText( resp.data.GRCUSTOMNAME );//标题
                        nameTv.setText( resp.data.GRCUSTOMNAME );//客户名称
                    } else {
                        titleTv.setText( "暂无" );//标题
                        nameTv.setText( "暂无" );
                    }
                    if (!TextUtils.isEmpty( resp.data.GRSHORTCUSTOMNAME )) {
                        nameShortTv.setText( resp.data.GRSHORTCUSTOMNAME );//客户简称
                    }else {
                        nameShortTv.setText( "暂无" );
                    }

                    if (!TextUtils.isEmpty( resp.data.LEGALPERSON )) {
                        legalPersonTv.setText( resp.data.LEGALPERSON );//法人
                    } else {
                        legalPersonTv.setText( "暂无" );
                    }
                    if (!TextUtils.isEmpty( resp.data.CHARGEPERSON )) {
                        personInChargeTv.setText( resp.data.CHARGEPERSON );//企业负责人
                    } else {
                        personInChargeTv.setText( "暂无" );
                    }
                    if (!TextUtils.isEmpty( resp.data.TEL )) {
                        contactNumberTv.setText( resp.data.TEL );//联系电话
                    } else {
                        contactNumberTv.setText( "暂无" );
                    }
//                companyTypeTv.setText( resp.data.GRCOMPANYTYPEID );//TODO 公司类型对应int值

//                areaTv.setText( resp.data.DISTRICTID );//TODO 所在区域id 所对应得地区
                    if (!TextUtils.isEmpty( resp.data.REGADDRESS )) {
                        signAddressTv.setText( resp.data.REGADDRESS );//注册地址
                    } else {
                        signAddressTv.setText( "暂无" );
                    }
                    if (!TextUtils.isEmpty( resp.data.TAXNUMBER )) {
                        taxCodeTv.setText( resp.data.TAXNUMBER );//税号
                    } else {
                        taxCodeTv.setText( "暂无" );
                    }
                    if (!TextUtils.isEmpty( resp.data.YYZZ )) {
                        licenseNumberTv.setText( resp.data.YYZZ ); //营业注册号
                    } else {
                        licenseNumberTv.setText( "暂无" );
                    }
                    if (!TextUtils.isEmpty( resp.data.ORGNIZECODE )) {
                        organizationCodeTv.setText( resp.data.ORGNIZECODE );//组织结构代码
                    } else {
                        organizationCodeTv.setText( "暂无" );
                    }

                    if (!TextUtils.isEmpty( resp.data.GRCUSTOMOPCODE )) {
                        operationCodeTv.setText( resp.data.GRCUSTOMOPCODE );//客户操作码
                    } else {
                        operationCodeTv.setText( "暂无" );
                    }
                    if (!TextUtils.isEmpty( resp.data.QAPERSON )) {
                        inspectorTv.setText( resp.data.QAPERSON );//
                    } else {
                        inspectorTv.setText( "暂无" );
                    }
                    if (!TextUtils.isEmpty( resp.data.MEMO )) {
                        remarksTv.setText( resp.data.MEMO);
                    }else {
                        remarksTv.setText( "无" );
                    }
                }
            }
        }
    }

    void showProgressDialog() {
        contentView.setVisibility( View.INVISIBLE );
        pdLoading = new ProgressDialog( this );
        pdLoading.setTitle( "" );
        pdLoading.setMessage( getString( R.string.loading ) );
        pdLoading.setIndeterminate( false );
        pdLoading.setCancelable( true );
        pdLoading.show();
    }

    void dismissProgressDialog() {
        contentView.setVisibility( View.VISIBLE );
        pdLoading.dismiss();
    }
}
