package com.haier.cabinet.customer.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.base.CommonWebActivity;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.Shop;
import com.haier.cabinet.customer.entity.ShopCartItem;
import com.haier.cabinet.customer.event.ProductEvent;
import com.haier.cabinet.customer.event.ShopCartEvent;
import com.haier.cabinet.customer.fragment.ShopCartFragment;
import com.haier.cabinet.customer.ui.MainUIActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DateUtil;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.HttpUtil;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.ShareUtil;
import com.haier.cabinet.customer.util.TimeCount;
import com.haier.cabinet.customer.util.UIHelper;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.FirstScrollView;
import com.haier.cabinet.customer.view.MyScrollViews;
import com.haier.cabinet.customer.view.YsnowWebView;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.view.BadgeView;
import com.haier.common.widget.CircleImageView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sunday.statagent.StatAgent;
import com.umeng.socialize.UMShareAPI;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class ProductDetailsActivity extends BaseActivity implements MyScrollViews.IsShowTop, FirstScrollView.ScrollViewListener {
    private static final String TAG = "ProductDetailsActivity";
    public static final String PRODUCT_TYPE = "product_type";
    public static final int ACTION_JPTJ = 1;//精品推荐
    public static final int ACTION_SLYM = 2;//送礼有面
    public static final int ACTION_XLQG = 3;//限量抢购
    public static final int ACTION_XSQG = 4;//限时抢购
    public static final int ACTION_XSTG = 5;//限时团购

    @Bind(R.id.right_img)
    ImageView right_img;
    @Bind(R.id.banner_image)
    ImageView bannerImage;
    @Bind(R.id.pro_name_text)
    TextView proNameText;
    @Bind(R.id.discount_price_text)
    TextView priceText;//折扣价
    @Bind(R.id.retail_price_text)
    TextView retailPriceText;//原价
    @Bind(R.id.spec_text)
    TextView specText;//规格
    @Bind(R.id.madein_text)
    TextView madeinText;//产地
    @Bind(R.id.phone_text)
    TextView phoneText;
    @Bind(R.id.service_area_text)
    TextView serviceAreaText;
    @Bind(R.id.cart_anim_icon)
    ImageView mAnimImageView;
    @Bind(R.id.iv_shop_cart)
    ImageView mShoppingCartImage;//购物车
    @Bind(R.id.goods_num_text)
    TextView mGoodsNumTextView;//购物车商品数量
    @Bind(R.id.pro_count_text)
    EditText countText;
    @Bind(R.id.addBtn)
    Button addBtn;
    @Bind(R.id.minusBtn)
    Button minusBtn;
    @Bind(R.id.view_type1)
    View view_type1;//精品推荐.
    @Bind(R.id.view_type2)
    View view_type2;//限时限量抢购
    @Bind(R.id.tv_boutique)
    TextView tv_boutique;//精品名称
    @Bind(R.id.linear_boutique)
    LinearLayout linear_boutique;//已售和好评布局
    @Bind(R.id.tv_sale)
    TextView tv_sale;//已售
    @Bind(R.id.tv_reception)
    TextView tv_reception;//好评
    @Bind(R.id.tv_limit)
    TextView tv_limit;//限时限量名称
    @Bind(R.id.tv_count)
    TextView tv_count;//时间和数目
    @Bind(R.id.tv_piece)
    TextView tv_piece;//件
    @Bind(R.id.linear_buy)
    LinearLayout linear_buy;//加、减布局,如果限时产品时间到了，需要进行隐藏
    @Bind(R.id.linear_tg_time)
    LinearLayout linear_tg_time;//限时团购布局
    //限时团购剩余时间时分秒，已售件数
    @Bind(R.id.tv_tg_hour)
    TextView tv_tg_hour;
    @Bind(R.id.tv_tg_min)
    TextView tv_tg_min;
    @Bind(R.id.tv_tg_sec)
    TextView tv_tg_sec;
    @Bind(R.id.tv_tg_sale)
    TextView tv_tg_sale;
    @Bind(R.id.relative_explain)
    RelativeLayout relative_explain;//说明
    @Bind(R.id.iv_shop)
    ImageView iv_shop;//店家图片
    @Bind(R.id.tv_shop_name)
    TextView tv_shop_name;//店家名称
    @Bind(R.id.tv_shop_type)
    TextView tv_shop_type;//店家类型
    @Bind(R.id.relative_goshop)
    RelativeLayout relative_goshop;
    @Bind(R.id.btn_goshop)
    Button btn_goshop;//进店看看
    @Bind(R.id.service_online_image)
    ImageView mOnlineServiceImage;//客服
    //评价布局
    @Bind(R.id.linear_comment)
    LinearLayout linear_comment;
    @Bind(R.id.linear_no_comment)
    LinearLayout linear_no_comment;
    @Bind(R.id.business_avtar_image)
    CircleImageView business_avtar_image;//评价头像
    @Bind(R.id.tv_phone)
    TextView tv_phone;//评价手机号
    @Bind(R.id.tv_comment_content)
    TextView tv_comment_content;//评价内容
    @Bind(R.id.tv_comment_time)
    TextView tv_comment_time;//评价时间
    @Bind(R.id.title_bar)
    RelativeLayout mTitlebar;
    private View mTitleBarLIne;
    @Bind(R.id.myscroll)
    MyScrollViews myScrollViews;
    @Bind(R.id.first_scroll)
    FirstScrollView mFirstScrollView;
    @Bind(R.id.btn_pay_now)
    Button btn_pay_now;
    @Bind(R.id.btn_add_car)
    Button btn_add_car;
    @Bind(R.id.iv_sold_out)
    ImageView iv_sold_out;
    @Bind(R.id.ysnowswebview)
    YsnowWebView mWebView;
    private ShareUtil mShareUtil;
    private BadgeView badgeView;
    private Animation mAnimation;
    private Product product;
    private int goodsNum = 0;
    private String city = null;//当前城市
    SpannableString msp = null;
    int proId;//商品id
    private int width;//图片宽度
    private int height;//图片高度
    private ArrayList<ShopCartItem> dataList;//立即下单数组

    private static final int GET_LIST_DATA = 1001;//请求数据
    private static final int UPDATE_LIST_DATA = 1002;//刷新数据

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_LIST_DATA:
                    getProductDetails(proId);
                    break;
                case UPDATE_LIST_DATA:
                    String json = (String) msg.obj;
                    product = getProductByJson(json);
                    initData(product);
                    break;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_product_details3;
    }

    public void initView() {
        StatAgent.initAction(this, "", "1", "11", "", "", "", "1", "");
        mTitlebar.setVisibility(View.VISIBLE);
        mTitlebar.getBackground().setAlpha(0);

        myScrollViews.setIsShowTop(this);
        mFirstScrollView.setScrollViewListener(this);

        mTitleText.setAlpha(0.0f);
        mTitleText.setText("商品详情");
        mBackBtn.setVisibility(View.VISIBLE);
        mBackBtn.getBackground().setAlpha(180);
        right_img.setVisibility(View.VISIBLE);

        badgeView = new BadgeView(this);
        badgeView.setBadgeCount(0);
        badgeView.setBackground(10, Color.RED);
        badgeView.setTextColor(Color.WHITE);
        badgeView.setTargetView(mShoppingCartImage);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.cart_anim_details);
        mAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimImageView.setVisibility(View.INVISIBLE);
                badgeView.setText(PushApplication.getInstance().getCartTotal());
            }
        });

        width = Util.getScreenWidth(this);
        this.height = width;
        //根据屏幕宽度动态设置每个图片的宽高
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width, height);
        // bannerImage.setLayoutParams(param);
    }

    @Override
    public void initData() {
        proId = getIntent().getExtras().getInt(SortResultActivity.RESULT_ID);
        mShareUtil = new ShareUtil();
        mShareUtil.init(this, "特产直供，一惠到底；真爱就一次，抓住机会",
                "听说现在有个卖全国特产的乐家，以后买特产再也不用千里迢迢带行李了！",
                "http://m.rrslj.com/h5/pages/shopstatic/down_load.html", ShareUtil.PRODUCT_SHARE);
        mHandler.sendEmptyMessage(GET_LIST_DATA);
    }

    public void onEventMainThread(ProductEvent event) {
        if (product != null) {
            if (product.count > 0) {//购物车中有此商品

                if (product.id == event.getId()) {
                    product.count = event.getCount();
                    initMinusBtnState();
                }
            }
        }
    }

    public void onEventMainThread(ShopCartEvent event) {
        if (product != null) {
            if (event.getId() > 0) {//添加到购物车
                if (product.id == event.getId()) {
                    product.count = event.getCount();
                    product.shopCardId = event.getShopCartId();
                    initMinusBtnState();
                }
            } else {//从购物车中删除该商品
                if (product.count > 0) {//购物车中有此商品

                    if (product.shopCardId == event.getShopCartId()) {
                        product.count = 0;
                        initMinusBtnState();
                    }
                }
            }

        }
    }

    /**
     * 根据不同参数加载不同布局
     */
    private void updateView(Product product) {
        int action = getIntent().getExtras().getInt(PRODUCT_TYPE);
        switch (action) {
            case ACTION_JPTJ:
                view_type1.setVisibility(View.VISIBLE);
                linear_boutique.setVisibility(View.VISIBLE);
                tv_sale.setText("已售" + product.goods_salenum + "件");
                tv_reception.setText("好评率" + product.goods_percent);
                break;
            case ACTION_SLYM:
                view_type1.setVisibility(View.VISIBLE);
                tv_boutique.setText("送礼有面");
                break;
            case ACTION_XLQG:
                view_type2.setVisibility(View.VISIBLE);
                tv_limit.setText("限量抢购");
                //本期先隐藏
//                tv_piece.setVisibility(View.VISIBLE);
//                tv_count.setText(product.groupbuy_info);
                break;
            case ACTION_XSQG:
                view_type2.setVisibility(View.VISIBLE);
                tv_limit.setText("限时抢购");
//                if (TextUtils.isEmpty(product.xianshi_info)) {
//                    tv_count.setText("0天 00:00:00");
//                } else {
//                    TimeCount timeCount = new TimeCount(tv_count, Long.parseLong(product.xianshi_info) * 1000, 1000);
//                    timeCount.start();
//                }
                break;
            case ACTION_XSTG:
                view_type2.setVisibility(View.VISIBLE);
                tv_limit.setText("限时团购");
                linear_tg_time.setVisibility(View.VISIBLE);
                TimeCount timeCount2 = new TimeCount(tv_tg_hour, tv_tg_min, tv_tg_sec, 209471000, 1000);
                timeCount2.start();
                tv_tg_sale.setText("1234");
                break;
        }
    }

    private void initData(Product product) {
        if (product.goods_storage == 0) {
            linear_buy.setVisibility(View.GONE);
            iv_sold_out.setVisibility(View.VISIBLE);
            btn_add_car.setEnabled(false);
            btn_pay_now.setEnabled(false);
        }
        proNameText.setText("" + product.title);
//        if (TextUtils.isEmpty(product.boutique) || product.boutique.equals("null")) {
//            proNameText.setText(product.title);
//        } else {
//            msp = new SpannableString("12" + product.title);
//            Drawable drawable = getResources().getDrawable(R.drawable.bg_boutique);
//            drawable.setBounds(0, 0, drawable.getIntrinsicWidth() + 5, drawable.getIntrinsicHeight() + 5);
//            msp.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            proNameText.setText(msp);
//        }
        priceText.setText("¥" + product.discountPrice);
        retailPriceText.setText("¥" + product.retailPrice);
        retailPriceText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        retailPriceText.getPaint().setAntiAlias(true);
        specText.setText(product.spec);
        madeinText.setText(product.madein);
        serviceAreaText.setText(product.serviceArea);
        if (product.count == 0) {//如果数量为0，那么商品详情中数量默认为1
            product.count = 1;//商品详情默认为1
        }
        countText.setText(product.count + "");

        initMinusBtnState();
        //店铺
        tv_shop_name.setText(product.shopName + "");
        tv_shop_type.setText(product.shopLevel + "");
        if (!TextUtils.isEmpty(product.shopLogo)) {
            ImageLoader.getInstance().displayImage(product.shopLogo,
                    iv_shop, PushApplication.getInstance().getDefaultOptions());
        } else {
            iv_shop.setImageResource(R.drawable.ic_shop_defult);
        }
        //用户评论
        if (product.gevalId != 0 || !TextUtils.isEmpty(product.geval_content)) {
            if (!product.geval_tel.equals("") && product.geval_tel.length() == 11) {
                tv_phone.setText(product.geval_tel.substring(0, 3) + "****" + product.geval_tel.substring(7, 11));
            } else {
                tv_phone.setText("微信用户");
            }
            tv_comment_content.setText(product.geval_content + "");
            tv_comment_time.setText(DateUtil.getDateToString(product.geval_addtime) + "");
//            if (!TextUtils.isEmpty(product.geval_image)) {
//                ImageLoader.getInstance().displayImage(product.geval_image,
//
//                        business_avtar_image, PushApplication.getInstance().getDefaultOptions());
            linear_comment.setVisibility(View.VISIBLE);
            linear_no_comment.setVisibility(View.GONE);
//            }
        } else {
            linear_comment.setVisibility(View.GONE);
            linear_no_comment.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(product.imgUrl)) {
            ImageLoader.getInstance().displayImage(product.imgUrl,
                    bannerImage, PushApplication.getInstance().getDefaultOptions());
        }
        if (!TextUtils.isEmpty(product.detailsUrl)) {
            mWebView.loadUrl(product.detailsUrl);
        }
        updateView(product);
    }

    @Override
    protected void onResume() {
        super.onResume();
        badgeView.setText(PushApplication.getInstance().getCartTotal());
    }

    @Override
    @OnClick({R.id.back_img, R.id.right_img, R.id.addBtn, R.id.minusBtn, R.id.relative_explain,
            R.id.relative_goshop, R.id.btn_goshop, R.id.linear_comment, R.id.iv_shop_cart,
            R.id.service_online_image, R.id.btn_add_car, R.id.btn_pay_now})
    public void onClick(View v) {
        Bundle bundle = null;
        switch (v.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.addBtn:
                if (!PushApplication.getInstance().isLogin()) {
                    UIHelper.showLoginActivity(this);
                    return;
                }
                if (product == null) {
                    AppToast.showShortText(this, "对不起，添加购物车失败(参数错误)!");
                    return;
                }
                if (product.count < product.goods_storage) {
                    if (product.count < 999) {
                        product.count++;
                    } else {
                        AppToast.showShortText(this, "对不起，购买数量已达上限，另请下单!");
                    }
                } else {
                    AppToast.showShortText(this, "对不起，商品库存不足!");
                }
                initMinusBtnState();
                StatAgent.initAction(this, "", "2", "11", "", "", "add product", "1", "");
                break;
            case R.id.minusBtn:
                if (!PushApplication.getInstance().isLogin()) {
                    UIHelper.showLoginActivity(this);
                    return;
                }
                if (product == null) {
                    AppToast.showShortText(this, "对不起，修改购物车失败(参数错误)!");
                    return;
                }
                if (product.count > 1) {
                    product.count--;
                }
                initMinusBtnState();
                StatAgent.initAction(this, "", "2", "11", "", "", "minus product", "1", "");
                break;
            case R.id.right_img:
                StatAgent.initAction(this, "", "2", "11", "", "", "share", "1", "");

                mShareUtil.startShare();
                break;
            case R.id.relative_explain:
                StatAgent.initAction(this, "", "2", "11", "", "", "relative explain", "1", "");

                IntentUtil.startActivity(this, ExplainActivity.class);
                break;
            case R.id.relative_goshop:
            case R.id.btn_goshop:
                if (product == null) {
//                    AppToast.showShortText(this, "对不起，获取商品失败(参数错误)!");
                    return;
                }
                bundle = new Bundle();
                bundle.putString(SortResultActivity.RESULT_NAME, "店铺");
                bundle.putInt(SortResultActivity.RESULT_TYPE, SortResultActivity.RESULT_SHOP);
                bundle.putInt(SortResultActivity.RESULT_ID, product.shopId);
                IntentUtil.startActivity(this, ShopDetailsActivity.class, bundle);
                if (btn_goshop.getText().toString() != null) {
                    StatAgent.initAction(this, "", "2", "11", "", "", btn_goshop.getText().toString(), "1", "");
                }
                break;
            case R.id.linear_comment:
                bundle = new Bundle();
                bundle.putInt(SortResultActivity.RESULT_ID, proId);
                IntentUtil.startActivity(this, CommentListActivity.class, bundle);
                break;
            case R.id.iv_shop_cart:
                bundle = new Bundle();
                bundle.putInt(MainUIActivity.ACTION_CURRETNTAB, Constant.SHOPC_CART_FRAGMENT_INDEX);
                IntentUtil.startActivity(this, MainUIActivity.class, bundle);
                break;
            case R.id.service_online_image:
                if (product == null) {
                    AppToast.showShortText(this, "对不起，参数错误");
                    return;
                }
                bundle = new Bundle();
                bundle.putString("title", getString(R.string.customer_service));
                bundle.putString("url", Constant.URL_ONLINE_SERVICE_PRODUCT + "?id=" + product.id + "&uid=" + PushApplication.getInstance().getUserId());
                IntentUtil.startActivity(this, CommonWebActivity.class, bundle);
                break;
            case R.id.btn_add_car:
                if (!PushApplication.getInstance().isLogin()) {
                    UIHelper.showLoginActivity(this);
                    return;
                }
                modifyShopCartData(product, true);
                break;
            case R.id.btn_pay_now:
                if (!PushApplication.getInstance().isLogin()) {
                    UIHelper.showLoginActivity(this);
                    return;
                }
                modifyShopCartData(product, false);
                break;
            default:
                break;
        }
    }

    private void initMinusBtnState() {
        if (product.count == 0) {
            product.count = 1;
            product.shopCardId = 0;
        }
        countText.setText(product.count + "");
        minusBtn.setVisibility(View.VISIBLE);
        countText.setVisibility(View.VISIBLE);
//        if (product.count > 0) {
//            minusBtn.setVisibility(View.VISIBLE);
//            countText.setVisibility(View.VISIBLE);
//        } else {
//            minusBtn.setVisibility(View.GONE);
//            countText.setVisibility(View.GONE);
//        }
    }


    /**
     * 获取商品详情
     *
     * @param proId
     */
    private void getProductDetails(int proId) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("goods_id", proId);
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_SHOP_PRODUCT, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(ProductDetailsActivity.this, getString(R.string.loading), true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                AppToast.showShortText(ProductDetailsActivity.this, "获取商品详情失败!");
                DialogHelper.stopProgressDlg();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        mHandler.obtainMessage(UPDATE_LIST_DATA, json).sendToTarget();
                    } else {
                        AppToast.showShortText(ProductDetailsActivity.this, "获取商品详情失败");
                    }

                }
            }
        });

    }

    private Product getProductByJson(String json) {
        Product product = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result")) {
                return null;
            }
            product = new Product();
            JSONObject resultObject = jsonObject.getJSONObject("result");
            product.id = proId;
            product.have_gift = resultObject.getString("have_gift");
            product.xianshi_info = resultObject.getString("xianshi_info");
            product.groupbuy_info = resultObject.getString("groupbuy_info");
            product.mansong_infol = resultObject.getString("mansong_info");

            JSONObject goods_info = resultObject.getJSONObject("goods_info");//商品信息
            product.spec = goods_info.getString("goods_guige");
            product.title = goods_info.getString("goods_name");
            product.discountPrice = goods_info.getDouble("goods_price");
            product.retailPrice = goods_info.getDouble("goods_marketprice");
            product.freight = goods_info.getDouble("goods_freight");
            product.imgUrl = goods_info.getString("goods_image");
            product.thumbUrl = goods_info.getString("goods_image");//为了直接下单显示图片,暂时使用这个url
            product.detailsUrl = goods_info.getString("content");
            product.goods_storage = goods_info.getInt("goods_storage");
            product.madein = goods_info.getString("goods_origin");
            product.goods_salenum = goods_info.getString("goods_salenum");
            product.serviceArea = goods_info.getString("goods_service_area");
            product.count = goods_info.getInt("cart_num");
            product.shopCardId = goods_info.getInt("cart_id");
//            product.boutique = goods_info.getString("jingpin");


            JSONObject store_info = resultObject.getJSONObject("store_info");//店铺信息
            product.shopId = store_info.getInt("store_id");
            product.shopName = store_info.getString("store_name");
            product.shopLogo = store_info.getString("store_logo");
            product.shopLevel = store_info.getString("store_sg_name");


            if (TextUtils.isEmpty(resultObject.getString("evaluate"))) {
                product.goods_percent = "100%";
            } else {
                JSONObject geval_info = resultObject.getJSONObject("evaluate");//评论信息
                product.geval_tel = geval_info.getString("geval_frommemberid");
                product.geval_content = geval_info.getString("geval_content");
                product.geval_addtime = geval_info.getLong("geval_addtime");
                product.goods_percent = geval_info.getString("good_percent");

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return product;
    }


//    /**
//     * 添加购物车
//     *
//     * @param product
//     */
//    private void add2ShopCart(final Product product) {
//        AsyncHttpClient client = new AsyncHttpClient();
//        RequestParams params = new RequestParams();
//        params.put("goods_id", proId);
//        params.put("quantity", product.count);

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }
//        params.put("member_id", PushApplication.getInstance().getUserId());
//        client.get(Constant.URL_SHOPPING_CART_ADD, params, new AsyncHttpResponseHandler() {
//
//            @Override
//            public void onStart() {
//                super.onStart();
//                if (!isFinishing()) {
//                    DialogHelper.showDialogForLoading(ProductDetailsActivity.this, "正在更新购物车", true);
//                }
//            }
//
//            @Override
//            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
//                                  Throwable arg3) {
//                DialogHelper.stopProgressDlg();
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers,
//                                  byte[] response) {
//                DialogHelper.stopProgressDlg();
//                String json = new String(response);
//                if (200 == statusCode) {
//                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
//                        HttpUtil.getShopCartTotal(ProductDetailsActivity.this);
//                        ShopCartFragment.isUpdate = true;
//                        product.shopCardId = JsonUtil.getShopCartId(json);
//
//                        initMinusBtnState();
//                        // 添加购物车动画
//                        mAnimImageView.setVisibility(View.VISIBLE);
//                        mAnimImageView.startAnimation(mAnimation);
//                        EventBus.getDefault().post(new ShopCartEvent(product.id, product.shopCardId, product.count));
//                    } else {
//                        product.count--;
//                        AppToast.showShortText(ProductDetailsActivity.this, "添加购物车失败");
//                    }
//
//                }
//            }
//
//        });
//
//    }

    private void modifyShopCartData(final Product product, final boolean isAdded) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        if (product == null) {
            AppToast.showShortText(this, "对不起，参数错误");
            return;
        }
        if (product.count >= 999) {
            AppToast.showShortText(this, "对不起，购买数量已达上限，另请下单!");
            return;
        }
        params.put("goods_id", proId);
        params.put("cart_id", product.shopCardId);
        params.put("quantity", product.count);
        params.put("member_id", PushApplication.getInstance().getUserId());
        String url="&cart_id=" + product.shopCardId + "&quantity=" + product.count + "&member_id=" + PushApplication.getInstance().getUserId();
        //Log.d("wjb", "url:" + Constant.URL_SHOPPING_CART_MODIFY + "&cart_id=" + product.shopCardId + "&quantity=" + product.count + "&member_id=" + PushApplication.getInstance().getUserId());
        client.get(Constant.URL_SHOPPING_CART_MODIFY, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(ProductDetailsActivity.this, "正在更新购物车", true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                DialogHelper.stopProgressDlg();
                if (isAdded) {
                    AppToast.showShortText(ProductDetailsActivity.this, "加入购物车失败");
                } else {
                    AppToast.showShortText(ProductDetailsActivity.this, "下单失败");
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        ShopCartFragment.isUpdate = true;
                        if (product.shopCardId == 0) {
                            product.shopCardId = JsonUtil.getShopCartId(json);
                        }
                        if (isAdded) {
                            HttpUtil.getShopCartTotal(ProductDetailsActivity.this);
                            ShopCartFragment.isUpdate = true;
                            initMinusBtnState();
                            // 添加购物车动画
                            mAnimImageView.setVisibility(View.VISIBLE);
                            mAnimImageView.startAnimation(mAnimation);
                        } else {
                            dataList = new ArrayList<>();//立即下单数组
                            ShopCartItem cartItem = new ShopCartItem();
                            Shop shop = new Shop();
                            shop.setName(product.shopName);
                            shop.id = product.shopId;
                            shop.fee = 10;
                            shop.free_delivery_pirce = 1.0;
                            cartItem.shop = shop;
                            product.isChecked = true;//新增需求
                            cartItem.products.add(product);
                            dataList.add(cartItem);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("order_list", (Serializable) dataList);
                            IntentUtil.startActivity(ProductDetailsActivity.this, PlaceOrderActivity.class, bundle);
                        }
                        EventBus.getDefault().post(new ShopCartEvent(product.id, product.shopCardId, product.count));
                        EventBus.getDefault().post(new ProductEvent(product.id, product.count));
                    } else {
                        if (isAdded) {
                            AppToast.showShortText(ProductDetailsActivity.this, "加入购物车失败");
                        } else {
                            AppToast.showShortText(ProductDetailsActivity.this, "下单失败");
                        }
                    }

                }
            }

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StatAgent.initAction(this, "", "2", "11", "", "", "back", "2", "");
        bannerImage.setBackgroundResource(0);
        System.gc();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** attention to this below ,must add this**/
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void isShow() {

    }

    @Override
    public void isHide() {

    }

    @Override
    public void scrollMove(int dy) {
        if (bannerImage.getHeight() > 0) {
            //define it for scroll height
            int lHeight = bannerImage.getHeight()-180;

            if (dy <= lHeight && dy >= 0) {
                float floatProgress = new Float(dy) / new Float(lHeight);
                int progress = (int) (new Float(dy) / new Float(lHeight) * 180);

                int alphaReverse = 150 - progress;
                if (alphaReverse < 0) {
                    alphaReverse = 0;
                }

                mBackBtn.getBackground().setAlpha(alphaReverse);
                right_img.getBackground().setAlpha(alphaReverse);

                mTitlebar.getBackground().setAlpha(progress);
                mTitlebar.invalidate();

                mTitleText.setAlpha(floatProgress);

                if (progress > 120) {
                    mBackBtn.setImageResource(R.drawable.back_btn);
                    right_img.setImageResource(R.drawable.ic_product_share);
                } else {
                    mBackBtn.setImageResource(R.drawable.back_btn_white);
                    right_img.setImageResource(R.drawable.ic_share_white);
                }

            } else if (dy > lHeight) {
                mTitleText.setAlpha(1.0f);
                mTitlebar.getBackground().setAlpha(255);
                mBackBtn.setImageResource(R.drawable.back_btn);
                right_img.setImageResource(R.drawable.ic_product_share);
            } else {
                mTitlebar.getBackground().setAlpha(0);
            }

        }
    }
}
