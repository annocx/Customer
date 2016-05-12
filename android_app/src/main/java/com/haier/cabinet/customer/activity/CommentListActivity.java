package com.haier.cabinet.customer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.CommentListAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.BCommentList;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.GsonUtils;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.DividerItemDecoration;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.common.util.AppToast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sunday.statagent.StatAgent;
import com.umeng.socialize.utils.Log;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 评论列表类
 * Created by jinbiao.wu on 2015/12/3.
 */
public class CommentListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    private List<BCommentList> mListItems = new ArrayList<BCommentList>();//评论内容
    private CommentListAdapter mListAdapter;
    private int goodId;//商品id
    private boolean mIsStart = false;
    private int mCurPageIndex = 1;
    private static final int pageSize = 10;
    private static int totalRecord = 0;//返回数据总数
    @Bind(R.id.empty_view) View emptyView;
    @Bind(R.id.refreshable_view) RecyclerView recyclerView;
    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    private HeaderAndFooterRecyclerViewAdapter mHeaderAndFooterRecyclerViewAdapter = null;
    private static final int GET_LIST_DATA = 1001;//请求数据
    private static final int UPDATE_LIST_DATA = 1002;//刷新数据
    private static final int NO_LIST_DATA = 1003;//无数据
    private static final int ERRO_LIST_DATA = 1004;//数据异常
    private static final int NO_MORE_LIST_DATA = 1005;//没有更多数据了

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_LIST_DATA:
                    String url = getRequestUrl(mIsStart);
                    requestOrderListData(url);
                    break;
                case UPDATE_LIST_DATA:
                    String json = (String) msg.obj;
                    ArrayList<BCommentList> data = GsonUtils.jsonToList(json, BCommentList.class);
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
                        RecyclerViewStateUtils.setFooterViewState(CommentListActivity.this, recyclerView, pageSize, LoadingFooter.State.Normal, null);
                        mListAdapter.addAll(data);
                    }

                    if ((mCurPageIndex *pageSize >= totalRecord) && mIsStart) {
                        RecyclerViewStateUtils.setFooterViewState2(CommentListActivity.this, recyclerView, pageSize, LoadingFooter.State.TheEnd, null);
                        recyclerView.removeOnScrollListener(mOnScrollListener);
                    }

                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case NO_LIST_DATA:
                    if (mIsStart) {
                        emptyView.setVisibility(View.VISIBLE);
                    }

                    mListAdapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);

                    mIsStart = false;
                    break;
                case NO_MORE_LIST_DATA:
                    mIsStart = false;
                    break;
                case ERRO_LIST_DATA:
                    AppToast.makeToast(CommentListActivity.this, "获取数据异常，请稍后再试！");
                    recyclerView.setVisibility(View.VISIBLE);

                    mIsStart = false;
                    break;
            }
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_comment_list;
    }

    @Override
    public void initData() {

    }

    public void initView() {
        PushApplication.addActivity(this);
        StatAgent.initAction(this, "", "1", "21", "", "", "", "1", "");
        goodId = getIntent().getExtras().getInt(SortResultActivity.RESULT_ID);
        mTitleText.setText("用户评论");
        mBackBtn.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(this, 24));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);

        mListAdapter = new CommentListAdapter(CommentListActivity.this, mListItems);
        mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mListAdapter);
        recyclerView.setAdapter(mHeaderAndFooterRecyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL_LIST));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mOnScrollListener.setSwipeRefreshLayout(swipeRefreshLayout);
        recyclerView.addOnScrollListener(mOnScrollListener);
        ((TextView) findViewById(R.id.load_text)).setText("暂时没有评论");
        mHandler.sendEmptyMessage(GET_LIST_DATA);
        mIsStart = true;
    }

    private RecyclerOnScrollListener mOnScrollListener = new RecyclerOnScrollListener() {

        public void onBottom() {

            LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(recyclerView);
            if(state == LoadingFooter.State.Loading) {
                return;
            }

            if (mCurPageIndex*pageSize < totalRecord) {
                // loading more
                RecyclerViewStateUtils.setFooterViewState(CommentListActivity.this, recyclerView, pageSize, LoadingFooter.State.Loading, null);
                mIsStart = false;
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
            } else {
                //the end
                RecyclerViewStateUtils.setFooterViewState(CommentListActivity.this, recyclerView, pageSize, LoadingFooter.State.TheEnd, null);
            }
        }
    };

    private String getRequestUrl(boolean isStart) {
        if ((mListAdapter.getItemCount() == 0) || isStart) {
            mCurPageIndex = 1;
        } else {
            ++mCurPageIndex;
        }
        String url = Constant.URL_COMMENT_LIST;
        return url;
    }

    private void requestOrderListData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("goods_id",""+goodId);
        params.add("page",""+mCurPageIndex);
        params.add("pagesize", "10");
//        Log.d("wjb","评论："+url+"&goods_id="+goodId+"&page="+mCurPageIndex+"&pagesize=10");
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                swipeRefreshLayout.setRefreshing(false);
                mHandler.sendEmptyMessage(ERRO_LIST_DATA);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                swipeRefreshLayout.setRefreshing(false);
                String json = new String(response);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            String json_comp = null;
                            String json_list = null;
                            try {
                                JSONObject jsonObject = new JSONObject(JsonUtil.getResultFromJson(json));
                                //评论综合
                                json_comp = jsonObject.getString("comp");
                                JSONObject object_comp = new JSONObject(json_comp);
                                totalRecord = object_comp.getInt("all");
                                //评论列表
                                json_list = jsonObject.getString("list");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mHandler.obtainMessage(UPDATE_LIST_DATA, json_list).sendToTarget();
                            break;
                        case 1002:
                            mHandler.sendEmptyMessage(NO_LIST_DATA);
                            break;
                        case 2001:
                            mHandler.sendEmptyMessage(ERRO_LIST_DATA);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        if (!mIsStart) {//防止多次下拉
            mIsStart = true;
            emptyView.setVisibility(View.GONE);
            mHandler.sendEmptyMessage(GET_LIST_DATA);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StatAgent.initAction(this, "", "2", "21", "", "", "back", "2", "");
    }
}
