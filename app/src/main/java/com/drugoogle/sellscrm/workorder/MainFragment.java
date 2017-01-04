package com.drugoogle.sellscrm.workorder;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.common.ActivityHelper_;
import com.drugoogle.sellscrm.common.MyApplication;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.common.SwipeListView;
import com.drugoogle.sellscrm.common.SwipeListView.OnLoadMore;
import com.drugoogle.sellscrm.data.WorkOrderInfo;
import com.drugoogle.sellscrm.data.request.WorkOrderListRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.WorkOrderListResponse;
import com.drugoogle.sellscrm.data.type.WorkOrderStatus;
import com.drugoogle.sellscrm.data.type.WorkOrderType;
import com.drugoogle.sellscrm.rest.MyRestClient;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ydwang on 2016/4/5.
 */
@EFragment(R.layout.workorder_main_fragment)
public class MainFragment extends Fragment implements OnClickListener, OnLoadMore, OnRefreshListener {

    public static final int REQUEST_CODE_STATUS_CHANGE = 33;

    @ViewById(R.id.order_state_layout)
    View orderSateLayout;
    @ViewById(R.id.order_state_tv)
    TextView orderStateTv;
    @ViewById(R.id.order_state_im)
    ImageView orderStateIm;
    @ViewById(R.id.order_type_layout)
    View orderTypeLayout;
    @ViewById(R.id.order_type_tv)
    TextView orderTypeTv;
    @ViewById(R.id.order_type_im)
    ImageView orderTypeIm;
    @ViewById(R.id.swipe_index)
    SwipeRefreshLayout swipeLay;
    @ViewById(R.id.list_lv)
    SwipeListView listLv;
    @ViewById(R.id.empty_layout)
    View emptyLayout;

    Context context;
    MyRestClient mRestClient;
    MyApplication mApp;
    ActivityHelper_ mActivityHelper;
    private PopupWindow mPopStateWin;
    private PopupWindow mPopTypeWin;
    private WorkOrderAdapter adapter;
    private List<WorkOrderInfo> infos = new ArrayList<>();
    private int sortSelect;
    private int filterSelect;
    //正在加载footer
    private LinearLayout footerParent;
    private View footer;
    private TextView footerText;
    private ProgressBar progressBar;
    private LayoutInflater inflater;

    private String token;
    private WorkOrderListRequest request = new WorkOrderListRequest();
    private WorkOrderListResponse resp;
    private boolean isFirst = true;//是否第一次进入页面
    private boolean hasNewOrder = false;
    private boolean isViewExist = false;//view是否被初始化

    @AfterViews
    void init() {

        Account account = Account.getInstance();
        context = this.getContext();
        mApp = MyApplication_.getInstance();
        mRestClient = mApp.restClient();
        mActivityHelper = ActivityHelper_.getInstance_( context );

        //添加正在加载中footer
        inflater = LayoutInflater.from( context );
        footerParent = new LinearLayout( context );
        footer = inflater.inflate( R.layout.listview_footer_shade, null, false );
        footerText = (TextView) footer.findViewById( R.id.no_more_textView );
        progressBar = (ProgressBar)footer.findViewById( R.id.progress );
        footerParent.addView( footer );
        footerParent.setGravity( Gravity.CENTER );
        listLv.addFooterView( footerParent );
        footer.setVisibility( View.GONE );

        orderStateTv.setText( CommonUtils.GetStringArrayValue( R.array.order_state, sortSelect ) );
        orderTypeTv.setText( CommonUtils.GetStringArrayValue( R.array.order_type, filterSelect ) );
        orderSateLayout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderStateTv.setTextColor( getResources().getColor( R.color.font_yellow ) );
                orderStateIm.setImageResource( R.drawable.pull_after );
                showPopStateWindow();
            }
        } );
        orderTypeLayout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderTypeTv.setTextColor( getResources().getColor( R.color.font_yellow ) );
                orderTypeIm.setImageResource( R.drawable.pull_after );
                showPopFilterWindow();
            }
        } );

        if (listLv.getEmptyView() != null) {
            listLv.getEmptyView().setVisibility( View.GONE );
        }
        listLv.setLoadMoreListen( this );
        swipeLay.setOnRefreshListener( this );
        swipeLay.setColorSchemeColors( getResources().getColor( R.color.yellow ) );
        listLv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < infos.size()) {
                    WorkOrderInfo info = infos.get( position );
                    WorkOrderActivity_.intent( context )._WorkOrderId( info.id )
                            .startForResult( REQUEST_CODE_STATUS_CHANGE );
                    if (info.status != null) {
                        if (Integer.parseInt( info.status ) == WorkOrderStatus.NOT_VIEWED) {
                            Intent intent = new Intent( "com.drugoogle.sellcrm.workorder.STATUS_BROADCAST" );
                            context.sendBroadcast( intent );
                        }
                    }
                }
            }
        } );

        //先初始化页面数据
        request.page = 1;
        request.pageSize = 25;
        if (isFirst) {
            request.type = new int[]{WorkOrderType.TOUSU, WorkOrderType.JIANJU
                    , WorkOrderType.ZIXUN, WorkOrderType.BAOPI, WorkOrderType.WEIHU, WorkOrderType.DINGHUO};
            request.status = new int[]{WorkOrderStatus.NOT_VIEWED, WorkOrderStatus.WAIT_PROCESSING
                    , WorkOrderStatus.PROCESSING, WorkOrderStatus.FINISHED};
        }
        token = account.getToken();
        adapter = new WorkOrderAdapter( context, R.layout.workorder_item, infos );
        listLv.setAdapter( adapter );
        if (isViewExist)
            swipeLay.post( new Runnable() {
                @Override
                public void run() {
                    if (!isViewExist)return;
                    swipeLay.setRefreshing( true );
                }
            } );
        onRefresh();
    }

    /**
     * 初始化view 向服务器请求数据
     */
    @Background
    void initView(WorkOrderListRequest request, boolean refresh ) {

        resp = mRestClient.getWorkOrderList( request, token );
        afterInitView(  resp,refresh );
    }

    /**
     * 处理服务器返回的数据
     */
    @UiThread
    void afterInitView( WorkOrderListResponse resp, boolean refresh) {
        progressBar.setVisibility( View.GONE );
        if (!isViewExist)return;
        if (refresh)
            swipeLay.post( new Runnable() {
                @Override
                public void run() {
                    if (!isViewExist)return;
                    swipeLay.setRefreshing( false );
                }
            } );
        else
            swipeLay.setEnabled( true );
        if (!BaseResponse.hasErrorWithOperation( resp, context )) {
            //请求客户列表成功
            if (resp.code == BaseResponse.SUCCESS_CODE) {
                //请求客户列表成功
                isFirst = false;
                hasNewOrder = false;
                if (resp.dataList.size() == 0) {
                    listLv.setEmptyView( emptyLayout );
                    listLv.getEmptyView().setVisibility( View.VISIBLE );
                }
                if (refresh)
                    infos.clear();
                if (resp.dataList != null){
                    infos.addAll( resp.dataList );
                    for (int i = 0; i < resp.dataList.size(); i++) {
                        if (Integer.parseInt( resp.dataList.get( i ).status ) == WorkOrderStatus.NOT_VIEWED) {
                            hasNewOrder = true;
                        }
                    }
                }
                adapter.notifyDataSetChanged();

                if (request.page == resp.totalPage) {
                    footerText.setText( getString( R.string.no_more ) );
                    footer.setVisibility( View.VISIBLE );
                } else {
                    footerText.setText( getString( R.string.order_scan_more ) );
                    footer.setVisibility( View.VISIBLE );
                }
                if (hasNewOrder) {
                    Intent intent = new Intent( "com.drugoogle.sellcrm.workorder.STATUS_BROADCAST" );
                    intent.putExtra( "hasNew", true );
                    context.sendBroadcast( intent );
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
            swipeLay.setRefreshing( true );
            onRefresh();
        } else if (requestCode == REQUEST_CODE_STATUS_CHANGE && resultCode == WorkOrderActivity.RESULT_CODE_STATUS_CHANGE) {
            swipeLay.setRefreshing( true );
            onRefresh();
        }
    }

    /**
     * 显示工单状态菜单栏
     */
    void showPopStateWindow() {
        //设置contentView
        View contentView = LayoutInflater.from( context ).inflate( R.layout.workorder_pop_sate, null );
        mPopStateWin = new PopupWindow( contentView,
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
        mPopStateWin.setContentView( contentView );
        //实例化圆点imgeview
        ImageView imageView01 = (ImageView) contentView.findViewById( R.id.pop_state_all_im );
        ImageView imageView02 = (ImageView) contentView.findViewById( R.id.pop_state_default_im );
        ImageView imageView03 = (ImageView) contentView.findViewById( R.id.pop_state_wait_im );
        ImageView imageView04 = (ImageView) contentView.findViewById( R.id.pop_state_resolving_im );
        ImageView imageView05 = (ImageView) contentView.findViewById( R.id.pop_state_resolved_im );

        //设置各个控件的点击响应
        RelativeLayout RelativeLayout01 = (RelativeLayout) contentView.findViewById( R.id.pop_state_all_lay );
        RelativeLayout RelativeLayout02 = (RelativeLayout) contentView.findViewById( R.id.pop_state_default_lay );
        RelativeLayout RelativeLayout03 = (RelativeLayout) contentView.findViewById( R.id.pop_state_wait_lay );
        RelativeLayout RelativeLayout04 = (RelativeLayout) contentView.findViewById( R.id.pop_state_resolving_lay );
        RelativeLayout RelativeLayout05 = (RelativeLayout) contentView.findViewById( R.id.pop_state_resolved_lay );
        RelativeLayout01.setOnClickListener( this );
        RelativeLayout02.setOnClickListener( this );
        RelativeLayout03.setOnClickListener( this );
        RelativeLayout04.setOnClickListener( this );
        RelativeLayout05.setOnClickListener( this );

        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add( imageView01 );
        imageViews.add( imageView02 );
        imageViews.add( imageView03 );
        imageViews.add( imageView04 );
        imageViews.add( imageView05 );
        List<RelativeLayout> layouts = new ArrayList<>();
        layouts.add( RelativeLayout01 );
        layouts.add( RelativeLayout02 );
        layouts.add( RelativeLayout03 );
        layouts.add( RelativeLayout04 );
        layouts.add( RelativeLayout05 );

        for (int i = 0; i < imageViews.size(); i++) {
            imageViews.get( i ).setVisibility( View.GONE );
            layouts.get( i ).setBackgroundColor( 0 );
        }
        imageViews.get( sortSelect ).setVisibility( View.VISIBLE );
        layouts.get( sortSelect ).setBackgroundColor( getResources().getColor( R.color.light_blue ) );
        //设置点击空白消失
        mPopStateWin.setBackgroundDrawable( new BitmapDrawable() );
        mPopStateWin.setOutsideTouchable( true );
        //显示PopupWindow
        mPopStateWin.showAsDropDown( orderSateLayout );
        //设置PopupWindow消失时的监听
        mPopStateWin.setOnDismissListener( new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                orderStateTv.setTextColor( getResources().getColor( R.color.yellowButton ) );
                orderStateIm.setImageResource( R.drawable.pull_before );
            }
        } );
    }

    /**
     * 显示工单类型菜单栏
     */
    void showPopFilterWindow() {
        //设置contentView
        View contentView = LayoutInflater.from( context ).inflate( R.layout.workorder_pop_type, null );
        mPopTypeWin = new PopupWindow( contentView,
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true );
        mPopTypeWin.setContentView( contentView );
        //实例化圆点imageView
        ImageView imageView10 = (ImageView) contentView.findViewById( R.id.pop_type_default_im );
        ImageView imageView11 = (ImageView) contentView.findViewById( R.id.pop_type_tousu_im );
        ImageView imageView12 = (ImageView) contentView.findViewById( R.id.pop_type_jianju_im );
        ImageView imageView13 = (ImageView) contentView.findViewById( R.id.pop_type_zixun_im );
        ImageView imageView14 = (ImageView) contentView.findViewById( R.id.pop_type_baopi_im );
        ImageView imageView15 = (ImageView) contentView.findViewById( R.id.pop_type_weihu_im );
        ImageView imageView16 = (ImageView) contentView.findViewById( R.id.pop_type_dinghuo_im );


        //设置各个控件的点击响应
        RelativeLayout RelativeLayout10 = (RelativeLayout) contentView.findViewById( R.id.pop_type_default_lay );
        RelativeLayout RelativeLayout11 = (RelativeLayout) contentView.findViewById( R.id.pop_type_tousu_lay );
        RelativeLayout RelativeLayout12 = (RelativeLayout) contentView.findViewById( R.id.pop_type_jianju_lay );
        RelativeLayout RelativeLayout13 = (RelativeLayout) contentView.findViewById( R.id.pop_type_zixun_lay );
        RelativeLayout RelativeLayout14 = (RelativeLayout) contentView.findViewById( R.id.pop_type_baopi_lay );
        RelativeLayout RelativeLayout15 = (RelativeLayout) contentView.findViewById( R.id.pop_type_weihu_lay );
        RelativeLayout RelativeLayout16 = (RelativeLayout) contentView.findViewById( R.id.pop_type_dinghuo_lay );
        RelativeLayout10.setOnClickListener( this );
        RelativeLayout11.setOnClickListener( this );
        RelativeLayout12.setOnClickListener( this );
        RelativeLayout13.setOnClickListener( this );
        RelativeLayout14.setOnClickListener( this );
        RelativeLayout15.setOnClickListener( this );
        RelativeLayout16.setOnClickListener( this );

        //显示上次选中的排序方式
        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add( imageView10 );
        imageViews.add( imageView11 );
        imageViews.add( imageView12 );
        imageViews.add( imageView13 );
        imageViews.add( imageView14 );
        imageViews.add( imageView15 );
        imageViews.add( imageView16 );
        List<RelativeLayout> layouts = new ArrayList<>();
        layouts.add( RelativeLayout10 );
        layouts.add( RelativeLayout11 );
        layouts.add( RelativeLayout12 );
        layouts.add( RelativeLayout13 );
        layouts.add( RelativeLayout14 );
        layouts.add( RelativeLayout15 );
        layouts.add( RelativeLayout16 );

        for (int i = 0; i < imageViews.size(); i++) {
            imageViews.get( i ).setVisibility( View.GONE );
            layouts.get( i ).setBackgroundColor( 0 );
        }
        imageViews.get( filterSelect ).setVisibility( View.VISIBLE );
        layouts.get( filterSelect ).setBackgroundColor( getResources().getColor( R.color.light_blue ) );
        //设置点击空白消失
        mPopTypeWin.setBackgroundDrawable( new BitmapDrawable() );
        mPopTypeWin.setOutsideTouchable( true );
        //显示PopupWindow
        mPopTypeWin.showAsDropDown( orderTypeLayout );
        //设置PopupWindow消失时的监听
        mPopTypeWin.setOnDismissListener( new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                orderTypeTv.setTextColor( getResources().getColor( R.color.yellowButton ) );
                orderTypeIm.setImageResource( R.drawable.pull_before );
            }
        } );
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.pop_state_all_lay: {
                orderStateTv.setText( WorkOrderStatus.getWorkOrderStatus( WorkOrderStatus.All ) );
                request.status = new int[]{WorkOrderStatus.NOT_VIEWED, WorkOrderStatus.WAIT_PROCESSING
                        , WorkOrderStatus.PROCESSING, WorkOrderStatus.FINISHED};
                sortSelect = 0;
                mPopStateWin.dismiss();
            }
            break;
            case R.id.pop_state_default_lay: {
                orderStateTv.setText( WorkOrderStatus.getWorkOrderStatus( WorkOrderStatus.NOT_VIEWED ) );
                request.status = new int[]{WorkOrderStatus.NOT_VIEWED};
                sortSelect = 1;
                mPopStateWin.dismiss();
            }
            break;
            case R.id.pop_state_wait_lay: {
                orderStateTv.setText( WorkOrderStatus.getWorkOrderStatus( WorkOrderStatus.WAIT_PROCESSING ) );
                request.status = new int[]{WorkOrderStatus.WAIT_PROCESSING};
                sortSelect = 2;
                mPopStateWin.dismiss();
            }
            break;
            case R.id.pop_state_resolving_lay: {
                orderStateTv.setText( WorkOrderStatus.getWorkOrderStatus( WorkOrderStatus.PROCESSING ) );
                request.status = new int[]{WorkOrderStatus.PROCESSING};
                sortSelect = 3;
                mPopStateWin.dismiss();
            }
            break;
            case R.id.pop_state_resolved_lay: {
                orderStateTv.setText( WorkOrderStatus.getWorkOrderStatus( WorkOrderStatus.FINISHED ) );
                request.status = new int[]{WorkOrderStatus.FINISHED};
                sortSelect = 4;
                mPopStateWin.dismiss();
            }
            break;
            case R.id.pop_type_default_lay: {
                orderTypeTv.setText( WorkOrderType.getWorkOrderType( WorkOrderType.ALL ) );
                request.type = new int[]{WorkOrderType.TOUSU, WorkOrderType.JIANJU
                        , WorkOrderType.ZIXUN, WorkOrderType.BAOPI, WorkOrderType.WEIHU, WorkOrderType.DINGHUO};
                filterSelect = 0;
                mPopTypeWin.dismiss();
            }
            break;
            case R.id.pop_type_tousu_lay: {
                orderTypeTv.setText( WorkOrderType.getWorkOrderType( WorkOrderType.TOUSU ) );
                request.type = new int[]{WorkOrderType.TOUSU};
                filterSelect = 1;
                mPopTypeWin.dismiss();
            }
            break;
            case R.id.pop_type_jianju_lay: {
                orderTypeTv.setText( WorkOrderType.getWorkOrderType( WorkOrderType.JIANJU ) );
                request.type = new int[]{WorkOrderType.JIANJU};
                filterSelect = 2;
                mPopTypeWin.dismiss();
            }
            break;
            case R.id.pop_type_zixun_lay: {
                orderTypeTv.setText( WorkOrderType.getWorkOrderType( WorkOrderType.ZIXUN ) );
                request.type = new int[]{WorkOrderType.ZIXUN};
                filterSelect = 3;
                mPopTypeWin.dismiss();
            }
            break;
            case R.id.pop_type_baopi_lay: {
                orderTypeTv.setText( WorkOrderType.getWorkOrderType( WorkOrderType.BAOPI ) );
                request.type = new int[]{WorkOrderType.BAOPI};
                filterSelect = 4;
                mPopTypeWin.dismiss();
            }
            break;
            case R.id.pop_type_weihu_lay: {
                orderTypeTv.setText( WorkOrderType.getWorkOrderType( WorkOrderType.WEIHU ) );
                request.type = new int[]{WorkOrderType.WEIHU};
                filterSelect = 5;
                mPopTypeWin.dismiss();
            }
            break;
            case R.id.pop_type_dinghuo_lay: {
                orderTypeTv.setText( WorkOrderType.getWorkOrderType( WorkOrderType.DINGHUO ) );
                request.type = new int[]{WorkOrderType.DINGHUO};
                filterSelect = 6;
                mPopTypeWin.dismiss();
            }
            default:
                break;
        }
        swipeLay.setRefreshing( true );
        onRefresh();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView( inflater, container, savedInstanceState );
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        isViewExist = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewExist = false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 加载更多的方法
     */
    @Override
    public void loadMore() {
        if (!swipeLay.isRefreshing()) {
            //刷新动画结束时
            request.page++;
            swipeLay.setRefreshing( false );
            if (request.page <= resp.totalPage) {
                progressBar.setVisibility( View.VISIBLE );
                //原数据后面加载新的数据
                initView(  request,false );
            }
        }
        listLv.onLoadComplete();
    }

    /**
     * 下拉刷新的方法
     */
    @Override
    public void onRefresh() {
        request.page = 1;
        //重新加载list
        if (listLv.getEmptyView() != null) {
            listLv.getEmptyView().setVisibility( View.GONE );
        }
        initView(  request ,true);
    }

}
