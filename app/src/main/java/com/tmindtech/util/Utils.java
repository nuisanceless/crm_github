package com.tmindtech.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by fanzhang on 16/10/26.
 */

public class Utils
{
    public static boolean copyFile(File src, File dst)
    {
        if (src.equals(dst))
        {
            return true;
        }
        if (!src.exists())
        {
            return false;
        }
        dst.delete();

        int length = 2097152;
        FileInputStream in = null;
        FileOutputStream out = null;
        try
        {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            byte[] buffer = new byte[length];
            while (true)
            {
                int ins = in.read(buffer);
                if (ins == -1)
                {
                    out.flush();
                    break;
                }
                else
                {
                    out.write(buffer, 0, ins);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            silenceClose(in);
            silenceClose(out);
        }
    }
    public static void silenceClose(Closeable c)
    {
        try
        {
            c.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
