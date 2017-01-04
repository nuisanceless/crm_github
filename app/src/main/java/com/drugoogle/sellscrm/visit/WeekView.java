package com.drugoogle.sellscrm.visit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by wgh on 2016/4/12.
 * 周滑动日历
 */
public class WeekView extends View
{

    public interface OnWeekViewListener
    {
        /**
         * 初始化周日历卡
         * */
        void initWeekCard (Date date);

        /**
         * 点击日期回调
         * */
        void OnSelectDate(Date date);

        /**
         * 滑动ViewPager改变月份回调
         * */
        void slideWeekCard(Date date);

        /**
         * 手势向下滑动监听
         * */
        void gestureDownglide();
    }

    private class CalendarAttribute
    {
        int dayTextColor;
        float dayTextSize;
        int todayTextColor;
        int dayRowHeight;
        Drawable selectorDrawable;
    }

    private final static String TAG = "Custom_Calendar";
    private final int RECT_STROKE_WIDTH = 2;//矩形框宽度
    private Context mContext;
    /**要显示该日期所在的周*/
    private static Date showWeekIndexDate;
    protected static Date curDate; // 当前日历显示的月
    protected Calendar calendar;
    protected int[] date = new int[7]; // 日历显示数字
    protected int curStartIndex, curEndIndex; // 当前显示的日历起始的索引
    private float deviceScale;
    private int touchSlop;

    protected int selectDayIndex = -1;
    protected int todayDayIndex = -1;

    protected CalendarAttribute attribute;
    protected Paint dayPaint;
    protected Paint selectorPaint;

    private Rect dayTextSize;//2位数的日期文字的大小，缓存起来用户绘制时候使用

    protected HashMap<OverlayKey, ArrayList<Overlay>> overlays = new HashMap<OverlayKey, ArrayList<Overlay>>();
    protected OnWeekViewListener weekViewListener;




    public WeekView(Context context) {
        super(context);
        init(context, null, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null, null);
    }

    public WeekView(Context context, OnWeekViewListener listener, Date date)
    {
        super(context);
        setWeekViewListener(listener);
        init(context, null, date);
    }

    private void init(Context context, AttributeSet attrs, Date date)
    {
        this.mContext = context;
        deviceScale = getResources().getDisplayMetrics().density;//密度
        attribute = readAttributes(attrs);
        touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();


        dayPaint = new Paint();
        dayPaint.setColor(attribute.dayTextColor);
        dayPaint.setAntiAlias(true);
        dayPaint.setTextSize(attribute.dayTextSize);
        dayPaint.setTextAlign(Paint.Align.CENTER);
        dayPaint.setTypeface(Typeface.MONOSPACE);

        dayTextSize = new Rect();
        dayPaint.getTextBounds("00", 0, 2, dayTextSize);


        selectorPaint = new Paint();
        selectorPaint.setStyle(Paint.Style.FILL);

        //数据
        if (date == null) {
            showWeekIndexDate = new Date();
        } else {
            showWeekIndexDate = date;
        }
        calendar = Calendar.getInstance();
        calendar.setTime(showWeekIndexDate);
        weekViewListener.initWeekCard(showWeekIndexDate);
        calculateDate();
    }


    /**
     * 配置日历外观属性
     * */
    private  CalendarAttribute readAttributes(AttributeSet attrs)
    {
        CalendarAttribute at = new CalendarAttribute();
        at.dayTextColor = getResources().getColor(R.color.font_black);
        at.todayTextColor = getResources().getColor(R.color.yellow);
        at.dayTextSize = 15 * deviceScale;
        at.dayRowHeight = (int)(40 * deviceScale + 0.5);

        at.selectorDrawable = new ColorDrawable(0xfff9ab2c);
        return  at;
    }



    private void calculateDate()
    {
        calendar.setTime(showWeekIndexDate);
        int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);


        //给date[7] 数组填充数据。
        int dixDay = 0 - dayInWeek;
        calendar.add(Calendar.DAY_OF_MONTH, dixDay);
        for(int i = 0; i < 7; i++)
        {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            date[i] = calendar.get(Calendar.DAY_OF_MONTH);
        }


        todayDayIndex = -1;
        if (DateUtils.inSameWeekWithToday(showWeekIndexDate))
        {
            calendar.setTime(new Date());
            todayDayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        }
        calendar.setTime(showWeekIndexDate);
    }

    public void calculateDateNum (Date date1)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int dixDay = 0 - dayInWeek;
        calendar.add(Calendar.DAY_OF_MONTH, dixDay);
        for(int i = 0; i < 7; i++)
        {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            date[i] = calendar.get(Calendar.DAY_OF_MONTH);
        }

//        selectDayIndex = -1;
//        todayDayIndex = -1;

        this.calendar.setTime(showWeekIndexDate);
    }









    /**
     * 给控件设置监听事件
     * */
    public void setWeekViewListener(OnWeekViewListener l)
    {
        weekViewListener =  l;
    }
    public OnWeekViewListener getWeekViewListener()
    {
        return weekViewListener;
    }


    public  void setOverlays(HashMap<OverlayKey, ArrayList<Overlay>> overlays)
    {
        this.overlays = overlays;
        invalidate();
    }

    public  Calendar getCalendar()
    {
        return  calendar;
    }

    public void setSelectDayIndex (int index)
    {
        selectDayIndex = index;
    }



    public void setWeek (Date date, Date selectDate)
    {
//        if (DateUtils.inSameWeek(showWeekIndexDate, date))
//            return;

        showWeekIndexDate = date;
        selectDayIndex = -1;

        calculateDate();
        weekViewListener.slideWeekCard(date);
        invalidate();
        if (DateUtils.inSameWeek(date, selectDate))
        {
            selectDayIndex = DateUtils.getWeekDay(selectDate);
        }
    }
    public Date getWeek(){
        return showWeekIndexDate;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY)  //表示父控件给子view一个具体的值，子view要设置成这些值的大小
        {
            height = MeasureSpec.getSize(heightMeasureSpec);
            attribute.dayRowHeight = height;
        }
        else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) //表示父控件个子view一个最大的特定值，而子view不能超过这个值的大小
        {
            height = attribute.dayRowHeight + getPaddingBottom() + getPaddingTop();
        }

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        Log.d(TAG, "onDraw");
        this.setBackgroundColor(getResources().getColor(R.color.calendar_gray));

        int cellWidth = (getWidth() - getPaddingLeft() - getPaddingRight()) / 7;

        Rect rcCell = new Rect();
        calendar.setTime(DateUtils.getFirstDayOfThisWeek(showWeekIndexDate));
        OverlayKey key = new OverlayKey(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 0);

        for (int i = 0; i < 7; i++)
        {
            ArrayList<Overlay> ol = null;
            int color = attribute.dayTextColor;
//            key.day = date[i];

            if (i == 0)
                key.day = date[i];
            else
                key = key.dayPlusOne(key);

            ol = overlays.get(key);
            if (i == todayDayIndex)
            {
                color = attribute.todayTextColor;
            }

            int row = 0;
            int col = i % 7;
            int x = getPaddingLeft() + col * cellWidth;
            int y = attribute.dayRowHeight * row;
            rcCell.set(x, y, x + cellWidth, y + attribute.dayRowHeight);
            if (selectDayIndex == i)
            {
                drawSelectBK(canvas, rcCell);
            }

            drawDayCell(canvas, rcCell, date[i] + "", color, ol);
        }
    }

    /**
     * 画日期单元格
     * */
    private void drawDayCell (Canvas canvas, Rect rcCell, String text, int color, ArrayList<Overlay> overlays)
    {
        Rect rcText = new Rect();
        rcText.left = rcCell.centerX() - dayTextSize.width() / 2;
        rcText.right = rcText.left + dayTextSize.width();
        rcText.top = rcCell.centerY() - dayTextSize.height() / 2;
        rcText.bottom = rcText.top + dayTextSize.height();

        dayPaint.setColor(color);
        canvas.drawText(text, rcText.centerX(), rcText.bottom, dayPaint);
        if (overlays != null)
        {
            for (Overlay ol : overlays)
            {
                drawCellOverlay(canvas, rcCell, rcText, ol);
            }
        }
    }

    /**
     * 画日期单元格覆盖层
     * */
    private void drawCellOverlay(Canvas canvas, Rect rcCell, Rect rcText, Overlay ol)
    {
        int standMargin = (int)(deviceScale * 2 + 0.5);
        int width = ol.overlayDrawable.getIntrinsicWidth();
        int height = ol.overlayDrawable.getIntrinsicHeight();
        int hExt = (int)(ol.paddingHorizontal * deviceScale + 0.5);
        int vExt = (int)(ol.paddingVertical * deviceScale + 0.5);
        int x, y;
        if ((ol.position & Overlay.POSITION_CENTER_HORIZONTAL) != 0)
        {
            x = rcText.centerX() - width / 2 + hExt;
        }
        else if ((ol.position & Overlay.POSITION_RIGHT) != 0)
        {
            x = rcText.right + standMargin + hExt;
        }
        else
        {
            x = rcText.left - standMargin - width - hExt;
        }
        if ((ol.position & Overlay.POSITION_CENTER_VERTICAL) != 0)
        {
            y = rcText.centerY() - height / 2 + vExt;
        }
        else if ((ol.position & Overlay.POSITION_BOTTOM)  != 0)
        {
            y = rcText.bottom + standMargin + vExt;
        }
        else
        {
            y = rcText.top - standMargin - height - vExt;
        }
        ol.overlayDrawable.setBounds(new Rect(x, y, x + width, y + height));
        ol.overlayDrawable.draw(canvas);
    }

    /**
     * 画日期选中状态
     * */
    private void drawSelectBK(Canvas canvas, Rect rcCell)
    {
        if (attribute.selectorDrawable instanceof ColorDrawable)
        {
            int color = ((ColorDrawable)attribute.selectorDrawable).getColor();
            selectorPaint.setColor(color);
            Rect rcText = new Rect();
            dayPaint.getTextBounds("00", 0, 2, rcText);
            float extend = deviceScale * 4;
            float radius = Math.max(rcText.width(), rcText.height()) / 2 + extend;

            //画圆
            //canvas.drawCircle(rcCell.centerX(), rcCell.centerY(), radius, selectorPaint);

            //画矩形框
            selectorPaint.setStyle(Paint.Style.STROKE);
            selectorPaint.setStrokeWidth(RECT_STROKE_WIDTH);
            canvas.drawRect(rcCell.centerX() - (int)(radius*1.5), rcCell.centerY() - (int)(radius*1.5), rcCell.centerX() + (int)(radius*1.5), rcCell.centerY() + (int)(radius*1.5), selectorPaint);
        }
        else if (attribute.selectorDrawable instanceof NinePatchDrawable)
        {
            attribute.selectorDrawable.setBounds(rcCell);
            attribute.selectorDrawable.draw(canvas);
        }
        else
        {
            int x = rcCell.centerX() - attribute.selectorDrawable.getIntrinsicWidth();
            int y = rcCell.centerY() - attribute.selectorDrawable.getIntrinsicHeight();
            attribute.selectorDrawable.setBounds(x, y, x + attribute.selectorDrawable.getIntrinsicWidth(), y + attribute.selectorDrawable.getIntrinsicHeight());
        }
    }








    private float mDownX;
    private float mDownY;
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float disX = event.getX() - mDownX;
                float disY = event.getY() - mDownY;
                if (Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop)
                {
                    selectDayAtPoint((int) event.getX(), (int) event.getY());
                }
                else if (disY > 0 && Math.abs(disX) * 2 < Math.abs(disY))
                {
                    weekViewListener.gestureDownglide();
                }
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * 点击选择时间
     * */
    private void selectDayAtPoint(int x, int y)
    {
        int index = getValidCellIndexAtPoint(x, y);
        if (index < 0)
        {
            return;
        }
        if (selectDayIndex != index)
        {
            selectDayIndex = index;
            invalidate();
            calendar.setTime(showWeekIndexDate);
            int showDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            calendar.add(Calendar.DAY_OF_MONTH, selectDayIndex - showDayOfWeek);
            if (weekViewListener != null)
            {
                weekViewListener.OnSelectDate(calendar.getTime());
            }

        }
    }

    /**
     * 根据手指点击位置获取日期单元索引
     * */
    private int getValidCellIndexAtPoint(int x, int y)
    {
        if (x < getPaddingLeft() || x > getWidth() - getPaddingRight() || y <= getPaddingTop() || y > getHeight() - getPaddingBottom())
        {
            return -1;
        }
        x  = x - getPaddingLeft();
        y = y - (getPaddingTop());
        int cellWidth = (getWidth() - getPaddingLeft() - getPaddingRight() ) / 7;
        int col = x / cellWidth;
        int row = y / attribute.dayRowHeight;
        int index = col + row * 7;

        return  index;
    }




}
