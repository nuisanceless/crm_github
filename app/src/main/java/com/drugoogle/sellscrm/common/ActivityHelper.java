package com.drugoogle.sellscrm.common;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;


/**
 * Created by wgh on 2016/3/28.
 */
@EBean
public class ActivityHelper
{
    @RootContext
    Context mContext;


    private Dialog mLoadDialog;
    @UiThread
    public void showLoadingDialog(String msg, DialogInterface.OnCancelListener cancelListener)
    {
       showLoadingDialogInternal(msg, cancelListener);
    }
    @UiThread
    public void showLoadingDialog (String msg)
    {
        showLoadingDialogInternal(msg, null);
    }

    protected void showLoadingDialogInternal (String msg, DialogInterface.OnCancelListener cancelListener)
    {
        dismissLoadingDialog();
        //mLoadDialog = new Dialog(mContext, R.style.CustomProgressDialog);
//        View contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_loading, null);
//        mLoadDialog.setContentView(contentView);
//        mLoadDialog.setCancelable(true);
//        if (msg != null)
//        {
//            TextView tv = (TextView)contentView.findViewById(R.id.text_view);
//            tv.setText(msg);
//        }
        mLoadDialog = new ProgressDialog(mContext);
        if (TextUtils.isEmpty(msg))
        {
            msg = mContext.getString(R.string.msg_waiting);
        }
        ((ProgressDialog)mLoadDialog).setMessage(msg);
        mLoadDialog.setOnCancelListener(cancelListener);
        mLoadDialog.setCancelable(cancelListener != null);
        mLoadDialog.setCanceledOnTouchOutside(false);
        mLoadDialog.show();
    }
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void dismissLoadingDialog ()
    {
        if (mLoadDialog != null && mLoadDialog.isShowing())
            mLoadDialog.dismiss();

        mLoadDialog = null;
    }

    @UiThread
    public void showToast(String text)
    {
        if (text != null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View root = inflater.inflate(R.layout.toast, null);
            TextView view = (TextView) root.findViewById(R.id.toast_text);
            view.setText(text);
            Toast toast = new Toast(mContext);
            toast.setView(root);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @UiThread
    public void showNormalToast(String text)
    {
        if (text != null)
        {
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        }
    }
}
