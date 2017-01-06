package com.drugoogle.sellscrm.visit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by wuguohao on 4/11/16.
 */
public class CalendarView extends View
{

    public interface OnCalendarViewListener
    {
        /**
         * 初始化日历卡
         * */
        void initCalendarCard(Date date);

        /**
         * 点击日期回调
         * */
        void OnSelectDate(Date date);

        /**
         * 滑动ViewPager改变月份回调
         * */
        void slideCalendarCard(Date date);

        /**
         * 收起月日历
         * */
        void gestureUpglide();

    }

    private class CalendarAttribute
    {
        int dayTextColor;
        float dayTextSize;
        int todayTextColor;
        int invalidDayTextColor;
        int dayRowHeight;
        Drawable selectorDrawable;
    }




    private final static String TAG = "Custom_Calendar";
    private final int RECT_STROKE_WIDTH = 2;//矩形框宽度
    private Context mContext;
    protected static Date curDate; // 当前日历显示的月
    private Date selectDate;
    protected Calendar calendar;
    protected int[] date = new int[42]; // 日历显示数字
    protected int curStartIndex, curEndIndex; // 当前显示的日历起始的索引
    private   float deviceScale;
    private int touchSlop;

    protected int selectDayIndex = -1;
    protected int todayDayIndex = -1;

    protected CalendarAttribute attribute;
    protected Paint dayPaint;
    protected Paint selectorPaint;

    private Rect dayTextSize;//2位数的日期文字的大小，缓存起来用户绘制时候使用

    protected HashMap<OverlayKey, ArrayList<Overlay>> overlays = new HashMap<OverlayKey, ArrayList<Overlay>>();
    protected OnCalendarViewListener calendarViewListener;


    public CalendarView(Context context) {
        super(context);
        init(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public CalendarView(Context context, OnCalendarViewListener listener)
    {
        super(context);
        setCalendarViewListener(listener);
        init(context, null);
    }


    private void init(Context context, AttributeSet attrs)
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
        curDate = new Date();
        selectDate = new Date();
        calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        calendarViewListener.initCalendarCard(curDate);
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
        at.invalidDayTextColor = getResources().getColor(R.color.light_gray);
        at.dayTextSize = 14 * deviceScale;
        at.dayRowHeight = (int)(40 * deviceScale + 0.5);

        at.selectorDrawable = new ColorDrawable(0xfff9c62c);
        return  at;
    }




    private void calculateDate() {
        calendar.setTime(curDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Log.d(TAG, "day in week:" + dayInWeek);
        int monthStart = dayInWeek;
        if (monthStart == 1) {
            monthStart = 8;
        }
        monthStart -= 1;  //以日为开头-1，以星期一为开头-2
        curStartIndex = monthStart;
        date[monthStart] = 1;
        // last month
        if (monthStart > 0) {
            calendar.set(Calendar.DAY_OF_MONTH, 0);
            int dayInmonth = calendar.get(Calendar.DAY_OF_MONTH);
            for (int i = monthStart - 1; i >= 0; i--) {
                date[i] = dayInmonth;
                dayInmonth--;
            }
            calendar.set(Calendar.DAY_OF_MONTH, date[0]);
        }
        // this month
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        // Log.d(TAG, "m:" + calendar.get(Calendar.MONTH) + " d:" +
        // calendar.get(Calendar.DAY_OF_MONTH));
        int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
        for (int i = 1; i < monthDay; i++) {
            date[monthStart + i] = i + 1;
        }
        curEndIndex = monthStart + monthDay;
        // next month
        for (int i = monthStart + monthDay; i < 42; i++) {
            date[i] = i - (monthStart + monthDay) + 1;
        }
        if (curEndIndex < 42) {
            // 显示了下一月的
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendar.set(Calendar.DAY_OF_MONTH, date[41]);

        //计算今天


        todayDayIndex = -1;
        calendar.setTime(curDate);
        int curYear = calendar.get(Calendar.YEAR);
        int curMonth = calendar.get(Calendar.MONTH);
        calendar.setTime(new Date());
        int todayYear = calendar.get(Calendar.YEAR);
        int todayMonth = calendar.get(Calendar.MONTH);
        if (todayYear == curYear && todayMonth == curMonth)
        {
            int dayIndexInMonth = calendar.get(Calendar.DAY_OF_MONTH) - 1;
            todayDayIndex = curStartIndex + dayIndexInMonth;
        }
    }








    /**
     * 给控件设置监听事件
     * */
    public void setCalendarViewListener(OnCalendarViewListener l)
    {
        calendarViewListener =  l;
    }
    public OnCalendarViewListener getCalendarViewListener()
    {
        return calendarViewListener;
    }



    public int getSelectDayIndex (Date date)
    {
        return selectDayIndex;
    }
    public void setSelectDayIndex (Date date)
    {
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int monthStart = dayInWeek;
        if (monthStart == 1) {
            monthStart = 8;
        }
        monthStart -= 1;  //以日为开头-1，以星期一为开头-2
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        selectDayIndex = calendar.get(Calendar.DAY_OF_MONTH) + monthStart - 1;
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

    public void setMonth(Date date, Date selectDate)
    {
//        if (DateUtils.inSameMonth(curDate, date))
//            return;

        curDate = date;
        selectDayIndex = -1;

        calculateDate();
        calendarViewListener.slideCalendarCard(date);
        invalidate();
        if (DateUtils.inSameMonth(date, selectDate))
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectDate);
            selectDayIndex = calendar.get(Calendar.DAY_OF_MONTH) + curStartIndex - 1;
        }
    }

    public Date getMonth(){
        return curDate;
    }














    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) //表示父控件给子view一个具体的值，子view要设置成这些值的大小
        {
            height = MeasureSpec.getSize(heightMeasureSpec);
            attribute.dayRowHeight = (height) / 6;
        }
        else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST)//表示父控件个子view一个最大的特定值，而子view不能超过这个值的大小
        {
            height = attribute.dayRowHeight * 6 + getPaddingBottom() + getPaddingTop();
        }

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                MeasureSpec.EXACTLY);
        //setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }




    @Override
    protected void onDraw(Canvas canvas)
    {
        Log.d(TAG, "onDraw");
        this.setBackgroundColor(getResources().getColor(R.color.dialog_color));

        int cellWidth = (getWidth() - getPaddingLeft() - getPaddingRight() ) / 7;
        int cellHeight = attribute.dayRowHeight;


        Rect rcCell = new Rect();

        calendar.setTime(curDate);
        OverlayKey key = new OverlayKey(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 0);

        for (int i = 0; i < 42; i++)
        {
            ArrayList<Overlay> ol = null;
            int color = attribute.dayTextColor;
            if (!isDayCellInCurrentMonth(i))
            {
                color = attribute.invalidDayTextColor;
            }
            else
            {
                key.day = date[i];
                ol = overlays.get(key);
                if (i == todayDayIndex)
                {
                    color = attribute.todayTextColor;
                }
            }
            int row = i / 7;
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

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.calendar_line));
        for (int i = 1; i <= 6; i++)
        {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, cellHeight * 6, paint);
            canvas.drawLine(0, (i-1)*cellHeight, getWidth(), (i-1)*cellHeight, paint);
        }
        super.onDraw(canvas);
    }

    /**
     * 画日期单元格
     * */
    private void drawDayCell(Canvas canvas, Rect rcCell, String text, int color, ArrayList<Overlay> overlays)
    {
        Rect rcText= new Rect();
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
        int standMargin = (int)(deviceScale * 4 + 0.5);
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
            float extend = deviceScale * 10;
            float radius = Math.max(rcText.width(), rcText.height()) / 2 + extend;

            //画圆
            selectorPaint.setStyle(Paint.Style.STROKE);//空心
            selectorPaint.setStrokeWidth(RECT_STROKE_WIDTH);
            selectorPaint.setAntiAlias(true);//去掉锯齿
            canvas.drawCircle(rcCell.centerX(), rcCell.centerY(), radius, selectorPaint);

            //画矩形框
//            selectorPaint.setStyle(Paint.Style.STROKE);//空心
//            selectorPaint.setStrokeWidth(RECT_STROKE_WIDTH);
////            canvas.drawRect(rcCell.centerX() - radius*2, rcCell.centerY() - radius*2, rcCell.centerX() + radius*2, rcCell.centerY() + radius*2, selectorPaint);
//            canvas.drawRect(rcCell.centerX() - (int)(radius*1.5), rcCell.centerY() - (int)(radius*1.5), rcCell.centerX() + (int)(radius*1.5), rcCell.centerY() + (int)(radius*1.5), selectorPaint);
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
                else if (disY < 0 && Math.abs(disX) * 2 < Math.abs(disY))
                {
                    calendarViewListener.gestureUpglide();
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
        //if (selectDayIndex != index)//点击已经选中的日期也是要收起月日历，所以注掉
        {
            selectDayIndex = index;
            invalidate();
            calendar.setTime(curDate);
            calendar.set(Calendar.DAY_OF_MONTH, date[selectDayIndex]);
            selectDate = calendar.getTime();
            if (calendarViewListener != null)
            {
                calendarViewListener.OnSelectDate(calendar.getTime());
            }
        }
    }

    /**
     * 根据坐标计算点击日期单元块的索引
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
        if (!isDayCellInCurrentMonth(index))
        {
            return  -1;
        }
        return  index;
    }


    /**
     * 判断索引对应日期是否在本月之内，本月之内为有效时间
     * */
    private boolean isDayCellInCurrentMonth(int cellIndex)
    {
        return  !(cellIndex < curStartIndex || cellIndex >= curEndIndex);
    }










    @Override
    public String toString ()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        return calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月";
    }

}
