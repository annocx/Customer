package com.haier.cabinet.customer.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.common.util.IntentUtil;

import butterknife.Bind;
import butterknife.OnClick;


public class PaySuccessActivity extends BaseActivity {

    private String paySn;

    private int payOrderWay = Constant.PAY_FROM_SHOPCART;

    private String orderId;

    @Bind(R.id.go_order_list_btn) Button goOrderListBtn;

    @Bind(R.id.go_order_detail_btn) Button goOrderDetailBtn;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pay_success;
    }

    public void initView() {
        mBackBtn.setVisibility(View.VISIBLE);
        mTitleText.setText("支付成功");

        paySn = getIntent().getStringExtra(Constant.INTENT_KEY_PAY_SN);
        payOrderWay = getIntent().getIntExtra("pay_src", Constant.PAY_FROM_SHOPCART);
        orderId = getIntent().getStringExtra(Constant.INTENT_KEY_ORDER_ID);

    }

    @Override
    public void initData() {

    }

    @Override
    @OnClick({R.id.go_order_detail_btn,R.id.go_order_list_btn})
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.go_order_detail_btn:
                bundle.putString(Constant.INTENT_KEY_PAY_SN, paySn);
                bundle.putInt("pay_src", payOrderWay);
                bundle.putString(Constant.INTENT_KEY_ORDER_ID, orderId);
                IntentUtil.startActivity(this, OrderDetailsActivity.class, bundle);
                break;
            case R.id.go_order_list_btn:
                bundle.putInt("order_state", 2);
                IntentUtil.startActivity(this, UserOrderListActivity.class, bundle);
                break;
        }

    }
}
