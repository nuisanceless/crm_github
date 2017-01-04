package com.drugoogle.sellscrm.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ImageUtils
{
	public static String compressImg(Context context, String originalFile)
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(originalFile, opt);

		final int minSize = 2000;
		if (opt.outWidth <= minSize && opt.outHeight <= minSize)
		{
			return originalFile;
		}
		int width = opt.outWidth;
		int height = opt.outHeight;

		do
		{
			width /= 2;
			height /= 2;
		}
		while (width > minSize || height > minSize);


		File tempFile = new File(context.getCacheDir(), "event_upload_temp.png");
		if (tempFile.exists())
		{
			tempFile.delete();
		}

		FileOutputStream out = null;
		Bitmap bmpSrc = null;
		try
		{
			out = new FileOutputStream(tempFile);
			bmpSrc = ImageUtils.decodeBitmap(originalFile, ImageDecodeType.SIZE_FIT_IN, width, height, Bitmap.Config.ARGB_8888, null, null);
			bmpSrc.compress(Bitmap.CompressFormat.JPEG, 100, out);
		}
		catch (Exception e)
		{
			return null;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (Exception e)
			{

			}
			if (bmpSrc != null)
			{
				bmpSrc.recycle();
			}
		}
		return tempFile.getAbsolutePath();
	}
	/**
	 * Get the fit-in area of the rcOrg in rcBound.
	 *
	 * @param rcBound
	 *            The boundary rectangle.
	 * @param rcOrg
	 *            Original rectangle of the rectangle
	 * @param outFitIn
	 *            The fin-in rectangle in the rcBound.
	 * @param bScaleToFit
	 *            If set to true, the fit-in rect will be scale if both the org
	 *            width and height is lesser than the boundary.
	 * @return scale factor need to be down-scale. return 0 if failed.
	 */

	public static float getFitInRect(Rect rcBound, Rect rcOrg, Rect outFitIn, boolean bScaleToFit)
	{
		if (null == rcBound || null == rcOrg || null == outFitIn)
		{
			return 0.f;
		}

		if (rcBound.width() <= 0 || rcBound.height() <= 0)
		{
			outFitIn.set(0, 0, 0, 0);
			return 0.f;
		}
		int bw = rcBound.width();
		int bh = rcBound.height();
		int ow = rcOrg.width();
		int oh = rcOrg.height();

		int nXRatio = 0;
		int nYRatio = 0;

		float fDownScale = 1.f;

		if (ow > bw || oh > bh || bScaleToFit)
		{
			nXRatio = (bw * 100000) / ow;
			nYRatio = (bh * 100000) / oh;
			int nNew = 0;
			if (nXRatio <= nYRatio)
			{// adjust by XRatio
				outFitIn.left = rcBound.left;
				outFitIn.right = rcBound.right;
				outFitIn.top = rcBound.top + ((bh - (oh * nXRatio) / 100000) >> 1);
				nNew = ((oh * nXRatio) + 50000) / 100000;
				outFitIn.bottom = outFitIn.top + nNew;
				if (nNew == 0)
					outFitIn.top--;
				fDownScale = 100000.f / nXRatio;
			}
			else
			{// adjust by YRatio
				outFitIn.top = rcBound.top;
				outFitIn.bottom = rcBound.bottom;
				outFitIn.left = rcBound.left + ((bw - (ow * nYRatio) / 100000) >> 1);
				nNew = ((ow * nYRatio) + 50000) / 100000;
				outFitIn.right = outFitIn.left + nNew;
				if (nNew == 0)
					outFitIn.left--;
				fDownScale = 100000.f / nYRatio;
			}
		}
		else
		{
			outFitIn.left = rcBound.left + ((bw - ow) >> 1);
			outFitIn.right = outFitIn.left + ow;
			outFitIn.top = rcBound.top + ((bh - oh) >> 1);
			outFitIn.bottom = outFitIn.top + oh;
			fDownScale = 1.f;
		}
		return fDownScale;
	}

	/**
	 * Get the fit-out area in the original rectangle corresponding the boundary
	 * rectangle
	 *
	 * @param rcBound
	 *            The boundary rectangle
	 * @param rcOrg
	 *            The Original rectangle
	 * @param outFitOut
	 *            Fit-out area in the original rectangle
	 * @param bScaleToFit
	 *            If set to true, the fit-in rect will be scale if both the org
	 *            width and height is lesser than the boundary.
	 * @return scale factor need to be down-scale. return 0 if failed.
	 */
	public static float getFitOutRect(Rect rcBound, Rect rcOrg, Rect outFitOut, boolean bScaleToFit)
	{
		if (null == rcBound || null == rcOrg || null == outFitOut)
		{
			return 0.f;
		}
		if (rcBound.width() <= 0 || rcBound.height() <= 0)
		{
			outFitOut.set(0, 0, 0, 0);
			return 0.f;
		}
		int bw = rcBound.width();
		int bh = rcBound.height();
		int ow = rcOrg.width();
		int oh = rcOrg.height();

		float fDownScale = 1.f;

		if (ow <= bw || oh <= bh)
		{
			if (!bScaleToFit)
			{
				if (ow <= bw && oh <= bh)
				{
					outFitOut.left = outFitOut.top = 0;
					outFitOut.right = rcOrg.width();
					outFitOut.bottom = rcOrg.height();
					return fDownScale;
				}
				else if (ow <= bw)
				{
					outFitOut.left = 0;
					outFitOut.right = ow;
					outFitOut.top = ((oh - bh) >> 1);
					outFitOut.bottom = outFitOut.top + bh;
					return fDownScale;
				}
				else if (oh <= bh)
				{
					outFitOut.top = 0;
					outFitOut.bottom = oh;
					outFitOut.left = ((ow - bw) >> 1);
					outFitOut.right = outFitOut.left + bw;
					return fDownScale;
				}
			}
		}

		boolean bByX = false;
		int nTmp = 0;

		int nZoomX = (ow * 100000) / bw;
		int nZoomY = (oh * 100000) / bh;

		if (nZoomX < nZoomY)
		{
			bByX = true;
		}

		if (bByX)
		{
			outFitOut.left = 0;
			outFitOut.right = ow;
			nTmp = (bh * nZoomX) / 100000;
			outFitOut.top = (rcOrg.top + rcOrg.bottom - nTmp) >> 1;
			outFitOut.bottom = outFitOut.top + nTmp;
			fDownScale = nZoomX / 100000.f;
		}
		else
		{
			outFitOut.top = 0;
			outFitOut.bottom = oh;
			nTmp = (bw * nZoomY) / 100000;
			outFitOut.left = (rcOrg.left + rcOrg.right - nTmp) >> 1;
			outFitOut.right = outFitOut.left + nTmp;
			fDownScale = nZoomY / 100000.f;
		}
		return fDownScale;
	}

    public static Bitmap decodeBitmap(String path, int decodeType, int width, int height, Bitmap.Config config, Point outOrgSize, Options opt)
    {
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(new File(path));
            return  decodeBitmap(in.getFD(), decodeType, width, height, config, outOrgSize, opt);
        }
        catch (Exception e)
        {
            return null;
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (Exception e2)
            {

            }
        }

    }

	public static Bitmap decodeBitmap(FileDescriptor fd, int decodeType, int width, int height, Bitmap.Config config, Point outOrgSize, Options opt)
	{
		if (null == fd)
		{
			return null;
		}
		if (opt == null)
		{
			opt = new Options();
		}
		opt.inPreferredConfig = config;
		opt.inDither = true;
		if (decodeType == ImageDecodeType.SIZE_NOSCALE)
		{
			Bitmap bmp = null;
			try
			{
				bmp = BitmapFactory.decodeFileDescriptor(fd, null, opt);
			}
			catch (OutOfMemoryError err)
			{
				return null;
			}
			
			if (outOrgSize != null)
			{
				outOrgSize.x = bmp.getWidth();
				outOrgSize.y = bmp.getHeight();
			}
			return bmp;
		}
		else if (decodeType == ImageDecodeType.SIZE_FITXY)
		{
			opt.inJustDecodeBounds = true;
			try
			{
				BitmapFactory.decodeFileDescriptor(fd, null, opt);
			}
			catch (OutOfMemoryError err)
			{
				return null;
			}
			int nextWidth = opt.outWidth >> 1;
			int nextHeight = opt.outHeight >> 1;
			int sampleSize = 1;

			while (nextWidth > width && nextHeight > height)
			{
				sampleSize <<= 1;
				nextWidth >>= 1;
				nextHeight >>= 1;
			}

			opt.inSampleSize = sampleSize;
			opt.inJustDecodeBounds = false;
			Bitmap bmp = null;
			try
			{
				bmp = BitmapFactory.decodeFileDescriptor(fd, null, opt);
			}
			catch (OutOfMemoryError err)
			{
				return null;
			}

			if (bmp != null)
			{
				if (opt.outWidth != width || opt.outHeight != height)
				{
					Bitmap temp = Bitmap.createScaledBitmap(bmp, width, height, true);
					if (temp != bmp)
					{
						bmp.recycle();
					}
					bmp = temp;
				}
			}
			return bmp;
		}

		Bitmap bmpResult = null;
		opt.inJustDecodeBounds = true;
		try
		{
			BitmapFactory.decodeFileDescriptor(fd, null, opt);
		}
		catch (OutOfMemoryError err)
		{
			return null;
		}

		if (outOrgSize != null)
		{
			outOrgSize.x = opt.outWidth;
			outOrgSize.y = opt.outHeight;
		}

		if (opt.outHeight <= 0 || opt.outWidth <= 0)
		{
			return null;
		}

		Rect rcOrg = new Rect();
		Rect rcBound = new Rect();
		Rect rcFit = new Rect();

		rcOrg.set(0, 0, opt.outWidth, opt.outHeight);
		rcBound.set(0, 0, width, height);
		float fDownScale = 1.f;
		if ((decodeType & ImageDecodeType.SIZE_FIT_OUT) != 0)
		{
			if ((decodeType & ImageDecodeType.SIZE_SCALE_BEFORE_FIT) == 0)
			{
				fDownScale = getFitOutRect(rcBound, rcOrg, rcFit, false);
			}
			else
			{
				fDownScale = getFitOutRect(rcBound, rcOrg, rcFit, true);
			}
		}
		else if ((decodeType & ImageDecodeType.SIZE_FIT_IN) != 0)
		{
			if ((decodeType & ImageDecodeType.SIZE_SCALE_BEFORE_FIT) == 0)
			{
				fDownScale = ImageUtils.getFitInRect(rcBound, rcOrg, rcFit, false);
			}
			else
			{
				fDownScale = ImageUtils.getFitInRect(rcBound, rcOrg, rcFit, true);
			}
		}

		int maxScale = Math.round(fDownScale);
		int nextSample = 1;
		while (nextSample <= maxScale)
		{
			nextSample <<= 1;
		}
		opt.inSampleSize = nextSample >> 1;
		opt.inJustDecodeBounds = false;

		try
		{
			bmpResult = BitmapFactory.decodeFileDescriptor(fd, null, opt);
		}
		catch (OutOfMemoryError err)
		{
			return null;
		}
		if (bmpResult == null)
		{
			return null;
		}

		rcOrg.set(0, 0, opt.outWidth, opt.outHeight);
		if ((decodeType & ImageDecodeType.SIZE_FIT_OUT) != 0)
		{
			// ���¼���Fitout
			fDownScale = ImageUtils.getFitOutRect(rcBound, rcOrg, rcFit, (decodeType & ImageDecodeType.SIZE_SCALE_BEFORE_FIT) != 0);
			rcOrg.set(rcFit);
			rcBound.set(0, 0, (int) (rcFit.width() / fDownScale), (int) (rcFit.height() / fDownScale));
		}
		else
		{
			if (rcFit.width() == bmpResult.getWidth() && rcFit.height() == bmpResult.getHeight())
			{
				return bmpResult;
			}
			rcBound.set(0, 0, rcFit.width(), rcFit.height());
		}

		Bitmap bmpTmp = Bitmap.createBitmap(rcBound.width(), rcBound.height(), config);

		if (null == bmpTmp)
		{
			bmpResult.recycle();
			bmpResult = null;
		}
		else
		{
			Canvas cav = new Canvas(bmpTmp);
			Paint paint = new Paint();
			paint.setDither(true);
			cav.setDensity(Bitmap.DENSITY_NONE);
			cav.drawBitmap(bmpResult, rcOrg, rcBound, paint);
			bmpResult.recycle();
			bmpResult = bmpTmp;
		}
		return bmpResult;
	}
}
