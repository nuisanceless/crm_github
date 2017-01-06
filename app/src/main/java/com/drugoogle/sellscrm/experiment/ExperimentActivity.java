package com.drugoogle.sellscrm.experiment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;

/**
 * Created by wuguohao on 17/1/5.
 */

public class ExperimentActivity extends Activity implements View.OnClickListener
{
    Button mRiliExperiment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);
        initView();
    }

    void initView ()
    {
        mRiliExperiment = (Button) findViewById(R.id.rili_experiment);

        mRiliExperiment.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.rili_experiment:
                startActivity(new Intent(ExperimentActivity.this, RiliExperimentActivity.class));
                Toast.makeText(ExperimentActivity.this, "rili", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
