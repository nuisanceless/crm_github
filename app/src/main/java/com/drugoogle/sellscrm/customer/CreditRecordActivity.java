package com.drugoogle.sellscrm.customer;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.CreditRecordResponse;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by ydwang on 2016/6/13.
 */
@EActivity(R.layout.activity_credit_record_detail)
public class CreditRecordActivity extends BaseActivity {

    @Extra
    public int _id;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.content_lay)
    View contentView;
    @ViewById(R.id.pay_id_tv)
    TextView payIdTv;
    @ViewById(R.id.invoice_code_tv)
    TextView invoiceCodeTv;
    @ViewById(R.id.deliver_date_tv)
    TextView deliverDateTv;
    @ViewById(R.id.pay_date_tv)
    TextView payDateTv;
    @ViewById(R.id.no_tax_amount_tv)
    TextView noTaxAmountTv;
    @ViewById(R.id.tax_tv)
    TextView taxTv;
    @ViewById(R.id.tax_amount_tv)
    TextView taxAmountTv;
    @ViewById(R.id.back_amount_tv)
    TextView backAmountTv;
    @ViewById(R.id.not_back_amount_tv)
    TextView notBackAmountTv;

    public Account account;
    public Context context;
    public String token;
    public CreditRecordResponse resp;
    private ProgressDialog pdLoading;

    @AfterViews
    void init() {

        account = Account.getInstance();
        context = this;
        mApp = MyApplication_.getInstance();
        mRestClient = mApp.restClient();
        //toolBar
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );
        toolbar.setNavigationIcon( R.drawable.back_white );
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );
        ((TextView) toolbar.findViewById( R.id.title )).setText( R.string.credit_pay );
        token = account.getToken();
        showProgressDialog();
        initView();
    }

    @Background
    void initView() {

        resp = mRestClient.getCreditRecordDetail( _id, token );
        afterInitView( resp );
    }

    @UiThread
    void afterInitView(CreditRecordResponse resp) {
        dismissProgressDialog();
        if (!BaseResponse.hasErrorWithOperation( resp, this )) {
            //请求成功
            if (resp.code == BaseResponse.SUCCESS_CODE) {
                payIdTv.setText( resp.erpSettlementId );
                if (!TextUtils.isEmpty( resp.invoiceNumber )) {
                    invoiceCodeTv.setText( resp.invoiceNumber );
                } else {
                    invoiceCodeTv.setText( "无" );
                }
                if (!TextUtils.isEmpty( resp.outDate )) {
                    deliverDateTv.setText( resp.outDate );
                } else {
                    deliverDateTv.setText( "暂无" );
                }
                if (!TextUtils.isEmpty( resp.buildDate )) {
                    payDateTv.setText( resp.buildDate );
                } else {
                    payDateTv.setText( "暂无" );
                }
                noTaxAmountTv.setText( String.valueOf( resp.noTaxAmount ) + "元" );
                taxTv.setText( String.valueOf( resp.taxAmount ) + "元" );
                taxAmountTv.setText( String.valueOf( resp.noTaxAmount.add( resp.taxAmount ) ) + "元" );
                if (resp.returnAmount != null) {
                    backAmountTv.setText( String.valueOf( resp.returnAmount ) );
                }
                if (resp.notReturnAmount != null) {
                    notBackAmountTv.setText( String.valueOf( resp.notReturnAmount ) );
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
