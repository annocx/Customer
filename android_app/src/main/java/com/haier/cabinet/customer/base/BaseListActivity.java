package com.haier.cabinet.customer.base;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.Entity;
import com.haier.cabinet.customer.listener.BaseViewInterface;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.widget.recyclerview.CustRecyclerView;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.common.util.AppToast;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * 碎片基类
 *
 * @author lzx
 */
public abstract class BaseListActivity<T extends Entity> extends BaseActivity implements BaseViewInterface, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    protected boolean mIsStart = false;
    protected boolean isRequestInProcess = false;
    private static final int REQUEST_COUNT = 10;
    protected int mCurrentPage = 0;
    protected int totalPage = 0;


    @Bind(R.id.swipe_refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recycler_view)
    protected CustRecyclerView mRecyclerView;
    @Bind(R.id.dynamic_view)
    protected LinearLayout mDynamicLayout;

    protected ListBaseAdapter<T> mListAdapter;
    protected HeaderAndFooterRecyclerViewAdapter mRecyclerViewAdapter = null;
    public static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

    @Bind(R.id.empty_view)
    protected View emptyView;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_pull_refresh_recyclerview;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        displayedImages.clear();
    }

    @Override
    public void initView() {
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(this, 24));
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (mListAdapter == null) {
            mListAdapter = getListAdapter();

            onRefresh();

        }

        initLayoutManager();
        mRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mListAdapter);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mOnScrollListener.setSwipeRefreshLayout(mSwipeRefreshLayout);
        mRecyclerView.setOnPauseListenerParams(ImageLoader.getInstance(), false, true);

    }

    @Override
    public void initData() {

    }

    protected void requestData() {
        Log.d("BaseListActivity","requestData");
        mCurrentPage++;
        sendRequestData();
        isRequestInProcess = true;
    }

    protected void sendRequestData() {}

    protected void initLayoutManager(){
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {
        mCurrentPage = 0;
        requestData();
    }

    protected abstract ListBaseAdapter<T> getListAdapter();

    protected RecyclerOnScrollListener mOnScrollListener = new RecyclerOnScrollListener() {

        @Override
        public void onBottom() {
            LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(mRecyclerView);
            if(state == LoadingFooter.State.Loading) {
                return;
            }

            if (mCurrentPage < totalPage) {
                // loading more
                RecyclerViewStateUtils.setFooterViewState(BaseListActivity.this, mRecyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
                requestData();
            } else {
                //the end
                if (totalPage > 1){
                    RecyclerViewStateUtils.setFooterViewState(BaseListActivity.this, mRecyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
                }else {
                    RecyclerViewStateUtils.setFooterViewState(BaseListActivity.this, mRecyclerView, mListAdapter.getItemCount(), LoadingFooter.State.TheEnd, null);
                }

            }
        }


    };
}