package com.drugoogle.sellscrm.visit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.common.MyApplication;
import com.drugoogle.sellscrm.common.MyApplication_;
import com.drugoogle.sellscrm.data.CalendarCellInfo;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;

@EBean
public class VisitOverlayDrawable extends Drawable
{
    private int alpha = 255;
    private boolean hasCountTask = false;
    private boolean hasSysTask = false;
    private Paint paint  = new Paint();
    private int width;
    private int height;
    private int colorNormal;
    private int colorCount;
    private int colorSystem;

    VisitOverlayDrawable(Context context)
    {
        colorNormal = context.getResources().getColor(R.color.yellow);
        colorSystem = context.getResources().getColor(R.color.main_red);
        colorCount = context.getResources().getColor(R.color.text_cursor_blue);

        final float scale = context.getResources().getDisplayMetrics().density;
        width =  (int) (20 * scale + 0.5f);
        height = (int) (1.3 * scale + 0.5f);
    }
    public void setItemInfo(CalendarCellInfo info)
    {
        hasCountTask = info.time > 0;
    }
    @Override
    public void setAlpha(int alpha)
    {
        this.alpha = alpha;
    }

    @Override
    public void draw(Canvas canvas)
    {
        Rect bounds = getBounds();
        int divide = 1;
        if (hasCountTask)
        {
            divide++;
        }
        if (hasSysTask)
        {
            divide++;
        }
        int endPos[] = new int[divide];
        int length = bounds.width() / divide;
        for (int i = 0; i < divide; i++)
        {
            endPos[i] = bounds.left + length * (i + 1);
        }
        int index = 0;
        Rect rcLine = new Rect(bounds);

        rcLine.right = endPos[index++];
        paint.setColor(colorNormal);
        canvas.drawRect(rcLine, paint);

        if (hasCountTask)
        {
            rcLine.left = rcLine.right;
            rcLine.right = endPos[index++];
            paint.setColor(colorCount);
            canvas.drawRect(rcLine, paint);
        }

        if (hasSysTask)
        {
            rcLine.left = rcLine.right;
            rcLine.right = endPos[index++];
            paint.setColor(colorSystem);
            canvas.drawRect(rcLine, paint);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter)
    {

    }

    @Override
    public int getOpacity()
    {
        return alpha;
    }

    @Override
    public int getIntrinsicHeight()
    {
        return height;
    }

    @Override
    public int getIntrinsicWidth()
    {
        return width;
    }
}
