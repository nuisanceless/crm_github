package com.drugoogle.sellscrm.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.LogTagResponse;
import com.drugoogle.sellscrm.selfinfo.Account;
import com.tmindtech.log.Logger;

import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.List;

/**
 * Created by fanzhang on 16/9/19.
 */
public class CrmLogger extends Logger
{

    public CrmLogger(Context context, String typePrefix)
    {
        super(context, typePrefix);
        checkHandler.sendEmptyMessage(0);
    }

    public void checkLogEnable()
    {
        if (getLogStatusTask != null)
        {
            getLogStatusTask.cancel(true);
            getLogStatusTask = null;
        }
        getLogStatusTask = new GetLogStatusTask();
        getLogStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    boolean isLogEnabled = false;

    @Override
    protected void onUploadBackground(Context context, List<File> files)
    {
//        for (File f : files)
//        {
//            String path = f.getAbsolutePath();
//            if (!path.endsWith(".upload"))
//            {
//                f.renameTo(new File(path + ".upload"));
//            }
//        }
        log("Logger", "Upload Log....");
        String token = Account.getInstance().getToken();
        if (token == null)
        {
            log("Logger", "Upload Log token is NULL");
            return;
        }

        for (File file : files)
        {
            log("Logger", "Upload file " + file.getName());
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            if (!file.exists())
            {
                log("Logger", "File not exist " + file.getName());
                continue;

            }
            String path = file.getAbsolutePath();
            //if (!path.endsWith(".upload"))
            {
                parts.add("file", new FileSystemResource(file));
                BaseResponse response = MyApplication_.getInstance().restClient().uploadLog(parts, token);
                if (!BaseResponse.hasError(response))
                {
                    log("Logger", "Upload success : " + file.getName());
                    file.delete();
                    //file.renameTo(new File(path + ".upload"));

                }
                else
                {
                    log("Logger", "Upload failed" + file.getName());
                }

            }

        }
    }

    @Override
    protected boolean logEnabled()
    {
        return isLogEnabled;
    }

    //每过一段时间检查一下log状体
    private static  final int CHECK_INTERVA = 5 * 60 * 1000;
    Handler checkHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            checkLogEnable();
            removeMessages(0);
            sendEmptyMessageDelayed(0, CHECK_INTERVA);
        }
    };
    GetLogStatusTask getLogStatusTask;
    class GetLogStatusTask extends AsyncTask<String, Integer, LogTagResponse>
    {
        @Override
        protected LogTagResponse doInBackground(String... params)
        {
            String token = Account.getInstance().getToken();
            if (token == null)
            {
                return null;
            }
            return MyApplication_.getInstance().restClient().getLogTag(token);
        }

        @Override
        protected void onPostExecute(LogTagResponse resp)
        {
            if (!BaseResponse.hasError(resp))
            {
                isLogEnabled = (resp.data != 0);
            }
        }
    }
}
