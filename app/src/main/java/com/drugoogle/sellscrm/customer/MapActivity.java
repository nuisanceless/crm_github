package com.drugoogle.sellscrm.customer;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.type.AddressInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by ydwang on 2016/5/6.
 */
@EActivity(R.layout.activity_map)
public class MapActivity extends BaseActivity {

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.title)
    TextView title;
    @ViewById(R.id.bmapview)
    MapView mMapView;
    private BaiduMap mBaiduMap;

    @Extra
    AddressInfo _mAddress;
    @Extra
    CustomerInfo _mCustomerInfo;

    public Context context;
    // 初始化全局 bitmap 信息，不用时及时 recycle
    BitmapDescriptor bdBlue = BitmapDescriptorFactory
            .fromResource(R.drawable.map_blue);
    BitmapDescriptor bdRed = BitmapDescriptorFactory
            .fromResource(R.drawable.map_red);

    @AfterViews
    void init(){

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //标题
        title.setText(_mCustomerInfo.NAME);

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        initCustomerLocation();//设置中心
        requestAroundCoordinates();//显示附近客户
    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity 暂停时同时暂停地图控件
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity 恢复时同时恢复地图控件
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // activity 销毁时同时销毁地图控件
        mMapView.onDestroy();
        bdBlue.recycle();
        bdRed.recycle();
    }

    /**
     * 设置当前客户为地图中心
     */
    void initCustomerLocation() {

        double latitude = Double.parseDouble(_mAddress.LATITUDE);
        double longitude = Double.parseDouble(_mAddress.LONGITUDE);
        LatLng currentLatLng = new LatLng(latitude, longitude);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions currentOption = new MarkerOptions()
                .position(currentLatLng)
                .icon(bdBlue);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(currentOption);
        //设置当前客户为地图中心
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(currentLatLng, mBaiduMap.getMaxZoomLevel() - 4);
        mBaiduMap.setMapStatus(msu);

    }

    /**
     * 请求附近客户经纬度
     */
    @Background
    void requestAroundCoordinates() {

        showAroundOverLay();
    }

    /**
     * 显示附近客户
     */
    @UiThread
    void showAroundOverLay() {

    }

//    private PopupWindow mPopWindow;
//    private void showPopupWindow() {
//        //设置contentView
//        View contentView = LayoutInflater.from(this).inflate(R.layout.map_pop, null);
//        mPopWindow = new PopupWindow(contentView,
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
//        mPopWindow.setContentView(contentView);
//        //设置各个控件的点击响应
//        TextView name = (TextView)contentView.findViewById(R.id.name_tv);
//        TextView address = (TextView)contentView.findViewById(R.id.address_tv);
//        name.setText(_mCustomerInfo.NAME);
//        address.setText(_mAddress.ADDRESS);
//        //显示PopupWindow
//        View rootview = LayoutInflater.from(this).inflate(R.layout.activity_map, null);
//        mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
//
//    }


}
