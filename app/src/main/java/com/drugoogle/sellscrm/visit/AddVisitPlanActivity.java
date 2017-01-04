package com.drugoogle.sellscrm.visit;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.Consts;
import com.drugoogle.sellscrm.common.DateTimePickPopupWindow;
import com.drugoogle.sellscrm.common.PopupWindowListAdapter;
import com.drugoogle.sellscrm.customer.ClientListActivity;
import com.drugoogle.sellscrm.customer.ClientListActivity_;
import com.drugoogle.sellscrm.customer.CustomerDetailsActivity;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.CustomerInfoItem;
import com.drugoogle.sellscrm.data.Model.VisitPlanModel;
import com.drugoogle.sellscrm.data.PlanDetailInfo;
import com.drugoogle.sellscrm.data.request.AddVisitPlanRequest;
import com.drugoogle.sellscrm.data.request.EditVisitPlanRequest;
import com.drugoogle.sellscrm.data.response.AddVisitPlanResponse;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.type.VisitModel;
import com.drugoogle.sellscrm.data.type.VisitPeriod;
import com.drugoogle.sellscrm.data.type.VisitType;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by wgh on 2016/3/22.
 * 添加拜访计划。
 */
@EActivity(R.layout.activity_add_visit_plan)
public class AddVisitPlanActivity extends BaseActivity
{
    private static final int REQUEST_CODE_CHOOSE_CLIENT = 1;
    VisitPlanModel mVisitPlanModel;
    Context mContext;


    @Extra
    boolean isFromCustomer = false;
    @Extra
    CustomerInfo mCustomerInfo;
    @Extra
    PlanDetailInfo mPlanDetailInfo;
    @Extra
    boolean isEditPlan = false;
    @Extra
    Date selectDate = new Date();

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.toolbar_title)
    TextView mToolbarTitle;


    @ViewById(R.id.visit_period_layout)
    View mVisitPeriodLayout;
    @ViewById(R.id.visit_period_tv)
    TextView mVisitPeriodTv;
    @ViewById(R.id.period_arrow)
    ImageView mPeriodArrow;

    @ViewById(R.id.total_visit_count_layout)
    View mTotalVisitCountLayout;
    @ViewById(R.id.total_visit_count_et)
    EditText mTotalVisitCountEt;      //到期之前一共要访问的次数

    @ViewById(R.id.date_time_tv)
    TextView mDateTimeTv;
    @ViewById(R.id.date_time_layout)
    View mDateTimeLayout;
    @ViewById(R.id.time_arrow)
    ImageView mTimeArrow;


    @ViewById(R.id.visit_type_tv)
    TextView mVisitTypeTv;
    @ViewById(R.id.visit_type_layout)
    View mVisitTypeLayout;
    @ViewById(R.id.type_arrow)
    ImageView mTypeArrow;
    @ViewById(R.id.visit_type_icon_iv)
    ImageView mVisitTypeIconIv;


    @ViewById(R.id.visit_model_layout)
    View mVisitModelLayout;
    @ViewById(R.id.visit_model_tv)
    TextView mVisitModelTv;
    @ViewById(R.id.model_arrow)
    ImageView mModelArrow;
    @ViewById(R.id.visit_model_icon_iv)
    ImageView mVisitModelIconIv;


    @ViewById(R.id.remark_et)
    EditText mRemarkEt;
    @ViewById(R.id.icon_add_remark)
    ImageView mAddRemarkIcon;


    @ViewById(R.id.client_name_tv)
    TextView mClientNameTv;
    @Click(R.id.select_client_layout)
    void onClickSelectClientLayout ()
    {
        ClientListActivity_.intent(this).startForResult(REQUEST_CODE_CHOOSE_CLIENT);
    }

    @ViewById(R.id.save_btn)
    Button mSaveBtn;

    @Click(R.id.save_btn)
    void onSave()
    {
        if (!CommonUtils.IsNullOrEmpty(mTotalVisitCountEt.getText().toString()))
            mVisitPlanModel.visitCount = Integer.parseInt(mTotalVisitCountEt.getText().toString());
        mVisitPlanModel.remark = mRemarkEt.getText().toString();
        String msg = mVisitPlanModel.checkValidity();
        if (!CommonUtils.IsNullOrEmpty(msg))
        {
            mActivityHelper.showToast(msg);
            return;
        }

        mSaveBtn.setClickable(false);
        if (isEditPlan)
            saveEditVisitPlan();
        else
            saveAddVisitPlan();
    }

    @AfterViews
    void init ()
    {
        if (Account.getInstance() == null)
        {
            return;
        }
        mContext = this;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mVisitPlanModel = new VisitPlanModel();
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.setTime(new Date());
            calendar.set(year, month, day);
            mVisitPlanModel.dateTime = calendar.getTime();
        }
        if (mPlanDetailInfo != null)
        {
            mVisitPlanModel.inflate(mPlanDetailInfo);
            mToolbarTitle.setText(getString(R.string.edit_visit_plan));
        }
        if (isFromCustomer){
            //从客户详情跳转过来
            if (mCustomerInfo != null) {
                mClientNameTv.setText(mCustomerInfo.NAME);
                if (mCustomerInfo.ERP_CODE != null) {
                    mVisitPlanModel.clientId = mCustomerInfo.ID;
                }
            }else {
                mClientNameTv.setText(mVisitPlanModel.clientName);
            }
        }else {
            mClientNameTv.setText(mVisitPlanModel.clientName);
        }

        initVisitPeriodSelect();
        initDateTimeSelect();
        initVisitTypeSelect();
        initVisitModelSelect();
        initRemark();

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_CLIENT)
        {
            if (data.hasExtra(ClientListActivity.CLIENT_EXTRA_STRING))
            {
                CustomerInfoItem client = (CustomerInfoItem) data.getSerializableExtra( ClientListActivity.CLIENT_EXTRA_STRING);
                mClientNameTv.setText(client.name);
                mVisitPlanModel.clientId = client.id;
            }
        }
    }









    private ListPopupWindow mPeriodListPopupWindow;
    private PopupWindowListAdapter mPeriodListPopupWindowAdapter;
    /**
     * 初始化拜访周期类型的选择
     * */
    void initVisitPeriodSelect()
    {
        mVisitPeriodTv.setText(VisitPeriod.getVisitPeriodStrRes(mVisitPlanModel.visitPeriod));
        if (mVisitPlanModel.visitPeriod == VisitPeriod.FIXED_TIME)
        {
            mTotalVisitCountLayout.setVisibility(View.VISIBLE);
            mTotalVisitCountEt.setText(mVisitPlanModel.visitCount + "");
        }

        mPeriodListPopupWindow = new ListPopupWindow(mContext);
        mPeriodListPopupWindowAdapter = new PopupWindowListAdapter(mContext, VisitPeriod.getStringArray(), mVisitPlanModel.visitPeriod);
        mPeriodListPopupWindow.setAdapter(mPeriodListPopupWindowAdapter);
        /**
         * <p>If a popup window is modal, it will receive all touch and key input.
         * If the user touches outside the popup window's content area the popup window
         * will be dismissed.
         * */
        mPeriodListPopupWindow.setModal(true);
        mPeriodListPopupWindow.setWidth(CommonUtils.getScreenWidth(mContext) / 2);
        mPeriodListPopupWindow.setHorizontalOffset(CommonUtils.getScreenWidth(mContext) / 2);
        mPeriodListPopupWindow.setHeight(ActionBar.LayoutParams.WRAP_CONTENT);
        mPeriodListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //为了兼容后台各种状态值的定义
                position++;
                // TODO: 4/23/16 点击之后的逻辑操作
                mVisitPlanModel.visitPeriod = position;
                mVisitPeriodTv.setText(VisitPeriod.getVisitPeriodStrRes(position));
                if (position == VisitPeriod.FIXED_TIME)
                {
                    mTotalVisitCountLayout.setVisibility(View.VISIBLE);
                    mTotalVisitCountEt.requestFocus();
                }
                else {
                    mTotalVisitCountLayout.setVisibility(View.GONE);
                    mTotalVisitCountEt.setText("");
                    mVisitPlanModel.visitCount = 0;
                }
                mPeriodListPopupWindowAdapter.setSelectItemIndex(position);
                mPeriodListPopupWindow.dismiss();
            }
        });
        mPeriodListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPeriodArrow.setImageResource(R.drawable.blue_arrow_bottom);
            }
        });

        mVisitPeriodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPeriodListPopupWindow.setAnchorView(v);
                mPeriodListPopupWindow.show();
                mPeriodArrow.setImageResource(R.drawable.yellow_arrow_top);
            }
        });
    }









    /**
     * 初始化选择日期
     * */
    void initDateTimeSelect () {
        final SimpleDateFormat sdf = new SimpleDateFormat(Consts.DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mVisitPlanModel.dateTime);
        int weekNum = calendar.get(Calendar.DAY_OF_WEEK);
        String weekStr = CommonUtils.GetWeekStr(weekNum - 1);

        mDateTimeTv.setText(sdf.format(mVisitPlanModel.dateTime) + " " + weekStr);
        mDateTimeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimePickPopupWindow.PopupWindowBuilder.getInstance(AddVisitPlanActivity.this, sdf.format(mVisitPlanModel.dateTime), new DateTimePickPopupWindow.ChangeTimeCallback() {
                    @Override
                    public void onChange(Date date) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        mDateTimeTv.setText(sdf.format(date) + " " + CommonUtils.GetWeekStr(calendar.get(Calendar.DAY_OF_WEEK) - 1));
                        mVisitPlanModel.dateTime = date;
                    }
                }).setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mTimeArrow.setImageResource(R.drawable.blue_arrow_bottom);
                    }
                }).getPopupWindow(sdf.format(mVisitPlanModel.dateTime)).showAsDropDown(mDateTimeLayout);
                mTimeArrow.setImageResource(R.drawable.yellow_arrow_top);
            }
        });
    }






    private ListPopupWindow mTypeListPopupWindow;
    private PopupWindowListAdapter mTypeListPopupWindowAdapter;
    /**
     * 初始化拜访类型选择
     * */
    void initVisitTypeSelect ()
    {
        mVisitTypeTv.setText(VisitType.getVisitTypeStrRes(mVisitPlanModel.visitType));
        mVisitTypeIconIv.setImageResource(VisitType.getVisitTypeYellowImgRes(mVisitPlanModel.visitType));
        mTypeListPopupWindow = new ListPopupWindow(mContext);
        mTypeListPopupWindowAdapter = new PopupWindowListAdapter(mContext, VisitType.getStringArray(), mVisitPlanModel.visitType);
        mTypeListPopupWindow.setAdapter(mTypeListPopupWindowAdapter);
        mTypeListPopupWindow.setModal(true);
        mTypeListPopupWindow.setWidth(CommonUtils.getScreenWidth(mContext) / 2);
        mTypeListPopupWindow.setHorizontalOffset(CommonUtils.getScreenWidth(mContext) / 2);
        mTypeListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //为了兼容后台各种状态值的定义
                position++;
                mVisitPlanModel.visitType = position;
                mVisitTypeTv.setText(VisitType.getVisitTypeStrRes(position));
                mVisitTypeIconIv.setImageResource(VisitType.getVisitTypeYellowImgRes(position));
                mTypeListPopupWindowAdapter.setSelectItemIndex(position);
                mTypeListPopupWindow.dismiss();
            }
        });
        mTypeListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mTypeArrow.setImageResource(R.drawable.blue_arrow_bottom);
            }
        });

        mVisitTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTypeListPopupWindow.setAnchorView(v);
                mTypeListPopupWindow.show();
                mTypeArrow.setImageResource(R.drawable.yellow_arrow_top);

            }
        });
    }






    private ListPopupWindow mModelListPopupWindow;
    private PopupWindowListAdapter mModelListPopupWindowAdapter;
    /**
     * 初始化拜访方式选择
     * */
    void initVisitModelSelect ()
    {
        mVisitModelTv.setText(VisitModel.getVisitModelStrRes(mVisitPlanModel.visitModel));
        mVisitModelIconIv.setImageResource(VisitModel.getVisitModelYellowImgRes(mVisitPlanModel.visitModel));
        mModelListPopupWindow = new ListPopupWindow(mContext);
        mModelListPopupWindowAdapter = new PopupWindowListAdapter(mContext, VisitModel.getStringArray(), mVisitPlanModel.visitModel);
        mModelListPopupWindow.setAdapter(mModelListPopupWindowAdapter);
        mModelListPopupWindow.setModal(true);
        mModelListPopupWindow.setWidth(CommonUtils.getScreenWidth(mContext) / 2);
        mModelListPopupWindow.setHorizontalOffset(CommonUtils.getScreenWidth(mContext) / 2);
        mModelListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //为了兼容后台各种状态值的定义
                position++;
                mVisitPlanModel.visitModel = position;
                mVisitModelTv.setText(VisitModel.getVisitModelStrRes(position));
                mVisitModelIconIv.setImageResource(VisitModel.getVisitModelYellowImgRes(position));
                mModelListPopupWindowAdapter.setSelectItemIndex(position);
                mModelListPopupWindow.dismiss();
            }
        });
        mModelListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mModelArrow.setImageResource(R.drawable.blue_arrow_bottom);
            }
        });

        mVisitModelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mModelListPopupWindow.setAnchorView(v);
                mModelListPopupWindow.show();
                mModelArrow.setImageResource(R.drawable.yellow_arrow_top);

            }
        });
    }




    /**
     * 初始化备注
     * */
    void initRemark ()
    {
        if (mVisitPlanModel.remark != null && mVisitPlanModel.remark.length() > 0) {
            mRemarkEt.setText(mVisitPlanModel.remark);
            mAddRemarkIcon.setVisibility(View.GONE);
        }

        mRemarkEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAddRemarkIcon.setVisibility(View.GONE);
                } else {
                    mAddRemarkIcon.setVisibility(View.VISIBLE);
                }
            }
        });

        mRemarkEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0)
                    mAddRemarkIcon.setVisibility(View.VISIBLE);
                else
                    mAddRemarkIcon.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }





    /**
     * 添加拜访计划
     * */
    @Background
    void saveAddVisitPlan()
    {
        mActivityHelper.showLoadingDialog(getString(R.string.submitting_visit_plan));
        BaseResponse resp = new BaseResponse();

        AddVisitPlanRequest request = new AddVisitPlanRequest();
        request.beginTime = CommonUtils.DateFormatTwo(mVisitPlanModel.dateTime);
        request.customerId = mVisitPlanModel.clientId + "";

        request.business = mVisitPlanModel.visitType + "";
        request.visitWay = mVisitPlanModel.visitModel + "";
        request.rule = mVisitPlanModel.visitPeriod + "";
        if (mVisitPlanModel.visitPeriod == VisitPeriod.FIXED_TIME)
        {
            request.ruleValue = mVisitPlanModel.visitCount + "";
        }
        request.remark = mVisitPlanModel.remark;


        resp = mRestClient.addVisitPlan(request, Account.getInstance().getToken());

        mActivityHelper.dismissLoadingDialog();
        onSaveAddResult(resp);
    }

    @UiThread
    void onSaveAddResult(BaseResponse resp)
    {
        mSaveBtn.setClickable(true);
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mActivityHelper.showToast(getString(R.string.save_visit_plan_success));
            setResult(RESULT_OK);
            finish();
        }
    }


    /**
     * 编辑拜访计划
     * */
    @Background
    void saveEditVisitPlan()
    {
        mActivityHelper.showLoadingDialog(getString(R.string.submitting_visit_plan));
        BaseResponse resp = new BaseResponse();

        EditVisitPlanRequest request = new EditVisitPlanRequest();
        request.id = mVisitPlanModel.id;
        request.beginTime = CommonUtils.DateFormatTwo(mVisitPlanModel.dateTime);
        request.customerId = mVisitPlanModel.clientId;

        request.business = mVisitPlanModel.visitType;
        request.visitWay = mVisitPlanModel.visitModel;
        request.rule = mVisitPlanModel.visitPeriod;
        if (request.rule == VisitPeriod.FIXED_TIME)
        {
            request.ruleValue = mVisitPlanModel.visitCount + "";
        }
        request.remark = mVisitPlanModel.remark;



        resp = mRestClient.editVisitPlan(request, Account.getInstance().getToken());

        mActivityHelper.dismissLoadingDialog();
        onSaveEditResult(resp);
    }

    @UiThread
    void onSaveEditResult(BaseResponse resp)
    {
        mSaveBtn.setClickable(true);
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mActivityHelper.showToast(getString(R.string.edit_visit_plan_success));
            setResult(RESULT_OK);
            finish();
        }
    }
}
