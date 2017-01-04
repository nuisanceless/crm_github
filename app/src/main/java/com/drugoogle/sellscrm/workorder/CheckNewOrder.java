package com.drugoogle.sellscrm.workorder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.drugoogle.sellscrm.common.MyApplication;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.response.NewWorkOrderCountResponse;
import com.drugoogle.sellscrm.rest.MyRestClient;
import com.drugoogle.sellscrm.selfinfo.Account;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ydwang on 2016/6/15.
 */
public class CheckNewOrder {

    protected Account account = Account.getInstance();
    protected MyApplication mApp = MyApplication_.getInstance();
    protected MyRestClient mRestClient = mApp.restClient();
    protected Context context = MyApplication_.getInstance().getApplicationContext();
    Timer mTimer;
    TimerTask mTimerTask;

    public void startCheck(final OrderCheckCallback callback) {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                CheckBackground( callback );
            }
        };
        //开始一个定时任务
        mTimer.schedule( mTimerTask, 0, 1 * 60 * 1000 );
    }

    Handler checkHandler = new Handler() {
        public void handleMessage(Message message) {
            Content content = (Content) message.obj;
            //回调
            content.getListener().onFinish( content.getResp() );
        }
    };

    public void CheckBackground(OrderCheckCallback callback) {
        NewWorkOrderCountResponse resp = mRestClient.getNewWorkOrderCount(
                account.getToken() );
        Content content = new Content();
        content.setListener( callback );
        content.setResp( resp );
        Message message = new Message();
        message.obj = content;
        checkHandler.sendMessage( message );
    }

    public void stopCheck() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    class Content {

        public NewWorkOrderCountResponse resp;
        public OrderCheckCallback listener;

        public NewWorkOrderCountResponse getResp() {
            return resp;
        }

        public void setResp(NewWorkOrderCountResponse resp) {
            this.resp = resp;
        }

        public OrderCheckCallback getListener() {
            return listener;
        }

        public void setListener(OrderCheckCallback listener) {
            this.listener = listener;
        }
    }
}
