package com.drugoogle.sellscrm.customer;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.SaleRecordDetailResponse;
import com.drugoogle.sellscrm.data.type.SaleRecordStatus;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by ydwang on 2016/5/23.
 */
@EActivity(R.layout.activity_sale_record)
public class SaleRecordActivity extends BaseActivity {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.tittle)
    TextView tittle;
    @ViewById(R.id.content_lay)
    View contentView;
    @ViewById(R.id.cus_name_tv)
    TextView cusNameTv;
    @ViewById(R.id.customer_code_tv)
    TextView customerCodeTv;
    @ViewById(R.id.status_tv)
    TextView statusTv;
    @ViewById(R.id.order_code_tv)
    TextView orderCodeTv;
    @ViewById(R.id.create_time_tv)
    TextView createTimeTv;
    @ViewById(R.id.detail_count_tv)
    TextView detailCountTv;
    @ViewById(R.id.total_money_tv)
    TextView totalMoneyTv;
    @ViewById(R.id.input_member_tv)
    TextView inputMemberTv;
    @ViewById(R.id.deal_company_tv)
    TextView dealCompanyTv;
    @ViewById(R.id.remarks_tv)
    TextView remarksTv;
    @Extra
    public String _sellOrderCode;
    public Account account;
    public Context context;
    public String token;
    public SaleRecordDetailResponse resp;
    private ProgressDialog pdLoading;

    @Click(R.id.detail_count_lay)
    void detailList() {
        if (resp.data != null) {
            DealListActivity_.intent(this)._SellOrderId(resp.data.SELL_ORDER_ID).start();
        }
    }

    @AfterViews
    void init() {

        account = Account.getInstance();
        context = this;
        mApp = MyApplication_.getInstance();
        mRestClient = mApp.restClient();
        //toolBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) toolbar.findViewById(R.id.title)).setText(R.string.sale);
        token = account.getToken();
        showProgressDialog();
        initView();
    }

    @Background
    void initView() {

        resp = mRestClient.getSaleRecordDetail(token, _sellOrderCode);
        afterInitView(resp);
    }

    @UiThread
    void afterInitView(SaleRecordDetailResponse resp) {
        dismissProgressDialog();
        if (!BaseResponse.hasErrorWithOperation(resp, this)) {
            //请求成功
            if (resp.code == BaseResponse.SUCCESS_CODE) {
                if (resp.data == null) {
                    Toast.makeText( this, R.string.no_sale_record, Toast.LENGTH_SHORT ).show();
                } else {
                    cusNameTv.setText(resp.data.BUYER_NAME);
                    customerCodeTv.setText(resp.data.BUYER_CODE);
                    statusTv.setText(SaleRecordStatus.getRecordStatusStr(resp.data.YD_STATUS));
                    orderCodeTv.setText(resp.data.SELL_ORDER_ID);
                    createTimeTv.setText(resp.data.CREATE_TIME);
                    detailCountTv.setText(String.valueOf(resp.data.QUANTITY));
                    totalMoneyTv.setText(String.valueOf(resp.data.AMOUNT));
                    inputMemberTv.setText(resp.data.OPERATOR);
                    dealCompanyTv.setText(resp.data.CHECK_CELL);
                    if (!TextUtils.isEmpty( resp.data.REMARK )) {
                        remarksTv.setText(resp.data.REMARK);
                    } else {
                        remarksTv.setText("无");
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
