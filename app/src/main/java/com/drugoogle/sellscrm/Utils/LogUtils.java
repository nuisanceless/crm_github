package com.drugoogle.sellscrm.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.drugoogle.sellscrm.common.MyApplication;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.LogTagResponse;
import com.drugoogle.sellscrm.data.type.CommonType;
import com.drugoogle.sellscrm.rest.MyRestClient;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;


/**
 * Created by wgh on 2016/7/6.
 */
public class LogUtils
{
    public static final String LOG_FILE_NAME = "crm_log.txt";
    public static final String NEED_UPLOAD_LOG_FILE_NAME = "crm_log_need_upload.txt";
    public static final String LOG_FILE_DIR = Environment.getExternalStorageDirectory().getPath() + "/crm";

    private MyApplication mApp = MyApplication_.getInstance();
    private MyRestClient mRestClient = mApp.restClient();
    private Context mContext = MyApplication_.getInstance().getApplicationContext();
    private SharedPreferences mPrefUtils = mContext.getSharedPreferences("PrefUtils", 0);
    private int ifNeedUploadLog;

    private static LogUtils mLogUtils = null;
    private LogUtils () {
        ifNeedUploadLog = mPrefUtils.getInt("logTag", CommonType.NOT_NEET_UPLOAD_LOG);
    }

    public static LogUtils getInstance ()
    {
        if (mLogUtils == null)
        {
            mLogUtils = new LogUtils();
        }
        return mLogUtils;
    }




    public int getIfNeedUploadLog ()
    {
        return ifNeedUploadLog;
    }



    public void getLogTag (Context context)
    {
        mMsg = new Message();
        mMsg.what = GET_LOG_TAG_WHAT;
        mMsg.obj = context;
        mHandler.sendMessage(mMsg);
    }

    Message mMsg;
    private final int GET_LOG_TAG_WHAT = 62;
    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case GET_LOG_TAG_WHAT:
                    getLogTagBk((Context)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private void getLogTagBk (final Context context)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogTagResponse resp;
                resp = mRestClient.getLogTag(Account.getInstance().getToken());
                if (!BaseResponse.hasErrorWithOperation(resp, context))
                {
                    mHandler.removeMessages(GET_LOG_TAG_WHAT);
                    mPrefUtils.edit().putInt("logTag", resp.data);
                    ifNeedUploadLog = resp.data;
                    if (resp.data == CommonType.NOT_NEET_UPLOAD_LOG)
                    {
                        deleteFile(LOG_FILE_NAME);
                    }
                }
                else
                {
                    mHandler.sendMessageDelayed(mMsg, 2000);
                }
            }
        }).start();
    }





    /**
     * 查看log文件是否存在
     * @return true 存在，false 不存在
     * */
    public static boolean checkLogFileExist ()
    {
        String filePath = LOG_FILE_DIR + "/" + LOG_FILE_NAME;
        File file = new File(filePath);
        return file.exists();
    }


    /**
     * 重命名日志文件
     * */
    public void renameLogFile ()
    {
        FileUtils.renameFile(LOG_FILE_DIR, LOG_FILE_NAME, NEED_UPLOAD_LOG_FILE_NAME);
    }

    /**
     * 删除文件
     * */
    public void deleteFile (String name)
    {
        FileUtils.delFile(LOG_FILE_DIR + "/" + name);
    }



    public interface UploadLogFinishCallback
    {
        void onFinish (boolean error);
    }

    public void uploadLog (final UploadLogFinishCallback callback)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean uploadSuccess = false;
                MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
                File file = new File(LOG_FILE_DIR + "/" + NEED_UPLOAD_LOG_FILE_NAME);
                if (file.exists())
                {
                    parts.add("file", new FileSystemResource(file));
                    BaseResponse response = mRestClient.uploadLog(parts, Account.getInstance().getToken());
                    if (BaseResponse.hasErrorWithOperation(response, mContext)) {
                        uploadSuccess = false;
                    } else {
                        uploadSuccess = true;
                    }
                } else {
                    uploadSuccess = false;
                }

                callback.onFinish(uploadSuccess);
            }
        }).start();
    }


    public static void writeLogToFile (String tag, String content)
    {
        if (LogUtils.getInstance().getIfNeedUploadLog() == CommonType.NEED_UPLOAD_LOG)
        {
            String filePath = LOG_FILE_DIR + "/" + LOG_FILE_NAME;
            //如果filePath是传递过来的参数，可以做一个后缀名称判断； 没有指定的文件名没有后缀，则自动保存为.txt格式
            if(!filePath.endsWith(".txt") && !filePath.endsWith(".log"))
                filePath += ".txt";
            //保存文件
            File file = new File(filePath);
            String str = CommonUtils.DateFormatTwo(new Date())
                    + " -- TAG:" + tag
                    + " -- " + content + "\n";
            try
            {
                OutputStream outStream = new FileOutputStream(file, true);
                OutputStreamWriter out = new OutputStreamWriter(outStream);
                out.write(str);
                out.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        Log.e(tag, content);
    }

}
