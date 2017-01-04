package com.drugoogle.sellscrm.workorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.common.Consts;
import com.drugoogle.sellscrm.data.WorkOrderInfo;
import com.drugoogle.sellscrm.data.type.WorkOrderStatus;
import com.drugoogle.sellscrm.data.type.WorkOrderType;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by ydwang on 2016/4/7.
 */
public class WorkOrderAdapter extends ArrayAdapter<WorkOrderInfo> {
    private int resourceId;

    public WorkOrderAdapter(Context context, int textViewResourceId, List<WorkOrderInfo> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WorkOrderInfo item = getItem(position); // 获取当前项的WorkOrder实例
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.itemOrderCustomer = (TextView) view.findViewById(R.id.item_order_customer);
            viewHolder.itemOrderTime = (TextView) view.findViewById(R.id.item_order_time);
            viewHolder.itemOrderType = (TextView) view.findViewById(R.id.item_order_type);
            viewHolder.itemOrderDescription = (TextView) view.findViewById(R.id.item_order_description);
            viewHolder.itemOrderState = (TextView) view.findViewById(R.id.item_order_state);
            view.setTag(viewHolder); // 将ViewHolder存储在View中
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag(); // 重新获取ViewHolder
        }
        viewHolder.itemOrderCustomer.setText(item.customerName);
        if (item.publishTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(Consts.DATE_FORMAT);
            String publishTime = sdf.format(Long.parseLong(item.publishTime));
            viewHolder.itemOrderTime.setText(publishTime);
        }
        if (item.type != null) {
            viewHolder.itemOrderType.setText(WorkOrderType
                    .getWorkOrderType(Integer.parseInt(item.type)));
        }
        if (item.status != null) {
            viewHolder.itemOrderState.setText(WorkOrderStatus
                    .getWorkOrderStatus(Integer.parseInt(item.status)));
            viewHolder.itemOrderState.setTextColor(CommonUtils.GetIntArrayValue(
                    R.array.order_state_color, Integer.parseInt(item.status)));
        }
        viewHolder.itemOrderDescription.setText(item.description);
        return view;
    }

    class ViewHolder {
        TextView itemOrderCustomer;
        TextView itemOrderTime;
        TextView itemOrderType;
        TextView itemOrderDescription;
        TextView itemOrderState;
    }


}
