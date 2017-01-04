package com.drugoogle.sellscrm.common;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;

/**
 * Created by wuguohao on 4/23/16.
 * 添加拜访计划等页面中选择拜访类型，拜访周期等弹出框（ListPopupWindow）list adapter
 */
public class PopupWindowListAdapter extends BaseAdapter
{
    private String[] mDataList;

    private Context mContext;
    private int mSelectItemIndex = -1;

    public PopupWindowListAdapter (Context context, String[] list, int selectItemIndex)
    {
        mContext = context;
        mDataList = list;
        mSelectItemIndex = selectItemIndex - 1;
    }

    public void setSelectItemIndex (int selectItemIndex)
    {
        mSelectItemIndex = selectItemIndex - 1;
    }


    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.length;
    }

    @Override
    public Object getItem(int position) {
        return mDataList == null ? null : mDataList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null)
        {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_popup_window, null, false);
            holder.listItemLayout = convertView.findViewById(R.id.list_item_layout);
            holder.pointIv = (ImageView)convertView.findViewById(R.id.yellow_round_dot);
            holder.textView = (TextView)convertView.findViewById(R.id.content_tv);
            holder.dividerLine = convertView.findViewById(R.id.divider_line);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        final String item = mDataList[position];
        holder.textView.setText(item);
        holder.pointIv.setVisibility(View.GONE);
        if (position == mSelectItemIndex)
        {
            holder.pointIv.setVisibility(View.VISIBLE);
            holder.listItemLayout.setBackgroundColor(mContext.getResources().getColor(R.color.light_blue));
        }
        else
        {
            holder.pointIv.setVisibility(View.INVISIBLE);
            holder.listItemLayout.setBackgroundColor(Color.WHITE);
        }

        //最后一条下划线隐藏掉
        if (position == mDataList.length - 1)
        {
            holder.dividerLine.setVisibility(View.INVISIBLE);
        }
        else
        {
            holder.dividerLine.setVisibility(View.VISIBLE);
        }


        return convertView;
    }

    private class ViewHolder
    {
        View listItemLayout;
        ImageView pointIv;
        TextView textView;
        View dividerLine;
    }
}
