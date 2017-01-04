package com.drugoogle.sellscrm.customer;

import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.common.SwipeListView;
import com.drugoogle.sellscrm.data.DealInfo;
import com.drugoogle.sellscrm.data.request.DealListRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.DealListResponse;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ydwang on 2016/5/23.
 */
@EActivity(R.layout.activity_deal_detail)
public class DealListActivity extends BaseActivity implements SwipeListView.OnLoadMore,
        SwipeRefreshLayout.OnRefreshListener {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.tittle)
    TextView tittle;
    @ViewById(R.id.swipe_index)
    SwipeRefreshLayout swipeLay;
    @ViewById(R.id.list_lv)
    SwipeListView listLv;
    @Extra
    public String _SellOrderId;

    Account account;
    Context context;
    private LayoutInflater inflater;
    private LinearLayout footerParent;
    private View footer;
    private TextView footerText;
    private ProgressBar progressBar;
    private DealListAdapter adapter;
    private List<DealInfo> infos = new ArrayList<>();
    private String token;
    private DealListRequest request;
    private DealListResponse resp;

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
        ((TextView) toolbar.findViewById( R.id.title )).setText( R.string.detailed );
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

        request = new DealListRequest();
        request.orderId = _SellOrderId;
        request.page = 1;
        request.pageSize = 25;
        token = account.getToken();
        adapter = new DealListAdapter( context, R.layout.list_item_deal_detail, infos );
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
    void initView( DealListRequest request, boolean refresh) {
        resp = mRestClient.getDealList( request, token );
        afterInitView(  resp,refresh );
    }

    @UiThread
    void afterInitView( DealListResponse resp, boolean refresh) {
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
                }else {
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
            request.page++;
            swipeLay.setRefreshing( false );
            if (request.page <= resp.totalPage) {
                progressBar.setVisibility( View.VISIBLE );
                //原数据后面加载新的数据
                initView(  request ,false);
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
        initView(  request,true );
    }

    public class DealListAdapter extends ArrayAdapter<DealInfo> {
        private int resourceId;

        public DealListAdapter(Context context, int textViewResourceId, List<DealInfo> objects) {
            super( context, textViewResourceId, objects );
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DealInfo item = getItem( position ); // 获取当前项的实例
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from( getContext() ).inflate( resourceId, null );
                viewHolder = new ViewHolder();
                viewHolder.goodsNameTv
                        = (TextView) view.findViewById( R.id.goods_name_tv );
                viewHolder.specificationsTv
                        = (TextView) view.findViewById( R.id.specifications_tv );
                viewHolder.unitTv
                        = (TextView) view.findViewById( R.id.unit_tv );
                viewHolder.quantityTv
                        = (TextView) view.findViewById( R.id.amount_tv );
                viewHolder.unitPriceTv
                        = (TextView) view.findViewById( R.id.unit_price_tv );
                viewHolder.amountTv
                        = (TextView) view.findViewById( R.id.sum_money_tv );
                viewHolder.remarksTv
                        = (TextView) view.findViewById( R.id.remarks_tv );

                view.setTag( viewHolder ); // 将ViewHolder存储在View中
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag(); // 重新获取ViewHolder
            }
            viewHolder.goodsNameTv.setText( item.MEDICINE_NAME );
            viewHolder.specificationsTv.setText( item.STANDARD );
            viewHolder.unitTv.setText( item.BASE_UNIT );
            viewHolder.quantityTv.setText( String.valueOf( item.QUANTITY ) );
            viewHolder.unitPriceTv.setText( String.valueOf( item.PRICE ) );
            viewHolder.amountTv.setText( String.valueOf( item.AMOUNT ) );
            if (!TextUtils.isEmpty( item.REMARK )) {
                viewHolder.remarksTv.setText( item.REMARK );
            } else {
                viewHolder.remarksTv.setText( "无" );
            }
            return view;
        }

        class ViewHolder {
            TextView goodsNameTv;
            TextView specificationsTv;
            TextView unitTv;
            TextView quantityTv;
            TextView unitPriceTv;
            TextView amountTv;
            TextView remarksTv;
        }
    }
}
