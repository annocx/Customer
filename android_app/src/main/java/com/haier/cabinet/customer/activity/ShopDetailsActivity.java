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
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.ShopProductListAdapter;
import com.haier.cabinet.customer.base.BaseAppCompatActivity;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.Shop;
import com.haier.cabinet.customer.event.ProductEvent;
import com.haier.cabinet.customer.event.ShopCartEvent;
import com.haier.cabinet.customer.ui.MainUIActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.HttpUtil;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.UIHelper;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.HeaderLayout;
import com.haier.cabinet.customer.widget.recyclerview.CustRecyclerView;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.haier.cabinet.customer.widget.recyclerview.HeaderSpanSizeLookup;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewUtils;
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

public class ShopDetailsActivity extends BaseAppCompatActivity implements View.OnClickListener, View.OnTouchListener,
        Animation.AnimationListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ShopDetailsActivity";
    @Bind(R.id.cart_anim_icon)
    ImageView mAnimImageView;
    @Bind(R.id.shopping_cart_img)
    ImageView shoppingCart;
    @Bind(R.id.shop_recyclerView)
    CustRecyclerView recyclerView;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private Animation mAnimation;
    private ShopProductListAdapter mListAdapter;

    private boolean mIsStart = false;
    private static int totalPage = 0;
    private int mCurPageIndex = 1;

    private Shop shop = null;//店铺

    private int shopId = -1;

    private ArrayList<Product> mListItems;

    private BadgeView badgeView;

    private Animation badgeAnimation;

    private ImageView shopLogoImg;

    private int badgeCount = 0;

    private int width, height;

    private static final int REQUEST_COUNT = 10;


    private GridLayoutManager manager;


    private HeaderAndFooterRecyclerViewAdapter mHeaderAndFooterRecyclerViewAdapter = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_shop_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatAgent.initAction(this, "", "1", "10", "", "", "", "1", "");
        shopId = getIntent().getIntExtra(SortResultActivity.RESULT_ID, -1);
        shop = new Shop();
    }

    public void initView() {
        mBackBtn.setVisibility(View.VISIBLE);
        badgeView = new BadgeView(this);
        badgeView.setBadgeCount(badgeCount);
        badgeView.setBackground(10, Color.WHITE);
        badgeView.setTextColor(Color.RED);
        badgeView.setBadgeMargin(0, 10, 10, 0);
        badgeView.setTargetView(shoppingCart);

        badgeAnimation = AnimationUtils.loadAnimation(ShopDetailsActivity.this, R.anim.scale_anim);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.cart_anim_new);
        mAnimation.setAnimationListener(this);

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(this, 24));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);

        View emptyView = findViewById(R.id.empty_view);
        emptyView.setOnTouchListener(this);
        recyclerView.setEmptyView(emptyView);

        mListItems = new ArrayList<>();
        mListAdapter = new ShopProductListAdapter(this, mHandler);

        mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mListAdapter);
        recyclerView.setAdapter(mHeaderAndFooterRecyclerViewAdapter);

        manager = new GridLayoutManager(this, 2);
        manager.setSpanSizeLookup(new HeaderSpanSizeLookup((HeaderAndFooterRecyclerViewAdapter) recyclerView.getAdapter(), manager.getSpanCount()));

        recyclerView.setLayoutManager(manager);
        recyclerView.setOnPauseListenerParams(ImageLoader.getInstance(), false, true);
        mOnScrollListener.setSwipeRefreshLayout(swipeRefreshLayout);
        recyclerView.addOnScrollListener(mOnScrollListener);

        HeaderLayout headerView = new HeaderLayout(this, R.layout.layout_header);
        RecyclerViewUtils.setHeaderView(recyclerView, headerView);
        shopLogoImg = (ImageView) headerView.findViewById(R.id.head_img);

        width = Util.getScreenWidth(this);
        height = (new Double(width * 0.49)).intValue();

        //根据屏幕宽度动态设置每个图片的宽高
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width, height);
        shopLogoImg.setLayoutParams(param);

        mHandler.sendEmptyMessage(GET_LIST_DATA);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyShopCartNumChanged();
    }

    public void onEventMainThread(ProductEvent event) {
        mListAdapter.updateProduct(event.getId(), event.getCount());
    }

    public void onEventMainThread(ShopCartEvent event) {
        if (event.getCount() > 0) {
            mListAdapter.updateProduct(event.getId(), event.getCount(), event.getShopCartId());
        } else {
            mListAdapter.updateProduct(event.getShopCartId());
        }
    }

    public static final int ADD_TO_SHOPPING_CART = 1009;
    public static final int PLUS_BADGE_ANIMATION = 1010;
    public static final int MINUS_BADGE_ANIMATION = 1011;

    private static final int GET_LIST_DATA = 1001;
    private static final int UPDATE_LIST = 1002;
    private static final int NO_LIST_DATA = 1003;
    private static final int GET_LIST_DATA_FAILURE = 1005;
    private static final int NO_MORE_LIST_DATA = 1006;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case GET_LIST_DATA:
                    String url = getRequestUrl(mIsStart);
                    requestPromotionData(url);
                    break;
                case UPDATE_LIST:
                    String json = (String) msg.obj;
                    ArrayList<Product> data = getProductListByJosn(json);

                    if (null == data || null == mListAdapter) {
                        return;
                    }

                    if (mIsStart) {
                        mListAdapter.setDataList(data);
                    } else {
                        RecyclerViewStateUtils.setFooterViewState(ShopDetailsActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.Normal, null);
                        mListAdapter.addAll(data);
                    }

                    if ((mCurPageIndex == totalPage) && mIsStart) {
                        RecyclerViewStateUtils.setFooterViewState2(ShopDetailsActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
                        recyclerView.removeOnScrollListener(mOnScrollListener);
                    }

                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case GET_LIST_DATA_FAILURE:
                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case NO_MORE_LIST_DATA:

                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case NO_LIST_DATA:
                    mListAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    break;
                case ADD_TO_SHOPPING_CART:
                    if (!PushApplication.getInstance().isLogin()) {
                        UIHelper.showLoginActivity(ShopDetailsActivity.this);
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

        ;
    };

    @Override
    public void notifyShopCartNumChanged() {
        super.notifyShopCartNumChanged();
        badgeView.setText(PushApplication.getInstance().getCartTotal());
        badgeView.startAnimation(badgeAnimation);
    }

    @Override
    @OnClick({R.id.back_img,R.id.shopping_cart_img})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                finish();
                break;
            case R.id.shopping_cart_img:
                Bundle bundle = new Bundle();
                bundle.putInt(MainUIActivity.ACTION_CURRETNTAB, Constant.SHOPC_CART_FRAGMENT_INDEX);
                IntentUtil.startActivity(this, MainUIActivity.class, bundle);
                break;
            default:
                break;
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
                RecyclerViewStateUtils.setFooterViewState(ShopDetailsActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
                mIsStart = false;
                mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
            } else {
                //the end
                RecyclerViewStateUtils.setFooterViewState(ShopDetailsActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
            }
        }
    };

    private void add2ShopCart(final int position, final Product product) {
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
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
//				Log.d(TAG, "json " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        HttpUtil.getShopCartTotal(ShopDetailsActivity.this);
                        // 添加购物车动画
                        //doAnim(mClickPosition,mDrawable,mStartLocation);

                        product.shopCardId = JsonUtil.getShopCartId(json);
                        mAnimImageView.setVisibility(View.VISIBLE);
                        mAnimImageView.startAnimation(mAnimation);
                        mListAdapter.updateList(position, product);

                        EventBus.getDefault().post(new ShopCartEvent(product.id, product.shopCardId, product.count));
                    } else {
                        product.count--;
                        AppToast.showShortText(ShopDetailsActivity.this, "添加购物车失败");
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
//				Log.d(TAG, "json " + json);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            Log.d(TAG, "修改购物车商品数量成功  isAdded = " + isAdded);

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
                            AppToast.showShortText(ShopDetailsActivity.this, "修改购物车失败");
                            break;
                        default:
                            break;
                    }

                }
            }

        });

    }

    private void requestPromotionData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                mHandler.sendEmptyMessage(GET_LIST_DATA_FAILURE);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "获取数据异常 ", arg3);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                swipeRefreshLayout.setRefreshing(false);

                String json = new String(response);
//				Log.d(TAG, "onSuccess " + json);

                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            if (JsonUtil.isHaveData(json)) {
                                mHandler.obtainMessage(UPDATE_LIST, json).sendToTarget();
                            }
                            break;
                        case 1002:
                            if (mListAdapter.getItemCount() > 0) {
                                mHandler.obtainMessage(NO_MORE_LIST_DATA).sendToTarget();
                            } else {
                                mHandler.obtainMessage(NO_LIST_DATA).sendToTarget();
                            }
                            break;
                        case 2001:
                            AppToast.makeToast(ShopDetailsActivity.this, "网络异常，请稍后再试!");

                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private ArrayList<Product> getProductListByJosn(String json) {
        ArrayList<Product> list = new ArrayList<Product>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result")) {
                return null;
            }
            JSONObject resultObject = jsonObject.getJSONObject("result");
            totalPage = resultObject.getInt("page_count");

            if (mCurPageIndex == 1) {
                JSONObject shopObject = resultObject.getJSONObject("store_info");
                shop.id = shopObject.getInt("store_id");
                shop.name = shopObject.getString("store_name");
                shop.imgUrl = shopObject.getString("store_logo");

                mTitleText.setText(shop.getName());
                shopLogoImg.setVisibility(View.GONE);
                ImageLoader.getInstance().displayImage(shop.getImgUrl(), shopLogoImg, PushApplication.getInstance().getDefaultOptions2());
            }

            JSONArray productArray = resultObject.getJSONArray("goods_list");

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
                if (mCurPageIndex == 1) {
                    product.shopId = shop.id;
                    product.shopName = shop.name;
                }
                product.count = proObject.getInt("cart_num");
                product.shopCardId = proObject.getInt("cart_id");
//                product.boutique = proObject.getString("jingpin");
                list.add(product);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }

        return list;
    }

    private String getRequestUrl(boolean isStart) {
        if ((mListAdapter.getItemCount() == 0) || isStart) {
            mCurPageIndex = 1;
        } else {
            ++mCurPageIndex;
        }
        String url = Constant.SHOP_DOMAIN
                + "/appapi/index.php?act=goods&op=goods_list"
                + "&store_id=" + shopId
                + "&member_id=" + PushApplication.getInstance().getUserId()
                + "&page=" + mCurPageIndex + "&city=" + PushApplication.getInstance().getProperty("user.city");
//        Log.d(TAG, "url -- " + url);
        return url;
    }

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
    public void onRefresh() {
        if (!mIsStart) {//防止多次下拉
            Log.d(TAG, "onPullDownToRefresh");
            mIsStart = true;
            mHandler.sendEmptyMessage(Constant.GET_LIST_DATA);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mListAdapter.getItemCount() == 0) {
                mHandler.sendEmptyMessage(GET_LIST_DATA);
            }
        }
        return false;
    }
}
