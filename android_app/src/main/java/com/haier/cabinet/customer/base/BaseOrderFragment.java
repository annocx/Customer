package com.haier.cabinet.customer.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.CheckoutCounterActivity;
import com.haier.cabinet.customer.activity.adapter.MyOrderListAdapter;
import com.haier.cabinet.customer.entity.Order;
import com.haier.cabinet.customer.event.OrderEvent;
import com.haier.cabinet.customer.interf.BaseOrderFragmentInterface;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
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

import de.greenrobot.event.EventBus;


/**
 * For Order
 * 
 * @author lzx
 *
 */
public abstract class BaseOrderFragment extends BaseListFragment implements BaseOrderFragmentInterface,SwipeRefreshLayout.OnRefreshListener{

    /** Fragment当前状态是否可见 */
    protected boolean isVisible;
    protected MyOrderListAdapter mListAdapter;

    protected boolean isRequestInProcess = false;
    protected ArrayList<Order> mListItems = new ArrayList<>();

    protected static int totalRecord = 0;
    protected int mCurPageIndex = 1;
    protected static final int REQUEST_COUNT = 10;

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // Unregister
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paid_order_list, container, false);
        return view;
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        emptyView = (View) view.findViewById(R.id.empty_view);
        mRecyclerView.setEmptyView(emptyView);
        mOnScrollListener.setSwipeRefreshLayout(mSwipeRefreshLayout);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mListAdapter = new MyOrderListAdapter(getActivity(), mHandler);
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
    protected void initLayoutManager() {
        super.initLayoutManager();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
                    requestOrderListData(url, mListItems.size());
                    isRequestInProcess = true;
                    break;
                case Constant.UPDATE_LIST:
                    String json = (String) msg.obj;
                    List<Order> data = getListByJosn(json);

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
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;

                case Constant.GET_LIST_DATA_FAILURE:
                case Constant.NO_LIST_DATA:
                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case Constant.NO_MORE_LIST_DATA:
                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    isRequestInProcess = false;

                    RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
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
        if (!isRequestInProcess) {
            if(!EventBus.getDefault().isRegistered(this)){
                EventBus.getDefault().register(this);
            }

            mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
            mIsStart = true;
        }
    }


    //  请求订单列表
    protected void requestOrderListData(String url, final int listSize) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                if (null != mSwipeRefreshLayout) mSwipeRefreshLayout.setRefreshing(false);
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA_FAILURE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                if (null != mSwipeRefreshLayout) mSwipeRefreshLayout.setRefreshing(false);
                String json = new String(response);
                if (200 == statusCode && !json.isEmpty()) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            if (JsonUtil.isHaveOrderListData(json)) {

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
    public List<Order> getListByJosn(String json) {
        return null;
    }

    @Override
    public String getRequestUrl(boolean isStart) {
        return null;
    }

    //不做实现，解决出现的异常（Subscriber ****has no public methods called ）
    public void onEventMainThread(OrderEvent event)
    {
        switch (event.getType()){
            case 1:
                mListAdapter.updateOrderState(event.getPosition());
                break;
            case 2:
                mListAdapter.updateOrderEvaluationState(event.getPosition());
                break;
            default:
                break;
        }
    }
}
