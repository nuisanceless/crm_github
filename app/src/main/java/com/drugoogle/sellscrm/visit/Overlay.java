package com.drugoogle.sellscrm.visit;

import android.graphics.drawable.Drawable;

/**
 * Created by wgh on 2016/5/4.
 */
public class Overlay
{
    public static  final   int POSITION_LEFT = 1;
    public static  final   int POSITION_CENTER_HORIZONTAL = 2;
    public static  final   int POSITION_RIGHT = 4;
    public static  final   int POSITION_TOP = 8;
    public static  final   int POSITION_CENTER_VERTICAL = 16;
    public static  final   int POSITION_BOTTOM = 32;
    public int tag;
    public Drawable overlayDrawable;
    public int position = 0;
    public int paddingVertical = 0;
    public int paddingHorizontal = 0;
}
