package com.drugoogle.sellscrm.visit;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.LogUtils;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.CommonDialog;
import com.drugoogle.sellscrm.data.AutoVisitCustomerInfo;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.BeginAutoVisitResponse;
import com.drugoogle.sellscrm.data.type.CustomerLevel;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wgh on 2016/5/16.
 */
@EActivity(R.layout.activity_auto_visit_client_list)
public class AutoVisitClientListActivity extends BaseActivity
{
    public static final String TAG = "LocationService";
    public static final int SYSTEM_MISINFORMATION_REQUEST_CODE = 2;

    private List<AutoVisitCustomerInfo> mDataList;
    private CustomerAdapter mAdapter;

    private VisitService.ServiceBinder mBinder;
    private AutoVisitCustomerInfo mSelectCustomer = new AutoVisitCustomerInfo();

    CommonDialog mCommonDialog;


    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.count_down_tv)
    TextView mCountDownTv;
    @ViewById(R.id.customer_list_lv)
    ListView mListView;
    @ViewById(R.id.operation_btn)
    Button mOperationBtn;
    @ViewById(R.id.misinformation_btn)
    Button mMisinformationBtn;


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        refreshData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.auto_visit_customer_list_menu, menu);
        return true;
    }

    Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.cancel:
                    confirmCancelAutoVisit();
                    break;
            }
            return true;
        }
    };

    void confirmCancelAutoVisit()
    {
        mCommonDialog.builder().setOnConfirmClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mBinder != null)
                {
                    mBinder.cancelAboutToStart(null);
                }
                else
                {
                    finish();
                }
            }
        }).setOnCancelClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mCommonDialog.dismiss();
            }
        }).setContent(getString(R.string.confirm_cancel_auto_visit)).show();
    }


    @Override
    public void onBackPressed()
    {
        confirmCancelAutoVisit();
    }

    @Override
    protected void onDestroy()
    {
        vibrateHandler.removeMessages(0);
        if (mCommonDialog != null)
        {
            mCommonDialog.dismiss();
        }
        super.onDestroy();
        unbindService(sConnection);
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SYSTEM_MISINFORMATION_REQUEST_CODE && resultCode == RESULT_OK)
        {
            finish();
        }
    }

    @AfterViews
    void init()
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mCommonDialog = new CommonDialog(AutoVisitClientListActivity.this);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);
        mAdapter = new CustomerAdapter();
        mDataList = new ArrayList<>();
        mListView.setAdapter(mAdapter);

        Intent bindIntent = new Intent(this, VisitService.class);
        bindService(bindIntent, sConnection, BIND_AUTO_CREATE);
        mOperationBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mBinder != null)
                {
                    mBinder.startVisit(mSelectCustomer.ID);
                }
            }
        });

        mMisinformationBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                confirmCancelAutoVisit();
//                mCountDownTv.setText(getString(R.string.please_select_visit_client));
//                SystemMisinformationActivity_.intent(AutoVisitClientListActivity.this).startForResult(SYSTEM_MISINFORMATION_REQUEST_CODE);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(VisitService.ACTION_VISIT_ENDED);
        intentFilter.addAction(VisitService.ACTION_VISIT_STARTED);
        intentFilter.addAction(VisitService.ACTION_CANCEL_ABOUT_TO_VISIT);
        intentFilter.addAction(VisitService.ACTION_ABOUT_TO_VISIT_COUNT_DOWN);
        registerReceiver(mReceiver, intentFilter);

        refreshData();

    }

    private void refreshData()
    {
        mDataList = (ArrayList<AutoVisitCustomerInfo>)getIntent().getSerializableExtra(VisitService.EXTRA_CUSTOMERS);
        if (mDataList == null || mDataList.size() == 0)
        {
            //TODO:Log error
            finish();
            return;
        }
        mSelectCustomer = mDataList.get(0);
        mListView.setAdapter(mAdapter);

        if (mDataList.size() == 1)
        {
            mCountDownTv.setText("");
        }
        else if (mDataList.size() > 1)
        {
            mCountDownTv.setText(getString(R.string.please_select_visit_client));
        }
        vibrateHandler.sendEmptyMessage(0);
    }
    private ServiceConnection sConnection = new ServiceConnection()
    {

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            finish();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mBinder = (VisitService.ServiceBinder) service;
        }
    };



    class CustomerAdapter extends BaseAdapter
    {

        @Override
        public int getCount()
        {
            return mDataList == null ? 0 : mDataList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mDataList == null ? null : mDataList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = null;
            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.list_item_auto_visit_customer, null);

                holder.listItemLayout = convertView.findViewById(R.id.list_item_layout);
                holder.itemCustomerInLegal = (TextView) convertView.findViewById(R.id.item_customer_inlegal);
                holder.itemCustomerLevelIm = (ImageView) convertView.findViewById(R.id.item_customer_level);
                holder.itemCustomerSortInfo = (TextView) convertView.findViewById(R.id.item_customer_sort_info);

                convertView.setTag(holder); // 将ViewHolder存储在View中
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }


            final AutoVisitCustomerInfo customerInfo = mDataList.get(position);
            holder.itemCustomerInLegal.setText(customerInfo.NAME);
            holder.itemCustomerLevelIm.setImageResource(CustomerLevel.getCustomerImRes(2));
            holder.itemCustomerSortInfo.setText("");
            if (customerInfo.ID == mSelectCustomer.ID)
            {
                holder.listItemLayout.setBackgroundColor(getResources().getColor(R.color.light_blue));
            }
            else
            {
                holder.listItemLayout.setBackgroundColor(getResources().getColor(R.color.content_background));
            }
            holder.listItemLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mSelectCustomer = customerInfo;
                    mOperationBtn.setClickable(true);
                    mOperationBtn.setTextColor(getResources().getColor(R.color.yellowButton));
                    notifyDataSetChanged();
                }
            });


            return convertView;

        }

        class ViewHolder
        {
            View listItemLayout;
            TextView itemCustomerInLegal;
            ImageView itemCustomerLevelIm;
            TextView itemCustomerSortInfo;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(VisitService.ACTION_CANCEL_ABOUT_TO_VISIT))
            {
                finish();
            }
            else if (action.equals(VisitService.ACTION_VISIT_STARTED))
            {
                boolean success = intent.getBooleanExtra(VisitService.EXTRA_RESULT, false);
                if (success)
                {
                    finish();
                }
            }
            else if (action.equals(VisitService.ACTION_ABOUT_TO_VISIT_COUNT_DOWN))
            {
                int count = intent.getIntExtra(VisitService.EXTRA_COUNT_DOWN, 0);
                mCountDownTv.setText(getString(R.string.count_down_start_auto_plan, count));
            }
        }
    };
    Handler vibrateHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            removeMessages(0);
            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            long [] pattern = {100,400,100,400, 100, 1000};   // 停止 开启 停止 开启
            vibrator.vibrate(pattern, -1);
            sendEmptyMessageDelayed(0, 30 * 1000);
        }
    };
}
