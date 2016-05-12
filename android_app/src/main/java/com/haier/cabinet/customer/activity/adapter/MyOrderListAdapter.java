package com.haier.cabinet.customer.activity.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.LogisticsActivity;
import com.haier.cabinet.customer.activity.OrderDetailsActivity;
import com.haier.cabinet.customer.activity.ServerCommentActivity;
import com.haier.cabinet.customer.activity.ShopDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.entity.Order;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sunday.statagent.StatAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.Header;

public class MyOrderListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<Order> mDataList = new ArrayList<>();
    private LayoutInflater inflater;
    private Handler mHandler;
    private static int mPosition = 0;

    public MyOrderListAdapter(Context context, Handler handler) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(mContext);
        this.mHandler = handler;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.layout_my_order_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Order order = mDataList.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        OrderProductListAdapter listAdapter = new OrderProductListAdapter(mContext, order.getDataList());
        viewHolder.listView.setAdapter(listAdapter);
        viewHolder.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                if (null != order.getDataList()) StatAgent.initAction(mContext, "", "2", "20", "", "", order.getDataList().get(index).getShopName(), "1", "");

                Bundle bundle = new Bundle();
                bundle.putString(Constant.INTENT_KEY_ORDER_ID, order.getOrderId());
                bundle.putInt("pay_src",Constant.PAY_FROM_ORDER);
                bundle.putInt("position", position);
                Activity activity = (Activity) mContext;
                IntentUtil.startActivity(activity, OrderDetailsActivity.class, bundle);
            }
        });

        String couponText = null;
        if (Integer.valueOf(order.getCouponUse()) == 0) {
            couponText = "";
        } else if (Integer.valueOf(order.getCouponUse()) == 1) {
            couponText = String.format(Locale.CHINA, "已优惠¥%s，", order.getCouponDiscount());
        }

        String totalPrice = String.format(Locale.CHINA,
                "合计：¥%s (" + couponText + "含运费¥%s)",
                String.valueOf(Util.getTotal(Double.valueOf(order.getOrderAmount()))),
                order.getShippingFee());
        viewHolder.totalPriceText.setText(totalPrice);

        viewHolder.shopNameText.setText(order.getStoreName());

        switch (Integer.valueOf(order.getOrderState())) {
            case Constant.ORDER_STATE_NEW:
                viewHolder.orderStateText.setText("待付款");

                viewHolder.btnLayout.setVisibility(View.VISIBLE);
                viewHolder.middleLine.setVisibility(View.VISIBLE);
                viewHolder.seeLogisticBtn.setVisibility(View.GONE);

                viewHolder.cancelBtn.setVisibility(View.VISIBLE);
                viewHolder.cancelBtn.setText("取消订单");
                viewHolder.cancelBtn.setTag(0);

                if (Integer.valueOf(order.getPayState()) == 1) {
                    viewHolder.payBtn.setVisibility(View.VISIBLE);
                    viewHolder.payBtn.setText("去支付");
                    viewHolder.payBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StatAgent.initAction(mContext, "", "2", "20", "", "", viewHolder.payBtn.getText().toString(), "1", "");

                            Message msg = new Message();
                            msg.what = Constant.GO_TO_PAY;
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Constant.INTENT_KEY_ORDER_DETAIL, order);
                            bundle.putString(Constant.INTENT_KEY_PAY_SN, order.getPaySn());
                            bundle.putString(Constant.INTENT_KEY_ORDER_SN, order.getOrderSn());
                            bundle.putString(Constant.INTENT_KEY_ORDER_ID, order.getOrderId());
                            bundle.putDouble("money", Double.valueOf(order.getOrderAmount()));
                            bundle.putInt("pay_src",Constant.PAY_FROM_ORDER);
                            bundle.putInt("position", position);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                    });
                } else if (Integer.valueOf(order.getPayState()) == 0) {
                    // 订单无效
                    viewHolder.payBtn.setVisibility(View.GONE);
                }

                break;
            case Constant.ORDER_STATE_PAY:
                viewHolder.orderStateText.setText("待发货");
                viewHolder.payBtn.setVisibility(View.GONE);
                viewHolder.cancelBtn.setVisibility(View.GONE);
                viewHolder.seeLogisticBtn.setVisibility(View.GONE);
                viewHolder.btnLayout.setVisibility(View.GONE);
                viewHolder.middleLine.setVisibility(View.GONE);
                break;
            case Constant.ORDER_STATE_SEND:
                viewHolder.orderStateText.setText("待收货");
                viewHolder.btnLayout.setVisibility(View.VISIBLE);
                viewHolder.middleLine.setVisibility(View.VISIBLE);
                viewHolder.seeLogisticBtn.setVisibility(View.VISIBLE);
                viewHolder.cancelBtn.setVisibility(View.GONE);
                // 此处将支付转换为确认按钮
                viewHolder.payBtn.setVisibility(View.VISIBLE);
                viewHolder.payBtn.setText("确认收货");
                viewHolder.payBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPosition = position;
                        String url = getRequestUrl();
                        requestOrderListData(url, order.getOrderId());
                    }
                });
                break;
            case Constant.ORDER_STATE_SUCCESS:
                viewHolder.orderStateText.setText("已完成");
                viewHolder.btnLayout.setVisibility(View.VISIBLE);
                viewHolder.middleLine.setVisibility(View.VISIBLE);
                viewHolder.seeLogisticBtn.setVisibility(View.VISIBLE);
                viewHolder.payBtn.setVisibility(View.GONE);
                viewHolder.cancelBtn.setVisibility(View.GONE);
                if(order.getEvaluationState().equals("0")){//未评价
                    viewHolder.commentBtn.setVisibility(View.VISIBLE);
                } else if (order.getEvaluationState().equals("1")) {
                    viewHolder.commentBtn.setVisibility(View.GONE);
                }
                viewHolder.cancelBtn.setText("删除订单");
                viewHolder.cancelBtn.setTag(1);
                viewHolder.cancelBtn.setVisibility(View.GONE);
                break;
            case Constant.ORDER_STATE_CANCEL:
                viewHolder.orderStateText.setText("交易关闭");
                viewHolder.btnLayout.setVisibility(View.GONE);
                viewHolder.middleLine.setVisibility(View.GONE);
                viewHolder.seeLogisticBtn.setVisibility(View.GONE);
                viewHolder.cancelBtn.setVisibility(View.GONE);
                viewHolder.payBtn.setVisibility(View.GONE);
                break;
        }

        viewHolder.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatAgent.initAction(mContext, "", "2", "20", "", "", viewHolder.cancelBtn.getText().toString(), "1", "");

                if (((Integer) v.getTag()) == 0) {
                    mPosition = position;
                    String url = getCancelOrderRequestUrl();
                    requestOrderListData(url, order.getOrderId());
                } else if (((Integer) v.getTag()) == 1) {

                }
            }
        });

        viewHolder.seeLogisticBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatAgent.initAction(mContext, "", "2", "20", "", "", viewHolder.seeLogisticBtn.getText().toString(), "1", "");

                    Bundle bundle = new Bundle();
                    bundle.putString("orderNo", order.getOrderId());
                    Activity activity = (Activity) mContext;
                    IntentUtil.startActivity(activity, LogisticsActivity.class, bundle);
            }
        });
        viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("orderNo", order.getOrderId());
                bundle.putInt("position", position);
                IntentUtil.startActivity((Activity) mContext, ServerCommentActivity.class, bundle);
            }
        });

        viewHolder.topView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(SortResultActivity.RESULT_NAME, order.getStoreName());
                bundle.putInt(SortResultActivity.RESULT_TYPE, SortResultActivity.RESULT_SHOP);
                bundle.putInt(SortResultActivity.RESULT_ID, Integer.valueOf(order.getStoreId()));
                IntentUtil.startActivity((Activity) mContext, ShopDetailsActivity.class, bundle);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addAll(Collection<Order> list) {
        int lastIndex = this.mDataList.size();
        if (this.mDataList.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size());
        }
    }

    public void clear() {
        if (mDataList.size() > 0) {
            mDataList.clear();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private TextView shopNameText;
        private TextView totalPriceText;
        private TextView countText;
        private TextView orderStateText;
        private ListView listView;
        private Button payBtn;
        private Button cancelBtn;
        private Button seeLogisticBtn;
        private Button commentBtn;
        private LinearLayout btnLayout;
        private View topView;
        private View middleLine;

        public ViewHolder(View itemView) {
            super(itemView);
            topView =  itemView.findViewById(R.id.top_layout);
            shopNameText = (TextView) itemView.findViewById(R.id.shop_name_text);
            totalPriceText = (TextView) itemView.findViewById(R.id.total_price_text);
            countText = (TextView) itemView.findViewById(R.id.total_count_text);
            orderStateText = (TextView) itemView.findViewById(R.id.order_state_text);
            seeLogisticBtn = (Button) itemView.findViewById(R.id.see_logistic_btn);
            cancelBtn = (Button) itemView.findViewById(R.id.cancel_btn);
            payBtn = (Button) itemView.findViewById(R.id.pay_btn);
            commentBtn = (Button) itemView.findViewById(R.id.comment_btn);
            btnLayout = (LinearLayout) itemView.findViewById(R.id.btn_layout);
            middleLine = itemView.findViewById(R.id.middle_line);
            listView = (ListView) itemView.findViewById(R.id.listView);
            listView.setDivider(mContext.getResources().getDrawable(android.R.color.white));
            listView.setDividerHeight(5);
            listView.setCacheColorHint(mContext.getResources().getColor(android.R.color.transparent));
        }
    }

    private void requestOrderListData(String url, String orderId) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("order_id", orderId);
        params.add("member_id", PushApplication.getInstance().getUserId());
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                DialogHelper.showDialogForLoading(mContext, mContext.getString(R.string.loading), false);
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                DialogHelper.stopProgressDlg();
                AppToast.showShortText(mContext, "操作失败");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                DialogHelper.stopProgressDlg();

                String json = new String(response);

                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            mDataList.remove(mPosition);
                            notifyDataSetChanged();
                            if (mDataList.size() == 0) {
                                mHandler.sendEmptyMessage(Constant.NO_LIST_DATA);
                            }
                            break;
                        default:
                            AppToast.showShortText(mContext, "操作失败");
                            break;
                    }
                } else {
                    AppToast.showShortText(mContext, "操作失败");
                }
            }
        });
    }

    private String getRequestUrl() {
        String url = Constant.SHOP_DOMAIN + "/appapi/index.php?act=member_order&op=order_receive";
        return url;
    }

    private String getCancelOrderRequestUrl() {
        String url = Constant.SHOP_DOMAIN + "/appapi/index.php?act=member_order&op=order_cancel";
        return url;
    }

    /**
     * 支付成功
     * @param position
     */
    public void updateOrderState(int position){
        if(position < mDataList.size()){
            mDataList.remove(position);
            notifyDataSetChanged();
        }
    }

    /**
     * 评论完成
     * @param position
     */
    public void updateOrderEvaluationState(int position){
        Order order = mDataList.get(position);
        order.setEvaluationState("1");
        mDataList.set(position, order);
        notifyItemChanged(position);
    }

}
