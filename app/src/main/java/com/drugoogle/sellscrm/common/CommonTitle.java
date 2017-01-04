package com.drugoogle.sellscrm.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by wgh on 2016/3/22.
 * 公用的title控件。
 */
@EViewGroup(R.layout.common_title)
public class CommonTitle extends RelativeLayout
{
    @ViewById(R.id.title_img)
    ImageView mTitleImg;

    @ViewById(R.id.title_text)
    TextView mTitleText;

    @ViewById(R.id.left_img)
    ImageView mLeftImg;

    @ViewById(R.id.left_text)
    TextView mLeftText;

    @ViewById(R.id.right_img_one)
    ImageView mRightImgOne;

    @ViewById(R.id.right_img_two)
    ImageView mRightImgTwo;

    @ViewById(R.id.right_text)
    TextView mRightText;

    @ViewById(R.id.right_img_layout)
    View mRightImgLayout;

    @ViewById(R.id.common_title_layout)
    View mCommonTitleLayout;


    String mTitleTextAttr;
    int mTitleImgAttr;

    String mLeftTextAttr;
    int mLeftImgAttr;

    String mRightTextAttr;
    int mRightImgOneAttr;
    int mRightImgTwoAttr;

    public CommonTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    private void initialize (AttributeSet attrs)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CommonTitle);

        mTitleTextAttr = a.getString(R.styleable.CommonTitle_title_text);
        mTitleImgAttr = a.getResourceId(R.styleable.CommonTitle_title_img, 0);

        mLeftTextAttr = a.getString(R.styleable.CommonTitle_left_text);
        mLeftImgAttr = a.getResourceId(R.styleable.CommonTitle_left_img, 0);

        mRightTextAttr = a.getString(R.styleable.CommonTitle_right_text);
        mRightImgOneAttr = a.getResourceId(R.styleable.CommonTitle_right_img_one, 0);
        mRightImgTwoAttr = a.getResourceId(R.styleable.CommonTitle_right_img_two, 0);
        a.recycle();
    }

    @AfterViews
    void afterViews()
    {
        if (mTitleImgAttr == 0)
        {
            mTitleImg.setVisibility(INVISIBLE);
            if (!CommonUtils.IsNullOrEmpty(mTitleTextAttr))
            {
                mTitleText.setVisibility(VISIBLE);
                mTitleText.setText(mTitleTextAttr);
            }
        }
        else
        {
            mTitleImg.setVisibility(VISIBLE);
            mTitleText.setVisibility(INVISIBLE);
            mTitleImg.setImageResource(mTitleImgAttr);
        }

        if (mLeftImgAttr == 0)
        {
            mLeftImg.setVisibility(INVISIBLE);
            if (!CommonUtils.IsNullOrEmpty(mLeftTextAttr))
            {
                mLeftText.setVisibility(VISIBLE);
                mLeftText.setText(mLeftTextAttr);
            }
        }
        else
        {
            mLeftImg.setVisibility(VISIBLE);
            mLeftText.setVisibility(INVISIBLE);
            mLeftImg.setImageResource(mLeftImgAttr);
        }

        if (mRightImgOneAttr == 0 && mRightImgTwoAttr == 0)
        {
            mRightImgLayout.setVisibility(INVISIBLE);
            if (!CommonUtils.IsNullOrEmpty(mRightTextAttr))
            {
                mRightText.setVisibility(VISIBLE);
                mRightText.setText(mRightTextAttr);
            }
        }
        else
        {
            mRightText.setVisibility(INVISIBLE);
            mRightImgLayout.setVisibility(VISIBLE);
            if (mRightImgOneAttr != 0)
            {
                mRightImgOne.setVisibility(VISIBLE);
                mRightImgOne.setImageResource(mRightImgOneAttr);
            }
            else
            {
                mRightImgOne.setVisibility(GONE);
            }
            if (mRightImgTwoAttr != 0)
            {
                mRightImgTwo.setVisibility(VISIBLE);
                mRightImgTwo.setImageResource(mRightImgTwoAttr);
            }
            else
            {
                mRightImgTwo.setVisibility(GONE);
            }
        }
    }
}
