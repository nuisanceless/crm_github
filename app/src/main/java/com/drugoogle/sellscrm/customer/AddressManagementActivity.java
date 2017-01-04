package com.drugoogle.sellscrm.customer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.CommonDialog;
import com.drugoogle.sellscrm.common.swipemenulistview.SwipeMenu;
import com.drugoogle.sellscrm.common.swipemenulistview.SwipeMenuCreator;
import com.drugoogle.sellscrm.common.swipemenulistview.SwipeMenuItem;
import com.drugoogle.sellscrm.common.swipemenulistview.SwipeMenuListView;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.request.AddressListRequest;
import com.drugoogle.sellscrm.data.response.AddressListResponse;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.type.AddressInfo;
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
 * Created by wuguohao on 15-5-18.
 */
@EActivity(R.layout.activity_address_management)
public class AddressManagementActivity extends BaseActivity {
    private static final int REQUEST_CODE_MODIFY_ADDRESS = 1;
    private static final int REQUEST_CODE_ADD_ADDRESS = 2;

    private List<AddressInfo> mDataList = new ArrayList<>();
    private AddressAdapter mAdapter;
    private boolean mTargetAddressDeleted = false;

    AddressInfo mAddressModified;
    public AddressListRequest request;
    public AddressListResponse resp;
    public String token;
    public Account account;
    public int mAddressSelectedId = -1;
    public Context context;

    @Extra
    boolean mChooseAddress = false;
    @Extra
    CustomerInfo mCustomerInfo;
    @Extra
    String companyId;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.listView)
    SwipeMenuListView mListView;

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setCancelResult();
    }

    private void setCancelResult() {
        Intent intent = new Intent();
        if (mAddressModified != null) {
            intent.putExtra("same", true);
            intent.putExtra("address", mAddressModified);
        } else if (mTargetAddressDeleted) {
            intent.putExtra("delete", true);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.add).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            //添加地址
            SetAddressActivity_.intent(context).isAddAddress(true)
                    .isEditAddress(false).extra("companyId", companyId)
                    .startForResult(REQUEST_CODE_ADD_ADDRESS);
            return true;
        }
    };

    @AfterViews
    void init() {

        if (mCustomerInfo != null)
        {
            companyId = mCustomerInfo.ERP_CODE;
        }
        account = Account.getInstance();
        context = this;

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);
        if (mListView.getEmptyView() != null) {
            mListView.getEmptyView().setVisibility( View.GONE );
        }
        mListView.addHeaderView(new View(this));
        mAdapter = new AddressAdapter();
        mListView.setAdapter(mAdapter);
        getAddressList();

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem modifyBtn = new SwipeMenuItem(getApplicationContext());
                modifyBtn.setBackground(new ColorDrawable(Color.rgb(0xd9, 0xd9, 0xd9)));
                modifyBtn.setWidth(160);
                modifyBtn.setTitle(getString(R.string.modify));
                modifyBtn.setTitleSize(18);
                modifyBtn.setTitleColor(Color.rgb(0x23, 0xc4, 0xec));
                menu.addMenuItem(modifyBtn);


                SwipeMenuItem deleteBtn = new SwipeMenuItem(getApplicationContext());
                deleteBtn.setBackground(new ColorDrawable(Color.rgb(0x23, 0xc4, 0xec)));
                deleteBtn.setWidth(160);
                deleteBtn.setTitle(getString(R.string.delete));
                deleteBtn.setTitleSize(18);
                deleteBtn.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteBtn);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);

        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    // 修改
                    case 0:
                        AddressInfo address = (AddressInfo) mAdapter.getItem(position);
                        SetAddressActivity_.intent(context)._addressInfo(address).isAddAddress(false)
                                .isEditAddress(true).startForResult(REQUEST_CODE_MODIFY_ADDRESS);
//                        open(item);
                        break;
                    // 删除
                    case 1:
                        final CommonDialog dialog = new CommonDialog(AddressManagementActivity.this)
                                .builder().setContent(getString(R.string.confirm_del_address));
                        dialog.setOnCancelClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setOnConfirmClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //删除地址
                                deleteAddress(position);
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        // set SwipeListener左滑菜单
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AddressInfo address = mDataList.get(position - mListView.getHeaderViewsCount());
                if (mChooseAddress) {
                    //添加拜访计划/临时计划 选择地址
                    Intent intent = new Intent();
                    intent.putExtra("address", address);
                    if (address.ID != null) {
                        if (Integer.parseInt(address.ID) == mAddressSelectedId)
                            intent.putExtra("same", true);
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    //地图显示
                    if(TextUtils.isEmpty( address.LATITUDE ) || TextUtils.isEmpty( address.LONGITUDE ) )
                        Toast.makeText( context,"该地址信息无经纬度",Toast.LENGTH_SHORT ).show();
                    else
                        MapActivity_.intent(context)._mCustomerInfo(mCustomerInfo)._mAddress(address).start();
                }
            }
        });
    }
    @Background
    void getAddressList() {
        request = new AddressListRequest();
        request.page = 1;//页码
        request.pageSize = 10000;//每页大小
        request.companyId = companyId;//公司id
        token = account.getToken();
        mActivityHelper.showLoadingDialog(null);
        resp = mRestClient.getAddressList(request, token);
        mActivityHelper.dismissLoadingDialog();
        updateAddressList(resp);
    }

    @UiThread
    void updateAddressList(AddressListResponse resp) {
        if (!BaseResponse.hasErrorWithOperation(resp, this)) {
            if (resp.dataList.size() == 0) {
                mListView.setEmptyView( findViewById( R.id.empty_layout ) );
                mListView.getEmptyView().setVisibility( View.VISIBLE );
            }
            mDataList = resp.dataList;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            //添加新的地址/修改地址 重新加载地址列表
            getAddressList();
//            if (requestCode == REQUEST_CODE_MODIFY_ADDRESS) {
//                //修改地址
//                AddressInfo address = (AddressInfo) data.getSerializableExtra( "address" );
//                if (address.ID != null) {
//                    if (Integer.parseInt( address.ID ) == mAddressSelectedId) {
//                        mAddressModified = address;
//                    }
//                }
//            }else if (requestCode==REQUEST_CODE_ADD_ADDRESS){
//                //添加地址
//
//            }
        } else {
            //登录后返回当前页面并重新加载数据
            if (requestCode == BaseResponse.REQUEST_CODE_EXPIRED && resultCode == BaseResponse.RESULT_CODE_EXPIRED) {
                token = Account.getInstance().getToken();
                getAddressList();
            }
        }
    }

    @Background
    void deleteAddress(int position) {
        token = account.getToken();

        if (!TextUtils.isEmpty(mDataList.get(position).ID)) {
            int id = Integer.parseInt(mDataList.get(position).ID);
            BaseResponse resp = mRestClient.deleteAddress(token, id);
            updateDeleteAddress(resp, position, id);
        }
    }

    @UiThread
    void updateDeleteAddress(BaseResponse resp, int position, int deletedId) {
        if (BaseResponse.hasError(resp)) {
            mActivityHelper.showToast(BaseResponse.getErrorMessage(resp));
        } else {
            mDataList.remove(position);
            mAdapter.notifyDataSetChanged();
            if (mAddressSelectedId == deletedId) {
                mTargetAddressDeleted = true;
            }
            getAddressList();
        }
    }


    class AddressAdapter extends BaseAdapter {

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
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.list_item_address, null);
                holder.addressName = (TextView) convertView.findViewById(R.id.addressName);
                holder.address = (TextView) convertView.findViewById(R.id.address);
                holder.oldAddress = (TextView) convertView.findViewById(R.id.oldaddress);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            AddressInfo item = mDataList.get(position);
            setAddress(holder, item);
            return convertView;
        }

        class ViewHolder {
            public TextView addressName;
            public TextView address;
            public TextView oldAddress;
        }
        private void setAddress(ViewHolder holder, AddressInfo info)
        {
            if (!TextUtils.isEmpty(info.ADDRESS))//使用详细地址的格式
            {
                holder.addressName.setText(info.ADDRESSNAME);
                holder.address.setText(info.ADDRESS);

                holder.addressName.setVisibility(View.VISIBLE);
                holder.address.setVisibility(View.VISIBLE);

                holder.oldAddress.setText("");
                holder.oldAddress.setVisibility(View.INVISIBLE);
            }
            else
            {

                holder.addressName.setText("");
                holder.address.setText("");

                holder.addressName.setVisibility(View.INVISIBLE);
                holder.address.setVisibility(View.INVISIBLE);

                holder.oldAddress.setVisibility(View.VISIBLE);
                holder.oldAddress.setText(info.ADDRESS_OLD);
            }
        }
    }
}
