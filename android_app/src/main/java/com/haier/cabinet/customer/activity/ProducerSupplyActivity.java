package com.haier.cabinet.customer.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.MyFragmentPagerAdapter;
import com.haier.cabinet.customer.base.BaseFragmentActivity;
import com.haier.cabinet.customer.fragment.SupplyListFragment;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.view.LineTabIndicator;
import com.sunday.statagent.StatAgent;

import java.util.ArrayList;

import butterknife.Bind;

public class ProducerSupplyActivity extends BaseFragmentActivity implements ViewPager.OnPageChangeListener {

    @Bind(R.id.line_tab_indicator) LineTabIndicator mLineTabIndicator;

    @Bind(R.id.view_pager) ViewPager mViewPager;

    private CharSequence[] mLabels;

    private ArrayList<Fragment> mFragments = new ArrayList<>();

    {
        mFragments.add(SupplyListFragment.newInstance(Constant.PRODUCER_NORTHEAST));
        mFragments.add(SupplyListFragment.newInstance(Constant.PRODUCER_NORTH));
        mFragments.add(SupplyListFragment.newInstance(Constant.PRODUCER_EAST));
        mFragments.add(SupplyListFragment.newInstance(Constant.PRODUCER_NORTHWEST));
        mFragments.add(SupplyListFragment.newInstance(Constant.PRODUCER_SOUTH));
        mFragments.add(SupplyListFragment.newInstance(Constant.PRODUCER_CENTRAL));
        mFragments.add(SupplyListFragment.newInstance(Constant.PRODUCER_SOUTHWEST));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_producer_supply;
    }

    public void initView() {
        StatAgent.initAction(this, "", "1", "7", "", "", "", "1", "");
        mTitleText.setText("产地直供");
        mBackBtn.setVisibility(View.VISIBLE);
        mLabels = getResources().getTextArray(R.array.producer_title_bar_labels);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments, mLabels));
        mLineTabIndicator.setViewPager(mViewPager);
        mLineTabIndicator.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        StatAgent.initAction(this, "", "2", "7", "", "", mLabels[position].toString(), "1", "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StatAgent.initAction(this, "", "2", "7", "", "", "back", "2", "");
    }
}
