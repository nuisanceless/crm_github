package com.drugoogle.sellscrm.customer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.ActivityHelper_;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.MyApplication;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.common.SwipeListView;
import com.drugoogle.sellscrm.common.SwipeListView.OnLoadMore;
import com.drugoogle.sellscrm.data.CustomerInfoItem;
import com.drugoogle.sellscrm.data.request.ShowCustomerListRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.ShowCustomerListResponse;
import com.drugoogle.sellscrm.data.type.CustomerLevel;
import com.drugoogle.sellscrm.data.type.CustomnerSortBy;
import com.drugoogle.sellscrm.rest.MyRestClient;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ydwang on 2016/5/6.
 */
@EActivity(R.layout.activity_customer_search_result)
public class SearchResultActivity extends BaseActivity implements OnClickListener, OnLoadMore, OnRefreshListener {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.customer_sorttype_layout)
    View custSortTypeLay;
    @ViewById(R.id.customer_sorttype_tv)
    TextView custSortTypeTv;
    @ViewById(R.id.customer_sorttype_im)
    ImageView custSortTypeIm;
    @ViewById(R.id.customer_level_layout)
    View custLevelLay;
    @ViewById(R.id.customer_level_tv)
    TextView custLevelTv;
    @ViewById(R.id.customer_level_im)
    ImageView custLevelIm;
    @ViewById(R.id.customer_swipe_index)
    SwipeRefreshLayout swipeLay;
    @ViewById(R.id.customer_list_lv)
    SwipeListView custListLv;
    @ViewById(R.id.empty_layout)
    View emptyLayout;

    Context context;
    Account account;
    MyRestClient mRestClient;
    MyApplication mApp;
    ActivityHelper_ mActivityHelper;
    private PopupWindow mPopSortTypeWin;
    private PopupWindow mPopLevelWin;
    private CustomerAdapter adapter;
    private ShowCustomerListResponse resp;
    private List<CustomerInfoItem> customerInfos = new ArrayList<>();
    private int sortSelect;
    private int levelSelect;

    //正在加载footer
    private LinearLayout footerParent;
    private View footer;
    private TextView footerText;
    private ProgressBar progressBar;
    private LayoutInflater inflater;

    //请求客户列表参数
    private ShowCustomerListRequest request = new ShowCustomerListRequest();
    private int pageNumber;
    private int pageSize;
    private String nameOrNum;
    private int sort;
    private int condition;
    private double longitude;
    private double latitude;
    private String token;
    private LocationClient locationClient;

    @AfterViews
    void init() {

        account = Account.getInstance();
        context = this;
        mApp = MyApplication_.getInstance();
        mRestClient = mApp.restClient();
        mActivityHelper = ActivityHelper_.getInstance_(context);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //添加正在加载中footer
        inflater = LayoutInflater.from(context);
        footerParent = new LinearLayout(context);
        footer = inflater.inflate(R.layout.listview_footer_shade, null, false);
        footerText = (TextView) footer.findViewById(R.id.no_more_textView);
        progressBar = (ProgressBar) footer.findViewById(R.id.progress);
        footerParent.addView(footer);
        footerParent.setGravity(Gravity.CENTER);
        custListLv.addFooterView(footerParent);
        footer.setVisibility(View.GONE);

        custSortTypeTv.setText(R.string.sort_first_letter);
        custLevelTv.setText(R.string.level_all);
        custSortTypeLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                custSortTypeTv.setTextColor(getResources().getColor(R.color.font_yellow));
                custSortTypeIm.setImageResource(R.drawable.pull_after);
                showPopSortWindow();
            }
        });
        custLevelLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                custLevelTv.setTextColor(getResources().getColor(R.color.font_yellow));
                custLevelIm.setImageResource(R.drawable.pull_after);
                showPopLevelWindow();
            }
        });

        if (custListLv.getEmptyView() != null) {
            custListLv.getEmptyView().setVisibility(View.GONE);
        }
        custListLv.setLoadMoreListen(this);
        swipeLay.setOnRefreshListener(this);
        swipeLay.setColorSchemeColors(getResources().getColor(R.color.yellow));
        custListLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position < customerInfos.size()) {
                    CustomerInfoItem customerInfoItem = customerInfos.get(position);
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id", customerInfoItem.id);
                    intent.setClass(context, CustomerDetailsActivity_.class);
                    startActivity(intent);
                }
            }
        });

        //先初始化页面数据
        getLocation();
        request.pageNumber = 1;
        request.pageSize = 50;
        request.nameOrNum = getIntent().getStringExtra(SearchManager.QUERY);//搜索条件
        request.sort = CustomnerSortBy.CUSTOMER_SORTTYPE_DEFAULT;//默认按照名字排序
        request.token = account.getToken();
        adapter = new CustomerAdapter(context, R.layout.list_item_customer, customerInfos);
        custListLv.setAdapter(adapter);
        swipeLay.post(new Runnable() {
            @Override
            public void run() {
                swipeLay.setRefreshing(true);
            }
        });
        onRefresh();
    }


    /**
     * d
     * 初始化view 向服务器请求数据
     */
    @Background
    void initView(ShowCustomerListRequest request, boolean refresh) {

        pageNumber = request.pageNumber;//页码
        pageSize = request.pageSize;//每页25条数据
        nameOrNum = request.nameOrNum;
        sort = request.sort;//按照首字母排序
        condition = request.condition;
        longitude = request.longitude;
        latitude = request.latitude;
        token = request.token;
        // 添加处理工单的api
        resp = mRestClient.getClientList(pageNumber, pageSize, nameOrNum, sort, condition, longitude, latitude, token);
        afterInitView(resp, refresh);
    }

    /**
     * 处理服务器返回的数据
     */
    @UiThread
    void afterInitView(ShowCustomerListResponse resp, boolean refresh) {
        progressBar.setVisibility(View.GONE);
        if (refresh)
            swipeLay.post(new Runnable() {
                @Override
                public void run() {
                    swipeLay.setRefreshing(false);
                }
            });
        else
            swipeLay.setEnabled(true);
        if (!BaseResponse.hasErrorWithOperation(resp, context)) {
            if (resp.ccCustomerList.size() == 0) {
                custListLv.setEmptyView(emptyLayout);
                custListLv.getEmptyView().setVisibility(View.VISIBLE);
            }
            if (request.pageNumber == resp.totalPage) {
                footerText.setText(getString(R.string.no_more));
                footer.setVisibility(View.VISIBLE);
            } else {
                footerText.setText(getString(R.string.order_scan_more));
                footer.setVisibility(View.VISIBLE);
            }
            //向列表中添加新的数据
            if (refresh)
                customerInfos.clear();
            if (resp.ccCustomerList != null)
                customerInfos.addAll(resp.ccCustomerList);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BaseResponse.REQUEST_CODE_EXPIRED && resultCode == BaseResponse.RESULT_CODE_EXPIRED) {
            request.token = Account.getInstance().getToken();
            //登录后返回当前页面并重新加载数据
            //初始化页面数据
            swipeLay.setRefreshing(true);
            onRefresh();
        }
    }

    /**
     * 显示工单状态菜单栏
     */
    void showPopSortWindow() {
        //设置contentView
        View contentView = LayoutInflater.from(context).inflate(R.layout.customer_pop_sort, null);
        mPopSortTypeWin = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopSortTypeWin.setContentView(contentView);

        //设置各个控件的点击响应
        RelativeLayout RelativeLayout01 = (RelativeLayout) contentView.findViewById(R.id.pop_sort_firstLetter_lay);
        RelativeLayout RelativeLayout02 = (RelativeLayout) contentView.findViewById(R.id.pop_sort_level_lay);
        RelativeLayout RelativeLayout03 = (RelativeLayout) contentView.findViewById(R.id.pop_sort_nearest_lay);
        RelativeLayout RelativeLayout04 = (RelativeLayout) contentView.findViewById(R.id.pop_sort_latest_lay);
        RelativeLayout RelativeLayout05 = (RelativeLayout) contentView.findViewById(R.id.pop_sort_times_lay);
        RelativeLayout01.setOnClickListener(this);
        RelativeLayout02.setOnClickListener(this);
        RelativeLayout03.setOnClickListener(this);
        RelativeLayout04.setOnClickListener(this);
        RelativeLayout05.setOnClickListener(this);

        //实例化圆点imgeview
        ImageView imageView01 = (ImageView) contentView.findViewById(R.id.pop_sort_firstLetter_im);
        ImageView imageView02 = (ImageView) contentView.findViewById(R.id.pop_sort_level_im);
        ImageView imageView03 = (ImageView) contentView.findViewById(R.id.pop_sort_nearest_im);
        ImageView imageView04 = (ImageView) contentView.findViewById(R.id.pop_sort_latest_im);
        ImageView imageView05 = (ImageView) contentView.findViewById(R.id.pop_sort_times_im);

        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add(imageView01);
        imageViews.add(imageView02);
        imageViews.add(imageView03);
        imageViews.add(imageView04);
        imageViews.add(imageView05);
        List<RelativeLayout> layouts = new ArrayList<>();
        layouts.add(RelativeLayout01);
        layouts.add(RelativeLayout02);
        layouts.add(RelativeLayout03);
        layouts.add(RelativeLayout04);
        layouts.add(RelativeLayout05);

        for (int i = 0; i < imageViews.size(); i++) {
            imageViews.get(i).setVisibility(View.GONE);
            layouts.get(i).setBackgroundColor(0);
        }
        imageViews.get(sortSelect).setVisibility(View.VISIBLE);
        layouts.get(sortSelect).setBackgroundColor(getResources().getColor(R.color.light_blue));

        //设置点击空白消失
        mPopSortTypeWin.setBackgroundDrawable(new BitmapDrawable());
        mPopSortTypeWin.setOutsideTouchable(true);
        //显示PopupWindow
        mPopSortTypeWin.showAsDropDown(custSortTypeLay);
        //设置PopupWindow消失时的监听
        mPopSortTypeWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                custSortTypeTv.setTextColor(getResources().getColor(R.color.yellowButton));
                custSortTypeIm.setImageResource(R.drawable.pull_before);
            }
        });
    }

    /**
     * 显示工单类型菜单栏
     */
    void showPopLevelWindow() {
        //设置contentView
        View contentView = LayoutInflater.from(context).inflate(R.layout.customer_pop_level, null);
        mPopLevelWin = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopLevelWin.setContentView(contentView);

        //设置各个控件的点击响应
        RelativeLayout RelativeLayout11 = (RelativeLayout) contentView.findViewById(R.id.pop_level_all_lay);
        RelativeLayout RelativeLayout12 = (RelativeLayout) contentView.findViewById(R.id.pop_level_high_lay);
        RelativeLayout RelativeLayout13 = (RelativeLayout) contentView.findViewById(R.id.pop_level_middle_lay);
        RelativeLayout RelativeLayout14 = (RelativeLayout) contentView.findViewById(R.id.pop_level_low_lay);
        RelativeLayout11.setOnClickListener(this);
        RelativeLayout12.setOnClickListener(this);
        RelativeLayout13.setOnClickListener(this);
        RelativeLayout14.setOnClickListener(this);

        //实例化圆点imageView
        ImageView imageView11 = (ImageView) contentView.findViewById(R.id.pop_level_all_im);
        ImageView imageView12 = (ImageView) contentView.findViewById(R.id.pop_level_high_im);
        ImageView imageView13 = (ImageView) contentView.findViewById(R.id.pop_level_middle_im);
        ImageView imageView14 = (ImageView) contentView.findViewById(R.id.pop_level_low_im);

        //显示上次选中的排序方式
        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add(imageView11);
        imageViews.add(imageView12);
        imageViews.add(imageView13);
        imageViews.add(imageView14);
        List<RelativeLayout> layouts = new ArrayList<>();
        layouts.add(RelativeLayout11);
        layouts.add(RelativeLayout12);
        layouts.add(RelativeLayout13);
        layouts.add(RelativeLayout14);

        for (int i = 0; i < imageViews.size(); i++) {
            imageViews.get(i).setVisibility(View.GONE);
            layouts.get(i).setBackgroundColor(0);
        }
        imageViews.get(levelSelect).setVisibility(View.VISIBLE);
        layouts.get(levelSelect).setBackgroundColor(getResources().getColor(R.color.light_blue));

        //设置点击空白消失
        mPopLevelWin.setBackgroundDrawable(new BitmapDrawable());
        mPopLevelWin.setOutsideTouchable(true);
        //显示PopupWindow
        mPopLevelWin.showAsDropDown(custLevelLay);
        //设置PopupWindow消失时的监听
        mPopLevelWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                custLevelTv.setTextColor(getResources().getColor(R.color.yellowButton));
                custLevelIm.setImageResource(R.drawable.pull_before);
            }
        });
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.pop_sort_firstLetter_lay: {
                custSortTypeTv.setText(R.string.sort_first_letter);
                sortSelect = 0;
                request.sort = CustomnerSortBy.CUSTOMER_SORTTYPE_DEFAULT;
                mPopSortTypeWin.dismiss();
            }
            break;
            case R.id.pop_sort_level_lay: {
                custSortTypeTv.setText(R.string.sort_level);
                sortSelect = 1;
                request.sort = CustomnerSortBy.CUSTOMER_SORTTYPE_LEVEL;
                mPopSortTypeWin.dismiss();
            }
            break;
            case R.id.pop_sort_nearest_lay: {
                custSortTypeTv.setText(R.string.sort_nearest);
                sortSelect = 2;
                request.sort = CustomnerSortBy.CUSTOMER_SORTTYPE_NEAREST;
                mPopSortTypeWin.dismiss();
            }
            break;
            case R.id.pop_sort_latest_lay: {
                custSortTypeTv.setText(R.string.sort_latest);
                sortSelect = 3;
                request.sort = CustomnerSortBy.CUSTOMER_SORTTYPE_LATEST;
                mPopSortTypeWin.dismiss();
                getLocation();
            }
            break;
            case R.id.pop_sort_times_lay: {
                custSortTypeTv.setText(R.string.sort_times);
                sortSelect = 4;
                request.sort = CustomnerSortBy.CUSTOMER_SORTTYPE_TIMES;
                mPopSortTypeWin.dismiss();
            }
            break;
            case R.id.pop_level_all_lay: {
                custLevelTv.setText(R.string.level_all);
                levelSelect = 0;
                request.condition = CustomerLevel.CUSTOMER_LEVEL_DEFAULT;
                mPopLevelWin.dismiss();
            }
            break;
            case R.id.pop_level_high_lay: {
                custLevelTv.setText(R.string.level_high);
                levelSelect = 1;
                request.condition = CustomerLevel.CUSTOMER_LEVEL_HIGH;
                mPopLevelWin.dismiss();
            }
            break;
            case R.id.pop_level_middle_lay: {
                custLevelTv.setText(R.string.level_middle);
                levelSelect = 2;
                request.condition = CustomerLevel.CUSTOMER_LEVEL_MIDDLE;
                mPopLevelWin.dismiss();
            }
            break;
            case R.id.pop_level_low_lay: {
                custLevelTv.setText(R.string.level_low);
                levelSelect = 3;
                request.condition = CustomerLevel.CUSTOMER_LEVEL_LOW;
                mPopLevelWin.dismiss();
            }
            default:
                break;
        }
        //初始化页面数据
        swipeLay.setRefreshing(true);
        onRefresh();
    }

    @Override
    public void loadMore() {
        if (!swipeLay.isRefreshing()) {
            //刷新动画结束时
            swipeLay.setRefreshing(false);
            request.pageNumber++;
            if (request.pageNumber <= resp.totalPage) {
                progressBar.setVisibility(View.GONE);
                //原数据后面加载新的数据
                initView(request, false);
            }
        }
        custListLv.onLoadComplete();
    }

    @Override
    public void onRefresh() {
        request.pageNumber = 1;
        //重新加载list
        if (custListLv.getEmptyView() != null) {
            custListLv.getEmptyView().setVisibility(View.GONE);
        }
        initView(request, true);
    }

    private void getLocation() {
        locationClient = new LocationClient(context);
        //设置定位条件
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(false);        //是否打开GPS
        option.setCoorType("bd09ll");       //设置返回值的坐标类型。
        option.setPriority(LocationClientOption.NetWorkFirst);  //设置定位优先级
        option.setProdName(""); //设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        option.setScanSpan(0);    //设置定时定位的时间间隔。单位毫秒
        locationClient.setLocOption(option);

        if (locationClient != null && !locationClient.isStarted()) {
            locationClient.start();
            locationClient.requestLocation();
        }
        //注册位置监听器
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation != null) {
                    //不为空,显示地理位置经纬度
                    request.longitude = bdLocation.getLongitude();
                    request.latitude = bdLocation.getLatitude();
                    if (locationClient != null) {
                        locationClient.stop();
                        locationClient = null;
                    }
                }
            }
        });
    }
}
