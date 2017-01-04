package com.tmindtech.log;

import android.content.Context;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger
{
    public static final String LOG_TAG_ERROR = "error";

    protected Context context;
    private String prefix;
    private LogFile logFile;
    private Timer uploadTimer;
    private UploadThread uploadThread;
    private long limitFileSize = 1024 * 50;//50KB

    private static final int UPLOAD_INTERVAL_SECONDS = 2 * 3600;
    private class LogMessage
    {
        LogMessage(String tag, String content)
        {
            this.tag = tag;
            this.content = content;
        }

        String tag;
        String content;
    }

    private static Logger defaultLogger;
    public static void initDefaultLogger(Context context)
    {
        if (defaultLogger == null)
        {
            defaultLogger = new Logger(context, context.getPackageName());
        }
    }
    public static Logger defaultLogger()
    {
        return defaultLogger;
    }
    public Logger(Context context, String typePrefix)
    {
        this.context = context;
        prefix = typePrefix;
        uploadThread = new UploadThread();
        uploadThread.start();
        setUploadInterval(UPLOAD_INTERVAL_SECONDS);
    }

    public void close()
    {
        if (uploadThread != null)
        {
            uploadThread.quitFlag.set(true);
            synchronized (uploadThread)
            {
                uploadThread.notify();
            }
            try
            {
                uploadThread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            uploadThread = null;
        }
    }

    public void setFileLimitSize(long bytes)
    {
        if (bytes < 1024)
        {
            bytes = 1024;
        }
        limitFileSize = bytes;
    }
    public void setUploadInterval(long seconds)
    {
        if (uploadTimer != null)
        {
            uploadTimer.cancel();
            uploadTimer = null;
        }
        if (seconds > 0)
        {
            uploadTimer = new Timer();
            uploadTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    if (uploadThread != null)
                    {
                        synchronized (uploadThread)
                        {
                            uploadThread.notify();
                        }
                    }
                }
            }, seconds * 1000, seconds * 1000);
        }
    }


    public synchronized void log(String tag, String message)
    {
        if (!logEnabled())
        {
            return;
        }
        if (logFile != null && logFile.isSizeFull(limitFileSize))
        {
            logFile = null;
        }
        if (logFile == null)
        {
            logFile = LogFile.getNew(getLogPath(), prefix);
        }
        if (logFile != null)
        {
            logFile.writeLine(tag, message);
        }
    }

    public File getLogPath()
    {
        File dir = context.getExternalCacheDir();
        dir.mkdirs();
        if (!dir.exists())
        {
            dir = context.getCacheDir();
        }
        return new File(dir, "logs");
    }

    public List<File> listUploadLogFiles()
    {
        File dir = getLogPath();
        String[] files = dir.list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String filename)
            {
//                if (logFile == null)
//                {
//                    return true;
//                }
//                else
//                {
//                    return !logFile.file.getName().equalsIgnoreCase(filename);
//                }
                return true;
            }
        });
        ArrayList<File> ret = new ArrayList<>();
        if (files != null)
        {
            for (String name : files)
            {
                ret.add(new File(dir, name));
            }
        }
        return ret;
    }

    protected void onUploadBackground(Context context, List<File> files)
    {
    }
    protected boolean logEnabled()
    {
        return true;
    }
    protected class UploadThread extends  Thread
    {
        private AtomicBoolean quitFlag = new AtomicBoolean(false);

        private void quit()
        {
            quitFlag.set(true);
            synchronized (this)
            {
                notify();
            }
        }
        @Override
        public void run()
        {
            while (!quitFlag.get())
            {
                synchronized (this)
                {
                    try
                    {
                        wait();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                synchronized (Logger.this)
                {
                    logFile = null;
                }
                List<File> files = listUploadLogFiles();
                if (files.size() == 0)
                {
                    continue;
                }
                ((Logger)(Logger.this)).onUploadBackground(context, files);
            }
        }
    }
}

