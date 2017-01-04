package com.drugoogle.sellscrm.visit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.DateUtils;
import com.drugoogle.sellscrm.common.AutoSwipeRefreshLayout;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.PopupWindowListAdapter;
import com.drugoogle.sellscrm.customer.ClientListActivity;
import com.drugoogle.sellscrm.customer.ClientListActivity_;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.CustomerInfoItem;
import com.drugoogle.sellscrm.data.PlanRecordListInfo;
import com.drugoogle.sellscrm.data.request.SearchVisitRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.VisitPlanListResponse;
import com.drugoogle.sellscrm.data.type.AdminPlan;
import com.drugoogle.sellscrm.data.type.ImportantLevel;
import com.drugoogle.sellscrm.data.type.PlanStatus;
import com.drugoogle.sellscrm.data.type.VisitModel;
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
import java.util.Iterator;
import java.util.List;

/**
 * Created by wgh on 2016/4/20.
 */
@EActivity(R.layout.activity_visit_search)
public class VisitSearchActivity extends BaseActivity implements LoadMoreListView.OnLoadMore
{
    private static final int REQUEST_CODE_CHOOSE_CLIENT = 1;
    private static final int REQUEST_CODE_PLAN_DETAIL = 999;
    Context mContext;
    FilterCondition mFilterCondition;

    /**
     * 筛选条件类，存储筛选条件信息
     * */
    class FilterCondition extends SearchVisitRequest
    {
        String customerName = "";

        public String checkError()
        {
            String errorMsg = "";

            return errorMsg;
        }

    }

    @Extra
    boolean isFromCustomer = false;
    @Extra
    CustomerInfo mCustomerInfo;

    final int pageSize = 25;
    int pageNumber = 1;
    boolean canLoadMore = true;


    @ViewById(R.id.toolbar)
    Toolbar toolbar;


    @ViewById(R.id.client_name_tv)
    TextView mClientNameTv;
    @Click(R.id.select_client_layout)
    void onClickSelectClientLayout ()
    {
        ClientListActivity_.intent(this).startForResult(REQUEST_CODE_CHOOSE_CLIENT);
    }


    @ViewById(R.id.begin_date_layout)
    View mBeginDateLayout;
    @ViewById(R.id.begin_date_tv)
    TextView mBeginDateTv;
    @ViewById(R.id.begin_date_arrow)
    ImageView mBeginDateArrow;
    @ViewById(R.id.end_date_layout)
    View mEndDateLayout;
    @ViewById(R.id.end_date_tv)
    TextView mEndDateTv;
    @ViewById(R.id.end_date_arrow)
    ImageView mEndDateArrow;


    @ViewById(R.id.visit_type_tv)
    TextView mVisitTypeTv;
    @ViewById(R.id.visit_type_layout)
    View mVisitTypeLayout;
    @ViewById(R.id.type_arrow)
    ImageView mTypeArrow;

    @ViewById(R.id.visit_model_tv)
    TextView mVisitModelTv;
    @ViewById(R.id.visit_model_layout)
    View mVisitModelLayout;
    @ViewById(R.id.model_arrow)
    ImageView mModelArrow;

    @ViewById(R.id.clean_filter_btn)
    Button mCleanFilterBtn;
    @ViewById(R.id.search_btn)
    Button mSearchBtn;



    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.filter_layout)
    LinearLayout mFilterLayout;

    @ViewById(R.id.swipe_refresh_layout)
    AutoSwipeRefreshLayout mSwipeRefreshLayout;
    @ViewById(R.id.swipeRefreshLayout_emptyView)
    AutoSwipeRefreshLayout mSwipeRefreshLayoutEmptyView;
    @ViewById(R.id.list_view)
    LoadMoreListView mListView;
    @ViewById(R.id.empty_layout)
    View mEmptyLayout;


    private List<PlanRecordListInfo> mDataList;
    private VisitAdapter mAdapter;

    @AfterViews
    protected void init ()
    {
        mContext = this;
        mFilterCondition = new FilterCondition();
        initFilterView();

        if (isFromCustomer){
            ((TextView)findViewById(R.id.toolbar_title)).setText(R.string.visit_plan);
            //从客户详情跳转过来
            if (mCustomerInfo != null) {
                mFilterCondition.customerName = mCustomerInfo.NAME ;
                mClientNameTv.setText(mFilterCondition.customerName);
                if (!CommonUtils.IsNullOrEmpty(mCustomerInfo.ID)) {
                    //选择客户id
                   mFilterCondition.customerId  = mCustomerInfo.ID;
                }
            }
        }

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
        if (!isFromCustomer) {
            mDrawerLayout.openDrawer(mFilterLayout);
        }

        initListView();
        initFooterView();
        mSwipeRefreshLayoutEmptyView.autoRefresh();
        getVisitList(false);
        initVisitTypeSelect();
        initVisitModelSelect();


        mBeginDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBeginDateSelect();
            }
        });
        mEndDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initEndDateSelect();
            }
        });
        mCleanFilterBtn.setOnClickListener(cleanFilterListener);
        mSearchBtn.setOnClickListener(searchListener);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_CLIENT)
        {
            if (data.hasExtra(ClientListActivity.CLIENT_EXTRA_STRING))
            {
                CustomerInfoItem client = (CustomerInfoItem)data.getSerializableExtra( ClientListActivity.CLIENT_EXTRA_STRING);
                mClientNameTv.setText(client.name);
                mFilterCondition.customerId = client.id + "";
            }
        }
        if (requestCode == BaseResponse.REQUEST_CODE_EXPIRED && resultCode == BaseResponse.RESULT_CODE_EXPIRED)
        {
            getVisitList(false);
        }
        if (requestCode == REQUEST_CODE_PLAN_DETAIL && resultCode == VisitPlanDetailActivity.RESULT_CODE_DELETE_PLAN)
        {
            int id = data.getIntExtra(VisitPlanDetailActivity.DELETE_PLAN_EXTRA_NAME, -1);
            for (Iterator<PlanRecordListInfo> it = mDataList.iterator(); it.hasNext(); )
            {
                PlanRecordListInfo info = it.next();
                if (id == info.visitPlanId)
                {
                    it.remove();
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.visit_search_menu, menu);
        return true;
    }


    @Override
    public void loadMore() {
        if (!canLoadMore) return;
        pageNumber++;
        getVisitList(true);
    }

    /**
     * 初始化list view
     * */
    private void initListView ()
    {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.yellow));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageNumber = 1;
                canLoadMore = true;
                getVisitList(false);
            }
        });
        mSwipeRefreshLayoutEmptyView.setColorSchemeColors(getResources().getColor(R.color.yellow));
        mSwipeRefreshLayoutEmptyView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageNumber = 1;
                canLoadMore = true;
                getVisitList(false);
            }
        });

        mAdapter = new VisitAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setLoadMoreListen(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= mDataList.size()) return;
                if (mDataList.get(position).visitRecordId > 0) {
                    VisitRecordDetailActivity_.intent(mContext).mRecordId(mDataList.get(position).visitRecordId).start();
                } else {
                    VisitPlanDetailActivity_.intent(mContext).mPlanId(mDataList.get(position).visitPlanId).startForResult(REQUEST_CODE_PLAN_DETAIL);
                }
            }
        });
        mListView.setEmptyView(mSwipeRefreshLayoutEmptyView);
    }


    private LinearLayout footerParent;
    private View footer;
    private LayoutInflater inflater;
    private TextView loadMoreTv;
    private void initFooterView ()
    {
        inflater = LayoutInflater.from(VisitSearchActivity.this);
        footerParent = new LinearLayout(VisitSearchActivity.this);
        footer = inflater.inflate( R.layout.listview_footer_shade, null, false );
        loadMoreTv = (TextView)footer.findViewById(R.id.no_more_textView);
        footerParent.addView(footer);
        footerParent.setGravity(Gravity.CENTER);
        mListView.addFooterView(footerParent);
    }






    /**
     * toolbar 上面按钮点击监听
     * */
    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.search:
                    // TODO: 2016/4/21 弹出右侧滑栏，输入搜索条件
                    mDrawerLayout.openDrawer(mFilterLayout);
                    break;
            }
            return true;
        }
    };






    void getVisitList (boolean loadMore)
    {
        mSwipeRefreshLayout.setRefreshing(true);
        getVisitListBg(loadMore);
    }

    /**
     * 根据日期获取计划/纪录列表
     * @param loadMore true 上拉加载更多 false 下拉刷星
     * */
    @Background
    void getVisitListBg (boolean loadMore)
    {
        if (!CommonUtils.IsNullOrEmpty(mFilterCondition.checkError()))
        {
            mActivityHelper.showNormalToast(mFilterCondition.checkError());
            return;
        }
        VisitPlanListResponse resp = mRestClient.visitSearch(pageNumber, pageSize, Account.getInstance().getToken(), mFilterCondition);

        updateVisitPlanList(resp, loadMore);
    }

    @UiThread
    void updateVisitPlanList (VisitPlanListResponse resp, boolean loadMore)
    {
        if (mSwipeRefreshLayout.isShown()) {
            mSwipeRefreshLayout.setRefreshing( false );
        }
        if (mSwipeRefreshLayoutEmptyView.isShown()) {
            mSwipeRefreshLayoutEmptyView.setRefreshing( false );
        }
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mListView.onLoadComplete();
            if (loadMore) {
                mDataList.addAll(resp.dataList);
            }
            else {
                mDataList = resp.dataList;
            }
            mAdapter.notifyDataSetChanged();

            if (resp.dataList == null || resp.dataList.size() < pageSize)
            {
                canLoadMore = false;
                loadMoreTv.setText(getString(R.string.no_more));
            }
            else
            {
                canLoadMore = true;
                loadMoreTv.setText(getString(R.string.order_scan_more));
            }
        }
    }









    private PopupWindow mBeginDateSelectPopupWindow;
    /**
     * 初始化开始时间选择器
     * */
    void initBeginDateSelect()
    {
        mBeginDateArrow.setImageResource(R.drawable.yellow_arrow_top);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.select_date, null);
        mBeginDateSelectPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        mBeginDateSelectPopupWindow.setContentView(contentView);
        final DatePicker datePicker = (DatePicker)contentView.findViewById(R.id.date_picker);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(CommonUtils.String2Date(mFilterCondition.beginTime));
        datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mBeginDateTv.setText(CommonUtils.DateFormate(calendar.getTime()));
                        mFilterCondition.beginTime = CommonUtils.DateFormate(calendar.getTime());
                    }
                });

        mBeginDateSelectPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mBeginDateSelectPopupWindow.setOutsideTouchable(true);
        mBeginDateSelectPopupWindow.setWidth(mBeginDateLayout.getWidth());
        mBeginDateSelectPopupWindow.showAsDropDown(mBeginDateLayout);
        mBeginDateSelectPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                mBeginDateTv.setText(CommonUtils.DateFormate(calendar.getTime()));
                mBeginDateArrow.setImageResource(R.drawable.blue_arrow_bottom);
            }
        });
    }








    private PopupWindow mEndDateSelectPopupWindow;
    /**
     * 初始化结束时间选择器
     * */
    void initEndDateSelect()
    {
        mEndDateArrow.setImageResource(R.drawable.yellow_arrow_top);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.select_date, null);
        mEndDateSelectPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        mEndDateSelectPopupWindow.setContentView(contentView);
        final DatePicker datePicker = (DatePicker)contentView.findViewById(R.id.date_picker);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(CommonUtils.String2Date(mFilterCondition.endDate));
        datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mEndDateTv.setText(CommonUtils.DateFormate(calendar.getTime()));
                        mFilterCondition.endDate = CommonUtils.DateFormate(calendar.getTime());
                    }
                });

        mEndDateSelectPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mEndDateSelectPopupWindow.setOutsideTouchable(true);
        mEndDateSelectPopupWindow.setWidth(mEndDateLayout.getWidth());
        mEndDateSelectPopupWindow.showAsDropDown(mEndDateLayout);
        mEndDateSelectPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                mEndDateTv.setText(CommonUtils.DateFormate(calendar.getTime()));
                mEndDateArrow.setImageResource(R.drawable.blue_arrow_bottom);
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
        mVisitTypeTv.setText(getString(R.string.unlimited));

        mTypeListPopupWindow = new ListPopupWindow(mContext);
        mTypeListPopupWindowAdapter = new PopupWindowListAdapter(mContext, VisitType.getStringArrayWithUnlimited(), 5);
        mTypeListPopupWindow.setAdapter(mTypeListPopupWindowAdapter);
        mTypeListPopupWindow.setModal(true);
        mTypeListPopupWindow.setWidth(CommonUtils.getScreenWidth(mContext) / 2);
        mTypeListPopupWindow.setHorizontalOffset(CommonUtils.getScreenWidth(mContext) / 2);
        mTypeListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //为了兼容后台的定义（从1开始）
                position++;
                //为了多一个不限
                if (position == 5)
                    mFilterCondition.business = "";
                else
                    mFilterCondition.business = position + "";

                mVisitTypeTv.setText(VisitType.getVisitTypeStrRes(position));
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
        mVisitTypeTv.setText(getString(R.string.unlimited));

        mModelListPopupWindow = new ListPopupWindow(mContext);
        mModelListPopupWindowAdapter = new PopupWindowListAdapter(mContext, VisitModel.getStringArrayWithUnlimited(), 3);
        mModelListPopupWindow.setAdapter(mModelListPopupWindowAdapter);
        mModelListPopupWindow.setModal(true);
        mModelListPopupWindow.setWidth(CommonUtils.getScreenWidth(mContext) / 2);
        mModelListPopupWindow.setHorizontalOffset(CommonUtils.getScreenWidth(mContext) / 2);
        mModelListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //为了兼容后台的定义（从1开始）
                position++;
                //为了多一个不限
                if (position == 3)
                    mFilterCondition.visitWay = "";
                else
                    mFilterCondition.visitWay = position + "";

                mVisitModelTv.setText(VisitModel.getVisitModelStrRes(position));
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
     * 清空按钮点击监听
     * */
    View.OnClickListener cleanFilterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFilterCondition = new FilterCondition();
            initFilterView();
        }
    };

    /**
     * 搜索按钮点击监听
     * */
    View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mDrawerLayout.closeDrawers();
            pageNumber = 1;
            getVisitList(false);
        }
    };


    void initFilterView ()
    {
        initFilterCondition();
        mClientNameTv.setText(mFilterCondition.customerName);
        mBeginDateTv.setText(mFilterCondition.beginTime);
        mEndDateTv.setText(mFilterCondition.endDate);
        mVisitTypeTv.setText(getString(R.string.unlimited));
        mVisitModelTv.setText(getString(R.string.unlimited));

        if (mTypeListPopupWindowAdapter != null && mModelListPopupWindowAdapter != null)
        {
            mTypeListPopupWindowAdapter.setSelectItemIndex(5);
            mModelListPopupWindowAdapter.setSelectItemIndex(3);
        }
    }

    void initFilterCondition ()
    {
        mFilterCondition.customerName = "";
        mFilterCondition.customerId = "";
        mFilterCondition.beginTime = CommonUtils.DateFormate(DateUtils.getFirstDayOfMonth(new Date()));
        mFilterCondition.endDate = CommonUtils.DateFormate(DateUtils.getEndDayOfMonth(new Date()));
        mFilterCondition.business = "";
        mFilterCondition.visitWay = "";
    }










    class VisitAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return mDataList == null ? 0 : mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList == null ? null : mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.list_item_visit_search, null);

                holder.visitStatus = (TextView)convertView.findViewById(R.id.status);
                holder.statusIcon = (ImageView)convertView.findViewById(R.id.status_icon_iv);
                holder.importantLevel = (ImageView)convertView.findViewById(R.id.important_level);
                holder.name = (TextView)convertView.findViewById(R.id.name);
                holder.time = (TextView)convertView.findViewById(R.id.time);
                holder.date = (TextView)convertView.findViewById(R.id.date);
                holder.systemPlanFlagIcon = convertView.findViewById(R.id.system_plan_flag_icon_iv);
                holder.visitModelIcon = (ImageView)convertView.findViewById(R.id.visit_model_icon_iv);
                holder.visitTypeIcon = (ImageView)convertView.findViewById(R.id.visit_type_icon_iv);

                holder.visitModelText = (TextView)convertView.findViewById(R.id.visit_model);
                holder.visitTypeText = (TextView)convertView.findViewById(R.id.visit_type);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag();
            }

            PlanRecordListInfo item = mDataList.get(position);
            holder.visitStatus.setText(PlanStatus.getPlanStatusStrRes(item.status));
            holder.visitStatus.setTextColor(getResources().getColor(PlanStatus.getPlanStatusColorRes(item.status)));
            holder.statusIcon.setImageResource(PlanStatus.getPlanStatusTimelineImgRes(item.status));
            holder.importantLevel.setImageResource(ImportantLevel.getImportantLevelImgRes(item.importantLevel));
            holder.name.setText(item.customerName);

            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
            SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
            Date date = CommonUtils.String2DateTwo(item.time);
            holder.date.setText(sdf.format(date));

            holder.time.setText(sdf1.format(date));
            holder.time.setTextColor(getResources().getColor(PlanStatus.getPlanStatusColorRes(item.status)));

            holder.systemPlanFlagIcon.setVisibility(item.is_admin == AdminPlan.IS_ADMIN ? View.VISIBLE : View.GONE);
            holder.visitModelIcon.setImageResource(VisitModel.getVisitModelImgRes(item.visitWay));
            holder.visitTypeIcon.setImageResource(VisitType.getVisitTypeImgRes(item.business));
            holder.visitModelText.setText(VisitModel.getVisitModelStrRes(item.visitWay));
            holder.visitTypeText.setText(VisitType.getVisitTypeStrRes(item.business));


            return convertView;

        }

        class ViewHolder
        {
            TextView visitStatus;   //拜访计划状态
            ImageView statusIcon;   //拜访计划状态icon
            ImageView importantLevel; //重要程度
            TextView name;    //客户名字
            TextView time;    //时间
            TextView date;      //日期
            View systemPlanFlagIcon;   //标记系统计划icon
            ImageView visitModelIcon; //拜访方式类型：电话/见面
            ImageView visitTypeIcon;  //拜访计划类型：财务/物流/业务/其他
            TextView visitModelText; //拜访方式类型：电话/见面
            TextView visitTypeText;  //拜访计划类型：财务/物流/业务/其他
        }
    }
}
