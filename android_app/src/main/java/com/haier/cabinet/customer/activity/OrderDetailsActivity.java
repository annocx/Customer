package com.haier.cabinet.customer.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.OrderDetailAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.base.CommonWebActivity;
import com.haier.cabinet.customer.entity.Order;
import com.haier.cabinet.customer.entity.OrderDetailItem;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.CustListView;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;

public class OrderDetailsActivity extends BaseActivity {
    private static final String TAG = "OrderDetailsActivity";
    @Bind(R.id.name_text) TextView nameText;
    @Bind(R.id.phone_text) TextView phoneText;
    @Bind(R.id.copy_number_btn) Button copyNumberBtn;
    @Bind(R.id.number_text) TextView numberText;
    @Bind(R.id.warm_prompt_text) TextView warmPromptText;
    @Bind(R.id.fast_delivery_text) TextView fastDeliveryText;
    @Bind(R.id.time_limit_text) TextView timeLimitText;
    @Bind(R.id.shop_name_text) TextView shopNameText;
    @Bind(R.id.total_price_text) TextView totalPriceText;
    @Bind(R.id.fee) TextView feeText;
    @Bind(R.id.address_text) TextView addressText;
    @Bind(R.id.order_no_text) TextView orderNoText;
    @Bind(R.id.query_text) TextView queryText;
    @Bind(R.id.btn_add_comment) Button btn_add_comment;//添加评论
    @Bind(R.id.btn_logistics) Button btn_logistics;//物流查询
    @Bind(R.id.query_layout) LinearLayout queryLayout;
    @Bind(R.id.query_track_number_layout) LinearLayout queryTrackNumberLayout;
    @Bind(R.id.pay_btn) Button payBtn;
    @Bind(R.id.pay_status_text) TextView payStatusText;
    @Bind(R.id.xiaoneng) ImageView xiaoNeng;
    @Bind(R.id.list_view) CustListView mListView;

    private String orderId;

    private String paySn;

    private int payOrderWay = Constant.PAY_FROM_SHOPCART;

    private Order order;

    private String idKey;

    private String opContent;

    private List<OrderDetailItem> mSelectedList;
    private OrderDetailAdapter mListAdapter;

    private boolean mIsStart = false;

    private ClipboardManager clipboardManager;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_order_detail;
    }

    private void setOrderState(int state, String Evaluation) {
        switch (state) {
            case Constant.ORDER_STATE_NEW:
                payStatusText.setText("待付款");

                fastDeliveryText.setVisibility(View.GONE);
                warmPromptText.setVisibility(View.GONE);
                queryTrackNumberLayout.setVisibility(View.GONE);
                break;
            case Constant.ORDER_STATE_PAY:
                payStatusText.setText("待发货");

                fastDeliveryText.setVisibility(View.VISIBLE);
                warmPromptText.setVisibility(View.VISIBLE);
                queryTrackNumberLayout.setVisibility(View.GONE);
                break;
            case Constant.ORDER_STATE_SEND:
                payStatusText.setText("待收货");

                fastDeliveryText.setVisibility(View.GONE);
                warmPromptText.setVisibility(View.GONE);
                queryTrackNumberLayout.setVisibility(View.VISIBLE);
                btn_logistics.setVisibility(View.VISIBLE);
                break;
            case Constant.ORDER_STATE_SUCCESS:
                payStatusText.setText("已完成");
                if (Evaluation.equals("0")) {//未评价
                    btn_add_comment.setVisibility(View.VISIBLE);
                }
                fastDeliveryText.setVisibility(View.GONE);
                warmPromptText.setVisibility(View.GONE);
                queryTrackNumberLayout.setVisibility(View.VISIBLE);
                btn_logistics.setVisibility(View.VISIBLE);
                break;
            case Constant.ORDER_STATE_CANCEL:
                payStatusText.setText("交易关闭");
                btn_add_comment.setVisibility(View.GONE);
                fastDeliveryText.setVisibility(View.GONE);
                warmPromptText.setVisibility(View.GONE);
                queryTrackNumberLayout.setVisibility(View.VISIBLE);
                copyNumberBtn.setVisibility(View.GONE);
                btn_logistics.setVisibility(View.VISIBLE);
                break;
        }
    }

    private static final int GET_DETAIL_DATA = 1001;
    private static final int UPDATE_DATA = 1002;
    private static final int NO_LIST_DATA = 1003;
    private static final int USER_TOKEN_TIMEOUT = 1004;
    private static final int GET_DETAIL_DATA_FAILURE = 1005;
    private static final int NO_MORE_LIST_DATA = 1006;
    private Handler mHandler;

    {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d(TAG, "msg.what = " + msg.what);
                switch (msg.what) {
                    case GET_DETAIL_DATA:
                        String url = getRequestUrl(mIsStart);
                        Log.d("wjb", "商品详情：" + url);
                        requestShoppingCardData(url);
                        break;
                    case UPDATE_DATA:
                        String json = (String) msg.obj;
                        if (idKey.equals("pay_sn")) {
                            order = Util.getOrderByPaySn(json);
                        } else if (idKey.equals("order_id")) {
                            order = Util.getOrder(json);
                        }

                        if (null == order) {
                            return;
                        }

                        initData(order);
                        mIsStart = false;

                        break;

                    case GET_DETAIL_DATA_FAILURE:
                        AppToast.showShortText(OrderDetailsActivity.this, "获取数据超时，请稍后再试！");
                        OrderDetailsActivity.this.finish();
                        break;
                    case NO_LIST_DATA:
                        mListAdapter.notifyDataSetChanged();

                        break;
                    default:
                        break;
                }
            }

            ;
        };
    }

    @Override
    @OnClick({R.id.pay_btn,
            R.id.copy_number_btn,R.id.query_text,R.id.btn_add_comment,R.id.btn_logistics,
            R.id.xiaoneng})
    public void onClick(View v) {
        Bundle bundle;
        switch (v.getId()) {
            case R.id.pay_btn:
                goToPay(order.getPaySn(), order.getOrderId(), order.getOrderSn(), Double.valueOf(order.getOrderAmount()));
                break;
            case R.id.copy_number_btn:
                StatAgent.initAction(this, "", "2", "26", "", "", copyNumberBtn.getText().toString(), "1", "");

                if (!copyNumberBtn.getText().toString().trim().equals("请上传运单号")) {
                    if (!TextUtils.isEmpty(numberText.getText().toString().trim())) {
                        if (null == clipboardManager) {
                            clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        }
                        ClipData clipData = ClipData.newPlainText("number text", numberText.getText().toString());
                        clipboardManager.setPrimaryClip(clipData);
                        AppToast.showShortText(this, "复制成功");
                    } else {
                        AppToast.showShortText(this, "未发现运单号");
                    }
                } else {
                    AppToast.showShortText(this, "我们已收到您的反馈，我们马上通知商家尽快为您上传运单号信息！");
                }
                break;
            case R.id.query_text:
                String url = Constant.URL_EXPRESS_QUERY + "?nu=" + numberText.getText().toString();
                bundle = new Bundle();
                bundle.putString("title", "物流查询");
                bundle.putString("url", url);
                IntentUtil.startActivity(OrderDetailsActivity.this, CommonWebActivity.class, bundle);
                break;
            case R.id.btn_add_comment:
                bundle = new Bundle();
                bundle.putString("orderNo", orderId);//商品id
                bundle.putInt("position", getIntent().getIntExtra("position", 0));
                IntentUtil.startActivity(OrderDetailsActivity.this, ServerCommentActivity.class, bundle);
                break;
            case R.id.btn_logistics:
                StatAgent.initAction(this, "", "2", "26", "", "", btn_logistics.getText().toString(), "1", "");

                bundle = new Bundle();
                bundle.putString("orderNo", orderId);//商品id
                IntentUtil.startActivity(OrderDetailsActivity.this, LogisticsActivity.class, bundle);
                break;
            case R.id.xiaoneng:
                bundle = new Bundle();
                bundle.putString("title", getString(R.string.customer_service));
                bundle.putString("url", Constant.URL_ONLINE_SERVICE_ORDER
                        + "?orderid=" + order.getOrderId()
                        + "&orderprice=" + order.getOrderAmount()
                        + "&uid=" + PushApplication.getInstance().getUserId());
                IntentUtil.startActivity(OrderDetailsActivity.this, CommonWebActivity.class, bundle);
                break;
            default:
                break;
        }
    }

    private void goToPay(String paySn, String orderId, String orderNo, double money) {
        //调用支付
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.INTENT_KEY_ORDER_DETAIL, order);
        bundle.putString("order_sn", orderNo);
        bundle.putDouble("money", money);
        bundle.putString(Constant.INTENT_KEY_PAY_SN, paySn);
        bundle.putString(Constant.INTENT_KEY_ORDER_ID, orderId);
        bundle.putInt("pay_src", Constant.PAY_FROM_ORDER);
        bundle.putInt("position", getIntent().getIntExtra("position", 0));
        IntentUtil.startActivity(this, CheckoutCounterActivity.class, bundle);
    }

    private void modifyShopCartData(int shopCartId, int gid, int num) {
        Log.d(TAG, "modifyShopCartData -- id " + shopCartId);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("m", "Cart");
        params.put("a", "edit");
        params.put("id", shopCartId);
        params.put("gid", gid);
        params.put("num", num);
        params.put("user_id", PushApplication.getInstance().getUserId());
        client.get(Constant.SHOP_DOMAIN, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
//                Log.d(TAG, "json " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        Log.d(TAG, "修改商品数量成功");
                    } else {
                        Log.d(TAG, "修改商品数量失败");
                    }
                }
            }
        });
    }

    private void requestShoppingCardData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(OrderDetailsActivity.this, getString(R.string.loading), false);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                DialogHelper.stopProgressDlg();
                mHandler.sendEmptyMessage(GET_DETAIL_DATA_FAILURE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                DialogHelper.stopProgressDlg();

                String json = new String(response);
                Log.d(TAG, "onSuccess " + json);

                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            mHandler.obtainMessage(UPDATE_DATA, json).sendToTarget();
                            break;
                        case 201:
                            if (mListAdapter.getCount() == 0) {
                                mHandler.sendEmptyMessage(NO_LIST_DATA);
                            } else {
                                mHandler.sendEmptyMessage(NO_MORE_LIST_DATA);
                            }

                            break;
                        case 504:
                            mHandler.sendEmptyMessage(USER_TOKEN_TIMEOUT);
                            break;
                        default:
                            break;
                    }

                }
            }
        });
    }

    private String getRequestUrl(boolean isStart) {
        String url = Constant.SHOP_DOMAIN
                + "/appapi/index.php?act=member_order"
                + "&op=" + opContent
                + "&member_id=" + PushApplication.getInstance().getUserId()
                + "&" + idKey + "=" + orderId.split(",")[0];
//        Log.d(TAG, "url -- " + url);
        return url;
    }

    public void initView() {
        mTitleText = (TextView) findViewById(R.id.title_text);
        mBackBtn = (ImageView) findViewById(R.id.back_img);
        mTitleText.setText("订单详情");
        mBackBtn.setVisibility(View.VISIBLE);
        mListView.setDivider(getResources().getDrawable(R.color.app_bg));
        mListView.setDividerHeight(30);
        mListView.setCacheColorHint(getResources().getColor(android.R.color.transparent));

        mSelectedList = new ArrayList<OrderDetailItem>();
        mListAdapter = new OrderDetailAdapter(this, mHandler);
        mListView.setAdapter(mListAdapter);

        StatAgent.initAction(this, "", "1", "26", "", "", "", "1", "");
        orderId = getIntent().getStringExtra(Constant.INTENT_KEY_ORDER_ID);
        paySn = getIntent().getStringExtra(Constant.INTENT_KEY_PAY_SN);
        payOrderWay = getIntent().getIntExtra("pay_src", Constant.PAY_FROM_SHOPCART);

        switch (payOrderWay) {
            // 从订单管理进入
            case Constant.PAY_FROM_ORDER:
                opContent = "order_detail";
                idKey = "order_id";
                mHandler.sendEmptyMessage(GET_DETAIL_DATA);
                break;
            // 从支付成功界面进入
            case Constant.PAY_FROM_SHOPCART:
                orderId = paySn;
                opContent = "get_pay_result";
                idKey = "pay_sn";
                mHandler.sendEmptyMessage(GET_DETAIL_DATA);
                break;
            default:
                break;
        }
    }

    @Override
    public void initData() {

    }

    public void initData(Order order) {
        shopNameText.setText(order.getStoreName());
        nameText.setText(String.format(Locale.CHINA, "%s%s", getString(R.string.consignee), order.getReciverName()));
        totalPriceText.setText(String.format(Locale.CHINA, "￥%s", String.valueOf(Util.getTotal(Double.valueOf(order.getOrderAmount())))));

        String couponText = null;
        if (Integer.valueOf(order.getCouponUse()) == 0) {
            couponText = "";
        } else if (Integer.valueOf(order.getCouponUse()) == 1) {
            couponText = String.format(Locale.CHINA, "已优惠¥%s，", order.getCouponDiscount());
        }

        feeText.setText(String.format(Locale.CHINA, "(" + couponText + "%s￥%s)", getString(R.string.fee), String.valueOf(Util.getTotal(Double.valueOf(order.getShippingFee())))));
        phoneText.setText(order.getPhone());
        addressText.setText(String.format(Locale.CHINA, "%s%s", getString(R.string.shipping_address), order.getAddress()));
        orderNoText.setText(order.getOrderSn());

        setOrderState(Integer.valueOf(order.getOrderState()), order.getEvaluationState());
        if(order.getOrderState().equals("10")){
            payBtn.setVisibility(View.VISIBLE);
        }else {
            payBtn.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(order.getShippingCode())) {
            numberText.setText("暂无亲的运单号呀~");
            copyNumberBtn.setText("请上传运单号");
            queryLayout.setVisibility(View.GONE);
            btn_logistics.setVisibility(View.GONE);
        } else {
            numberText.setText("运单号：" + order.getShippingCode());
            copyNumberBtn.setText("复制运单号");
            queryLayout.setVisibility(View.GONE);
            btn_logistics.setVisibility(View.VISIBLE);
        }

        OrderDetailItem orderDetailItem = new OrderDetailItem();
        orderDetailItem.products = order.getDataList();
        mSelectedList.add(orderDetailItem);
        mListAdapter.setDataList(mSelectedList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StatAgent.initAction(this, "", "2", "26", "", "", "back", "2", "");
    }
}
