package com.haier.cabinet.customer.activity;


import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.CategorizeAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.Categorize;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;

public class CategorizeActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "CategorizeActivity";

    @Bind(R.id.empty_view) View emptyView;
    @Bind(R.id.refreshable_view) RecyclerView recyclerView;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    private boolean mIsStart = false;
    
    private int mCurPageIndex = 1;

    private ArrayList<Categorize> data;
    private CategorizeAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_categorize;
    }

    public void initView() {
        mTitleText.setText("分类");
        mBackBtn.setVisibility(View.VISIBLE);

        emptyView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (adapter.getItemCount() == 0) {
                        emptyView.setVisibility(View.GONE);
                        mHandler.sendEmptyMessage(GET_LIST_DATA);
                    }
                }
                return false;
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(this, 24));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter = new CategorizeAdapter(this));

        mHandler.sendEmptyMessage(GET_LIST_DATA);
    }

    @Override
    public void initData() {
        StatAgent.initAction(mContext, "", "1", "4", "", "", "", "1", "");
    }

    private static final int GET_LIST_DATA = 1001;
    private static final int UPDATE_LIST = 1002;
    private static final int NO_LIST_DATA = 1003;
    private static final int GET_LIST_DATA_FAILURE = 1005;
    private static final int NO_MORE_LIST_DATA = 1006;

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case GET_LIST_DATA:
                    String url = getRequestUrl(mIsStart);
                    requestOrderListData(url);
                    break;
                case UPDATE_LIST:
                    String json = (String) msg.obj;
                    data = getCategorizeListByJosn(json);

                    if (null == data) {
                        return;
                    }

                    if (mIsStart) {
                        //清空数据
                        if (adapter.getItemCount() > 0) {
                            adapter.clear();
                        }
                        adapter.addAll(data);
                    } else {
                        adapter.addAll(data);
                    }
                    adapter.notifyDataSetChanged();

                    mIsStart = false;
                    break;

                case GET_LIST_DATA_FAILURE:
                    mIsStart = false;
                    if (adapter.getItemCount() == 0) {
                        showNoDataView();
                    }
                    break;
                case NO_MORE_LIST_DATA:

                    adapter.notifyDataSetChanged();

                    mIsStart = false;
                    break;
                case NO_LIST_DATA:
                    //清空数据
                    if (adapter.getItemCount() > 0) {
                        adapter.clear();
                    }
                    adapter.notifyDataSetChanged();
                    mIsStart = false;
                    showNoDataView();
                    break;
                default:
                    break;
            }
        };
    };

    private void showNoDataView(){
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void requestOrderListData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, null, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                mHandler.sendEmptyMessage(GET_LIST_DATA_FAILURE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                swipeRefreshLayout.setRefreshing(false);
                String json = new String(response);
                Log.d(TAG, "onSuccess " + json);

                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            if (JsonUtil.isHaveData(json)){
                                mHandler.obtainMessage(UPDATE_LIST, json).sendToTarget();
                            }else {
                                if (adapter.getItemCount() > 0) {
                                    mHandler.obtainMessage(NO_MORE_LIST_DATA).sendToTarget();
                                }else {
                                    mHandler.obtainMessage(NO_LIST_DATA).sendToTarget();
                                }

                            }
                            break;
                        case 2001:
                            mHandler.obtainMessage(GET_LIST_DATA_FAILURE).sendToTarget();
                            break;
                        default:
                            break;
                    }

                }
            }

        });

    }

    private ArrayList<Categorize> getCategorizeListByJosn(String json) {
        ArrayList<Categorize> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result") || TextUtils.isEmpty(jsonObject.getJSONObject("result").toString())) {
                return null;
            }
            JSONObject ordersObject = jsonObject.getJSONObject("result");
            JSONArray orderArray = ordersObject.getJSONArray("class_list");
            for (int i = 0; i < orderArray.length(); i++) {
                JSONObject categorizeObject = orderArray.getJSONObject(i);
                list.add(Util.getCategorizeByJson(categorizeObject));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }

        return list;
    }

    private String getRequestUrl(boolean isStart) {
        if ((adapter.getItemCount() == 0) || isStart) {
            mCurPageIndex = 1;
        } else {
            ++mCurPageIndex;
        }
        String url = Constant.SHOP_DOMAIN
                + "/appapi/index.php?act=goods_class&op=index"
                + "&member_id="+ PushApplication.getInstance().getUserId()
                /*+ "&page=" + mCurPageIndex*/;
//        Log.d(TAG, "url -- " + url);
        return url;
    }

    @Override
    public void onRefresh() {
        if (!mIsStart) {//防止多次下拉
            Log.d(TAG, "onRefresh");
            mIsStart = true;
            emptyView.setVisibility(View.GONE);
            mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
        }
    }

}

