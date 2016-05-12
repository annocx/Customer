package com.haier.cabinet.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.PlaceOrderItemAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.AddressInfo;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.ShopCartItem;
import com.haier.cabinet.customer.event.AddressInfoEvent;
import com.haier.cabinet.customer.event.DeleteAddressEvent;
import com.haier.cabinet.customer.event.ShopCartEvent;
import com.haier.cabinet.customer.fragment.ShopCartFragment;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.HttpUtil;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import kankan.wheel.widget.adapters.City;
import kankan.wheel.widget.adapters.Province;

public class PlaceOrderActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = "PlaceOrderActivity";
    @Bind(R.id.total_money_text)
    TextView mTotalMoneyText;
    @Bind(R.id.submit_order_btn)
    Button mSubmitOrderBtn;
    @Bind(R.id.listView)
    ListView mListView;
    @Bind(R.id.user_address_layout)
    View addressView;
    @Bind(R.id.default_address_view)
    View myAddressView;
    @Bind(R.id.no_address_view)
    TextView noAddressView;
    @Bind(R.id.order_username_text)
    TextView mUserNameText;
    @Bind(R.id.order_phone_text)
    TextView mPhoneText;
    @Bind(R.id.order_address_text)
    TextView mAddressText;
    @Bind(R.id.money_coupon_text)
    TextView money_coupon_text;
    @Bind(R.id.scrollLayout)
    ScrollView mScrollView;

    private PlaceOrderItemAdapter mListAdapter;

    private List<ShopCartItem> mListItems;

    private AddressInfo addressInfo;

    private String orderNo = null;

    private String goods_id_array;//优惠劵返回的所有商品id

    private String goods_coupon_sn;//优惠劵返回的优惠劵sn

    private double goods_coupon_price;//优惠劵金额

    public static PlaceOrderActivity instance = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_place_order;
    }

    public void initView() {

        StatAgent.initAction(this, "", "1", "15", "", "", "", "1", "");
        mTitleText.setText("确认订单");
        mBackBtn.setVisibility(View.VISIBLE);

        mListView.setDivider(getResources().getDrawable(R.color.app_bg));
        mListView.setDividerHeight(15);
        mListView.setCacheColorHint(getResources().getColor(android.R.color.transparent));
        mScrollView.smoothScrollTo(0, 0);
    }

    public void onEventMainThread(AddressInfoEvent event) {
        if (addressInfo == null) {
            getDefautlAddress();
        } else {
            if (null == event.addressInfo) {
                getDefautlAddress();
            } else {

                if (addressInfo.id != null && event.addressInfo.id != null) {
                    if (event.addressInfo.id.equals(addressInfo.id) && (addressInfo.id != null)) {
                        initAddressView(event.addressInfo);
                    }
                } else {
                    getDefautlAddress();
                }
                if (addressInfo.id != null) {
                    if (event.addressInfo.id.equals(addressInfo.id)) {
                        initAddressView(event.addressInfo);
                    } else {
                        getDefautlAddress();
                    }
                }
            }

        }
    }

    public void onEventMainThread(DeleteAddressEvent event) {
        if (addressInfo != null) {
            if (null != event.addressInfo && event.addressInfo.id.equals(addressInfo.id)) {
                addressInfo = null;
                noAddressView.setVisibility(View.VISIBLE);
                myAddressView.setVisibility(View.INVISIBLE);
                getDefautlAddress();
            }
        } else {
            getDefautlAddress();
        }
    }

    double orderTotalPrice = 0.0;

    public void initData() {
        mListItems = new ArrayList<>();
        List<ShopCartItem> productList = (List<ShopCartItem>) getIntent().getSerializableExtra("order_list");
        mListItems.addAll(productList);
        mListAdapter = new PlaceOrderItemAdapter(this, mListItems);
        mListView.setAdapter(mListAdapter);

        instance = PlaceOrderActivity.this;

        mHandler.sendEmptyMessage(GET_DEFAULT_ADDRESS);
    }

    private void initTotalMoney() {
        orderTotalPrice = mListAdapter.calculatingTotalPrice();
        mTotalMoneyText.setText("￥" + orderTotalPrice);
    }


    private void initAddressView(AddressInfo addressInfo) {
        if (addressInfo != null) {
            noAddressView.setVisibility(View.GONE);
            myAddressView.setVisibility(View.VISIBLE);
        }
        mUserNameText.setText(addressInfo.name);
        mPhoneText.setText(addressInfo.phone);
        String address_union = addressInfo.getCurCity() + addressInfo.street;
        String address = address_union;
        mAddressText.setText(address);

    }


    @Override
    @OnClick({R.id.submit_order_btn, R.id.user_address_layout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_order_btn:
//                Log.d("wjb", "submitOrder info = " + JsonUtil.getCouponJsonText(mListItems));
                if (addressInfo == null) {
                    AppToast.showShortText(PlaceOrderActivity.this, "亲，请先添加一个收货地址！");
                    return;
                }
                StatAgent.initAction(this, "", "2", "15", "", "", mSubmitOrderBtn.getText().toString(), "1", "");

                if (isReady) {
                    if (mListAdapter.calculatingTotalPrice() < 0) {
                        AppToast.showShortText(PlaceOrderActivity.this, "亲，订单提交失败啦");
                    } else {
                        submitOrder();
                    }
                } else {
                    AppToast.showShortText(PlaceOrderActivity.this, "亲，同时下单的人数过多，请重试一次吧");
                    getOrderFreight();
                }

                break;
            case R.id.user_address_layout:
                StatAgent.initAction(this, "", "2", "15", "", "", "choose address", "1", "");

                Intent intent = new Intent(this, ChooseAddressListActivity.class);
                if (addressInfo != null) {
                    intent.putExtra("address_id", addressInfo.id);
                }
                startActivityForResult(intent, Constant.REQUEST_CHANGE_ADDRESS);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case Constant.REQUEST_CHANGE_ADDRESS:
                if (resultCode == RESULT_OK) {
                    addressInfo = (AddressInfo) data.getSerializableExtra("address");
                    initAddressView(addressInfo);
                    mHandler.sendEmptyMessage(GET_ORDER_FERIGHT);
                }
                break;
            case Constant.REQUEST_COUPON_PIRCE:
                if (resultCode == RESULT_OK) {
                    goods_coupon_price = data.getDoubleExtra(Constant.INTENT_KEY_COUPON_DISCOUNT, 0.00);
                    goods_id_array = data.getStringExtra(Constant.INTENT_KEY_GOODS_ID);
                    goods_coupon_sn = data.getStringExtra(Constant.INTENT_KEY_COUPON_CODE);
                    for (int i = 0; i < mListItems.size(); i++) {
                        List<Product> products = mListItems.get(i).products;
                        for (int j = 0; j < products.size(); j++) {
                            Product product = products.get(j);
                            if (goods_coupon_price != 0) {//如果优惠劵价钱不为0
                                if (product.id == Integer.valueOf(data.getStringExtra(Constant.INTENT_KEY_GOODS_ID).split("_")[0])) {
                                    product.couponPrice = goods_coupon_price;
                                    product.goodsCouponId = goods_id_array;
                                    product.couponCode = goods_coupon_sn;
                                }
                            } else {//如果不使用优惠劵，那么所有商品的优惠劵金额全部为0
                                for (int k = 0; k < getGoodsId().size(); k++) {
                                    if (product.id == Integer.valueOf(getGoodsId().get(k).toString())) {
                                        product.couponPrice = 0;
                                        product.goodsCouponId = goods_id_array;
                                        product.couponCode = "";
                                    }
                                }
                            }
                        }
                    }
                    //刷新列表
                    mListAdapter.updateProduct(Integer.valueOf(goods_id_array.split("_")[0]), goods_coupon_price);
                    initTotalMoney();
                }
                break;

            default:
                break;
        }
    }

    private final int GET_DEFAULT_ADDRESS = 1001;
    private final int GET_ORDER_FERIGHT = 1002;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case GET_DEFAULT_ADDRESS:
                    getDefautlAddress();

                    break;
                case GET_ORDER_FERIGHT:
                    getOrderFreight();

                    break;

                default:
                    break;
            }
        }
    };

    private void getDefautlAddress() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_GET_USER_DEFAULT_ADDRESS, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(PlaceOrderActivity.this, getString(R.string.loading), true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                noAddressView.setVisibility(View.VISIBLE);
                myAddressView.setVisibility(View.INVISIBLE);
                DialogHelper.stopProgressDlg();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
//				Log.d(TAG, "onSuccess json = " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        addressInfo = getDefautlAddressByJson(json);
                        if (TextUtils.isEmpty(addressInfo.id)) {
                            noAddressView.setVisibility(View.VISIBLE);
                            myAddressView.setVisibility(View.INVISIBLE);
                        } else {
                            initAddressView(addressInfo);
                            noAddressView.setVisibility(View.INVISIBLE);
                            myAddressView.setVisibility(View.VISIBLE);
                            mHandler.sendEmptyMessage(GET_ORDER_FERIGHT);
                        }
                    } else {
                        noAddressView.setVisibility(View.VISIBLE);
                        myAddressView.setVisibility(View.INVISIBLE);
                    }

                }
            }
        });

    }

    private AddressInfo getDefautlAddressByJson(String json) {
        AddressInfo address = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject resultObject = jsonObject.getJSONObject("result");

            address = new AddressInfo();
            JSONObject object = resultObject.getJSONObject("address_info");

            address.id = object.getString("address_id");
            address.street = object.getString("address");
            address.name = object.getString("true_name");
            address.phone = object.getString("mob_phone");

            Province province = new Province();
            province.id = object.getInt("area_id");
            address.province = province;
            City city = new City();
            city.id = object.getInt("city_id");
            address.city = city;


            address.provincialCityArea = object.getString("area_info");

            int state = object.getInt("is_default");
            address.status = (state == 1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return address;
    }

    private boolean isReady = false;//提交订单钱是否准确就绪

    /**
     * 获取运费
     */
    private void getOrderFreight() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("member_id", PushApplication.getInstance().getUserId());
        params.put("address_id", addressInfo.id);
        params.put("cart_id", JsonUtil.getOderJsonText(mListItems));
//        Log.d("wjb", "url:"+Constant.URL_GET_ORDER_FREIGHT + "?member_id=" + PushApplication.getInstance().getUserId() + "&address_id=" + addressInfo.id + "&cart_id=" + JsonUtil.getOderJsonText(mListItems));
        client.post(Constant.URL_GET_ORDER_FREIGHT, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(PlaceOrderActivity.this, "正在处理数据，请稍后...", true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                isReady = false;
                AppToast.showShortText(PlaceOrderActivity.this, "对不起，服务器繁忙！");
                DialogHelper.stopProgressDlg();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        isReady = true;
                        updateOrderInfo(json);
                        mListAdapter.notifyDataSetChanged();
                        initTotalMoney();
                    } else {
                        AppToast.showShortText(PlaceOrderActivity.this, "对不起，服务器繁忙！");
                    }

                }
            }
        });

    }

    /**
     * 将运费和金额更新进去
     */
    private void updateOrderInfo(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject resultObject = jsonObject.getJSONObject("result");
            JSONObject freightObject = resultObject.getJSONObject("freight_list");
            JSONObject amountObject = resultObject.getJSONObject("order_amount");
            for (int i = 0; i < mListItems.size(); i++) {

                ShopCartItem cartItem = mListItems.get(i);
                cartItem.freight = freightObject.getDouble(String.valueOf(cartItem.shop.id));
                cartItem.orderTotalPirce = amountObject.getDouble(String.valueOf(cartItem.shop.id));

            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }
    }

    private void submitOrder() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("member_id", PushApplication.getInstance().getUserId());
        params.put("address_id", addressInfo.id);
        params.put("cart_id", JsonUtil.getOderJsonText(mListItems));
        params.put("coupon", JsonUtil.getCouponJsonText(mListItems));
        Log.d("wjb", Constant.URL_SUMIT_ORDER + "&member_id=" + PushApplication.getInstance().getUserId() + "&address_id=" + addressInfo.id
                + "&cart_id=" + JsonUtil.getOderJsonText(mListItems) + "&coupon=" + JsonUtil.getCouponJsonText(mListItems));
        client.post(Constant.URL_SUMIT_ORDER, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(PlaceOrderActivity.this, "正在创建乐家订单，请稍后...", true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                Log.e(TAG, "submitOrder onFailure ", e);
                AppToast.showShortText(PlaceOrderActivity.this, "对不起，创建订单失败！");
                DialogHelper.stopProgressDlg();
                ShopCartFragment.isUpdate = true;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            ShopCartFragment.isUpdate = true;
                            HttpUtil.getShopCartTotal(PlaceOrderActivity.this);
                            notifyUpdateProduct();

                            String paySn = JsonUtil.getOrderNoFromServer(json);
                            //调用支付
                            Bundle bundle = new Bundle();
                            bundle.putString("pay_sn", paySn);
                            bundle.putDouble("money", orderTotalPrice);
                            bundle.putInt("pay_src", Constant.PAY_FROM_SHOPCART);
                            IntentUtil.startActivity(PlaceOrderActivity.this, CheckoutCounterActivity.class, bundle);
                            PlaceOrderActivity.this.finish();
                            break;
                        case 2002:
                            AppToast.showShortText(PlaceOrderActivity.this, "对不起，暂不支持配送到该地区，请重新选择收货地址!");
                            break;
                        default:
                            AppToast.showShortText(PlaceOrderActivity.this, "对不起，创建订单失败！");
                            break;
                    }

                }
            }
        });

    }

    private void notifyUpdateProduct() {
        for (ShopCartItem cartItem : mListItems) {
            for (Product product : cartItem.products) {
                EventBus.getDefault().post(new ShopCartEvent(product.shopCardId));
            }
        }

    }

    /**
     * 截取回调回来的商品id进行匹配
     *
     * @return
     */
    private ArrayList<String> getGoodsId() {
        ArrayList<String> Ids = new ArrayList<String>();
        int end = 0;
        int start = 0;
        String id = goods_id_array + "_";
        if (!TextUtils.isEmpty(id)) {
            while ((end = id.indexOf("_", start)) > -1) {
                Ids.add(id.substring(start, end));
                start = end + 1;
            }
        }
        return Ids;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StatAgent.initAction(this, "", "2", "15", "", "", "back", "2", "");
    }
}
