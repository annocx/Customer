package com.haier.cabinet.customer.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.PlaceOrderActivity;
import com.haier.cabinet.customer.activity.adapter.ShopCartListAdapter;
import com.haier.cabinet.customer.base.BaseFragment;
import com.haier.cabinet.customer.base.BaseListFragment;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.Shop;
import com.haier.cabinet.customer.entity.ShopCartItem;
import com.haier.cabinet.customer.event.ProductEvent;
import com.haier.cabinet.customer.event.ShopCartEvent;
import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.cabinet.customer.ui.MainUIActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.HttpUtil;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.widget.recyclerview.CustRecyclerView;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ShopCartFragment extends BaseListFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ShopCartFragment";
    private TextView mEditText;
    private TextView mCompleteText;
    private CheckBox selectAllCbx;
    private TextView mTotalPriceText;
    private Button mOrderBtn;
    private LinearLayout mllPrice;

    private ShopCartListAdapter mListAdapter;
    private List<ShopCartItem> mSelectedList;
    private List<ShopCartItem> mDeleteList;
    private boolean mIsStart = false;
    private int mCurPageIndex = 1;
    //编辑商品标签 0为正常状态，1为编辑状态
    private int mEdit = 0;

    public static boolean isUpdate = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_cart, container, false);
        return view;
    }

    public void initView(View view) {
        super.initView(view);
        mTitleText.setText(R.string.tab_shop_cart);
        mEditText = (TextView) view.findViewById(R.id.title_text_edit);
        mCompleteText = (TextView) view.findViewById(R.id.title_text_complete);
        mTotalPriceText = (TextView) view.findViewById(R.id.total_price_text);
        selectAllCbx = (CheckBox) view.findViewById(R.id.all_checkbox);
        mOrderBtn = (Button) view.findViewById(R.id.orderBtn);
        mllPrice = (LinearLayout) view.findViewById(R.id.ll_price);

        RecyclerOnScrollListener recyclerOnScrollListener = new RecyclerOnScrollListener();
        recyclerOnScrollListener.setSwipeRefreshLayout(mSwipeRefreshLayout);
        mRecyclerView.addOnScrollListener(recyclerOnScrollListener);

        mRecyclerView.setEmptyView(view.findViewById(R.id.empty_view));
        view.findViewById(R.id.look_button).setOnClickListener(this);
        //增加购物车编辑功能
        mEditText.setVisibility(View.VISIBLE);
        mEditText.setOnClickListener(this);
        mCompleteText.setOnClickListener(this);
        selectAllCbx.setOnClickListener(this);
        mOrderBtn.setOnClickListener(this);

        isUpdate = true;
    }

    @Override
    protected void initLayoutManager() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        StatAgent.initAction(getActivity(), "", "1", "12", "", "", "", "1", "");
        mSelectedList = new ArrayList<>();
        mDeleteList = new ArrayList<>();
        mListAdapter = new ShopCartListAdapter(getActivity(), mHandler);
        mRecyclerView.setAdapter(mListAdapter);
        if (!PushApplication.getInstance().isLogin()) {
            mHandler.sendEmptyMessage(NO_LIST_DATA);
        }
    }

    private boolean isRequestInProcess = false;

    @Override
    public void onResume() {
        super.onResume();
        if (PushApplication.getInstance().isLogin()) {
            if (isUpdate && !isRequestInProcess) {
                onRefresh();
            }
        }
    }

//    //切换到其他fragment时变成编辑状态
//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//        if (hidden) {
//            EditShopCart();
//        }
//    }

    public void EditShopCart() {
        mEdit = 0;
        if (mEditText != null) {
            mEditText.setVisibility(View.VISIBLE);
            mllPrice.setVisibility(View.VISIBLE);
            mCompleteText.setVisibility(View.INVISIBLE);
            mOrderBtn.setText("去下单");
            refreshView();
        }
    }

    @Override
    public void onRefresh() {
        if (!mIsStart) {//防止多次下拉
            mIsStart = true;
            mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
        }
    }

    public void refreshView() {
        if (mEdit == 0) {
            double total = mListAdapter.calculatingTotalPrice();
            mTotalPriceText.setText("￥" + total);
        }
        if (mEdit == 1) {
            mOrderBtn.setText("删除（" + mListAdapter.getShopCartDelete().size() + "）");
        }
        selectAllCbx.setChecked(mListAdapter.isSelectedAll());
        mSelectedList = mListAdapter.getSelectedProducts();
        if (PushApplication.getInstance().getCartTotalNum() != mListAdapter.getShopCartNumber()) {
            PushApplication.getInstance().setCartTotal(mListAdapter.getShopCartNumber());
        }

        mActivity.sendBroadcast(new Intent(Constant.INTENT_ACTION_SHOP_CART_TOTAL_CHANGE));
    }

    public void onEventMainThread(UserChangedEvent event) {
        if (PushApplication.getInstance().isLogin()) {
            onRefresh();
        } else {
            mListAdapter.clear();
            refreshView();
        }
    }


    private static final int GET_LIST_DATA = 1001;
    private static final int UPDATE_LIST = 1002;
    private static final int NO_LIST_DATA = 1003;
    private static final int GET_LIST_DATA_FAILURE = 1005;
    private static final int NO_MORE_LIST_DATA = 1006;
    public static final int REFRESH_VIEW = 1007;
    public static final int DELETE_PRODUCT = 1008;
    public static final int DELETE_PRODUCT_SUCCESS = 1009;
    public static final int EDIT_PRODUCT = 1010;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case GET_LIST_DATA:
                    String url = getRequestUrl(mIsStart);
                    requestShoppingCardData(url);
                    break;
                case UPDATE_LIST:
                    String json = (String) msg.obj;
                    List<ShopCartItem> data = getProductListByJosn(json);

                    Log.d(TAG, "data.size() = " + data.size());
                    if (null == data) {
                        return;
                    }

                    if (mIsStart) {
                        mListAdapter.setDataList(data);
                    } else {
                        mListAdapter.addAll(data);
                    }

                    refreshView();
                    mIsStart = false;
                    isUpdate = false;
                    break;
                case GET_LIST_DATA_FAILURE:
                case NO_LIST_DATA:
                case NO_MORE_LIST_DATA:

                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case REFRESH_VIEW:
                    //更新总价
                    refreshView();
                    break;
                case DELETE_PRODUCT:
                    Bundle bundle = msg.getData();
                    int shopCardId = bundle.getInt("shopCardId");
                    int position = bundle.getInt("position");
                    int index = bundle.getInt("index");
                    deleteShopCartData(shopCardId, index, position);
                    break;
                case DELETE_PRODUCT_SUCCESS:
                    mListAdapter.deleteProduct(msg.arg1, msg.arg2, mRecyclerView);
                    refreshView();
                    HttpUtil.getShopCartTotal(getActivity());
                    break;
                case EDIT_PRODUCT:
//                    Product product = (Product) msg.obj;
//                    modifyShopCartData(product);
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //编辑商品
            case R.id.title_text_edit:
                if (mListAdapter.getItemCount() == 0) {
                    AppToast.showShortText(getActivity(), "您的购物车还没有宝贝哦！");
                    return;
                }
                mEdit = 1;
                mEditText.setVisibility(View.INVISIBLE);
                mllPrice.setVisibility(View.INVISIBLE);
                mCompleteText.setVisibility(View.VISIBLE);
                mListAdapter.setSelectedAll(false);
                refreshView();
                break;
            //编辑完成
            case R.id.title_text_complete:
                EditShopCart();
                break;
            case R.id.all_checkbox:
                StatAgent.initAction(getActivity(), "", "2", "12", "", "", selectAllCbx.getText().toString(), "1", "");
                mListAdapter.setSelectedAll(selectAllCbx.isChecked());
                refreshView();
                mListAdapter.notifyDataSetChanged();
                break;
            case R.id.orderBtn:
                if (mEdit == 0) {
                    if (mSelectedList.size() == 0) {
                        AppToast.showShortText(getActivity(), R.string.no_choose_product);
                    } else {
                        StatAgent.initAction(getActivity(), "", "2", "12", "", "", mOrderBtn.getText().toString(), "1", "");

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("order_list", (Serializable) mSelectedList);
                        IntentUtil.startActivity(getActivity(), PlaceOrderActivity.class, bundle);
                    }
                }
                if (mEdit == 1) {
                    if (mListAdapter.getShopCartDelete().size() == 0) {
                        AppToast.showShortText(getActivity(), "您还没有选择要删除的宝贝哦！");
                    } else {
                        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                .setMessage("确认要删除选择的商品吗?")
                                .setPositiveButton("确定",
                                        new AlertDialog.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                                for (int location = 0; location < mListAdapter.getShopCartDelete().size(); location++) {
                                                    int shopCardId = (int) mListAdapter.getShopCartDelete().get(location);
                                                    deleteShopCartData(shopCardId, 0, 0);
                                                }
                                            }
                                        })
                                .setNegativeButton("取消",
                                        new AlertDialog.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                            }
                                        }).create();
                        //refreshView();
                        // 显示对话框
                        dialog.show();
                    }
                }

                break;
            case R.id.look_button:
                Bundle bundle = new Bundle();
                bundle.putInt(MainUIActivity.ACTION_CURRETNTAB, Constant.HOME_FRAGMENT_INDEX);
                IntentUtil.startActivity(getActivity(), MainUIActivity.class, bundle);
                break;
            default:
                break;
        }
    }

    private void requestShoppingCardData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (mCurPageIndex == 1) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                mHandler.sendEmptyMessage(GET_LIST_DATA_FAILURE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
//				Log.d(TAG, "onSuccess " + json);

                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            if (JsonUtil.isHaveData(json)) {
                                mHandler.obtainMessage(UPDATE_LIST, json).sendToTarget();
                            } else {
                                if (mListAdapter.getItemCount() == 0) {
                                    mHandler.obtainMessage(NO_LIST_DATA).sendToTarget();
                                } else {
                                    mHandler.obtainMessage(NO_MORE_LIST_DATA).sendToTarget();
                                }

                            }
                            break;
                        case 1002:
                            if (mListAdapter.getItemCount() == 0) {
                                mHandler.obtainMessage(NO_LIST_DATA).sendToTarget();
                            } else {
                                mHandler.obtainMessage(NO_MORE_LIST_DATA).sendToTarget();
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

    private List<ShopCartItem> getProductListByJosn(String json) {
        List<ShopCartItem> list = new ArrayList<ShopCartItem>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result") || TextUtils.isEmpty(jsonObject.getJSONObject("result").toString())) {
                return null;
            }


            JSONObject cartsObject = jsonObject.getJSONObject("result");
            //totalRecord = cartsObject.getInt("total");
            JSONArray itemArray = cartsObject.getJSONArray("cart_list");
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject cartObject = itemArray.getJSONObject(i);

                ShopCartItem cartItem = new ShopCartItem();

                //店铺信息
                /*JSONObject shopObject = cartObject.getJSONObject("minfo");*/
                Shop shop = new Shop();
                shop.id = cartObject.getInt("store_id");
                shop.name = cartObject.getString("store_name");
                shop.fee = 10;
                shop.free_delivery_pirce = 1.0;
                cartItem.shop = shop;

                //产品列表
                JSONArray productArray = cartObject.getJSONArray("list");
                for (int j = 0; j < productArray.length(); j++) {
                    JSONObject proObject = productArray.getJSONObject(j);

                    Product product = new Product();
                    product.shopCardId = proObject.getInt("cart_id");
                    product.id = proObject.getInt("goods_id");
                    product.title = proObject.getString("goods_name");
                    product.discountPrice = proObject.getDouble("goods_price");
                    product.retailPrice = proObject.getDouble("goods_price");
                    product.thumbUrl = proObject.getString("goods_image");
                    product.count = proObject.getInt("goods_num");
                    product.shopId = cartObject.getInt("store_id");
                    product.goods_storage = proObject.optInt("goods_storage");//使用opt，如果接口没有这个参数不会报错
//                    product.boutique = proObject.getString("jingpin");
                    product.pay_state = proObject.getInt("pay_state");
                    if (product.pay_state == 0) {//为0时是已售完或下架
                        product.isChecked = false;//新增需求
                    } else {
                        product.isChecked = true;//新增需求
                    }
                    cartItem.products.add(product);
                }

                list.add(cartItem);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }

        return list;
    }


    private void deleteShopCartData(final int shopCardId, final int index, final int position) {
        Log.d(TAG, "deleteShopCartData -- shopCardId " + shopCardId);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("cart_id", shopCardId);
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_SHOPPING_CART_DELETE, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
//				Log.d(TAG, "json " + json);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            mHandler.obtainMessage(DELETE_PRODUCT_SUCCESS, index, position).sendToTarget();
                            EventBus.getDefault().post(new ShopCartEvent(shopCardId));
                            break;
                        case 2001:
                            break;

                        default:
                            break;
                    }
                }
            }
        });
    }

    private String getRequestUrl(boolean isStart) {
        if (mListAdapter == null) {
            mCurPageIndex = 1;
        } else {
            if ((mListAdapter.getItemCount() == 0) || isStart) {
                mCurPageIndex = 1;
            } else {
                ++mCurPageIndex;
            }
        }

        String url = Constant.URL_SHOPPING_CART_LIST
                + "&member_id=" + PushApplication.getInstance().getUserId()
                + "&page=" + mCurPageIndex;
        Log.d("wjb", "url:" + url);
        return url;
    }
}
