package com.haier.cabinet.customer.activity;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.ShopProductListAdapter;
import com.haier.cabinet.customer.base.BaseAppCompatActivity;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.event.ProductEvent;
import com.haier.cabinet.customer.event.ShopCartEvent;
import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.cabinet.customer.ui.MainUIActivity;
import com.haier.cabinet.customer.util.Constant;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class SpecialityDetailActivity extends BaseAppCompatActivity implements View.OnClickListener, View.OnTouchListener,
        Animation.AnimationListener, SwipeRefreshLayout.OnRefreshListener,
        AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "SpecialityDetailActivity";
    @Bind(R.id.shopping_cart_img)
    ImageView shoppingCart;
    @Bind(R.id.cart_anim_icon)
    ImageView mAnimImageView;
    @Bind(R.id.appbar_layout)
    AppBarLayout appBarLayout;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Bind(R.id.banner_image)
    ImageView headImg;

    @Bind(R.id.speciality_recyclerView)
    CustRecyclerView recyclerView;

    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private BadgeView badgeView;

    private Animation badgeAnimation;

    private Animation mAnimation;

    private int badgeCount = 0;

    private boolean mIsStart = false;

    private static int totalRecord = 0;

    private ArrayList<Product> data;

    private int mCurPageIndex = 1;

    private int producerID = -1;

    private int width, height;

    private String title;

    private ShopProductListAdapter mListAdapter;

    private GridLayoutManager manager;

    private static final int REQUEST_COUNT = 10;

    private HeaderAndFooterRecyclerViewAdapter mHeaderAndFooterRecyclerViewAdapter = null;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_speciality_detail;
    }

    public void initView() {
        StatAgent.initAction(this, "", "1", "8", "", "", "", "1", "");

        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        producerID = getIntent().getIntExtra("id", -1);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        appBarLayout.addOnOffsetChangedListener(this);

        badgeView = new BadgeView(this);
        badgeView.setBadgeCount(badgeCount);
        badgeView.setBackground(10, Color.WHITE);
        badgeView.setBadgeMargin(0, 10, 10, 0);
        badgeView.setTextColor(Color.RED);
        badgeView.setTargetView(shoppingCart);

        badgeAnimation = AnimationUtils.loadAnimation(SpecialityDetailActivity.this, R.anim.scale_anim);

        mAnimation = AnimationUtils.loadAnimation(this, R.anim.cart_anim_new);
        mAnimation.setAnimationListener(this);

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(this, 24));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);

        View emptyView = findViewById(R.id.empty_view);
        emptyView.setOnTouchListener(this);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);
        mListAdapter = new ShopProductListAdapter(this, mHandler);
        mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mListAdapter);
        recyclerView.setAdapter(mHeaderAndFooterRecyclerViewAdapter);

        manager = new GridLayoutManager(this, 2);
        manager.setSpanSizeLookup(new HeaderSpanSizeLookup((HeaderAndFooterRecyclerViewAdapter) recyclerView.getAdapter(), manager.getSpanCount()));
        manager.setSmoothScrollbarEnabled(true);

        recyclerView.setLayoutManager(manager);

        mOnScrollListener.setSwipeRefreshLayout(swipeRefreshLayout);
        recyclerView.addOnScrollListener(mOnScrollListener);
        recyclerView.setOnPauseListenerParams(ImageLoader.getInstance(), false, true);
        width = Util.getScreenWidth(this);
        height = (new Double(width * 0.49)).intValue();

        //根据屏幕宽度动态设置每个图片的宽高
        CollapsingToolbarLayout.LayoutParams param = new CollapsingToolbarLayout.LayoutParams(width, height);
        headImg.setLayoutParams(param);

        mIsStart = true;
        mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyShopCartNumChanged();
    }

    public void onEventMainThread(ProductEvent event) {
        mListAdapter.updateProduct(event.getId(), event.getCount());
    }

    public void onEventMainThread(UserChangedEvent event) {
        onRefresh();
    }

    public void onEventMainThread(ShopCartEvent event) {

        if (event.getCount() > 0) {
            mListAdapter.updateProduct(event.getId(), event.getCount(), event.getShopCartId());
        } else {
            mListAdapter.updateProduct(event.getShopCartId());
        }
    }

    @Override
    @OnClick({R.id.shopping_cart_img})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shopping_cart_img:
                StatAgent.initAction(this, "", "2", "8", "", "", "shopping cart", "1", "");

                Bundle bundle = new Bundle();
                bundle.putInt(MainUIActivity.ACTION_CURRETNTAB, Constant.SHOPC_CART_FRAGMENT_INDEX);
                IntentUtil.startActivity(this, MainUIActivity.class, bundle);
                break;

            default:
                break;
        }
    }

    public static final int ADD_TO_SHOPPING_CART = 1009;
    public static final int PLUS_BADGE_ANIMATION = 1010;
    public static final int MINUS_BADGE_ANIMATION = 1011;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.GET_LIST_DATA:
                    String url = getRequestUrl(mIsStart);
                    requestOrderListData(url);
                    break;
                case Constant.UPDATE_LIST:
                    String json = (String) msg.obj;
                    data = getProductListByJosn(json);

                    if (null == data) {
                        return;
                    }

                    if (mIsStart) {
                        mListAdapter.setDataList(data);
                    } else {
                        RecyclerViewStateUtils.setFooterViewState(SpecialityDetailActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.Normal, null);
                        mListAdapter.addAll(data);
                    }

                    if ((mCurPageIndex == totalRecord) && mIsStart) {
                        RecyclerViewStateUtils.setFooterViewState2(SpecialityDetailActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
                        recyclerView.removeOnScrollListener(mOnScrollListener);
                    }

                    mListAdapter.notifyDataSetChanged();

                    mIsStart = false;
                    break;
                case Constant.GET_LIST_DATA_FAILURE:
                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case Constant.NO_MORE_LIST_DATA:
                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case Constant.NO_LIST_DATA:
                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case ADD_TO_SHOPPING_CART:
                    if (!PushApplication.getInstance().isLogin()) {
                        UIHelper.showLoginActivity(SpecialityDetailActivity.this);
                        return;
                    }
                    add2ShopCart(msg.arg1, (Product) msg.obj);
                    break;
                case PLUS_BADGE_ANIMATION:
                    modifyShopCartData(msg.arg1, (Product) msg.obj, true);
                    break;
                case MINUS_BADGE_ANIMATION:
                    modifyShopCartData(msg.arg1, (Product) msg.obj, false);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mAnimImageView.setVisibility(View.INVISIBLE);
        notifyShopCartNumChanged();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mListAdapter.getItemCount() == 0) {
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
            }
        }
        return false;
    }

    private RecyclerOnScrollListener mOnScrollListener = new RecyclerOnScrollListener() {

        public void onBottom() {

            LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(recyclerView);
            if (state == LoadingFooter.State.Loading) {
                return;
            }

            if (mCurPageIndex < totalRecord) {
                // loading more
                RecyclerViewStateUtils.setFooterViewState(SpecialityDetailActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
                mIsStart = false;
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
            } else {
                //the end
                RecyclerViewStateUtils.setFooterViewState(SpecialityDetailActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
            }
        }
    };

    @Override
    public void notifyShopCartNumChanged() {
        super.notifyShopCartNumChanged();

        badgeView.setText(PushApplication.getInstance().getCartTotal());
        badgeView.startAnimation(badgeAnimation);
    }

    private ArrayList<Product> getProductListByJosn(String json) {
        ArrayList<Product> list = new ArrayList<Product>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result")) {
                return null;
            }
            JSONObject resultObject = jsonObject.getJSONObject("result");

            totalRecord = resultObject.getInt("page_count");
            /*JSONObject shopObject = resultObject.getJSONObject("store_info");*/
            String producerImg = resultObject.getString("activity_image");
            title = resultObject.getString("title");
            mTitleText.setText(title);

            ImageLoader.getInstance().displayImage(producerImg, headImg, PushApplication.getInstance().getDefaultOptions2());

            JSONArray productArray = resultObject.getJSONArray("list");

            for (int i = 0; i < productArray.length(); i++) {
                JSONObject proObject = productArray.getJSONObject(i);

                Product product = new Product();
                product.id = proObject.getInt("goods_id");
                product.title = proObject.getString("goods_name");
                product.discountPrice = proObject.getDouble("goods_price");
                product.retailPrice = proObject.getDouble("goods_marketprice");
                //product.serviceArea = shopObject.getString("service_range");
                product.spec = proObject.getString("goods_guige");
                product.goods_storage = proObject.getInt("goods_storage");
                //product.madein = proObject.getString("origin");
                product.thumbUrl = proObject.getString("goods_image");
                //product.shopId = shop.id;
                //product.shopName = shop.name;
                product.shopLogo = producerImg;
                product.count = proObject.getInt("cart_num");
                product.shopCardId = proObject.getInt("cart_id");
//                product.boutique = proObject.getString("jingpin");
                list.add(product);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void add2ShopCart(final int position, final Product product) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("goods_id", product.id);
        params.put("quantity", product.count);
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_SHOPPING_CART_ADD, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String json = new String(response);
//                Log.d(TAG, "json " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        HttpUtil.getShopCartTotal(SpecialityDetailActivity.this);
                        // 添加购物车动画
                        //doAnim(mClickPosition,mDrawable,mStartLocation);

                        product.shopCardId = JsonUtil.getShopCartId(json);
                        mAnimImageView.setVisibility(View.VISIBLE);
                        mAnimImageView.startAnimation(mAnimation);
                        mListAdapter.updateList(position, product);

                        EventBus.getDefault().post(new ShopCartEvent(product.id, product.shopCardId, product.count));
                    } else {
                        product.count--;
                        AppToast.showShortText(SpecialityDetailActivity.this, "商品已下架或不存在");
                    }
                }
            }
        });
    }

    private void modifyShopCartData(final int position, final Product product, final boolean isAdded) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("cart_id", product.shopCardId);
        params.put("quantity", product.count);
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_SHOPPING_CART_MODIFY, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String json = new String(response);
//                Log.d(TAG, "json " + json);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            if (isAdded) {
                                // 添加购物车动画
                                mAnimImageView.setVisibility(View.VISIBLE);
                                mAnimImageView.startAnimation(mAnimation);
                            } else {
                                if (product.count == 0) {
                                    int count = JsonUtil.getShopCartTotal(json);
                                    PushApplication.getInstance().setCartTotal(count);
                                    notifyShopCartNumChanged();
                                }
                            }

                            mListAdapter.updateList(position, product);

                            EventBus.getDefault().post(new ProductEvent(product.id, product.count));
                            break;
                        case 2001:
                            product.count--;
                            AppToast.showShortText(SpecialityDetailActivity.this, "修改购物车失败");
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void requestOrderListData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA_FAILURE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                swipeRefreshLayout.setRefreshing(false);
                String json = new String(response);

                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            if (JsonUtil.isHaveData(json)) {
                                mHandler.obtainMessage(Constant.UPDATE_LIST, json).sendToTarget();
                            }
                            break;
                        case 1002:
                            if (mListAdapter.getItemCount() > 0) {
                                mHandler.obtainMessage(Constant.NO_MORE_LIST_DATA).sendToTarget();
                            } else {
                                mHandler.obtainMessage(Constant.NO_LIST_DATA).sendToTarget();
                            }
                            break;
                        case 2001:
                            mHandler.obtainMessage(Constant.GET_LIST_DATA_FAILURE).sendToTarget();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private String getRequestUrl(boolean isStart) {
        if ((mListAdapter.getItemCount() == 0) || isStart) {
            mCurPageIndex = 1;
        } else {
            ++mCurPageIndex;
        }
        String url = Constant.SHOP_DOMAIN
                + "/appapi/index.php?act=activity&op=supply_goods_list"
                + "&id=" + producerID
                + "&member_id=" + PushApplication.getInstance().getUserId()
                + "&page=" + mCurPageIndex + "&city=" + PushApplication.getInstance().getProperty("user.city");
        return url;
    }

    @Override
    public void onRefresh() {
        if (!mIsStart) {//防止多次下拉
            mIsStart = true;
            mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            swipeRefreshLayout.setEnabled(true);
        } else {
            swipeRefreshLayout.setEnabled(false);
        }

        if (verticalOffset < (getSupportActionBar().getHeight() - appBarLayout.getTotalScrollRange())) {
            mTitleText.setVisibility(View.VISIBLE);
        } else {
            mTitleText.setVisibility(View.GONE);
        }

        int alphaReverse = appBarLayout.getTotalScrollRange() + verticalOffset;
        if (alphaReverse <= 0) {
            mBackBtn.setImageResource(R.drawable.back_btn);
            mBackBtn.setBackground(null);
        } else {
            mBackBtn.setImageResource(R.drawable.back_btn_white);
            mBackBtn.setBackgroundResource(R.drawable.shape_circle);
            mBackBtn.setPadding(Util.dip2px(this, 13), Util.dip2px(this, 8), Util.dip2px(this, 13), Util.dip2px(this, 8));
            mBackBtn.getBackground().setAlpha(alphaReverse > 255 ? 255 : alphaReverse);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StatAgent.initAction(this, "", "2", "8", "", "", "back", "2", "");
    }
}
