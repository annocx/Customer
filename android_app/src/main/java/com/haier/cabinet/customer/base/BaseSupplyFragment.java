package com.haier.cabinet.customer.base;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.CheckoutCounterActivity;
import com.haier.cabinet.customer.activity.adapter.ProducerAdapter;
import com.haier.cabinet.customer.entity.Order;
import com.haier.cabinet.customer.entity.Supply;
import com.haier.cabinet.customer.interf.BaseSupplyFragmentInterface;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.widget.recyclerview.CustRecyclerView;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;


/**
 * For Order
 * 
 * @author lzx
 *
 */
public abstract class BaseSupplyFragment extends BaseListFragment implements BaseSupplyFragmentInterface,SwipeRefreshLayout.OnRefreshListener{

    /** Fragment当前状态是否可见 */
    protected boolean isVisible;
    protected ProducerAdapter mListAdapter;

    protected boolean isRequestInProcess = false;
    protected ArrayList<Order> mListItems = new ArrayList<>();

    protected static int totalRecord = 0;
    protected int mCurPageIndex = 1;
    protected static final int REQUEST_COUNT = 10;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_producer, container, false);
        return view;
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        emptyView = (View) view.findViewById(R.id.empty_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setEmptyView(emptyView);
        mOnScrollListener.setSwipeRefreshLayout(mSwipeRefreshLayout);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mListAdapter = new ProducerAdapter(getActivity());
        mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mListAdapter);
        mRecyclerView.setAdapter(mHeaderAndFooterRecyclerViewAdapter);

        emptyView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mListAdapter.getItemCount() == 0) {
                        mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onRefresh() {
        if (!mIsStart) {//防止多次下拉
            mIsStart = true;
            mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
        }
    }

    private RecyclerOnScrollListener mOnScrollListener = new RecyclerOnScrollListener() {

        public void onBottom() {

            LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(mRecyclerView);
            if(state == LoadingFooter.State.Loading) {
                return;
            }

            if (mCurPageIndex < totalRecord) {
                // loading more
                RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
                mIsStart = false;
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
            } else {

                //the end
                RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
            }
        }
    };

    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.GET_LIST_DATA:
                    String url = getRequestUrl(mIsStart);
                    requestSupplyListData(mHandler, url, mListItems.size());
                    isRequestInProcess = true;
                    break;
                case Constant.UPDATE_LIST:
                    String json = (String) msg.obj;
                    List<Supply> data = Util.getSupplyListByJosn(json);
                    if (null == data) {
                        return;
                    }

                    if (mIsStart) {
                        //清空数据
                        if (mListAdapter.getItemCount() > 0) {
                            mListAdapter.clear();
                        }
                        mListAdapter.addAll(data);
                    } else {
                        RecyclerViewStateUtils.setFooterViewState(mRecyclerView, LoadingFooter.State.Normal);
                        mListAdapter.addAll(data);
                    }

                    mListAdapter.notifyDataSetChanged();
                    emptyView.setVisibility(View.GONE);

                    mIsStart = false;
                    isRequestInProcess = false;
                    break;

                case Constant.GET_LIST_DATA_FAILURE:
                case Constant.NO_LIST_DATA:
                case Constant.NO_MORE_LIST_DATA:
                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case Constant.GO_TO_PAY:
                    goToPay(msg.getData());
                    break;
                default:
                    break;
            }
        }
    };

    private void goToPay(Bundle bundle) {
        //调用支付
        IntentUtil.startActivity(getActivity(), CheckoutCounterActivity.class, bundle);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }


    /**
     * 可见
     */
    protected void onVisible() {
        lazyLoad();
    }


    /**
     * 不可见
     */
    protected void onInvisible() {
        if (null != mRecyclerView) {
            //清空数据
            if (mListAdapter.getItemCount() > 0) {
                mListAdapter.clear();
            }
            mListAdapter.notifyDataSetChanged();
        }

    }


    /**
     * 延迟加载
     */
    protected void lazyLoad(){
        onRefresh();
    }


    //  请求产地列表
    protected void requestSupplyListData(final Handler mHandler, String url, final int listSize) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();

            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                mSwipeRefreshLayout.setRefreshing(false);
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA_FAILURE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                if (mSwipeRefreshLayout!=null){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                String json = new String(response);
                if (200 == statusCode && !json.isEmpty()) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            if (JsonUtil.isHaveData(json)) {

                                mHandler.obtainMessage(Constant.UPDATE_LIST, json).sendToTarget();
                            } else {
                                if (listSize > 0) {
                                    mHandler.obtainMessage(Constant.NO_MORE_LIST_DATA).sendToTarget();
                                } else {
                                    mHandler.obtainMessage(Constant.NO_LIST_DATA).sendToTarget();
                                }

                            }
                            break;
                        case 1002:
                            mHandler.obtainMessage(Constant.NO_LIST_DATA).sendToTarget();
                            break;
                        case 2001:
                            mHandler.obtainMessage(Constant.GET_LIST_DATA_FAILURE).sendToTarget();

                            break;
                        default:
                            break;
                    }

                } else {
                    mHandler.obtainMessage(Constant.NO_MORE_LIST_DATA).sendToTarget();
                }
            }

        });

    }


    @Override
    public List<Supply> getListByJosn(String json) {
        return null;
    }

    @Override
    public String getRequestUrl(boolean isStart) {
        return null;
    }
}
