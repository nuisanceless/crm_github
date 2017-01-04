package com.drugoogle.sellscrm.customer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.Consts;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.CustomerInfoItem;
import com.drugoogle.sellscrm.data.type.CustomerLevel;
import com.drugoogle.sellscrm.data.type.CustomnerSortBy;
import com.drugoogle.sellscrm.data.type.VisitModel;
import com.drugoogle.sellscrm.data.type.VisitType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ydwang on 2016/4/7.
 */
public class CustomerAdapter extends ArrayAdapter<CustomerInfoItem> {
    private int resourceId;

    public CustomerAdapter(Context context, int textViewResourceId, List<CustomerInfoItem> objects) {
        super( context, textViewResourceId, objects );
        resourceId = textViewResourceId;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("getView","position"+position);
        CustomerInfoItem customerInfo = getItem( position ); // 获取当前项的实例
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from( getContext() ).inflate( resourceId, null );
            viewHolder = new ViewHolder();
            viewHolder.itemCustomerInLegal
                    = (TextView) view.findViewById( R.id.item_customer_inlegal );
            viewHolder.itemCustomerLevelIm
                    = (ImageView) view.findViewById( R.id.item_customer_level );
            viewHolder.itemCustomerSortInfo
                    = (TextView) view.findViewById( R.id.item_customer_sort_info );

            view.setTag( viewHolder ); // 将ViewHolder存储在View中
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag(); // 重新获取ViewHolder
        }
        viewHolder.itemCustomerInLegal.setText( customerInfo.name );
        viewHolder.itemCustomerLevelIm.setImageResource(
                CustomerLevel.getCustomerImRes( customerInfo.grade ) );
        switch (customerInfo.sort) {
            case CustomnerSortBy.CUSTOMER_SORTTYPE_LEVEL:
                viewHolder.itemCustomerSortInfo.setText(
                        CustomerLevel.getCustomerLevel( customerInfo.grade ) );
                break;
            case CustomnerSortBy.CUSTOMER_SORTTYPE_NEAREST:
                if (customerInfo.distance != null){
                    if (Integer.parseInt( customerInfo.distance ) < 10000) {
                        viewHolder.itemCustomerSortInfo.setText( customerInfo.distance + "m" );
                    }else if (Integer.parseInt( customerInfo.distance ) == 99999999){
                        viewHolder.itemCustomerSortInfo.setText( "无地址" );
                    } else {
                        viewHolder.itemCustomerSortInfo.setText(
                                (Integer.parseInt( customerInfo.distance ))/1000+"km" );
                    }
                }

                break;
            case CustomnerSortBy.CUSTOMER_SORTTYPE_LATEST:
                if (customerInfo.date != null){
                    if (getShortOfDays(customerInfo.date) == "0")
                        viewHolder.itemCustomerSortInfo.setText( "今天已拜访过" );
                    else
                        viewHolder.itemCustomerSortInfo.setText( "已有"+getShortOfDays( customerInfo.date )+"天未拜访" );
                }else {
                    viewHolder.itemCustomerSortInfo.setText("从未拜访过");
                }
                break;
            case CustomnerSortBy.CUSTOMER_SORTTYPE_TIMES:
                viewHolder.itemCustomerSortInfo.setText( "上月拜访"+customerInfo.count +"次");
                break;
            default:
                viewHolder.itemCustomerSortInfo.setText("");
        }
        return view;
    }

    class ViewHolder {
        TextView itemCustomerInLegal;
        ImageView itemCustomerLevelIm;
        TextView itemCustomerSortInfo;
    }

    /**
     * 相隔天数（过凌晨00：00记为第二天）
     * @param dateString
     * @return
     */
    public String getShortOfDays(String dateString){
        SimpleDateFormat sdf1 =   new SimpleDateFormat( "yyyy-MM-dd");
        String todayDate = sdf1.format(new Date());
        long dateMillionSeconds = 0L;
        long todayMillionSeconds = 0L;
        try {
            dateMillionSeconds = sdf1.parse( dateString).getTime();
            todayMillionSeconds = sdf1.parse( todayDate ).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String shortOfDays = String.valueOf((todayMillionSeconds-dateMillionSeconds)/(1000*60*60*24));
        return shortOfDays;
    }

}
