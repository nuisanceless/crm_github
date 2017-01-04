package com.drugoogle.sellscrm.visit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
@EFragment(R.layout.visit_main_fragment_bak)
public class MainFragmentBak extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "Visit_MainFragment";

    public static final int CONST_INITIAL_CURRENT_INDEX = 498;
    public static final int WEEK = 1;
    public static final int MONTH = 2;
    public static final int REQUEST_CODE_VISIT_PLAN_DETAIL = 14;

    private Context mContext;
    protected MyApplication mApp;
    protected ActivityHelper_ mActivityHelper;
    protected LayoutInflater mLayoutInflater;
    protected MyRestClient mRestClient;

    Drawable mYellowPoint;
    Drawable mYellowStar;




    @ViewById(R.id.custom_calendar_vp)
    ViewPager mCustomCalendarVp;
    @ViewById(R.id.custom_calendar_layout)
    View mCustomCalendarLayout;

    @ViewById(R.id.custom_week_vp)
    ViewPager mCustomWeekVp;
    @ViewById(R.id.custom_week_layout)
    View mCustomWeekLayout;

    @ViewById(R.id.current_month_number_tv)
    TextView mCurrentMonthNumberTv;
    @ViewById(R.id.current_year_number_tv)
    TextView mCurrentYearNumberTv;

    @ViewById(R.id.fold_calendar_layout)
    View mFoldCalendarLayout;
    @ViewById(R.id.fold_calendar)
    ImageView mFoldCalendarIv;

    @ViewById(R.id.list_view)
    ListView mListView;
    @ViewById(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @ViewById(R.id.empty_layout)
    View mEmptyLayout;






    private CalendarViewAdapter<CalendarView> calendarAdapter;
    private CalendarViewAdapter<WeekView> weekAdapter;
    private int mCurrentCalendarCardIndex = CONST_INITIAL_CURRENT_INDEX,
            mCurrentWeekCardIndex = CONST_INITIAL_CURRENT_INDEX;
    /**折叠月日历标记，默认为true*/
    private boolean mFoldCalendarFlag = true;
    private Date mSelectDate = new Date();
    public Date getSelectDate () {
        return mSelectDate;
    }



    private List<PlanRecordListInfo> mDataList;
    private VisitAdapter mAdapter;







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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume()
    {
        refreshVisitList(mSelectDate);
        getVisitSummaryDate();
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * 列表下拉刷新
     * */
    @Override
    public void onRefresh() {
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
            getVisitSummaryDate();
        }
        if (requestCode == REQUEST_CODE_VISIT_PLAN_DETAIL && resultCode == VisitPlanDetailActivity.RESULT_CODE_DELETE_PLAN)
        {
            refreshVisitList(mSelectDate);
            getVisitSummaryDate();
        }
    }











    @AfterViews
    void init ()
    {
        mContext = this.getContext();
        mApp = MyApplication_.getInstance();
        mActivityHelper = ActivityHelper_.getInstance_(mContext);
        mLayoutInflater = getActivity().getLayoutInflater();
        mRestClient = mApp.restClient();
        mDataList = new ArrayList<>();

        mYellowPoint = mContext.getResources().getDrawable(R.drawable.yellow_line);
        mYellowStar = mContext.getResources().getDrawable(R.drawable.icon_yellow_star);

        mFoldCalendarLayout.setOnClickListener(mFoldCalendarListener);


        WeekView[] weekViews = new WeekView[3];
        CalendarView[] cardViews = new CalendarView[3];
        for (int i = 0; i < 3; i++)
        {
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.WEEK_OF_YEAR, i - 1);
            weekViews[i] = new WeekView(mContext, mWeekViewListener, mSelectDate);
            cardViews[i] = new CalendarView(mContext, mCalendarViewListener);
        }
        //为了初始化一开始当周的前后两周的日期数字。
        weekViews[0].calculateDateNum(mSelectDate);
        weekViews[1].calculateDateNum(DateUtils.getNextWeek(mSelectDate));
        weekViews[2].calculateDateNum(DateUtils.getLastWeek(mSelectDate));


        weekAdapter = new CalendarViewAdapter<>(weekViews);
        mCustomWeekVp.setAdapter(weekAdapter);
        mCustomWeekVp.setCurrentItem(CONST_INITIAL_CURRENT_INDEX);
        mCustomWeekVp.addOnPageChangeListener(weekPageChangeListener);
        weekAdapter.getItem(CONST_INITIAL_CURRENT_INDEX).setSelectDayIndex(DateUtils.getWeekDay(mSelectDate));


        calendarAdapter = new CalendarViewAdapter<>(cardViews);
        mCustomCalendarVp.setAdapter(calendarAdapter);
        mCustomCalendarVp.setCurrentItem(CONST_INITIAL_CURRENT_INDEX);
        mCustomCalendarVp.addOnPageChangeListener(calendarPageChangeListener);
        calendarAdapter.getItem(CONST_INITIAL_CURRENT_INDEX).setSelectDayIndex(mSelectDate);

        initListView();
        getWeekVisitSummary();
        getMonthVisitSummary();
    }







    private float mDownX;
    private float mDownY;
    /**
     * 初始化list view
     * */
    private void initListView ()
    {
        mAdapter = new VisitAdapter();
        mListView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.yellow));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mDataList.get(position).visitRecordId > 0) {
                    VisitRecordDetailActivity_.intent(mContext).mRecordId(mDataList.get(position).visitRecordId).start();
                } else {
                    VisitPlanDetailActivity_.intent(mContext).mPlanId(mDataList.get(position).visitPlanId).startForResult(REQUEST_CODE_VISIT_PLAN_DETAIL);
                }
            }
        });
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = event.getX();
                        mDownY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float disX = event.getX() - mDownX;
                        float disY = event.getY() - mDownY;
                        if (disY < 0 && Math.abs(disX) * 2 < Math.abs(disY)) {
                            if (!mFoldCalendarFlag) {
                                showWeekCard();
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









    /**
     * 获取日历接口信息
     * */
    void getVisitSummaryDate()
    {
        if (mFoldCalendarFlag)
            getWeekVisitSummary();
        else
            getMonthVisitSummary();
    }






    /**
     * 获取日历控件里面计划记录标记的接口
     * */
    @Background
    void getSummaryCalendar (String beginDate, String endDate, int weekMonth)
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
                try {
                    date = sdf.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                calendar.setTime(date);


                if (item.tag == 1)//表示有计划或者带起之前拜访次数
                {
                    OverlayKey key = new OverlayKey(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                    ArrayList<Overlay> curArray = new ArrayList<>();
                    overlays.put(key, curArray);

                    Overlay overlay = new Overlay();
                    overlay.overlayDrawable = new VisitOverlayDrawable(getContext());
                    ((VisitOverlayDrawable)overlay.overlayDrawable).setItemInfo(item);
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
        onGetSummaryCalendarResult(resp, overlays, weekMonth);
    }

    @UiThread
    void onGetSummaryCalendarResult(SummaryCalendarResponse resp, HashMap<OverlayKey, ArrayList<Overlay>> overlays, int weekMonth)
    {
        if (weekMonth == WEEK) {
            weekAdapter.getItem(mCurrentWeekCardIndex).setOverlays(overlays);
        } else if (weekMonth == MONTH) {
            calendarAdapter.getItem(mCurrentCalendarCardIndex).setOverlays(overlays);
        }
    }







    private final int REFRESH_HANDLER_WHAT = 3;
    private int REFRESH_LIST_DELAY = 500;//ms
    private int flagId;
    Handler mRefreshHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_HANDLER_WHAT:
                    if (mEmptyLayout != null)
                        mEmptyLayout.setVisibility(View.GONE);
                    mDataList.clear();
                    mAdapter.notifyDataSetChanged();
                    getVisitList((Date) msg.obj);
                    break;
            }
        }
    };
    void refreshVisitList (Date date)
    {
        mRefreshHandler.removeMessages(REFRESH_HANDLER_WHAT);
        mSwipeRefreshLayout.setRefreshing(true);
        Message msg = new Message();
        msg.what = REFRESH_HANDLER_WHAT;
        msg.obj = date;
        mRefreshHandler.sendMessageDelayed(msg, REFRESH_LIST_DELAY);
    }
    @Background
    void getVisitList (Date date)
    {
        flagId++;
        VisitPlanListResponse resp = mRestClient.getVisitPlanList(CommonUtils.DateFormate(date), Account.getInstance().getToken(), flagId);
        afterGetVisitList(resp);
    }
    @UiThread
    void afterGetVisitList(VisitPlanListResponse resp)
    {
        if (mSwipeRefreshLayout == null) return;
        mSwipeRefreshLayout.setRefreshing(false);
        if (!BaseResponse.hasErrorWithOperation(resp, mContext))
        {
            if (resp.flagId != flagId) return;
            if (resp.dataList != null) {
                mDataList = resp.dataList;
            } else {
                mDataList = new ArrayList<>();
            }
        }
        mAdapter.notifyDataSetChanged();
        if (mDataList != null && mDataList.size() > 0) {
            mEmptyLayout.setVisibility(View.GONE);
        } else {
            mEmptyLayout.setVisibility(View.VISIBLE);
        }
    }
















    /**展开/折叠月日历按钮点击事件监听*/
    View.OnClickListener mFoldCalendarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mFoldCalendarFlag)
            {
                //TODO 更新月日历
                showCalendarCard();
            }
            else
            {
                //TODO 更新周日历
                showWeekCard();
            }
        }
    };




    /**月日历事件监听*/
    CalendarView.OnCalendarViewListener mCalendarViewListener = new CalendarView.OnCalendarViewListener() {
        @Override
        public void initCalendarCard(Date date) {
            setCurrentMonthTv(date);
        }

        @Override
        public void OnSelectDate(Date date) {
            mSelectDate = date;
            refreshVisitList(date);
            // TODO: 2016/4/19 更新周日历 更新周日历的选中日期
            weekAdapter.getItem(mCurrentWeekCardIndex).setWeek(date, mSelectDate);

            showWeekCard();
        }

        @Override
        public void slideCalendarCard(Date date)
        {
            setCurrentMonthTv(date);
        }

        @Override
        public void gestureUpglide()
        {
            showWeekCard();
        }
    };

    /**周日历事件监听*/
    WeekView.OnWeekViewListener mWeekViewListener = new WeekView.OnWeekViewListener() {
        @Override
        public void initWeekCard(Date date) {
            setCurrentMonthTv(date);
        }

        @Override
        public void OnSelectDate(Date date) {
            mSelectDate = date;
            refreshVisitList(date);
            setCurrentMonthTv(date);//周日历点击之后刷新上面的月份textView，月日历不需要
            // TODO: 2016/4/19 更新月日历 更新月日历的选中日期
            calendarAdapter.getItem(mCurrentCalendarCardIndex).setMonth(date, mSelectDate);
        }

        @Override
        public void slideWeekCard(Date date) {
            setCurrentMonthTv(date);
            mAfterSlideDate = date;
        }

        @Override
        public void gestureDownglide()
        {
            showCalendarCard();
        }
    };
    private Date mAfterSlideDate;

    /**月日历卡滑动监听*/
    ViewPager.OnPageChangeListener calendarPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            updateCalendarView(position);
            getVisitSummaryDate();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**周日历卡滑动监听*/
    ViewPager.OnPageChangeListener weekPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            updateWeekView(position);
            getVisitSummaryDate();
            Date date = mAfterSlideDate;
            weekAdapter.getItem(mCurrentWeekCardIndex).calculateDateNum(date);
            weekAdapter.getItem(mCurrentWeekCardIndex - 1).calculateDateNum(DateUtils.getLastWeek(date));
            weekAdapter.getItem(mCurrentWeekCardIndex + 1).calculateDateNum(DateUtils.getNextWeek(date));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };









    /**
     * 更新周日历视图
     * */
    private void updateWeekView(int position)
    {
        WeekView[] mShowViews = weekAdapter.getAllItems();
        WeekView wv = mShowViews[position % mShowViews.length];

        if (position > mCurrentWeekCardIndex)
        {
            Date date = wv.getWeek();
            wv.setWeek(DateUtils.getNextWeek(date), mSelectDate);
        }
        else if (position < mCurrentWeekCardIndex)
        {
            Date date = wv.getWeek();
            wv.setWeek(DateUtils.getLastWeek(date), mSelectDate);
        }
        mCurrentWeekCardIndex = position;
    }


    /**
     * 更新月历视图
     * */
    private void updateCalendarView(int position)
    {
        CalendarView[] mShowViews = calendarAdapter.getAllItems();
        CalendarView cv = mShowViews[position % mShowViews.length];

        Calendar c = cv.getCalendar();
        Date curDate = cv.getMonth();
        c.setTime(curDate);

        if (position > mCurrentCalendarCardIndex)
        {
            c.add(Calendar.MONTH, 1);
            cv.setMonth(c.getTime(), mSelectDate);
        }
        else if (position < mCurrentCalendarCardIndex)
        {
            c.add(Calendar.MONTH, -1);
            cv.setMonth(c.getTime(), mSelectDate);
        }
        mCurrentCalendarCardIndex = position;
    }





    /**
     * 给月份文本框更新内容
     * */
    private void setCurrentMonthTv (Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        mCurrentMonthNumberTv.setText((calendar.get(Calendar.MONTH) + 1) + "");
        mCurrentYearNumberTv.setText(calendar.get(Calendar.YEAR) + "年");
    }

    /**
     * 显示周日历
     * */
    private void showWeekCard ()
    {
        mCustomCalendarLayout.setVisibility(View.GONE);
        mCustomWeekLayout.setVisibility(View.VISIBLE);
        mFoldCalendarIv.setImageResource(R.drawable.blue_arrow_bottom);
        mFoldCalendarFlag = true;
        getWeekVisitSummary();
    }
    /**
     * 显示月日历
     * */
    private void showCalendarCard ()
    {
        mCustomCalendarLayout.setVisibility(View.VISIBLE);
        mCustomWeekLayout.setVisibility(View.GONE);
        mFoldCalendarIv.setImageResource(R.drawable.yellow_arrow_top);
        mFoldCalendarFlag = false;
        getMonthVisitSummary();
    }



    /**
     * 刷新周日历拜访概要
     * */
    private void getWeekVisitSummary()
    {
        Date date = weekAdapter.getItem(mCurrentWeekCardIndex).getWeek();
        Date beginDate = DateUtils.getFirstDayOfThisWeek(date);
        Date endDate = DateUtils.getEndDayOfThisWeek(date);
        getSummaryCalendar(CommonUtils.DateFormate(beginDate), CommonUtils.DateFormate(endDate), WEEK);
    }
    /**
     * 刷新月日历拜访概要
     * */
    private void getMonthVisitSummary()
    {
        Date date = calendarAdapter.getItem(mCurrentCalendarCardIndex).getMonth();
        Date beginDate = DateUtils.getFirstDayOfMonth(date);
        Date endDate = DateUtils.getEndDayOfMonth(date);
        getSummaryCalendar(CommonUtils.DateFormate(beginDate), CommonUtils.DateFormate(endDate), MONTH);
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
                convertView = mLayoutInflater.inflate(R.layout.list_item_visit, null);

                holder.visitStatus = (TextView)convertView.findViewById(R.id.status);
                holder.statusIcon = (ImageView)convertView.findViewById(R.id.status_icon_iv);
                holder.importantLevel = (ImageView)convertView.findViewById(R.id.important_level);
                holder.name = (TextView)convertView.findViewById(R.id.name);
                holder.time = (TextView)convertView.findViewById(R.id.time);
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
            holder.time.setText(item.time.substring(0, 5));
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
            View systemPlanFlagIcon;   //标记系统计划icon
            ImageView visitModelIcon; //拜访方式类型：电话/见面
            ImageView visitTypeIcon;  //拜访计划类型：财务/物流/业务/其他
            TextView visitModelText; //拜访方式类型：电话/见面
            TextView visitTypeText;  //拜访计划类型：财务/物流/业务/其他
        }
    }
}
