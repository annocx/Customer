package com.haier.cabinet.customer.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.sunday.statagent.StatAgent;

/**
 * Created by Administrator on 2015/12/3.
 */
public class ExplainActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_explain;
    }

    public void initView(){
        PushApplication.addActivity(this);
        StatAgent.initAction(this, "", "1", "19", "", "", "", "1", "");
        mTitleText.setText("保证说明");
        mBackBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void initData() {

    }
}
