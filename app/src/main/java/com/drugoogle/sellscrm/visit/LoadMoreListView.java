package com.drugoogle.sellscrm.visit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.drugoogle.sellscrm.R;

/**
 * Created by wgh on 2016/5/23.
 */
public class LoadMoreListView extends ListView implements AbsListView.OnScrollListener
{

    private LinearLayout footerParent;
    private View footer;

    private int totalItem;
    private int lastItem;

    private boolean isLoading;

    private OnLoadMore onLoadMore;

    private LayoutInflater inflater;

    public LoadMoreListView(Context context) {
        super(context);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("InflateParams")
    private void init(Context context) {
        inflater = LayoutInflater.from(context);
        footerParent = new LinearLayout(context);
        footer = inflater.inflate( R.layout.listview_footer,null ,false);
        footerParent.addView( footer );
        footerParent.setGravity( Gravity.CENTER );
        this.addFooterView(footerParent);
        footer.setVisibility(View.VISIBLE);
        this.setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.lastItem = firstVisibleItem+visibleItemCount;
        this.totalItem = totalItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(this.totalItem == lastItem&&scrollState == SCROLL_STATE_TOUCH_SCROLL){
            Log.v("isLoading", "yes");
            if(!isLoading){
                isLoading=true;
                footer.setVisibility(View.VISIBLE);
                onLoadMore.loadMore();
            }
        }
    }
    public void setLoadMoreListen(OnLoadMore onLoadMore){
        this.onLoadMore = onLoadMore;
    }
    /**
     * 加载完成调用此方法
     */
    public void onLoadComplete(){
        footer.setVisibility(View.VISIBLE);
        isLoading = false;

    }

    public interface OnLoadMore{
        public void loadMore();
    }
}
