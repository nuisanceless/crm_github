package com.drugoogle.sellscrm.Utils;

import com.drugoogle.sellscrm.common.MyApplication_;
import com.tmindtech.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by fanzhang on 16/10/26.
 */

public class CacheUtils
{
    private static String TEXT_FILE_NAME = "text.txt";
    private static String IMAGE_FILE_NAME = "image.jpg";
    public static class VisitAttachment
    {
        public String file;
        public String text;
    }
    public static void DeleteVisitSavedAttachment(int visitID)
    {
        File dir = GetAttachmentSavedPath(visitID);
        File textFile = new File(dir, TEXT_FILE_NAME);
        File imageFile = new File(dir, IMAGE_FILE_NAME);
        textFile.delete();
        imageFile.delete();
        dir.delete();
    }
    public static VisitAttachment GetVisitSavedAttachment(int visitID)
    {
        File dir = GetAttachmentSavedPath(visitID);
        if (!dir.exists())
        {
            return null;
        }
        VisitAttachment attachment = new VisitAttachment();
        File textFile = new File(dir, TEXT_FILE_NAME);
        File imageFile = new File(dir, IMAGE_FILE_NAME);
        if (textFile.exists())
        {
            attachment.text = "";
            BufferedReader bufferedReader = null;
            try
            {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null)
                {
                    attachment.text = attachment.text + lineTxt + "\n";
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                Utils.silenceClose(bufferedReader);
            }
        }
        if (imageFile.exists())
        {
            attachment.file = imageFile.getAbsolutePath();
        }
        return attachment;
    }

    private static File GetAttachmentSavedPath(int visitID)
    {
        File dir = new File(MyApplication_.getInstance().getExternalFilesDir(null), "Visit/" + visitID);
        return dir;
    }

    public static void setVisitImage(int visitID, String filePath)
    {
        File dir = GetAttachmentSavedPath(visitID);
        File dst = new File(dir, IMAGE_FILE_NAME);
        if (filePath == null)
        {
            dst.delete();
            return;
        }

        File org = new File(filePath);
        if (org.exists())
        {
            dir.mkdirs();
            Utils.copyFile(org, dst);
        }
    }

    public static void setVisitText(int visitID, String text)
    {
        File dir = GetAttachmentSavedPath(visitID);
        dir.mkdirs();
        File textFile = new File(dir, TEXT_FILE_NAME);
        if (text == null)
        {
            textFile.delete();
            return;
        }
        BufferedWriter bufferedWriter = null;
        try
        {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(textFile)));
            bufferedWriter.write(text);
            bufferedWriter.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            Utils.silenceClose(bufferedWriter);
        }
    }
}
