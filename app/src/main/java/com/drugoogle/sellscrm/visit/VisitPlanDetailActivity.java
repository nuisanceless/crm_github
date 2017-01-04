package com.drugoogle.sellscrm.visit;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.CommonDialog;
import com.drugoogle.sellscrm.customer.CustomerDetailsActivity_;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.PlanDetailInfo;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.PlanDetailResponse;
import com.drugoogle.sellscrm.data.type.AdminPlan;
import com.drugoogle.sellscrm.data.type.PlanStatus;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wgh on 2016/4/26.
 * 拜访计划详情页面
 */
@EActivity(R.layout.activity_visit_plan_detail)
public class VisitPlanDetailActivity extends BaseActivity
{
    public static final int RESULT_CODE_DELETE_PLAN = 15;
    public static final int REQUEST_CODE_EDIT_PLAN = 17;
    public static final String DELETE_PLAN_EXTRA_NAME = "delete_id";

    PlanDetailInfo mInfo = new PlanDetailInfo();
    @Extra
    int mPlanId;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
//    @ViewById(R.id.user_admin)
//    TextView mUserAdminTv;
    @ViewById(R.id.plan_status_tv)
    TextView mPlanStatusTv;
    @ViewById(R.id.plan_status_icon)
    ImageView mPlanStatusIcon;
    @ViewById(R.id.customer_name_tv)
    TextView mCustomerNameTv;
    @ViewById(R.id.plan_content_tv)
    TextView mPlanContentTv;
    @ViewById(R.id.visit_model_icon_iv)
    ImageView mVisitModelIconIv;
    @ViewById(R.id.visit_model_tv)
    TextView mVisitModelTv;
    @ViewById(R.id.visit_type_icon_iv)
    ImageView mVisitTypeIconIv;
    @ViewById(R.id.visit_type_tv)
    TextView mVisitTypeTv;
    @ViewById(R.id.remark_tv)
    TextView mRemarkTv;
    @ViewById(R.id.operation_btn)
    Button mOperationBtn;


    @Click(R.id.customer_name)
    void    onClickCustomerName()
    {
        CustomerDetailsActivity_.intent(this).extra("id", mInfo.customerId).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BaseResponse.REQUEST_CODE_EXPIRED && resultCode == BaseResponse.RESULT_CODE_EXPIRED)
        {
            getPlanDetail(mPlanId);
        }
        if (requestCode == REQUEST_CODE_EDIT_PLAN && resultCode == RESULT_OK)
        {
            getPlanDetail(mPlanId);
        }
    }






    @AfterViews
    protected void init ()
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getPlanDetail(mPlanId);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (mInfo.status)
        {
            case PlanStatus.NONSTART:
                getMenuInflater().inflate(R.menu.visit_detail_menu, menu);
                break;
            case PlanStatus.UNDERWAY:

                break;
            case PlanStatus.FINISHED:

                break;
            default:
                break;
        }
        return true;
    }

    /**
     * toolbar 上面按钮点击监听
     * */
    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    final CommonDialog dialog = new CommonDialog(VisitPlanDetailActivity.this);
                    dialog.builder().setOnConfirmClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deletePlanDetail(mInfo.id);
                        }
                    }).setOnCancelClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    }).setContent(getString(R.string.confirm_to_delete_visit)).show();
                    break;
                case R.id.edit:
                    AddVisitPlanActivity_.intent(VisitPlanDetailActivity.this).mPlanDetailInfo(mInfo).isEditPlan(true).startForResult(REQUEST_CODE_EDIT_PLAN);
                    break;
            }
            return true;
        }
    };







    /**
     *
     * */
    @Background
    void getPlanDetail (int id)
    {
        mActivityHelper.showLoadingDialog(getString(R.string.loading_plan_detail));

        PlanDetailResponse resp = mRestClient.getPlanDetail(id, Account.getInstance().getToken());

        mActivityHelper.dismissLoadingDialog();
        onGetPlanDetail(resp);
    }
    @UiThread
    void onGetPlanDetail (PlanDetailResponse resp)
    {
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mInfo = resp.visitPlan;
            initDate(resp.visitPlan);
        }
    }




    @Background
    void deletePlanDetail (int id)
    {
        mActivityHelper.showLoadingDialog(getString(R.string.delete_plan));
        BaseResponse resp = mRestClient.deleteVisitPlan(id, Account.getInstance().getToken());

        mActivityHelper.dismissLoadingDialog();
        onDeletePlanDetail(resp, id);
    }
    @UiThread
    void onDeletePlanDetail (BaseResponse resp, int id)
    {
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mActivityHelper.showToast(getString(R.string.already_delete_plan));
            Intent intent = new Intent();
            intent.putExtra(DELETE_PLAN_EXTRA_NAME, id);
            setResult(RESULT_CODE_DELETE_PLAN, intent);
            finish();
        }
    }






    @Background
    void startPlan(int id)
    {
        mActivityHelper.showLoadingDialog(null);
//        BaseResponse resp = mRestClient.startPlan(id);
        // TODO: 2016/4/28 模拟数据
        BaseResponse resp = new BaseResponse();
        resp.code = 1;

        mActivityHelper.dismissLoadingDialog();
        onStartPlan(resp);
    }
    @UiThread
    void onStartPlan(BaseResponse resp)
    {
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mActivityHelper.showToast(getString(R.string.plan_started));
            getPlanDetail(mInfo.id);
        }
    }




    @Background
    void finishPlan(int id)
    {
        mActivityHelper.showLoadingDialog(null);
//        BaseResponse resp = mRestClient.finishPlan(id);
        // TODO: 2016/4/28 模拟数据
        BaseResponse resp = new BaseResponse();
        resp.code = 1;

        mActivityHelper.dismissLoadingDialog();
        onFinishPlan(resp);
    }
    @UiThread
    void onFinishPlan(BaseResponse resp)
    {
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mActivityHelper.showToast(getString(R.string.plan_finished));
            getPlanDetail(mInfo.id);
        }
    }








    /**
     * 显示详情
     * */
    void initDate(PlanDetailInfo info)
    {
//        mUserAdminTv.setText(AdminPlan.getAdminPlanStrRes(info.is_admin));
        mPlanStatusTv.setText(PlanStatus.getPlanStatusStrRes(info.status));
        mPlanStatusTv.setTextColor(getResources().getColor(PlanStatus.getPlanStatusColorRes(info.status)));
        mPlanStatusIcon.setImageResource(PlanStatus.getPlanStatusImgRes(info.status));

        mCustomerNameTv.setText(info.customerName);

        Date date = CommonUtils.String2DateTwo(mInfo.beginTime);
        String time = CommonUtils.DateFormate(date);
        String content = "";
        switch (info.rule)
        {
            case VisitPeriod.ONLY_ONE_TIME:
                content = time + " " + getString(VisitPeriod.getVisitPeriodStrRes(info.rule));
                break;
            case VisitPeriod.EVERY_DAY:
            case VisitPeriod.EVERY_WEEK:
            case VisitPeriod.EVERY_MONTH:
                content = time + "日起 " + getString(VisitPeriod.getVisitPeriodStrRes(info.rule)) + " 拜访";
                break;
            case VisitPeriod.FIXED_TIME:
                content = time + "日前拜访" + info.ruleValue + "次";
                break;
            default:
                break;
        }
        mPlanContentTv.setText(content);

        mVisitModelTv.setText(VisitModel.getVisitModelStrRes(info.visitWay));
        mVisitModelIconIv.setImageResource(VisitModel.getVisitModelYellowImgRes(info.visitWay));

        mVisitTypeTv.setText(VisitType.getVisitTypeStrRes(info.business));
        mVisitTypeIconIv.setImageResource(VisitType.getVisitTypeYellowImgRes(info.business));

        mRemarkTv.setText(info.remark);

        switch (info.status)
        {
            case PlanStatus.NONSTART:
                mOperationBtn.setText(getString(R.string.start_plan));
                mOperationBtn.setOnClickListener(startPlanClickListener);
                break;
            case PlanStatus.UNDERWAY:
                mOperationBtn.setText(getString(R.string.finish_plan));
                mOperationBtn.setOnClickListener(finishPlanClickListener);
                break;
            case PlanStatus.FINISHED:
                mOperationBtn.setVisibility(View.GONE);
                break;
            default:
                break;
        }

    }






    View.OnClickListener startPlanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startPlan(mInfo.id);
        }
    };

    View.OnClickListener finishPlanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finishPlan(mInfo.id);
        }
    };

}
