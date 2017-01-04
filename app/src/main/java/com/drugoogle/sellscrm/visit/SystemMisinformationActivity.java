package com.drugoogle.sellscrm.visit;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.data.request.MisinformationRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * Created by wgh on 2016/5/19.
 */
@EActivity(R.layout.activity_system_misinformation)
public class SystemMisinformationActivity extends BaseActivity
{
    ArrayList<String> mDataList;
    MyAdapter myAdapter;

    @ViewById(R.id.list_view)
    ListView mListView;


    @ViewById(R.id.confirm_btn)
    Button mConfirmBtn;
    @ViewById(R.id.cancel_btn)
    Button mCancelBtn;

    @ViewById(R.id.remark)
    EditText mRemark;

    MisinformationRequest request;


    @AfterViews
    void init()
    {
        setResult(RESULT_CANCELED);
        request = new MisinformationRequest();

        mDataList = new ArrayList<>();
        String[] infos = getResources().getStringArray(R.array.misinformation);
        for (String a : infos)
        {
            mDataList.add(a);
        }

        myAdapter = new MyAdapter();
        mListView.setAdapter(myAdapter);

        mCancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        mConfirmBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (selectPosition < 0)
                {
                    Toast.makeText(SystemMisinformationActivity.this, "请选择误报原因。", Toast.LENGTH_SHORT).show();
                    return;
                }
                String reason = mDataList.get(selectPosition);
                if (selectPosition == mDataList.size() - 1)
                {
                    reason = mRemark.getText().toString().trim();
                }
                Intent intent = new Intent();
                intent.putExtra(VisitService.EXTRA_MISMATCH_REASON, reason);
                setResult(RESULT_OK);
                finish();

            }
        });

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

//
//    @Background
//    void sendMisInformation()
//    {
//        MisinformationRequest request = new MisinformationRequest();
//        request.cause = mDataList.get(selectPosition);
//        request.remark = mRemark.getText().toString();
//        request.latitude = mBDLocation.getLatitude() + "";
//        request.longitude = mBDLocation.getLongitude() + "";
//
//        BaseResponse resp = mRestClient.locationMisinformation(request, Account.getInstance().getToken());
//        resultSendMisInformation(resp);
//    }
//
//    @UiThread
//    void resultSendMisInformation(BaseResponse resp)
//    {
//        if (!BaseResponse.hasErrorWithOperation(resp, this))
//        {
//            Toast.makeText(SystemMisinformationActivity.this, "提交成功。", Toast.LENGTH_SHORT).show();
//            setResult(RESULT_OK);
//            finish();
//        }
//    }


    int selectPosition = -1;

    class MyAdapter extends BaseAdapter
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
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = null;
            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.list_item_mininfomation, null);
                holder.itemLayout = convertView.findViewById(R.id.item_layout);
                holder.misInfo = (TextView) convertView.findViewById(R.id.misinformation);
                holder.select = (ImageView) convertView.findViewById(R.id.select);

                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            String item = mDataList.get(position);

            holder.misInfo.setText(item);
            if (position == selectPosition)
            {
                holder.select.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.select.setVisibility(View.GONE);
            }
            holder.itemLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    selectPosition = position;
                    notifyDataSetChanged();
                    if (position == 2)
                    {
                        mRemark.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        mRemark.setVisibility(View.GONE);
                    }
                }
            });

            return convertView;
        }

        class ViewHolder
        {
            View itemLayout;
            TextView misInfo;
            ImageView select;
        }
    }

}
