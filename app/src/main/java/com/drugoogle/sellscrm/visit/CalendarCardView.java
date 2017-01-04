package com.drugoogle.sellscrm.visit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.drugoogle.sellscrm.Utils.DateUtils;

/**
 * Created by wuguohao on 4/6/16.
 */
public class CalendarCardView extends View
{
    public static final int TOTAL_COL = 7;
    public static final int TOTAL_ROW = 6;

    private Paint mCirclePaint;//绘制圆的画笔
    private Paint mTextPaint;//绘制文本的画笔
    private int touchSlop;
    private int mViewWidth;
    private int mViewHeight;
    private int mCellSpace;

    private Row[] rows = new Row[TOTAL_ROW];
    private static int mYear;
    private static int mMonth;
    private CalendarCardViewListener mCalendarCardViewListener;

    private Cell mClickCell;
    private float mDownX;
    private float mDownY;



    /**
     * 时间单元格点击的回调接口，月份滑动的回调接口
     * */
    public interface CalendarCardViewListener
    {

        /**
         * 初始化日历卡
         * */
        void initCalendarCard (int year, int month);

        /**
         * 点击日期回调
         * */
        void clickCellDate(CustomCalendarDate date);

        /**
         * 滑动ViewPager改变月份回调
         * */
        void slideCalendarCard(int year, int month);
    }





    public CalendarCardView(Context context) {
        super(context);
        init(context);
    }

    public CalendarCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CalendarCardView(Context context, CalendarCardViewListener listener)
    {
        super(context);
        this.mCalendarCardViewListener = listener;
        init(context);
    }




    private void init (Context context)
    {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(Color.parseColor("#F24949")); // 红色圆形

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initDate();
    }

    /**
     * 初始化属性信息
     * */
    private void initDate()
    {
        mYear = DateUtils.getCurrentYear();
        mMonth = DateUtils.getCurrentMonth();
        fillDate();
        mCalendarCardViewListener.initCalendarCard(mYear, mMonth);
    }


    private void fillDate ()
    {
        int monthDay = DateUtils.getCurrentMonthDay();//当前日期是当月第几天

        int lastMonthDays = DateUtils.getMonthDays(mYear, mMonth - 1);//上个月有多少天

        int currentMonthDays = DateUtils.getMonthDays(mYear, mMonth);//当月有多少天

        int firstDayWeek = DateUtils.getWeekDayFromDate(mYear, mMonth);

        boolean isCurrentMonth = false;
        if (DateUtils.isCurrentMonth(mYear, mMonth))
            isCurrentMonth = true;

        int day = 0;
        for (int j = 0; j< TOTAL_ROW; j++)
        {
            rows[j] = new Row(j);
            for (int i = 0; i < TOTAL_COL; i++)
            {
                int position = i + j * TOTAL_COL;
                if (position >= firstDayWeek && position < firstDayWeek + currentMonthDays)
                {
                    day++;
                    rows[j].cells[i] = new Cell(new CustomCalendarDate(mYear, mMonth, day), State.CURRENT_MONTH_DAY, i, j);

                    //今天
                    if (isCurrentMonth && day == monthDay)
                    {
                        CustomCalendarDate date = new CustomCalendarDate(mYear, mMonth, day);
                        rows[j].cells[i] = new Cell(date, State.TODAY, i, j);
                    }

                    //这个月今天之后的日期
                    if (isCurrentMonth && day > monthDay)
                    {
                        rows[j].cells[i] = new Cell(
                                new CustomCalendarDate(mYear, mMonth, day),
                                State.UNREACH_DAY,
                                i,
                                j
                        )
                        ;
                    }
                }
                //上一个月
                else if (position < firstDayWeek)
                {
                    rows[j].cells[i] = new Cell(
                            new CustomCalendarDate(mYear, mMonth - 1, lastMonthDays - (firstDayWeek - position - 1)),
                            State.PAST_MONTH_DAY, i , j);
                }
                //下一个月
                else if (position >= firstDayWeek + currentMonthDays)
                {
                    rows[j].cells[i] = new Cell((new CustomCalendarDate(mYear, mMonth + 1, position - firstDayWeek - currentMonthDays + 1)),
                            State.NEXT_MONTH_DAY, i, j);
                }
            }
        }
    }


    @Override
    protected void onDraw (Canvas canvas)
    {
        super.onDraw(canvas);
        for (int i = 0 ; i < TOTAL_ROW ; i++)
        {
            if (rows[i] != null)
            {
                rows[i].drawCells(canvas);
            }
        }
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        mCellSpace = Math.min(mViewHeight / TOTAL_ROW, mViewWidth / TOTAL_COL);

        mTextPaint.setTextSize(mCellSpace / 3);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event)
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
                    int col = (int) (mDownX / mCellSpace);
                    int row = (int) (mDownY / (mCellSpace * 0.8));
                    measureCellClick(col, row);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void measureCellClick (int col, int row)
    {
        if (col >= TOTAL_COL || row >= TOTAL_ROW)
            return;
//        if (mClickCell != null)
//        {
//            rows[mClickCell.j].cells[mClickCell.i] = mClickCell;
//        }
        if (rows[row] != null)
        {
            mClickCell = new Cell(
                    rows[row].cells[col].date,
                    rows[row].cells[col].state,
                    rows[row].cells[col].i,
                    rows[row].cells[col].j
            );

            CustomCalendarDate date = rows[row].cells[col].date;
            date.week = col;
            mCalendarCardViewListener.clickCellDate(date);

            //刷新界面(不用刷新)
            update();
        }
    }


    // 从左往右划，上一个月
    public void leftSlide() {
        if (mMonth == 1) {
            mMonth = 12;
            mYear -= 1;
        } else {
            mMonth -= 1;
        }
        update();
    }



    // 从右往左划，下一个月
    public void rightSlide() {
        if (mMonth == 12) {
            mMonth = 1;
            mYear += 1;
        } else {
            mMonth += 1;
        }
        update();
    }



    public void update() {
        fillDate();
        mCalendarCardViewListener.slideCalendarCard(mYear, mMonth);
        invalidate();
    }


    @Override
    public String toString()
    {
        return mYear + "年" + mMonth + "月";
    }





    /**
     * 行元素
     * */
    class Row
    {
        //标记第几行
        public int j;

        //
        public Cell[] cells = new Cell[TOTAL_COL];

        Row (int j)
        {
            this.j = j;
        }

        public void drawCells (Canvas canvas)
        {
            for (int i = 0 ; i < cells.length ; i++)
            {
                if (cells[i] != null)
                    cells[i].drawSelf(canvas);
            }
        }
    }

    /**
     * 单元格的不同状态
     * */
    enum State
    {
        TODAY,//今天
        CURRENT_MONTH_DAY,
        PAST_MONTH_DAY,
        NEXT_MONTH_DAY,
        UNREACH_DAY,

        SELECTED,   //选中状态
        HAS_VISIT_PLAN, //有拜访计划的状态
        HAS_TOTAL_VISIT_PLAN    //截止当天有总共拜访次数的状态
    }

    /**
     * 单个日期元素
     * */
    class Cell
    {
        public CustomCalendarDate date;
        public State state;
        public int i;   //第几列
        public int j;   //第几行


        public Cell (CustomCalendarDate date, State state, int i, int j)
        {
            super();
            this.date = date;
            this.state = state;
            this.i = i;
            this.j = j;
        }

        public void drawSelf(Canvas canvas)
        {
            switch (state){
                case TODAY:
                    mTextPaint.setColor(Color.parseColor("#fffffe"));
                    //画圆
                    canvas.drawCircle (
                            (float)(mCellSpace * (i + 0.5)),
                            (float)(mCellSpace * (j + 0.4) * 0.8),
                            mCellSpace / 3,
                            mCirclePaint
                    );
                    break;
                case CURRENT_MONTH_DAY:
                    mTextPaint.setColor(Color.BLACK);
                    break;
                case PAST_MONTH_DAY:
                case NEXT_MONTH_DAY:
                    mTextPaint.setColor(Color.parseColor("#fffffe"));
                    break;
                case UNREACH_DAY:
                    mTextPaint.setColor(Color.GRAY);
                    break;
                default:
                    break;

            }
            String content = date.day + "";
            canvas.drawText(
                    content,
                    (float)((i + 0.5) * mCellSpace - mTextPaint.measureText(content) / 2),
                    (float)((j + 0.7) * mCellSpace * 0.8 - mTextPaint.measureText(content, 0, 1) / 2),
                    mTextPaint);
        }
    }
}
