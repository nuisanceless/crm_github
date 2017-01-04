package com.drugoogle.sellscrm.common;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;


/**
 * Created by wuguohao on 15-5-21.
 */
public class CommonDialog extends Dialog
{

    TextView mCancel;
    TextView mConfirm;
    TextView mContent;
    TextView mContentBlue;

    public CommonDialog(Context context) {
        this(context, R.style.add_dialog);
    }

    public CommonDialog(Context context, int theme) {
        super(context, theme);
        LayoutInflater inflaterDl = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout)inflaterDl.inflate(R.layout.common_confirm_dialog, null );
        getWindow().setContentView(layout);

        mCancel = (TextView) layout.findViewById(R.id.cancel);
        mConfirm = (TextView) layout.findViewById(R.id.confirm);
        mContent = (TextView) layout.findViewById(R.id.content);
        mContentBlue = (TextView) layout.findViewById(R.id.content_blue);
    }

    protected CommonDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public CommonDialog builder ()
    {
        return this;
    }

    public CommonDialog setContent (String content)
    {
        mContent.setText(content);
        return this;
    }

    public CommonDialog setBlueContent(String text) {
        mContentBlue.setText(text);
        mContentBlue.setVisibility(View.VISIBLE);
        return this;
    }

    public CommonDialog setCancelString (String cancelStr)
    {
        mCancel.setText(cancelStr);
        return this;
    }

    public CommonDialog setConfirmString (String confirmStr)
    {
        mConfirm.setText(confirmStr);
        return this;
    }

    public CommonDialog setOnCancelClickListener (View.OnClickListener listener)
    {
        mCancel.setOnClickListener(listener);
        return this;
    }

    public CommonDialog setOnConfirmClickListener (View.OnClickListener listener)
    {
        mConfirm.setOnClickListener(listener);
        return this;
    }



}
