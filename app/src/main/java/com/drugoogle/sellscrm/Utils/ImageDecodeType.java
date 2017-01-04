package com.drugoogle.sellscrm.Utils;

import android.graphics.Point;

public class ImageDecodeType
{
	/**
	 * Image will decode to fit in mode, and scale width to boundary width and height to boundary height
	 */
	public final static int SIZE_FITXY = -1;
	/**
	 * No scale, original size
	 */
	public final static int SIZE_NOSCALE = 0;
	
	/**
	 * Combine with {@link #SIZE_FIT_IN} or {@link #SIZE_FIT_OUT}
	 * @see {@link #SIZE_FIT_IN}, {@link #SIZE_FIT_OUT}
	 */
	public final static int SIZE_SCALE_BEFORE_FIT = 1;
	/**
	 * Decode in fit in mode. if Combined with {@link #SIZE_SCALE_BEFORE_FIT} and the decoded image is smaller than the boundary, the image will be scaled to fit the boundary
	 */
	public final static int SIZE_FIT_IN = 2;
	/**
	 * Decode in fit out mode. if Combined with {@link #SIZE_SCALE_BEFORE_FIT} and the decoded image is smaller than the boundary, the image will be scaled to fit the boundary
	 */
	public final static int SIZE_FIT_OUT = 4;
	
	public	static	class	DecodeSize
	{
		public final Point mBoundary = new Point();
		public final	int		mScaleType;
		public	DecodeSize(int boundaryW, int boundaryH, int scaleType)
		{
			mBoundary.set(boundaryW, boundaryH);
			mScaleType	= scaleType;
		}
		public	DecodeSize(DecodeSize src)
		{
			mBoundary.set(src.mBoundary.x, src.mBoundary.y);
			mScaleType	= src.mScaleType;
		}
		
		@Override
		protected Object clone() throws CloneNotSupportedException
		{
			return	new DecodeSize(this);
		}
		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof DecodeSize))
			{
				return	false;
			}
			DecodeSize info = (DecodeSize)o;
			return	(info.mBoundary.x == mBoundary.x) && (info.mBoundary.y == mBoundary.y) && (info.mScaleType == mScaleType);
		}
	}
}
