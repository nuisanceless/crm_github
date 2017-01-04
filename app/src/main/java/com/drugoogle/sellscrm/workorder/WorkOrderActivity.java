package com.drugoogle.sellscrm.workorder;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.ActivityHelper_;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.Consts;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.request.FinishWorkOrderRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.WorkOrderDetailResponse;
import com.drugoogle.sellscrm.data.type.WorkOrderStatus;
import com.drugoogle.sellscrm.data.type.WorkOrderType;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;

/**
 * Created by ydwang on 2016/4/6.
 * 工单详情
 */
@EActivity(R.layout.workorder_order)
public class WorkOrderActivity extends BaseActivity {

    @ViewById(R.id.order_toolbar)
    Toolbar orderToolbar;
    @ViewById(R.id.content_lay)
    View contentView;
    @ViewById(R.id.order_type)
    TextView mOrderTypeTv;
    @ViewById(R.id.order_id)
    TextView mOrderIdTv;
    @ViewById(R.id.order_publish_time)
    TextView mPublishTimeTv;
    @ViewById(R.id.order_customer)
    TextView mOrderCustomerTv;
    @ViewById(R.id.order_contact_people)
    TextView mContactPeopleTv;
    @ViewById(R.id.order_phone_number)
    TextView mPhoneNumberTv;
    @ViewById(R.id.order_description)
    TextView mOrderDescriptionTv;
    @ViewById(R.id.order_resolve_im)
    ImageView mResolveIm;
    @ViewById(R.id.order_resolve_lay)
    View idorderResolveLay;
    @ViewById(R.id.order_resolve)
    EditText mResolveEt;
    @ViewById(R.id.order_finish)
    Button mFinishBt;
    @Extra
    public String _WorkOrderId;

    public ProgressDialog pdLoading;
    public FinishWorkOrderRequest request;//设置工单完成请求
    public String token;
    public Account account;
    public Context context;
    public int mStatus;
    public static final int RESULT_CODE_STATUS_CHANGE = 34;

    @AfterViews
    void init() {

        account = Account.getInstance();
        context = this;
        mApp = MyApplication_.getInstance();
        mRestClient = mApp.restClient();
        mActivityHelper = ActivityHelper_.getInstance_( context );

        setSupportActionBar( orderToolbar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );
        orderToolbar.setNavigationIcon( R.drawable.back_white );
        orderToolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );

        mResolveEt.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mResolveIm.setVisibility( View.GONE );
                } else {
                    mResolveIm.setVisibility( View.VISIBLE );
                }
            }
        } );
        token = account.getToken();
        showProgressDialog();
        initView();
    }

    /**
     * 初始化view
     */
    @Background
    void initView() {
        WorkOrderDetailResponse resp = mRestClient.getWorkOrderDetail( token, _WorkOrderId );
        afterInitView( resp );
    }

    @UiThread
    void afterInitView(WorkOrderDetailResponse resp) {
        dismissProgressDialog();
        if (!BaseResponse.hasErrorWithOperation( resp, context )) {
            //请求客户成功
            if (resp.code == BaseResponse.SUCCESS_CODE) {
                if (resp.data == null) {
                    Toast.makeText( this, R.string.no_work_order, Toast.LENGTH_SHORT ).show();
                } else {
                    if (resp.data.type != null) {
                        mOrderTypeTv.setText( WorkOrderType.getWorkOrderType( Integer.parseInt( resp.data.type ) ) );
                    }
                    mOrderIdTv.setText( resp.data.id );
                    if (resp.data.publishTime != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat( Consts.DATE_FORMAT );
                        String publishTime = sdf.format( Long.parseLong( resp.data.publishTime ) );
                        mPublishTimeTv.setText( publishTime );
                    }
                    mOrderCustomerTv.setText( resp.data.customerName );
                    mContactPeopleTv.setText( resp.data.linkerName );
                    mPhoneNumberTv.setText( resp.data.linkerPhone );
                    mOrderDescriptionTv.setText( resp.data.description );
                    if (resp.data.result != null) {
                        mResolveEt.setText( resp.data.result );
                    }
                    if (resp.data.status != null) {
                        mStatus = Integer.parseInt( resp.data.status );
                        //如果状态为未查看设置为待处理
                        if (Integer.parseInt( resp.data.status ) == WorkOrderStatus.NOT_VIEWED) {
                            orderWaitToResolve();
                            setResult( RESULT_CODE_STATUS_CHANGE );
                        }
                    }
                    initFinishButton();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == BaseResponse.REQUEST_CODE_EXPIRED && resultCode == BaseResponse.RESULT_CODE_EXPIRED) {
            token = Account.getInstance().getToken();
            //登录后返回当前页面并重新加载数据
            showProgressDialog();
            initView();
        }
    }

    /***
     * 如果状态为未查看设置为待处理
     */
    @Background
    void orderWaitToResolve() {
        BaseResponse resp = mRestClient.setWorkOrderViewed( token, _WorkOrderId );
        afterOrderWaitToResolve( resp );
    }

    @UiThread
    void afterOrderWaitToResolve(BaseResponse resp) {
        if (!BaseResponse.hasErrorWithOperation( resp, context )) {
            if (resp.code == BaseResponse.SUCCESS_CODE) {
                mStatus = WorkOrderStatus.WAIT_PROCESSING;
                initFinishButton();
            }
        }
    }

    /**
     * 稍后处理/完成
     */
    @Click(R.id.order_finish)
    void orderFinish() {
        if (mStatus == WorkOrderStatus.WAIT_PROCESSING) {
            //状态设置为处理中
            resolveAffairs();
        } else if (mStatus == WorkOrderStatus.PROCESSING) {
            //状态设置为已处理
            if (TextUtils.isEmpty( mResolveEt.getText() )) {
                //处理信息未输入
                mActivityHelper.showToast( getString( R.string.please_enter_sloveinfo ) );
            } else {
                finishOrder();
            }
        }
    }

    /**
     * 如果状态为待处理设置为处理中
     */
    @Background
    void resolveOrder() {
        BaseResponse resp = mRestClient.setWorkOrderProcessing( token, _WorkOrderId );
        afterResolveOrder( resp );
    }

    @UiThread
    void afterResolveOrder(BaseResponse resp) {
        if (!BaseResponse.hasErrorWithOperation( resp, context )) {
            //请求客户列表成功
            if (resp.code == BaseResponse.SUCCESS_CODE) {
                mStatus = WorkOrderStatus.PROCESSING;
                initFinishButton();
            }
        }
    }

    /**
     * 如果状态为处理中设置为结束
     */
    @Background
    void finishOrder() {
        request = new FinishWorkOrderRequest();
        request.workOrderId = _WorkOrderId;
        request.result = mResolveEt.getText().toString();
        BaseResponse resp = mRestClient.setWorkOrderFinished( request, token, _WorkOrderId );
        afterFinishOrder( resp );
    }

    @UiThread
    void afterFinishOrder(BaseResponse resp) {
        if (!BaseResponse.hasErrorWithOperation( resp, context )) {
            //请求客户列表成功
            if (resp.code == BaseResponse.SUCCESS_CODE) {
                mStatus = WorkOrderStatus.FINISHED;
                mActivityHelper.showToast( getString( R.string.resolve_success ) );
                initFinishButton();
                setResult( RESULT_CODE_STATUS_CHANGE );
                finish();
            }
        }
    }

    /**
     * 初始化进入工单详情按钮
     */
    void initFinishButton() {
        switch (mStatus) {
            case WorkOrderStatus.WAIT_PROCESSING:
                //待处理
                mFinishBt.setText( getString( R.string.bt_iknow ) );
                mFinishBt.setClickable( true );
                idorderResolveLay.setVisibility( View.GONE );
                break;
            case WorkOrderStatus.PROCESSING:
                //处理中
                idorderResolveLay.setVisibility( View.VISIBLE );
                mFinishBt.setText( getString( R.string.bt_finish ) );
                mFinishBt.setClickable( true );
                break;
            case WorkOrderStatus.FINISHED:
                //已处理
                mFinishBt.setVisibility( View.GONE );
                idorderResolveLay.setVisibility( View.VISIBLE );
                mResolveEt.setFocusable( false );
                mResolveEt.setEnabled( false );
        }
    }

    public void resolveAffairs() {

        final Dialog mentionDialog = new Dialog( this, R.style.OrderMentionDialog );
        mentionDialog.setCanceledOnTouchOutside( false );
        mentionDialog.show();
        Window window = mentionDialog.getWindow();
        window.setContentView( R.layout.workorder_mention_dialog );
        final TextView button = (TextView) window.findViewById( R.id.dialog_know );
        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setBackgroundColor( getResources().getColor( R.color.dialog_color_click ) );
                resolveOrder();
                setResult( RESULT_CODE_STATUS_CHANGE );
                mentionDialog.dismiss();
            }
        } );
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
