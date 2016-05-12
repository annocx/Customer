package com.haier.cabinet.customer.base;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.event.CityChangedEvent;
import com.haier.cabinet.customer.interf.BaseFragmentInterface;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.widget.recyclerview.CustRecyclerView;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;


/**
 * 碎片基类
 *
 * @author lzx
 */
public class BaseListFragment extends BaseFragment implements BaseFragmentInterface, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    protected boolean mIsStart = false;
    @Bind(R.id.swipe_refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recycler_view)
    protected CustRecyclerView mRecyclerView;
    protected HeaderAndFooterRecyclerViewAdapter mHeaderAndFooterRecyclerViewAdapter = null;

    public static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

    public Application getApplication() {
        return PushApplication.getInstance();
    }

    protected View emptyView;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initView(view);
        initLayoutManager();
        initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        displayedImages.clear();
    }

    //不做实现，解决出现的异常（Subscriber ****has no public methods called ）
    public void onEventMainThread(CityChangedEvent event) {
    }

    @Override
    public void initView(View view) {
        //设置刷新时动画的颜色，可以设置4个
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(getActivity(), 24));
            mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
            mSwipeRefreshLayout.setOnRefreshListener(this);
            mRecyclerView.setOnPauseListenerParams(ImageLoader.getInstance(), false, true);
        }
    }

    @Override
    public void initData() {

    }

    protected void initLayoutManager() {
        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setHasFixedSize(true);
        }
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {
    }
}