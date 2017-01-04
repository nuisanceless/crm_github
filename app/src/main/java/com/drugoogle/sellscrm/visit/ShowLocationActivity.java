package com.drugoogle.sellscrm.visit;

import android.support.v7.widget.Toolbar;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by wgh on 2016/5/19.
 */
@EActivity(R.layout.activity_show_location)
public class ShowLocationActivity extends BaseActivity
{
    BaiduMap mBaiduMap;

    @Extra
    double latitude;
    @Extra
    double longitude;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.map_view)
    MapView mMapView;

    @AfterViews
    void init ()
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }





    private void initMyLocation()
    {
        mBaiduMap.setMyLocationEnabled(true);
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(100)
                .direction(90.0f)
                .latitude(latitude)
                .longitude(longitude).build();

        float f = mBaiduMap.getMaxZoomLevel();//19.0 最小比例尺
        mBaiduMap.setMyLocationData(locData);
        mBaiduMap.setMyLocationEnabled(true);
        LatLng ll = new LatLng(latitude,longitude);
        //MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll,f);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, f - 2);//设置缩放比例
        mBaiduMap.animateMapStatus(u);
    }


    public void initLocation()
    {
        mBaiduMap = mMapView.getMap();
//        initMyLocation();

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        LatLng point = new LatLng(latitude, longitude);


        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.map_red);
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        mBaiduMap.addOverlay(option);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(point, mBaiduMap.getMaxZoomLevel() - 4);
        mBaiduMap.animateMapStatus(msu);


    }

}
