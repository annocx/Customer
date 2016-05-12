package com.haier.cabinet.customer.activity;

import android.content.Intent;
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
import com.haier.cabinet.customer.fragment.CompletedOrderListFragment;
import com.haier.cabinet.customer.fragment.OrderListFragment;
import com.haier.cabinet.customer.fragment.PaidOrderListFragment;
import com.haier.cabinet.customer.fragment.UnPaidOrderListFragment;
import com.haier.cabinet.customer.fragment.UnReceivedOrderListFragment;
import com.haier.cabinet.customer.view.LineTabIndicator;
import com.sunday.statagent.StatAgent;

import java.util.ArrayList;

import butterknife.Bind;

public class UserOrderListActivity extends BaseFragmentActivity implements ViewPager.OnPageChangeListener {

    @Bind(R.id.line_tab_indicator) LineTabIndicator mLineTabIndicator;
    @Bind(R.id.view_pager) ViewPager mViewPager;
    CharSequence[] mLabels;

    public static boolean isUpdate = false;

    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

    {
        mFragments.add(new OrderListFragment());
        mFragments.add(new UnPaidOrderListFragment());
        mFragments.add(new PaidOrderListFragment());
        mFragments.add(new UnReceivedOrderListFragment());
        mFragments.add(new CompletedOrderListFragment());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_order_list;
    }

    @Override
    public void initView() {
        super.initView();

        StatAgent.initAction(this, "", "1", "20", "", "", "", "1", "");
        mTitleText.setText("我的订单");
        mBackBtn.setVisibility(View.VISIBLE);

        mLabels = getResources().getTextArray(R.array.order_title_bar_labels);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments, mLabels));
        mLineTabIndicator.setViewPager(mViewPager);
        mLineTabIndicator.setOnPageChangeListener(this);
    }

    // 重新装配intent
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 110) {
            mFragments.get(4).onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int orderState = getIntent().getIntExtra("order_state", -1);
        switch (orderState) {
            case -1:
                // 再次回来还回到原来tab
                break;
            case 0:
                mLineTabIndicator.setCurrentItem(0);
                break;
            case 1:
                mLineTabIndicator.setCurrentItem(1);
                break;
            case 2:
                mLineTabIndicator.setCurrentItem(2);
                break;
            case 3:
                mLineTabIndicator.setCurrentItem(3);
                break;
            case 4:
                mLineTabIndicator.setCurrentItem(4);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isUpdate = false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        // 切换其他tab，清除此数据
        getIntent().removeExtra("order_state");
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

}
