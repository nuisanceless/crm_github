package com.drugoogle.sellscrm.customer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

/**
 * Created by ydwang on 2016/4/29.
 */
public class ContactExpandListView extends ExpandableListView {
    public ContactExpandListView(Context context) {
        super( context );
    }

    public ContactExpandListView(Context context, AttributeSet attrs) {
        super( context, attrs );
    }

    public ContactExpandListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super( context, attrs, defStyleAttr );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec( Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST );

        super.onMeasure( widthMeasureSpec, expandSpec );
    }

}
