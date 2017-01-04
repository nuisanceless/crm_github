package com.tmindtech.log;

import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.LogUtils;
import com.drugoogle.sellscrm.common.MyApplication_;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fanzhang on 16/9/19.
 */
class LogFile
{

    private static final SimpleDateFormat TAG_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static LogFile getNew(File dir, String logType)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd—HH-mm-ss");
        Date date = new Date();
        String dateStr = formatter.format(date);
        File file = new File(dir, logType + "_" + dateStr + ".log");

        try
        {
            dir.mkdirs();
            file.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        LogFile info = new LogFile();
        info.file = file;
        info.date = date;
        info.writeLine("BRAND :\t\t" + CommonUtils.getDeviceBrand());
        info.writeLine("MODEL :\t\t" + CommonUtils.getDeviceModel());
        info.writeLine("SDD :\t\t" + CommonUtils.getSdkVersion());
        info.writeLine("CRM Version : \t\t" + MyApplication_.getInstance().getVersion());
        info.writeLine("============================Log file start=============================");
        return info;
    }

    private LogFile()
    {

    }

    public synchronized void writeLine(String tag, String line)
    {
        if (tag != null)
        {
            String dateString = TAG_DATE_FMT.format(new Date());
            StringBuffer builder = new StringBuffer();
            builder.append("<");
            builder.append(tag);
            builder.append("> ");
            builder.append(dateString);
            builder.append(" : ");
            builder.append(line);
            line = builder.toString();
        }
        BufferedWriter bw = null;
        try
        {
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(line);
            bw.write("\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bw != null)
            {
                try
                {
                    bw.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    public void writeLine(String line)
    {
        writeLine(null, line);
    }

    public boolean  isSizeFull(long limitSize)
    {
        return file.length() > limitSize;
    }

    public File file;
    public Date date;
    public long size;
}
