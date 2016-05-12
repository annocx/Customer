package com.haier.cabinet.customer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.CouponsAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.base.CommonWebActivity;
import com.haier.cabinet.customer.entity.Coupon;
import com.haier.cabinet.customer.entity.Order;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.widget.recyclerview.CustRecyclerView;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by SK on 2016/2/18.
 */
public class UserCouponsActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.right_text) TextView rightText;

    private static int totalRecord = 0;

    private boolean mIsStart = false;

    private int mCurPageIndex = 1;

    private ArrayList<Order> mListItems = new ArrayList<>();

    @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.recycler_view) CustRecyclerView recyclerView;

    private CouponsAdapter couponsAdapter;

    private String params = null;

    private int from = -1;

    @Bind(R.id.no_use_coupon) View noUseCoupon;

    private String shopListId = null;

    private String couponCode = null;

    private static final int REQUEST_COUNT = 10;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_coupons;
    }

    @Override
    public void initData() {
        shopListId = getIntent().getStringExtra(Constant.INTENT_KEY_GOODS_ID);
        couponCode = getIntent().getStringExtra(Constant.INTENT_KEY_COUPON_CODE);
        from = getIntent().getIntExtra(Constant.INTENT_KEY_FROM, -1);   /// 0 为从我的优惠券进入；1 为从下单界面进入

        // 不使用优惠券按钮只有在下单进入的时候展示
        if (from == 0) {
            noUseCoupon.setVisibility(View.GONE);
        } else if (from == 1) {
            noUseCoupon.setVisibility(View.VISIBLE);
            noUseCoupon.setOnClickListener(this);
        }

        // 进入方式不一样接口不同
        if (from == 0) {
            params = "/get_list_by_member?member_id=" + PushApplication.getInstance().getUserId();
        } else if (from == 1) {
            params = "/get_available_coupons?member_id="+ PushApplication.getInstance().getUserId()
                    + "&goods=" + shopListId;
        }
        couponsAdapter.setFrom(from);

        mIsStart = true;
        mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
    }

    @Override
    public void onRefresh() {
        if (!mIsStart) {//防止多次下拉
            mIsStart = true;
            mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
        }
    }

    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.GET_LIST_DATA:
                    String url = getRequestUrl(mIsStart);
                    requestOrderListData(url, mListItems.size());
                    break;
                case Constant.UPDATE_LIST:
                    String json = (String) msg.obj;
                    List<Coupon> data = getListByJosn(json);

                    if (null == data) {
                        return;
                    }

                    if (mIsStart) {
                        //清空数据
                        if (couponsAdapter.getItemCount() > 0) {
                            couponsAdapter.clear();
                        }
                        couponsAdapter.addAll(data);
                    } else {
                        RecyclerViewStateUtils.setFooterViewState(recyclerView, LoadingFooter.State.Normal);
                        couponsAdapter.addAll(data);
                    }

                    couponsAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;

                case Constant.GET_LIST_DATA_FAILURE:
                case Constant.NO_LIST_DATA:
                case Constant.NO_MORE_LIST_DATA:
                    couponsAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                default:
                    break;
            }
        }
    };

    public List<Coupon> getListByJosn(String json) {
        ArrayList<Coupon> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("data") || TextUtils.isEmpty(jsonObject.getJSONArray("data").toString())) {
                return null;
            }
            JSONArray couponsObject = jsonObject.getJSONArray("data");
            totalRecord = jsonObject.getInt("total");

            for (int i = 0; i < couponsObject.length(); i++) {
                JSONObject orderObject = couponsObject.getJSONObject(i);

                Coupon coupon = Util.getCouponByJosn(orderObject, from);

                if (from == 0) {
                    list.add(coupon);
                } else if (from == 1) {
                    boolean isEquals = false;

                    if (null != couponCode) {
                        for (int j = 0; j < couponCode.split("_").length; j ++) {
                            if (coupon.getCoupon_sn().equals(couponCode.split("_")[j])) {
                                isEquals = true;
                                break;
                            }
                        }

                        if (!isEquals) list.add(coupon);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    //  请求优惠券列表
    private void requestOrderListData(String url, final int listSize) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                swipeRefreshLayout.setRefreshing(false);
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA_FAILURE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                swipeRefreshLayout.setRefreshing(false);
                String json = new String(response);
                Log.e("haipeng---",json);
                if (200 == statusCode && !json.isEmpty()) {
                    switch (JsonUtil.getCouponFromServer(json)) {
                        case 1001:
                            mHandler.obtainMessage(Constant.UPDATE_LIST, json).sendToTarget();
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

    public String getRequestUrl(boolean isStart) {
        if ((couponsAdapter.getItemCount() == 0) || isStart) {
            mCurPageIndex = 1;
        } else {
            ++mCurPageIndex;
        }
        String url = Constant.URL_COUPON
                                + params
                                + "&page_index=" + mCurPageIndex;
        return url;
    }

    @Override
    @OnClick({R.id.back_img,R.id.right_text,R.id.no_use_coupon})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.right_text:
                StatAgent.initAction(this, "", "2", "18", "", "", getString(R.string.rules_coupons), "1", "");

                Bundle bundle = new Bundle();
                bundle.putString("title", getString(R.string.rules_coupons));
                bundle.putString("url", Constant.URL_COUPON_RULES);
                IntentUtil.startActivity(this, CommonWebActivity.class, bundle);
                break;
            case R.id.no_use_coupon:
                Intent intent = new Intent();
                intent.putExtra(Constant.INTENT_KEY_COUPON_CODE, ""); // 优惠券code
                intent.putExtra(Constant.INTENT_KEY_GOODS_ID, shopListId); // 优惠商品ID
                intent.putExtra(Constant.INTENT_KEY_COUPON_DISCOUNT, 0.00);  // 优惠券金额
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }

    private RecyclerOnScrollListener mOnScrollListener = new RecyclerOnScrollListener() {

        public void onBottom() {

            LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(recyclerView);
            if(state == LoadingFooter.State.Loading) {
                return;
            }

            if (mCurPageIndex < totalRecord) {
                // loading more
                RecyclerViewStateUtils.setFooterViewState(UserCouponsActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
            } else {

                //the end
                RecyclerViewStateUtils.setFooterViewState(UserCouponsActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
            }
        }
    };

    public void initView() {
        StatAgent.initAction(this, "", "1", "18", "", "", "", "1", "");
        mBackBtn.setVisibility(View.VISIBLE);
        mTitleText.setText(R.string.user_coupons);
        rightText.setText(R.string.rules_coupons);
        rightText.setTextSize(15);

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(this, 24));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setEmptyView(findViewById(R.id.empty_view));
        mOnScrollListener.setSwipeRefreshLayout(swipeRefreshLayout);
        recyclerView.addOnScrollListener(mOnScrollListener);

        couponsAdapter = new CouponsAdapter(this);
        recyclerView.setAdapter(couponsAdapter);
    }

}
