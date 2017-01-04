package com.drugoogle.sellscrm.customer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.customviews.ClearEditText;
import com.drugoogle.sellscrm.data.type.AddressInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Created by Lei on 2015/6/3.
 */
public class AddressSelectActivity extends BaseActivity implements BDLocationListener
        , OnGetPoiSearchResultListener, BaiduMap.OnMapStatusChangeListener
        , OnGetGeoCoderResultListener {

    Toolbar toolbar;
    ClearEditText _textAddress;
    View _btnConfirm;
    ListView _suggestListView;
    MapView _mapView;
    ImageView mapCenter;
    BaiduMap _baiduMap;

    LocationClient _locClient;
    boolean _isFirstLoc = true;// 是否首次定位
    boolean _needSuggest = true;//是否显示提示
    SuggestResultAdapter _sugAdapter;
    List<PoiInfo> _dataList = new ArrayList<>();
    PoiSearch _poiSearch;
    GeoCoder _geoCoder;
    //地图选址
    PoiInfo _selectedPoiInfo;
    //地址setAddress页面传递过来的地址
    AddressInfo _addressInfo;
    //返回给setAddress页面的地图选址
    AddressInfo info = new AddressInfo();
    // 初始化全局 bitmap 信息，不用时及时 recycle
    BitmapDescriptor bdBlue = BitmapDescriptorFactory
            .fromResource(R.drawable.map_blue);
    BitmapDescriptor bdRed = BitmapDescriptorFactory
            .fromResource(R.drawable.map_red);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在setContentView方法之前MyApplication初始化全局context
        setContentView(R.layout.activity_address_select);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        _textAddress = (ClearEditText) findViewById(R.id.address);
        _suggestListView = (ListView) findViewById(R.id.suggestList);
        _btnConfirm = findViewById(R.id.confirm);
        _mapView = (MapView) findViewById(R.id.mapView);
        mapCenter = (ImageView) findViewById(R.id.map_center);

        //设置tolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //poi搜索
        _poiSearch = PoiSearch.newInstance();
        _poiSearch.setOnGetPoiSearchResultListener(this);
        //
        _geoCoder = GeoCoder.newInstance();
        _geoCoder.setOnGetGeoCodeResultListener(this);

        //监听输出内容
        _textAddress.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                if (arg0.length() == 0) {
                    _btnConfirm.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                _btnConfirm.setVisibility(View.GONE);
                if (cs.length() <= 0 || !_needSuggest) {
                    return;
                }
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更�?
                 */
                _poiSearch.searchInCity(new PoiCitySearchOption().city("杭州").keyword(cs.toString()).pageNum(0));
            }
        });
        //确定button点击监听
        _btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_selectedPoiInfo != null) {
                    info.PROVINCE = "浙江省";
                    info.CITY = _selectedPoiInfo.city;
                    info.LONGITUDE = String.valueOf(_selectedPoiInfo.location.longitude);
                    info.LATITUDE = String.valueOf(_selectedPoiInfo.location.latitude);
                    info.ADDRESS = _selectedPoiInfo.address;
                    info.ADDRESSNAME = _selectedPoiInfo.name;
                    _geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(_selectedPoiInfo.location));
                    Intent intent = new Intent();
                    intent.putExtra("address", info);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        //搜索提示list
        _sugAdapter = new SuggestResultAdapter(_dataList);
        _suggestListView.setAdapter(_sugAdapter);
        _suggestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiInfo info = (PoiInfo) _sugAdapter.getItem(position);
                _selectedPoiInfo = info;
                _needSuggest = false;
                _textAddress.setText(info.name);
                _textAddress.setSelection(info.name.length());
                _btnConfirm.setVisibility(View.VISIBLE);

                LatLng ll = new LatLng(info.location.latitude,
                        info.location.longitude);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                _baiduMap.animateMapStatus(u);
            }
        });

        //获得地图
        _baiduMap = _mapView.getMap();
        //监听地图状态改变
        _baiduMap.setOnMapStatusChangeListener(this);

        //判断添加/修改
        Object extra = getIntent().getSerializableExtra("address");
        fromAddOrModify(extra);
    }

    /**
     * 判断添加/修改
     *
     * @param extra
     */
    private void fromAddOrModify(Object extra) {
        if (extra != null) {
            //从修改进入，显示已有地址
            _addressInfo = (AddressInfo) extra;
            PoiInfo info = new PoiInfo();
            String latitude = _addressInfo.LATITUDE;
            String longitude = _addressInfo.LONGITUDE;
            if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                double latitudeDouble = Double.parseDouble(latitude);
                double longitudeDouble = Double.parseDouble(longitude);

                info.location = new LatLng(latitudeDouble, longitudeDouble);
            }
            info.name = _addressInfo.ADDRESSNAME;
            if (TextUtils.isEmpty(_addressInfo.ADDRESS)) {
//                info.name = _addressInfo.ADDRESS_OLD;
                info.address = _addressInfo.ADDRESS_OLD;
            } else {
//                info.name = _addressInfo.ADDRESS;
                info.address = _addressInfo.ADDRESS;
            }
            _selectedPoiInfo = info;
            _textAddress.setText(info.name);
            if (info.name != null) {
                _textAddress.setSelection(info.name.length());
            }
            _btnConfirm.setVisibility(View.VISIBLE);

            //设置当前客户为地图中心
            MapStatusUpdate u = MapStatusUpdateFactory
                    .newLatLngZoom(info.location, _baiduMap.getMaxZoomLevel() - 4);
            _baiduMap.setMapStatus(u);
            //反向地理编码
            _geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(info.location));

        } else {

            //从添加地址进入，进行定位
            _baiduMap.setMyLocationEnabled(true);
            _baiduMap
                    .setMyLocationConfigeration(new MyLocationConfiguration(
                            MyLocationConfiguration.LocationMode.NORMAL, true, null));
            _locClient = new LocationClient(getApplicationContext());
            _locClient.registerLocationListener(this);
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);// 打开gps
            option.setCoorType("bd09ll"); // 设置坐标类型
            option.setScanSpan(50000);
            _locClient.setLocOption(option);
            _locClient.start();
        }
    }


    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if (bdLocation == null || _mapView == null)
            return;
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                // 此处设置�?发�?�获取到的方向信息，顺时�?0-360
                .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();
        _baiduMap.setMyLocationData(locData);

        if (_isFirstLoc) {
            _locClient.stop();
            _isFirstLoc = false;
            LatLng ll = new LatLng(bdLocation.getLatitude(),
                    bdLocation.getLongitude());
            //反向地理编码
            _geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, _baiduMap.getMaxZoomLevel() - 4);
            _baiduMap.animateMapStatus(u);
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if (poiResult == null
                || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND || poiResult.getAllPoi() == null) {
            return;
        }

        performSuggestion(poiResult.getAllPoi());
    }

    void performSuggestion(List<PoiInfo> data) {
        _sugAdapter.clear();
        _sugAdapter.addAll(data);
        _sugAdapter.notifyDataSetChanged();
        _suggestListView.smoothScrollToPosition(0);

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        if (_needSuggest)
            _geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mapStatus.target));
        else
            _needSuggest = true;

        _baiduMap.clear();

    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        //反向地理编码查询结果
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR || reverseGeoCodeResult.getPoiList() == null) {
            return;
        }
        info.PROVINCE = "浙江省";
        info.CITY = reverseGeoCodeResult.getAddressDetail().city;
        info.REGION = reverseGeoCodeResult.getAddressDetail().district;

        performSuggestion(reverseGeoCodeResult.getPoiList());
    }

    class SuggestResultAdapter extends BaseAdapter {
        private List<PoiInfo> mObjects;
        private final Object mLock = new Object();

        public SuggestResultAdapter(List<PoiInfo> data) {
            mObjects = data;
        }

        public void add(PoiInfo object) {
            synchronized (mLock) {
                mObjects.add(object);
            }
        }

        /**
         * Adds the specified Collection at the end of the array.
         *
         * @param collection The Collection to add at the end of the array.
         */
        public void addAll(Collection<? extends PoiInfo> collection) {
            synchronized (mLock) {
                mObjects.addAll(collection);
            }
        }

        /**
         * Adds the specified items at the end of the array.
         *
         * @param items The items to add at the end of the array.
         */
        public void addAll(PoiInfo... items) {
            synchronized (mLock) {
                Collections.addAll(mObjects, items);
            }
        }

        /**
         * Inserts the specified object at the specified index in the array.
         *
         * @param object The object to insert into the array.
         * @param index  The index at which the object must be inserted.
         */
        public void insert(PoiInfo object, int index) {
            synchronized (mLock) {
                mObjects.add(index, object);
            }
        }

        /**
         * Remove all elements from the list.
         */
        public void clear() {
            synchronized (mLock) {
                mObjects.clear();
            }
        }

        @Override
        public int getCount() {
            return mObjects == null ? 0 : mObjects.size();
        }

        @Override
        public Object getItem(int position) {
            return mObjects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_addr_suggestion, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.text1);
                holder.address = (TextView) convertView.findViewById(R.id.text2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            PoiInfo info = mObjects.get(position);
            holder.name.setText(info.name);
            holder.address.setText(info.address);

            return convertView;
        }

        private class ViewHolder {
            public TextView name;
            public TextView address;
        }
    }


//    public double longitude;
//    public double latitude;
//    public String token;

//    /**
//     * 请求选点周围客户
//     */
//    private void requestAroundCoordinates() {
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                BaseResponse baseResponse = mRestClient.getCustomersByDistance(longitude, latitude, token);
//                Message message = new Message();
//                accountHandler.sendMessage(message);
//            }
//        }).start();
//    }
//
//    Handler accountHandler = new Handler() {
//        public void handleMessage(Message message) {
//            showAroundOverLay();
//        }
//    };
//
//    /**
//     * 显示周围客户
//     */
//    public void showAroundOverLay() {
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        _mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //回收overlay图标
        if (bdBlue != null) {
            bdBlue.recycle();
        }
        if (bdRed != null) {
            bdRed.recycle();
        }
        _geoCoder.destroy();
        _poiSearch.destroy();
        // �?出时�?毁定�?
        if (_locClient != null)
            _locClient.stop();
        // 关闭定位图层
        _baiduMap.setMyLocationEnabled(false);
        _mapView.onDestroy();
    }
}
