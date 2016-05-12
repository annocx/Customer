package com.haier.cabinet.customer.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.SortGridAdapter;
import com.haier.cabinet.customer.base.BaseAppCompatActivity;
import com.haier.cabinet.customer.entity.BProduct;
import com.haier.cabinet.customer.event.ProductEvent;
import com.haier.cabinet.customer.event.ShopCartEvent;
import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.cabinet.customer.fragment.ShopCartFragment;
import com.haier.cabinet.customer.ui.MainUIActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.GsonUtils;
import com.haier.cabinet.customer.util.HttpUtil;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.UIHelper;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.widget.recyclerview.CustRecyclerView;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.haier.cabinet.customer.widget.recyclerview.HeaderSpanSizeLookup;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.view.BadgeView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 搜索结果排序类
 *
 * @author Created by jinbiao.wu on 2015/12/1.
 */
public class SortResultActivity extends BaseAppCompatActivity implements View.OnClickListener, View.OnTouchListener,
        Animation.AnimationListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String RESULT_NAME = "result_name";//分类和搜索需要传递一个关键字过来

    public static final String RESULT_TYPE = "result_type";//根据类型判断是分类还是搜索进来的

    public static final String RESULT_ID = "result_ID";//店铺和分类需要传一个id

    public static final String URL = null;

    public static final int RESULT_CLASSIFY = 1001;//分类

    public static final int RESULT_SEARCH = 1002;//搜索

    public static final int RESULT_SHOP = 1003;//店铺

    private String str_result;

    private int mId;

    private int int_type;
    private SortGridAdapter mAdapter;
    private ArrayList<BProduct> mProductList;

    private int count_num = 0;//购物车数量

    private static int mCurPageIndex = 1;
    private int order = 1;//正序1，倒序2
    private static int totalPage = 0;//返回数据总页数

    @Bind(R.id.tv_title)
    TextView tv_title;//分类进来的显示tv_title
    @Bind(R.id.et_title)
    TextView et_title;//搜索进来的显示et_title
    @Bind(R.id.tv_no_search)
    TextView tv_no_search;//未搜索到数据
    @Bind(R.id.iv_shop_cart)
    ImageView mShoppingCartImage;//购物车
    @Bind(R.id.cart_anim_icon)
    ImageView mAnimImageView;//购物车动画
    @Bind(R.id.linear_show)
    LinearLayout linear_show;//结果布局
    @Bind(R.id.linear_no_result)
    LinearLayout linear_no_result;//无结果布局
    @Bind(R.id.refreshable_view)
    CustRecyclerView recyclerView;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.rb_com)
    RadioButton rb_com;
    @Bind(R.id.rb_sales)
    RadioButton rb_sales;
    @Bind(R.id.rb_price)
    RadioButton rb_price;
    @Bind(R.id.rb_grade)
    RadioButton rb_grade;

    private Animation mAnimation;
    private Animation badgeViewAnimation;
    private GridLayoutManager manager;
    private static final int REQUEST_COUNT = 10;
    private HeaderAndFooterRecyclerViewAdapter mHeaderAndFooterRecyclerViewAdapter = null;
    private BadgeView badgeView;
    private String pageType = "5";

    private static final int GET_LIST_DATA = 1001;//请求数据
    private static final int UPDATE_LIST_DATA = 1002;//刷新数据
    private static final int NO_LIST_DATA = 1003;//无数据
    private static final int ERRO_LIST_DATA = 1004;//数据异常
    private static final int NO_MORE_LIST_DATA = 1005;//没有更多数据了
    public static final int ADD_TO_SHOPPING_CART = 1006;
    public static final int INCREASE_FROM_SHOPPING_CART = 1007;
    public static final int REDUCE_FROM_SHOPPING_CART = 1008;

    //是否完成清理
    private boolean isClean = false;
    protected boolean mIsStart = false;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_LIST_DATA:
                    String url = getRequestUrl(mIsStart);
                    requestOrderListData(url);
                    break;
                case UPDATE_LIST_DATA:
                    linear_no_result.setVisibility(View.GONE);
                    linear_show.setVisibility(View.VISIBLE);
                    String json = (String) msg.obj;
                    mProductList = GsonUtils.jsonToList(json, BProduct.class);
                    if (null == mProductList) {
                        return;
                    }

                    if (mIsStart) {
                        //清空数据
                        if (mAdapter.getItemCount() > 0) {
                            mAdapter.clear();
                        }
                        mAdapter.addAll(mProductList);
                    } else {
                        RecyclerViewStateUtils.setFooterViewState(SortResultActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.Normal, null);
                        mAdapter.addAll(mProductList);
                    }

                    if ((mCurPageIndex == totalPage) && mIsStart) {
                        RecyclerViewStateUtils.setFooterViewState2(SortResultActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
                        recyclerView.removeOnScrollListener(mOnScrollListener);
                    }

                    badgeView.setText(PushApplication.getInstance().getCartTotal());
                    mAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case NO_LIST_DATA:
                    linear_show.setVisibility(View.GONE);
                    linear_no_result.setVisibility(View.VISIBLE);
                    showNoResult();
                    mAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case NO_MORE_LIST_DATA:
                    mAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case ERRO_LIST_DATA:
                    AppToast.makeToast(SortResultActivity.this, "获取数据异常，请稍后再试！");
                    mIsStart = false;
                    break;
                case ADD_TO_SHOPPING_CART:
                    if (!PushApplication.getInstance().isLogin()) {
                        UIHelper.showLoginActivity(SortResultActivity.this);
                        return;
                    }
                    add2ShopCart(msg.arg1, (BProduct) msg.obj);
                    break;
                case INCREASE_FROM_SHOPPING_CART:
                    modifyShopCartData(msg.arg1, (BProduct) msg.obj, true);
                    break;
                case REDUCE_FROM_SHOPPING_CART:
                    modifyShopCartData(msg.arg1, (BProduct) msg.obj, false);
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sort_result;
    }

    public void initView() {
        int_type = getIntent().getExtras().getInt(RESULT_TYPE);
        str_result = getIntent().getExtras().getString(RESULT_NAME);

        mAnimation = AnimationUtils.loadAnimation(this, R.anim.cart_anim_new);
        badgeViewAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim);
        badgeView = new BadgeView(this);
        badgeView.setBadgeCount(count_num);
        badgeView.setBackground(15, Color.WHITE);
        badgeView.setTextColor(Color.RED);
        badgeView.setTextSize(12);
        badgeView.setBadgeMargin(0, 10, 10, 0);
        badgeView.setTargetView(mShoppingCartImage);

        switchPage();
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(this);

        mAdapter = new SortGridAdapter(this, mHandler, pageType);
        mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mAdapter);
        recyclerView.setAdapter(mHeaderAndFooterRecyclerViewAdapter);

        manager = new GridLayoutManager(this, 2);
        manager.setSpanSizeLookup(new HeaderSpanSizeLookup((HeaderAndFooterRecyclerViewAdapter) recyclerView.getAdapter(), manager.getSpanCount()));

        recyclerView.setLayoutManager(manager);
        recyclerView.setOnPauseListenerParams(ImageLoader.getInstance(), false, true);
        mOnScrollListener.setSwipeRefreshLayout(swipeRefreshLayout);
        recyclerView.addOnScrollListener(mOnScrollListener);
        mAnimation.setAnimationListener(this);
    }

    private void switchPage() {
        switch (int_type) {
            case RESULT_CLASSIFY:
                pageType = "6";
                StatAgent.initAction(this, "", "1", pageType, "", "", "", "1", "");
                mId = getIntent().getExtras().getInt(RESULT_ID);
                tv_title.setVisibility(View.VISIBLE);
                tv_title.setText(str_result);
                et_title.setVisibility(View.GONE);
                break;
            case RESULT_SHOP:
                mId = getIntent().getExtras().getInt(RESULT_ID);
                tv_title.setVisibility(View.VISIBLE);
                tv_title.setText(str_result);
                break;
            case RESULT_SEARCH:
                pageType = "5";
                StatAgent.initAction(this, "", "1", pageType, "", "", "", "1", "");
                et_title.setVisibility(View.VISIBLE);
                et_title.setText(str_result);
                break;
        }
    }

    public void initData() {
        if (mAdapter.getItemCount() > 0) {
            mAdapter.clear();
        }

        swipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(this, 24));
        swipeRefreshLayout.setRefreshing(true);

        mIsStart = true;
        mHandler.sendEmptyMessage(GET_LIST_DATA);
    }

    @Override
    protected void onResume() {
        super.onResume();

        badgeView.setText(PushApplication.getInstance().getCartTotal());
    }

    public void onEventMainThread(UserChangedEvent event) {
        onRefresh();
    }

    private View createView() {
        View view = this.getLayoutInflater().inflate(R.layout.layout_speciality_result, null);
//        mGridview.setHorizontalSpacing(20);
//        mGridview.setVerticalSpacing(20);
//        mGridview.setPadding(20,0,20,0);
        view.findViewById(R.id.head_img).setVisibility(View.GONE);
        return view;
    }

    public void onEventMainThread(ProductEvent event) {
        mAdapter.updateProduct(event.getId(), event.getCount());
    }

    public void onEventMainThread(ShopCartEvent event) {
        if (event.getCount() > 0) {
            mAdapter.updateProduct(event.getId(), event.getCount(), event.getShopCartId());
        } else {
            mAdapter.updateProduct(event.getShopCartId());
        }
    }

    private RecyclerOnScrollListener mOnScrollListener = new RecyclerOnScrollListener() {

        public void onBottom() {

            LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(recyclerView);
            if (state == LoadingFooter.State.Loading) {
                return;
            }

            if (mCurPageIndex < totalPage) {
                // loading more
                RecyclerViewStateUtils.setFooterViewState(SortResultActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
                mIsStart = false;
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
            } else {
                //the end
                RecyclerViewStateUtils.setFooterViewState(SortResultActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
            }
        }
    };

    private boolean isCom = false, isSales = false, isPrice = false, isGrade = false;//判断是否点击过
    private int checkType = 1;//看接口需要传递什么参数

    @Override
    @OnClick({R.id.back_img,R.id.et_title,R.id.iv_shop_cart, R.id.rb_com, R.id.rb_sales,
            R.id.rb_price, R.id.rb_grade})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
            case R.id.et_title:
                finish();
                break;
            case R.id.iv_shop_cart:
                StatAgent.initAction(this, "", "2", pageType, "", "", "shopping cart", "1", "");

                Bundle bundle = new Bundle();
                bundle.putInt(MainUIActivity.ACTION_CURRETNTAB, Constant.SHOPC_CART_FRAGMENT_INDEX);
                IntentUtil.startActivity(this, MainUIActivity.class, bundle);
                break;
            case R.id.rb_com://综合
                checkType = 1;
                if (rb_com.isChecked()) {
                    if (isCom) {//正序
                        isCom = false;
                        order = 1;
                        rb_com.setBackgroundResource(R.drawable.bg_search_radio_press);
                    } else {//倒序
                        isCom = true;
                        order = 2;
                        rb_com.setBackgroundResource(R.drawable.bg_search_radio_normal);
                    }
                    mCurPageIndex = 1;
                    initData();
                }
                setSelector(rb_grade, rb_price, rb_sales, isSales, isPrice, isGrade);

                StatAgent.initAction(this, "", "2", pageType, "", "", rb_com.getText().toString(), "1", "");
                break;
            case R.id.rb_sales://销量
                checkType = 2;
                if (rb_sales.isChecked()) {
                    if (isSales) {
                        isSales = false;
                        order = 2;
                        rb_sales.setBackgroundResource(R.drawable.bg_search_radio_normal);
                    } else {
                        isSales = true;
                        order = 1;
                        rb_sales.setBackgroundResource(R.drawable.bg_search_radio_press);
                    }
                    mCurPageIndex = 1;
                    initData();
                }
                setSelector(rb_grade, rb_price, rb_com, isCom, isPrice, isGrade);

                StatAgent.initAction(this, "", "2", pageType, "", "", rb_sales.getText().toString(), "1", "");
                break;
            case R.id.rb_price://价格
                checkType = 3;
                if (rb_price.isChecked()) {
                    if (isPrice) {//倒序
                        isPrice = false;
                        order = 2;
                        rb_price.setBackgroundResource(R.drawable.bg_search_radio_normal);
                    } else {//正序
                        isPrice = true;
                        order = 1;
                        rb_price.setBackgroundResource(R.drawable.bg_search_radio_press);
                    }
                    mCurPageIndex = 1;
                    initData();
                }
                setSelector(rb_grade, rb_com, rb_sales, isCom, isSales, isGrade);

                StatAgent.initAction(this, "", "2", pageType, "", "", rb_price.getText().toString(), "1", "");
                break;
            case R.id.rb_grade://评分
                checkType = 4;
                if (rb_grade.isChecked()) {
                    if (isGrade) {
                        isGrade = false;
                        order = 2;
                        rb_grade.setBackgroundResource(R.drawable.bg_search_radio_normal);
                    } else {
                        isGrade = true;
                        order = 1;
                        rb_grade.setBackgroundResource(R.drawable.bg_search_radio_press);
                    }
                    mCurPageIndex = 1;
                    initData();
                }
                setSelector(rb_com, rb_price, rb_sales, isCom, isPrice, isSales);

                StatAgent.initAction(this, "", "2", pageType, "", "", rb_grade.getText().toString(), "1", "");
                break;
        }
    }

    /**
     * 状态还原
     *
     * @param rb1
     * @param rb2
     * @param rb3
     * @param isboolean1
     * @param isboolean2
     * @param isboolean3
     */
    private void setSelector(RadioButton rb1, RadioButton rb2, RadioButton rb3, boolean isboolean1, boolean isboolean2, boolean isboolean3) {
        rb1.setBackgroundResource(R.drawable.bg_search_radio_selector);
        rb2.setBackgroundResource(R.drawable.bg_search_radio_selector);
        rb3.setBackgroundResource(R.drawable.bg_search_radio_selector);
        isboolean1 = false;
        isboolean2 = false;
        isboolean3 = false;
    }

    @Override
    public void notifyShopCartNumChanged() {
        super.notifyShopCartNumChanged();

    }

    /**
     * 未搜索到结果
     */
    private void showNoResult() {
        switch (int_type) {
            case RESULT_CLASSIFY:
                tv_no_search.setText(String.format(getString(R.string.no_search_show),
                        str_result.toString()));
                break;
            case RESULT_SHOP:
                tv_no_search.setText(String.format(getString(R.string.no_search_show),
                        "店铺"));
                break;
            case RESULT_SEARCH:
                if (str_result.length() > 6) {
                    tv_no_search.setText(String.format(getString(R.string.no_search_show),
                            str_result.substring(0, 6) + "..."));
                } else {
                    tv_no_search.setText(String.format(getString(R.string.no_search_show),
                            str_result.toString()));
                }
                break;
        }
    }

    /**
     * 请求url
     *
     * @param isStart
     * @return
     */
    private String getRequestUrl(boolean isStart) {
        String url = null;
        if ((mAdapter.getItemCount() == 0) || isStart) {
            mCurPageIndex = 1;
        } else {
            ++mCurPageIndex;
        }
        switch (int_type) {
            case RESULT_CLASSIFY:
                url = Constant.URL_SHOP_LIST + "&gc_id=" + mId;
                break;
            case RESULT_SHOP:
                url = Constant.URL_SHOP_LIST + "&store_id=" + mId;
                break;
            case RESULT_SEARCH:
                url = Constant.URL_SHOP_LIST + "&keyword=" + str_result;
                break;
        }
        url = url + "&city=" + PushApplication.getInstance().getProperty("user.city");
        return url;
    }

    /**
     * 请求方法
     *
     * @param url
     */
    private void requestOrderListData(String url) {
        Log.d("wjb", "url:" + url);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("key", "" + checkType);
        params.add("order", "" + order);
        params.add("page", "" + mCurPageIndex);
        params.add("pagesize", "10");
        params.add("member_id", PushApplication.getInstance().getUserId());
//        Log.d("wjb", "url:" + url + "&key=" + checkType + "&order=" + order +
//                "&page=" + mCurPageIndex + "&pagesize=" + mCurPageIndex + "&member_id=" + PushApplication.getInstance().getUserId());
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                swipeRefreshLayout.setRefreshing(false);
                mHandler.sendEmptyMessage(ERRO_LIST_DATA);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                swipeRefreshLayout.setRefreshing(false);
                String json = new String(response);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            String json_list = null;
                            try {
                                JSONObject jsonObject = new JSONObject(JsonUtil.getResultFromJson(json));
                                totalPage = jsonObject.getInt("page_count");
                                json_list = jsonObject.getString("goods_list");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mHandler.obtainMessage(UPDATE_LIST_DATA, json_list).sendToTarget();
                            break;
                        case 1002:
                            if (mAdapter.getItemCount() > 0) {
                                mHandler.obtainMessage(NO_MORE_LIST_DATA).sendToTarget();
                            } else {
                                mHandler.obtainMessage(NO_LIST_DATA).sendToTarget();
                            }
                            break;
                        case 2001:
                            mHandler.sendEmptyMessage(ERRO_LIST_DATA);
                            break;
                        default:
                            break;
                    }

                } else {
                    mHandler.sendEmptyMessage(NO_LIST_DATA);
                }
            }
        });
    }

    /**
     * 添加购物车
     */
    BProduct mProduct;

    private void add2ShopCart(final int position, final BProduct product) {
        mProduct = product;
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("goods_id", product.goods_id);
        params.put("quantity", product.cart_num);
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_SHOPPING_CART_ADD, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        HttpUtil.getShopCartTotal(SortResultActivity.this);
                        ShopCartFragment.isUpdate = true;
                        // 添加购物车动画
                        product.cart_id = JsonUtil.getShopCartId(json);
                        mAnimImageView.setVisibility(View.VISIBLE);
                        mAnimImageView.startAnimation(mAnimation);
                        mAdapter.updateList(position, product);

                        EventBus.getDefault().post(new ShopCartEvent(product.goods_id, product.cart_id, product.cart_num));
                    } else {
                        product.cart_num--;
                        AppToast.showShortText(SortResultActivity.this, "添加购物车失败");
                    }

                }
            }

        });

    }

    /**
     * 修改购物车数量
     *
     * @param product
     * @param isAdded
     */
    private void modifyShopCartData(final int position, final BProduct product, final boolean isAdded) {
        mProduct = product;
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("cart_id", product.cart_id);
        params.put("quantity", product.cart_num);
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_SHOPPING_CART_MODIFY, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        ShopCartFragment.isUpdate = true;
                        if (isAdded) {
                            // 添加购物车动画
                            mAnimImageView.setVisibility(View.VISIBLE);
                            mAnimImageView.startAnimation(mAnimation);
                        } else {
                            if (product.cart_num == 0) {
                                int count = JsonUtil.getShopCartTotal(json);
                                PushApplication.getInstance().setCartTotal(count);
                                mAnimImageView.setVisibility(View.INVISIBLE);
                                badgeView.setText(PushApplication.getInstance().getCartTotal());
                            }
                        }
                        mAdapter.updateList(position, product);
                        EventBus.getDefault().post(new ProductEvent(product.goods_id, product.cart_num));
                    } else {
                        product.cart_num--;
                        AppToast.showShortText(SortResultActivity.this, "添加购物车失败");
                    }

                }
            }

        });

    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mAnimImageView.setVisibility(View.INVISIBLE);

        badgeView.setText(PushApplication.getInstance().getCartTotal());
        badgeView.startAnimation(badgeViewAnimation);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onRefresh() {
        if (!mIsStart) {//防止多次下拉
            mIsStart = true;
            linear_no_result.setVisibility(View.GONE);
            mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            /** 滑动到顶部和底部做处理 **/
            mHandler.sendMessageDelayed(mHandler.obtainMessage(-9983761, v), 5);
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        switch (int_type) {
            case RESULT_CLASSIFY:
                StatAgent.initAction(this, "", "2", "6", "", "", "back", "2", "");
                break;
            case RESULT_SEARCH:
                StatAgent.initAction(this, "", "2", "5", "", "", "back", "2", "");
                break;
        }
    }
}
