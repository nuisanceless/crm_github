package com.drugoogle.sellscrm.customer;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.Consts;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.common.SwipeListView;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.SaleRecordItem;
import com.drugoogle.sellscrm.data.request.SaleRecordListRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.SaleRecordListResponse;
import com.drugoogle.sellscrm.data.type.SaleRecordStatus;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ydwang on 2016/5/24.
 */
@EActivity(R.layout.activity_sale_record_list)
public class SaleRecordListActivity extends BaseActivity implements SwipeListView.OnLoadMore
        , SwipeRefreshLayout.OnRefreshListener {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.swipe_index)
    SwipeRefreshLayout swipeLay;
    @ViewById(R.id.list_lv)
    SwipeListView listLv;
    @Extra
    public CustomerInfo _CustomerInfo;

    private Account account;
    private Context context;
    private LayoutInflater inflater;
    private LinearLayout footerParent;
    private View footer;
    private TextView footerText;
    private ProgressBar progressBar;
    private SaleRecordListAdapter adapter;
    private List<SaleRecordItem> infos = new ArrayList<>();
    private SaleRecordListRequest request;
    private SaleRecordListResponse resp;
    private String token;

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
        ((TextView) toolbar.findViewById( R.id.title )).setText( _CustomerInfo.NAME );

        //添加正在加载中footer
        inflater = LayoutInflater.from( context );
        footerParent = new LinearLayout( context );
        footer = inflater.inflate( R.layout.listview_footer_without_shade, null, false );
        footerText = (TextView) footer.findViewById( R.id.no_more_textView );
        progressBar = (ProgressBar)footer.findViewById( R.id.progress );
        footerParent.addView( footer );
        footerParent.setGravity( Gravity.CENTER );
        listLv.addFooterView( footerParent );
        footer.setVisibility( View.GONE );

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
                    SaleRecordItem item = infos.get( position );
                    SaleRecordActivity_.intent( context )._sellOrderCode( item.sell_order_id ).start();
                }
            }
        } );

        request = new SaleRecordListRequest();
        request.erpCode = _CustomerInfo.ERP_CODE;
        request.page = 1;
        request.pageSize = 15;
        token = account.getToken();
        adapter = new SaleRecordListAdapter( context, R.layout.list_item_sale_record, infos );
        listLv.setAdapter( adapter );
        swipeLay.post( new Runnable() {
            @Override
            public void run() {
                swipeLay.setRefreshing( true );
            }
        } );
        onRefresh();
    }

    @Background
    void initView(SaleRecordListRequest request, boolean refresh) {

        resp = mRestClient.getSaleRecordList( request, token );
        afterInitView( resp, refresh );
    }

    @UiThread
    void afterInitView(SaleRecordListResponse resp, boolean refresh) {
        progressBar.setVisibility( View.GONE );
        if (refresh)
            swipeLay.post( new Runnable() {
                @Override
                public void run() {
                    swipeLay.setRefreshing( false );
                }
            } );
        else
            swipeLay.setEnabled( true );
        if (!BaseResponse.hasErrorWithOperation( resp, context )) {
            if (resp.code == BaseResponse.SUCCESS_CODE) {
                if (resp.dataList.size() == 0) {
                    listLv.setEmptyView( findViewById( R.id.empty_layout ) );
                    listLv.getEmptyView().setVisibility( View.VISIBLE );
                }
                if (request.page == resp.totalPage) {
                    footerText.setText( getString( R.string.no_more ) );
                    footer.setVisibility( View.VISIBLE );
                } else {
                    footerText.setText( getString( R.string.order_scan_more ) );
                    footer.setVisibility( View.VISIBLE );
                }
                //向列表中添加新的数据
                if (refresh)
                    infos.clear();
                if (resp.dataList != null)
                    infos.addAll( resp.dataList );
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void loadMore() {
        if (!swipeLay.isRefreshing()) {
            //刷新动画结束时
            swipeLay.setEnabled( false );
            request.page++;
            if (request.page <= resp.totalPage) {
                progressBar.setVisibility( View.VISIBLE );
                //原数据后面加载新的数据
                initView( request, false );
            }
        }
        listLv.onLoadComplete();
    }

    @Override
    public void onRefresh() {
        request.page = 1;
        //重新加载list
        if (listLv.getEmptyView() != null) {
            listLv.getEmptyView().setVisibility( View.GONE );
        }
        initView( request, true );

    }

    public class SaleRecordListAdapter extends ArrayAdapter<SaleRecordItem> {
        private int resourceId;

        public SaleRecordListAdapter(Context context, int textViewResourceId, List<SaleRecordItem> objects) {
            super( context, textViewResourceId, objects );
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SaleRecordItem item = getItem( position ); // 获取当前项的实例
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from( getContext() ).inflate( resourceId, null );
                viewHolder = new ViewHolder();
                viewHolder.statusTv
                        = (TextView) view.findViewById( R.id.status_tv );
                viewHolder.orderIdTv
                        = (TextView) view.findViewById( R.id.order_id_tv );
                viewHolder.createTimeTv
                        = (TextView) view.findViewById( R.id.create_time_tv );
                viewHolder.dealCountTv
                        = (TextView) view.findViewById( R.id.deal_count_tv );
                viewHolder.totalMoneyTv
                        = (TextView) view.findViewById( R.id.total_money_tv );

                view.setTag( viewHolder ); // 将ViewHolder存储在View中
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag(); // 重新获取ViewHolder
            }

            if (item.yd_status != null) {
                int status = Integer.parseInt( item.yd_status );
                viewHolder.statusTv.setText( SaleRecordStatus
                        .getRecordStatusStr( status ) );//交易状态
            }
            viewHolder.orderIdTv.setText( item.sell_order_id );
            if (item.create_time != null) {
                SimpleDateFormat sdf = new SimpleDateFormat( Consts.DATE_FORMAT_ );
                String createTime = sdf.format( Long.parseLong( item.create_time ) );
                viewHolder.createTimeTv.setText( createTime );
            }
            viewHolder.dealCountTv.setText( item.quantity );
            viewHolder.totalMoneyTv.setText( String.valueOf( item.amount ) );
            return view;
        }

        class ViewHolder {
            TextView statusTv;
            TextView orderIdTv;
            TextView createTimeTv;
            TextView dealCountTv;
            TextView totalMoneyTv;
        }
    }
}
