package com.drugoogle.sellscrm.visit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.drugoogle.sellscrm.MainActivity;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.DateUtils;
import com.drugoogle.sellscrm.common.ActivityHelper_;
import com.drugoogle.sellscrm.common.CustomCalendar;
import com.drugoogle.sellscrm.common.MyApplication;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.CalendarCellInfo;
import com.drugoogle.sellscrm.data.PlanRecordListInfo;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.SummaryCalendarResponse;
import com.drugoogle.sellscrm.data.response.VisitPlanListResponse;
import com.drugoogle.sellscrm.data.type.AdminPlan;
import com.drugoogle.sellscrm.data.type.ImportantLevel;
import com.drugoogle.sellscrm.data.type.PlanStatus;
import com.drugoogle.sellscrm.data.type.VisitModel;
import com.drugoogle.sellscrm.data.type.VisitType;
import com.drugoogle.sellscrm.rest.MyRestClient;
import com.drugoogle.sellscrm.selfinfo.Account;
import com.nineoldandroids.animation.ObjectAnimator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fanzhang on 2016/3/17.
 */
@EFragment(R.layout.visit_main_fragment)
public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener
{
    private static final String TAG = "Visit_MainFragment";

    public static final int CONST_INITIAL_CURRENT_INDEX = 498;
    public static final int REQUEST_CODE_VISIT_PLAN_DETAIL = 14;

    private Context mContext;
    protected MyApplication mApp;
    protected ActivityHelper_ mActivityHelper;
    protected LayoutInflater mLayoutInflater;
    protected MyRestClient mRestClient;


    @ViewById(R.id.date_info_layout)
    View mDateInfoLayout;
    @ViewById(R.id.date_info_tv)
    TextView mDateInfoTv;
    @ViewById(R.id.pre_date_iv)
    ImageView mPreDateIv;
    @ViewById(R.id.next_date_iv)
    ImageView mNextDateIv;

    @ViewById(R.id.show_calendar_iv)
    ImageView mShowCalendarIv;
    @ViewById(R.id.show_today_iv)
    ImageView mShowTodayIv;


    //@ViewById(R.id.week_sign_layout)
    View mWeekSignLayout;
    //@ViewById(R.id.custom_calendar_vp)
    ViewPager mCustomCalendarVp;
    @ViewById(R.id.rili_layout)
    CustomCalendar mRiliLayout;


    @ViewById(R.id.list_view)
    ListView mListView;
    @ViewById(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @ViewById(R.id.empty_layout)
    View mEmptyLayout;


    View mNoMoreFooterView;
    View mShadeFooterView;
    private int mCurrentCalendarCardIndex = CONST_INITIAL_CURRENT_INDEX;
    private CalendarViewAdapter<CalendarView> mCalendarAdapter;
    /**
     * 折叠月日历标记，默认为true
     */
    private boolean mFoldCalendarFlag = true;
    private Date mSelectDate = new Date();
    private Date mSelectMonth = new Date();

    public Date getSelectDate()
    {
        return mSelectDate;
    }


    private List<PlanRecordListInfo> mDataList;
    private VisitAdapter mAdapter;
    private EndVisitReceiver mEndVisitReceiver;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        flagId = -1;
        mRefreshHandler.removeMessages(REFRESH_HANDLER_WHAT);
        mCustomCalendarVp.removeOnPageChangeListener(calendarPageChangeListener);
    }

    @Override
    public void onStart()
    {
        mEndVisitReceiver = new EndVisitReceiver();
        IntentFilter tcFilter = new IntentFilter(VisitService.ACTION_VISIT_ENDED);
        mContext.registerReceiver(mEndVisitReceiver, tcFilter);
        super.onStart();
    }

    @Override
    public void onResume()
    {
        refreshVisitList(mSelectDate);
        getMonthVisitSummary();
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        mContext.unregisterReceiver(mEndVisitReceiver);
        super.onStop();
    }

    /**
     * 列表下拉刷新
     */
    @Override
    public void onRefresh()
    {
        mDataList.clear();
        mAdapter.notifyDataSetChanged();

        refreshVisitList(mSelectDate);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.ADD_PLAN_RECORD_REQUEST)
        {
            refreshVisitList(mSelectDate);
            getMonthVisitSummary();
        }
        if (requestCode == REQUEST_CODE_VISIT_PLAN_DETAIL && resultCode == VisitPlanDetailActivity.RESULT_CODE_DELETE_PLAN)
        {
            refreshVisitList(mSelectDate);
            getMonthVisitSummary();
        }
    }




    @AfterViews
    void init()
    {
        mContext = this.getContext();
        mApp = MyApplication_.getInstance();
        mActivityHelper = ActivityHelper_.getInstance_(mContext);
        mLayoutInflater = getActivity().getLayoutInflater();
        mRestClient = mApp.restClient();
        mDataList = new ArrayList<>();
        mDateInfoLayout.bringToFront();

        mWeekSignLayout = mRiliLayout.findViewById(R.id.week_sign_layout);
        mCustomCalendarVp = (ViewPager) mRiliLayout.findViewById(R.id.custom_calendar_vp);



        CalendarView[] cardViews = new CalendarView[3];
        for (int i = 0; i < 3; i++)
        {
            cardViews[i] = new CalendarView(mContext, mCalendarViewListener);
        }

        mCalendarAdapter = new CalendarViewAdapter<>(cardViews);
        mCustomCalendarVp.setAdapter(mCalendarAdapter);
        mCustomCalendarVp.setCurrentItem(CONST_INITIAL_CURRENT_INDEX);
        mCustomCalendarVp.addOnPageChangeListener(calendarPageChangeListener);
        mCalendarAdapter.getItem(CONST_INITIAL_CURRENT_INDEX).setSelectDayIndex(mSelectDate);


        mPreDateIv.setOnClickListener(this);
        mNextDateIv.setOnClickListener(this);
        mShowCalendarIv.setOnClickListener(this);
        mShowTodayIv.setOnClickListener(this);
        refreshDateInfo(mSelectDate);
        initListView();
        getMonthVisitSummary();
    }


    private float mDownX;
    private float mDownY;

    /**
     * 初始化list view
     */
    private void initListView()
    {
        mAdapter = new VisitAdapter();
        mNoMoreFooterView = mLayoutInflater.inflate(R.layout.view_footer_visit_list_item, null);
        mNoMoreFooterView.setVisibility(View.INVISIBLE);
        mShadeFooterView = mLayoutInflater.inflate(R.layout.view_footer_shade, null);
        mListView.addFooterView(mNoMoreFooterView);
        mListView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.yellow));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position >= mDataList.size())
                {
                    return;
                }
                if (mDataList.get(position).visitRecordId > 0)
                {
                    VisitRecordDetailActivity_.intent(mContext).mRecordId(mDataList.get(position).visitRecordId).start();
                }
                else
                {
                    VisitPlanDetailActivity_.intent(mContext).mPlanId(mDataList.get(position).visitPlanId).startForResult(REQUEST_CODE_VISIT_PLAN_DETAIL);
                }
            }
        });
        mListView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = event.getX();
                        mDownY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float disX = event.getX() - mDownX;
                        float disY = event.getY() - mDownY;
                        if (disY < 0 && Math.abs(disX) * 2 < Math.abs(disY))
                        {
                            if (!mFoldCalendarFlag)
                            {
                                foldCalendarCard();
                            }
                        }
//                        if (disY > 0 && Math.abs(disX) * 2 < Math.abs(disY))
//                        {
//                            if (mFoldCalendarFlag && mListView.getFirstVisiblePosition() == 0)
//                            {
//                                showCalendarCard();
//                            }
//                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });


        refreshVisitList(mSelectDate);
    }


    private final int REFRESH_HANDLER_WHAT = 3;
    private int REFRESH_LIST_DELAY = 500;//ms
    private int flagId;
    Handler mRefreshHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case REFRESH_HANDLER_WHAT:
                    if (mEmptyLayout != null)
                    {
                        mEmptyLayout.setVisibility(View.GONE);
                    }
                    mDataList.clear();
                    mAdapter.notifyDataSetChanged();
                    getVisitList((Date) msg.obj);
                    break;
            }
        }
    };

    void refreshVisitList(Date date)
    {
        mRefreshHandler.removeMessages(REFRESH_HANDLER_WHAT);
        mSwipeRefreshLayout.setRefreshing(true);
        Message msg = new Message();
        msg.what = REFRESH_HANDLER_WHAT;
        msg.obj = date;
        mRefreshHandler.sendMessageDelayed(msg, REFRESH_LIST_DELAY);
    }

    @Background
    void getVisitList(Date date)
    {
        flagId++;
        VisitPlanListResponse resp = mRestClient.getVisitPlanList(CommonUtils.DateFormate(date), Account.getInstance().getToken(), flagId);
        afterGetVisitList(resp);
    }

    @UiThread
    void afterGetVisitList(VisitPlanListResponse resp)
    {
        if (mSwipeRefreshLayout == null)
        {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(false);
        if (!BaseResponse.hasErrorWithOperation(resp, mContext))
        {
            if (resp.flagId != flagId)
            {
                return;
            }
            if (resp.dataList != null)
            {
                mDataList = resp.dataList;
            }
            else
            {
                mDataList = new ArrayList<>();
            }
        }
        mAdapter.notifyDataSetChanged();
        if (mDataList != null && mDataList.size() > 0)
        {
            mEmptyLayout.setVisibility(View.GONE);
            mNoMoreFooterView.setVisibility(View.VISIBLE);
        }
        else
        {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mNoMoreFooterView.setVisibility(View.INVISIBLE);
//            mListView.removeFooterView(mNoMoreFooterView);
        }
    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.pre_date_iv:
//                setCalendarDate(DateUtils.getYesterday(mSelectDate));

                if (mFoldCalendarFlag)
                {
                    setCalendarDate(DateUtils.getYesterday(mSelectDate));
                }
                else
                {
                    mCustomCalendarVp.setCurrentItem(mCurrentCalendarCardIndex - 1, true);
                }
                break;
            case R.id.next_date_iv:
//                setCalendarDate(DateUtils.getTomorrow(mSelectDate));

                if (mFoldCalendarFlag)
                {
                    setCalendarDate(DateUtils.getTomorrow(mSelectDate));
                }
                else
                {
                    mCustomCalendarVp.setCurrentItem(mCurrentCalendarCardIndex + 1, true);
                }
                break;
            case R.id.show_calendar_iv:
                if (mFoldCalendarFlag)
                {
                    showCalendarCard();
                }
                else
                {
                    foldCalendarCard();
                    mCalendarAdapter.getItem(mCurrentCalendarCardIndex).setMonth(mSelectDate, mSelectDate);
                }
                break;
            case R.id.show_today_iv:
                setCalendarDate(new Date());
                getMonthVisitSummary();
                break;
            default:
                break;
        }
    }


    /**
     * 月日历事件监听
     */
    CalendarView.OnCalendarViewListener mCalendarViewListener = new CalendarView.OnCalendarViewListener()
    {
        @Override
        public void initCalendarCard(Date date)
        {
            refreshDateInfo(date);
        }

        @Override
        public void OnSelectDate(Date date)
        {
            mSelectDate = date;
            mSelectMonth = date;
            refreshVisitList(date);
            foldCalendarCard();
            refreshDateInfo(date);
        }

        @Override
        public void slideCalendarCard(Date date)
        {

        }

        @Override
        public void gestureUpglide()
        {
            //foldCalendarCard();
        }
    };


    /**
     * 月日历卡滑动监听
     */
    ViewPager.OnPageChangeListener calendarPageChangeListener = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {

        }

        @Override
        public void onPageSelected(int position)
        {
            updateCalendarView(position);
            getMonthVisitSummary();
        }

        @Override
        public void onPageScrollStateChanged(int state)
        {

        }
    };


    /**
     * 更新月历视图
     */
    private void updateCalendarView(int position)
    {
        CalendarView[] mShowViews = mCalendarAdapter.getAllItems();
        CalendarView cv = mShowViews[position % mShowViews.length];

        Calendar c = cv.getCalendar();
        Date curDate = cv.getMonth();
        c.setTime(curDate);

        if (position > mCurrentCalendarCardIndex)
        {
            c.add(Calendar.MONTH, 1);
            refreshMonthInfo(c.getTime());
            cv.setMonth(c.getTime(), mSelectDate);
        }
        else if (position < mCurrentCalendarCardIndex)
        {
            c.add(Calendar.MONTH, -1);
            refreshMonthInfo(c.getTime());
            cv.setMonth(c.getTime(), mSelectDate);
        }
        mCurrentCalendarCardIndex = position;
    }


    /**
     * 刷新日期信息
     */
    private void refreshDateInfo(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        mDateInfoTv.setText(sdf.format(date) + " , " + "周" + DateUtils.weekName[calendar.get(Calendar.DAY_OF_WEEK) - 1]);

        if (DateUtils.isSameDay(date, new Date()))
        {
            mShowTodayIv.setVisibility(View.INVISIBLE);
        }
        else
        {
            mShowTodayIv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 刷新月信息
     */
    private void refreshMonthInfo(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
        mDateInfoTv.setText(sdf.format(date));

    }

    /**
     * 隐藏日历控件
     */
    private void foldCalendarCard()
    {

        ObjectAnimator.ofFloat(mRiliLayout, "translationY", 0).start();
        mFoldCalendarFlag = true;
        refreshDateInfo(mSelectDate);
        mShowCalendarIv.setImageResource(R.drawable.icon_show_calendar);
    }

    /**
     * 显示日历控件
     */
    private void showCalendarCard()
    {

        int mWeekSignLayoutHeight = mWeekSignLayout.getHeight();
        int mCustomCalendarVpHeight = mCustomCalendarVp.getHeight();
        ObjectAnimator.ofFloat(mRiliLayout, "translationY", mWeekSignLayoutHeight + mCustomCalendarVpHeight).start();
        mFoldCalendarFlag = false;
        refreshMonthInfo(mSelectMonth);
        mShowCalendarIv.setImageResource(R.drawable.icon_fold_calendar);
        getMonthVisitSummary();
    }


    private void setCalendarDate(Date date)
    {
        mSelectDate = date;
        mSelectMonth = date;
        refreshDateInfo(date);
        mCalendarAdapter.getItem(mCurrentCalendarCardIndex).setMonth(date, date);
        getMonthVisitSummary();
        refreshVisitList(date);

    }

    /**
     * 刷新月日历拜访概要
     */
    private void getMonthVisitSummary()
    {
        Date date = mCalendarAdapter.getItem(mCurrentCalendarCardIndex).getMonth();
        Date beginDate = DateUtils.getFirstDayOfMonth(date);
        Date endDate = DateUtils.getEndDayOfMonth(date);
        getSummaryCalendar(CommonUtils.DateFormate(beginDate), CommonUtils.DateFormate(endDate));
    }


    /**
     * 获取日历控件里面计划记录标记的接口
     */
    @Background
    void getSummaryCalendar(String beginDate, String endDate)
    {
        SummaryCalendarResponse resp = new SummaryCalendarResponse();
        Log.e("Visit/MainFragment", "beginDate:" + beginDate + "endDate:" + endDate);
        resp = mRestClient.getSummaryCalendar(beginDate, endDate, Account.getInstance().getToken());
        HashMap<OverlayKey, ArrayList<Overlay>> overlays = new HashMap<OverlayKey, ArrayList<Overlay>>();
        if (!BaseResponse.hasErrorWithOperation(resp, mContext))
        {
            Calendar calendar = Calendar.getInstance();
            for (CalendarCellInfo item : resp.dateArrayList)
            {
                String dateStr = item.planDate;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                try
                {
                    date = sdf.parse(dateStr);
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
                calendar.setTime(date);


                if (item.tag == 1)//表示有计划或者带起之前拜访次数
                {
                    OverlayKey key = new OverlayKey(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                    ArrayList<Overlay> curArray = new ArrayList<>();
                    overlays.put(key, curArray);

                    Overlay overlay = new Overlay();
                    overlay.overlayDrawable = new VisitOverlayDrawable(mContext);
                    ((VisitOverlayDrawable) overlay.overlayDrawable).setItemInfo(item);
                    overlay.position = Overlay.POSITION_CENTER_HORIZONTAL | Overlay.POSITION_BOTTOM;

//                    if (item.time == 0)
//                    {
//                        //有计划
//                        overlay.overlayDrawable = mYellowPoint;
//                        overlay.position = Overlay.POSITION_CENTER_HORIZONTAL | Overlay.POSITION_BOTTOM;
//                    }
//                    else if (item.time > 0)
//                    {
//                        //有到期前次数
//                        overlay.overlayDrawable = mYellowStar;
//                        overlay.position = Overlay.POSITION_RIGHT | Overlay.POSITION_TOP;
//                    }
                    curArray.add(overlay);
                }
            }
        }
        onGetSummaryCalendarResult(resp, overlays);
    }

    @UiThread
    void onGetSummaryCalendarResult(SummaryCalendarResponse resp, HashMap<OverlayKey, ArrayList<Overlay>> overlays)
    {
        mCalendarAdapter.getItem(mCurrentCalendarCardIndex).setOverlays(overlays);
    }


    private class EndVisitReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction() == VisitService.ACTION_VISIT_ENDED && mSelectDate != null)
            {
                boolean success = intent.getBooleanExtra(VisitService.EXTRA_RESULT, false);
                if (success)
                {
                    refreshVisitList(mSelectDate);
                }
            }
        }
    }


    class VisitAdapter extends BaseAdapter
    {

        @Override
        public int getCount()
        {
            return mDataList == null ? 0 : mDataList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mDataList == null ? null : mDataList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = null;
            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.list_item_visit, null);

                holder.visitStatus = (TextView) convertView.findViewById(R.id.status);
                holder.importantLevel = (ImageView) convertView.findViewById(R.id.important_level);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.systemPlanFlagIcon = convertView.findViewById(R.id.system_plan_flag_icon_iv);
                holder.visitModelIcon = (ImageView) convertView.findViewById(R.id.visit_model_icon_iv);
                holder.visitTypeIcon = (ImageView) convertView.findViewById(R.id.visit_type_icon_iv);

                holder.visitModelText = (TextView) convertView.findViewById(R.id.visit_model);
                holder.visitTypeText = (TextView) convertView.findViewById(R.id.visit_type);
                holder.dividerLine = convertView.findViewById(R.id.divider_line);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            PlanRecordListInfo item = mDataList.get(position);
            holder.visitStatus.setText(PlanStatus.getPlanStatusStrRes(item.status));
            holder.visitStatus.setTextColor(getResources().getColor(PlanStatus.getPlanStatusColorRes(item.status)));
            holder.importantLevel.setImageResource(ImportantLevel.getImportantLevelImgRes(item.importantLevel));
            holder.name.setText(item.customerName);
            holder.time.setText(item.time.substring(0, 5));
            holder.systemPlanFlagIcon.setVisibility(item.is_admin == AdminPlan.IS_ADMIN ? View.VISIBLE : View.INVISIBLE);
            holder.visitModelIcon.setImageResource(VisitModel.getVisitModelImgRes(item.visitWay));
            holder.visitTypeIcon.setImageResource(VisitType.getVisitTypeImgRes(item.business));
            holder.visitModelText.setText(VisitModel.getVisitModelStrRes(item.visitWay));
            holder.visitTypeText.setText(VisitType.getVisitTypeStrRes(item.business));
            if (position == mDataList.size() - 1)
            {
                holder.dividerLine.setVisibility(View.INVISIBLE);
            }
            else
            {
                holder.dividerLine.setVisibility(View.VISIBLE);
            }


            return convertView;

        }

        class ViewHolder
        {
            TextView visitStatus;   //拜访计划状态
            ImageView importantLevel; //重要程度
            TextView name;    //客户名字
            TextView time;    //时间
            View systemPlanFlagIcon;   //标记系统计划icon
            ImageView visitModelIcon; //拜访方式类型：电话/见面
            ImageView visitTypeIcon;  //拜访计划类型：财务/物流/业务/其他
            TextView visitModelText; //拜访方式类型：电话/见面
            TextView visitTypeText;  //拜访计划类型：财务/物流/业务/其他
            View dividerLine;
        }
    }
}
