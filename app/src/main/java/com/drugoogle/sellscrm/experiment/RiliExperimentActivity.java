package com.drugoogle.sellscrm.experiment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.drugoogle.sellscrm.R;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by wuguohao on 17/1/5.
 */

public class RiliExperimentActivity extends Activity implements View.OnClickListener
{
    Button mZkzdButton;
    final boolean zk = true;
    final boolean zd = false;
    boolean zkorzd = zd;
    View zkzdlLayout;
    int zkzdLayoutHeight;
    View oneTwoLayout;
    int oneTwoLayoutHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rili_experiment);

        initView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
            oneTwoLayoutHeight = oneTwoLayout.getMeasuredHeight();
            zkzdLayoutHeight = zkzdlLayout.getMeasuredHeight();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(oneTwoLayout.getLayoutParams());
            lp.setMargins(0, zkzdLayoutHeight - oneTwoLayoutHeight, 0, 0);
            oneTwoLayout.setLayoutParams(lp);

//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)oneTwoLayout.getLayoutParams();
//            params.topMargin -= (oneTwoLayoutHeight - zkzdLayoutHeight);
//            //oneTwoLayout.requestLayout();
//            oneTwoLayout.setLayoutParams(params);
        }
    }

    void initView ()
    {
        mZkzdButton = (Button) findViewById(R.id.zkzd_button);
        oneTwoLayout = findViewById(R.id.one_two_layout);
        zkzdlLayout = findViewById(R.id.zkzd_layout);

        mZkzdButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.zkzd_button:
                if (zkorzd == zk) {
                    dozd();
                } else if (zkorzd == zd) {
                    dozk();
                }

                break;

        }
    }

    void dozd ()
    {
        ObjectAnimator.ofFloat(oneTwoLayout, "translationY", 0).start();
        zkorzd = zd;
    }

    void dozk ()
    {
        ObjectAnimator.ofFloat(oneTwoLayout, "translationY", oneTwoLayoutHeight).start();
        zkorzd = zk;
    }
}
