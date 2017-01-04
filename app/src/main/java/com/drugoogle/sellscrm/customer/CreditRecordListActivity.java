package com.drugoogle.sellscrm.customer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.common.SwipeListView;
import com.drugoogle.sellscrm.data.CreditRecordItem;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.request.CreditRecordListRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.CreditRecordListResponse;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ydwang on 2016/6/13.
 */
@EActivity(R.layout.activity_credit_record_list)
public class CreditRecordListActivity extends BaseActivity implements SwipeListView.OnLoadMore
        , SwipeRefreshLayout.OnRefreshListener {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.content_lay)
    View contentView;
    @ViewById(R.id.total_amount_tv)
    TextView totalAmountTv;
    @ViewById(R.id.left_amount_tv)
    TextView leftAmountTv;
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
    private CreditRecordListAdapter adapter;
    private List<CreditRecordItem> infos = new ArrayList<>();
    private CreditRecordListRequest request;
    private CreditRecordListResponse resp;
    private int pageNumber;
    private int pageSize;
    private int customerId;
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
                    CreditRecordItem item = infos.get( position );
                    CreditRecordActivity_.intent( context )._id( item.id ).start();
                }
            }
        } );

        request = new CreditRecordListRequest();
        if (!TextUtils.isEmpty( _CustomerInfo.ID )) {
            request.customerId = Integer.parseInt( _CustomerInfo.ID );
        }
        request.pageNumber = 1;
        request.pageSize = 15;
        token = account.getToken();
        adapter = new CreditRecordListAdapter( context, R.layout.list_item_credit_record, infos );
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
    void initView( CreditRecordListRequest request, boolean refresh) {
        pageNumber = request.pageNumber;
        pageSize = request.pageSize;
        customerId = request.customerId;
        resp = mRestClient.getCrreditRecordList( pageNumber, pageSize, customerId, token );
        afterInitView( resp ,refresh);
    }

    @UiThread
    void afterInitView( CreditRecordListResponse resp, boolean refresh) {
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
                contentView.setVisibility( View.VISIBLE );
                totalAmountTv.setText( String.valueOf( resp.totalAmount ) );
                leftAmountTv.setText( String.valueOf( resp.residualAmount ) );
                if (resp.list.size() == 0) {
                    listLv.setEmptyView( findViewById( R.id.empty_layout ) );
                    listLv.getEmptyView().setVisibility( View.VISIBLE );
                }
                if (request.pageNumber == resp.totalPage) {
                    footerText.setText( getString( R.string.no_more ) );
                    footer.setVisibility( View.VISIBLE );
                } else {
                    footerText.setText( getString( R.string.order_scan_more ) );
                    footer.setVisibility( View.VISIBLE );
                }
                //向列表中添加新的数据
                if (refresh)
                    infos.clear();
                if (resp.list != null)
                    infos.addAll( resp.list );
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void loadMore() {
        if (!swipeLay.isRefreshing()) {
            //刷新动画结束时
            swipeLay.setEnabled( false );
            request.pageNumber++;
            if (request.pageNumber <= resp.totalPage) {
                progressBar.setVisibility( View.VISIBLE );
                //原数据后面加载新的数据
                initView(  request,false );
            }
        }
        listLv.onLoadComplete();
    }

    @Override
    public void onRefresh() {
        request.pageNumber = 1;
        //重新加载list
        if (listLv.getEmptyView() != null) {
            listLv.getEmptyView().setVisibility( View.GONE );
        }
        initView( request ,true);
    }

    public class CreditRecordListAdapter extends ArrayAdapter<CreditRecordItem> {
        private int resourceId;

        public CreditRecordListAdapter(Context context, int textViewResourceId, List<CreditRecordItem> objects) {
            super( context, textViewResourceId, objects );
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CreditRecordItem item = getItem( position ); // 获取当前项的实例
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from( getContext() ).inflate( resourceId, null );
                viewHolder = new ViewHolder();
                viewHolder.payIdTv
                        = (TextView) view.findViewById( R.id.pay_id_tv );
                viewHolder.deliverDateTv
                        = (TextView) view.findViewById( R.id.deliver_date_tv );
                viewHolder.payDateTv
                        = (TextView) view.findViewById( R.id.pay_date_tv );
                viewHolder.totalMoneyTv
                        = (TextView) view.findViewById( R.id.total_money_tv );

                view.setTag( viewHolder ); // 将ViewHolder存储在View中
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag(); // 重新获取ViewHolder
            }

            viewHolder.payIdTv.setText( item.erpSettlementId );
            if (!TextUtils.isEmpty( item.outDate )) {
                viewHolder.deliverDateTv.setText( item.outDate );
            }else {
                viewHolder.deliverDateTv.setText( "暂无" );
            }
            if (!TextUtils.isEmpty( item.buildDate )) {
                viewHolder.payDateTv.setText( item.buildDate );
            }else {
                viewHolder.payDateTv.setText( "暂无" );
            }
            viewHolder.totalMoneyTv.setText( String.valueOf( item.amount ) );
            return view;
        }

        class ViewHolder {
            TextView payIdTv;
            TextView deliverDateTv;
            TextView payDateTv;
            TextView totalMoneyTv;
        }
    }
}
