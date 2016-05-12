package com.haier.cabinet.customer.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.CategorizeActivity;
import com.haier.cabinet.customer.activity.CityListActivity;
import com.haier.cabinet.customer.activity.FragmentDetailsActivity;
import com.haier.cabinet.customer.activity.ProducerSupplyActivity;
import com.haier.cabinet.customer.activity.SalesPromotionActivity;
import com.haier.cabinet.customer.activity.SearchActivity;
import com.haier.cabinet.customer.activity.SpecialityDetailActivity;
import com.haier.cabinet.customer.activity.adapter.HotsGridAdapter;
import com.haier.cabinet.customer.base.BaseFragment;
import com.haier.cabinet.customer.base.BaseListFragment;
import com.haier.cabinet.customer.base.CommonWebActivity;
import com.haier.cabinet.customer.entity.Activities;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.event.CityChangedEvent;
import com.haier.cabinet.customer.event.ExpressMailEvent;
import com.haier.cabinet.customer.event.ProductEvent;
import com.haier.cabinet.customer.event.ShopCartEvent;
import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.cabinet.customer.push.PushMsgListActivity;
import com.haier.cabinet.customer.ui.MainUIActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.HttpUtil;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.UIHelper;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.HardRefSimpleImageLoadingListener;
import com.haier.cabinet.customer.view.HomeSlideShowView;
import com.haier.cabinet.customer.viewholder.ProductsViewHolder;
import com.haier.cabinet.customer.widget.recyclerview.CommonHeader;
import com.haier.cabinet.customer.widget.recyclerview.CustRecyclerView;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.haier.cabinet.customer.widget.recyclerview.HeaderSpanSizeLookup;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewUtils;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.NetUtil;
import com.haier.common.view.BadgeView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class HomeFragment extends BaseListFragment {

    private static final String TAG = "HomeFragment";

    @Bind(R.id.index_title_bar)
    RelativeLayout mTitlebar;
    @Bind(R.id.tv_title)
    TextView keyTextView;
    @Bind(R.id.image_right)
    TextView msgTextView;
    @Bind(R.id.top_btn)
    Button toTopBtn;// 返回顶部的按钮
    @Bind(R.id.current_city_text)
    TextView mCurCityText;//城市


    private TextView scanView;

    private BadgeView badgeView;

    private View cart_btn;

    private HotsGridAdapter mGridAdapter;

    private static int totalPage = 0;
    private int mCurPageIndex = 1;
    private HomeSlideShowView slideShowView;
    public static boolean isUpdate = false;

    //动画时间
    private int AnimationDuration = 1000;
    //正在执行的动画数量
    private int number = 0;
    //是否完成清理
    private boolean isClean = false;
    private FrameLayout animation_viewGroup;

    private static final int MSG_CLEAR = 1001;//用来清除动画后留下的垃圾
    private static final int GET_LIST_DATA = 1002;
    private static final int UPDATE_LIST_DATA = 1003;
    private static final int NO_MORE_LIST_DATA = 1004;
    private static final int NO_LIST_DATA = 1005;
    private static final int GET_LIST_DATA_FAILURE = 1006;
    public static final int ADD_TO_SHOPPING_CART = 1007;
    public static final int INCREASE_FROM_SHOPPING_CART = 1008;
    public static final int REDUCE_FROM_SHOPPING_CART = 1009;

    private boolean isRequestInProcess = false;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what = " + msg.what);
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CLEAR:
                    try {
                        animation_viewGroup.removeAllViews();
                    } catch (Exception e) {

                    }

                    isClean = false;
                    break;
                case GET_LIST_DATA:
                    if (NetUtil.isNetConnected(getActivity())) {
                        // loading more
                        String url = getRequestUrl(mIsStart);
                        if (!isRequestInProcess) {
                            requestData(url);
                            isRequestInProcess = true;
                        }

                    } else {
                        mHandler.sendEmptyMessage(GET_LIST_DATA_FAILURE);
                    }

                    break;
                case UPDATE_LIST_DATA:
                    String json = (String) msg.obj;
                    List data = getListByJosn(json);
                    if (null == data) {
                        return;
                    }
                    if (mIsStart) {
                        mGridAdapter.setDataList(data);

                    } else {
                        RecyclerViewStateUtils.setFooterViewState(mRecyclerView, LoadingFooter.State.Normal);
                        mGridAdapter.addAll(data);
                    }

                    mTitlebar.setVisibility(View.VISIBLE);
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case NO_MORE_LIST_DATA:

                    mGridAdapter.notifyDataSetChanged();

                    mTitlebar.setVisibility(View.VISIBLE);
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case GET_LIST_DATA_FAILURE:
                    mSwipeRefreshLayout.setRefreshing(false);
                    RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, REQUEST_COUNT, LoadingFooter.State.NetWorkError, mFooterClick);
                    mGridAdapter.notifyDataSetChanged();

                    initOneActivitiesView();
                    initRecommendActivitiesView();
                    initSpecialityActivitiesView();

                    mTitlebar.setVisibility(View.VISIBLE);
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case NO_LIST_DATA:
                    mGridAdapter.notifyDataSetChanged();
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case ADD_TO_SHOPPING_CART:
                    if (!PushApplication.getInstance().isLogin()) {
                        UIHelper.showLoginActivity(getActivity());
                        return;
                    }
                    add2ShopCart(msg.arg1, (Product) msg.obj);
                    break;
                case INCREASE_FROM_SHOPPING_CART:
                    if (!PushApplication.getInstance().isLogin()) {
                        UIHelper.showLoginActivity(getActivity());
                        return;
                    }
                    modifyShopCartData(msg.arg1, (Product) msg.obj, true);
                    break;
                case REDUCE_FROM_SHOPPING_CART:
                    if (!PushApplication.getInstance().isLogin()) {
                        UIHelper.showLoginActivity(getActivity());
                        return;
                    }
                    modifyShopCartData(msg.arg1, (Product) msg.obj, false);
                    break;
                default:
                    break;

            }
        }

    };

    private View.OnClickListener mFooterClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
            mHandler.sendEmptyMessage(GET_LIST_DATA);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StatAgent.initAction(getActivity(), "", "1", "2", "", "", "", "1", "");
    }

    CommonHeader headerView;

    @Override
    public void initView(View view) {
        super.initView(view);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(getActivity(), 68));
            mGridAdapter = new HotsGridAdapter(getActivity(), mHandler);
            mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mGridAdapter);
            mRecyclerView.setAdapter(mHeaderAndFooterRecyclerViewAdapter);
            headerView = new CommonHeader(getActivity(), R.layout.layout_home_header);
            RecyclerViewUtils.setHeaderView(mRecyclerView, headerView);
            scanView = (TextView) headerView.findViewById(R.id.scan_button);
            mTitlebar.setVisibility(View.VISIBLE);
            mTitlebar.getBackground().setAlpha(0);

            cart_btn = getActivity().findViewById(R.id.shop_cart_btn);
            slideShowView = (HomeSlideShowView) headerView.findViewById(R.id.slideshowView);
            animation_viewGroup = createAnimLayout();

            mOnScrollListener.setSwipeRefreshLayout(mSwipeRefreshLayout);
            mRecyclerView.addOnScrollListener(mOnScrollListener);
            mRecyclerView.setOnPauseListenerParams(ImageLoader.getInstance(), false, true);

            badgeView = new BadgeView(getActivity());
            badgeView.setBadgeMargin(0, 5, 20, 0);

            isUpdate = true;
        }


    }

    @Override
    protected void initLayoutManager() {
        super.initLayoutManager();
        if (mRecyclerView != null) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
            layoutManager.setSpanSizeLookup(new HeaderSpanSizeLookup((HeaderAndFooterRecyclerViewAdapter) mRecyclerView.getAdapter(), layoutManager.getSpanCount()));
            layoutManager.setOrientation(GridLayoutManager.VERTICAL);
            layoutManager.setSmoothScrollbarEnabled(true);
            mRecyclerView.setLayoutManager(layoutManager);
        }
    }

    private RecyclerOnScrollListener mOnScrollListener = new RecyclerOnScrollListener() {

        @Override
        public void onScrolled(int dx, int dy) {
            super.onScrolled(dx, dy);
            if (slideShowView.getHeight() > 0) {
                //define it for scroll height
                int lHeight = slideShowView.getHeight();
                if (dy > 0) {
                    if (dy < lHeight) {
                        int progress = (int) (new Float(dy) / new Float(lHeight) * 200);//255
                        mTitlebar.getBackground().setAlpha(progress);
                    } else {
                        mTitlebar.getBackground().setAlpha(255 - 55);
                    }

                } else {
                    mTitlebar.getBackground().setAlpha(0);
                }

            }

            if (dy == 0 || dy < headerView.getHeight()) {
                toTopBtn.setVisibility(View.GONE);
            }

        }

        @Override
        public void onScrollUp() {
            // 滑动时隐藏float button
            if (toTopBtn.getVisibility() == View.VISIBLE) {
                toTopBtn.setVisibility(View.GONE);
                animate(toTopBtn, R.anim.floating_action_button_hide);
            }
        }

        @Override
        public void onScrollDown() {
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItemPosition < 1) {
                if (toTopBtn.getVisibility() == View.VISIBLE) {
                    toTopBtn.setVisibility(View.GONE);
                    animate(toTopBtn, R.anim.floating_action_button_hide);
                }
            } else {
                if (toTopBtn.getVisibility() != View.VISIBLE) {
                    toTopBtn.setVisibility(View.VISIBLE);
                    animate(toTopBtn, R.anim.floating_action_button_show);
                }
            }
        }

        @Override
        public void onBottom() {
            LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(mRecyclerView);
            if (state == LoadingFooter.State.Loading) {
                Log.d(TAG, "the state is Loading, just wait..");
                return;
            }

            if (mCurPageIndex < totalPage) {
                // loading more
                RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
                mHandler.sendEmptyMessage(GET_LIST_DATA);
            } else {
                //the end
                if (totalPage > 1) {
                    RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
                } else {
                    RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, mGridAdapter.getDataList().size(), LoadingFooter.State.TheEnd, null);
                }

            }
        }


    };

    private void animate(View view, int anim) {
        if (anim != 0) {
            Animation a = AnimationUtils.loadAnimation(view.getContext(), anim);
            view.startAnimation(a);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(PushApplication.getInstance().getProperty("user.city"))) {
            startActivity(new Intent(getActivity(), CityListActivity.class));
        } else {
            mCurCityText.setText(PushApplication.getInstance().getProperty("user.city"));
            if (isUpdate && !isRequestInProcess) {
                onRefresh();
                isUpdate = false;
            }
        }

    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        Log.d(TAG, "onRefresh");
        if (!mIsStart) {//防止多次下拉
            mIsStart = true;

            activitiesList.clear();
            recommendList.clear();
            specialityList.clear();
            quickList.clear();
            slideShowView.refreshView();
            mGridAdapter.clear();
            mHandler.sendEmptyMessage(GET_LIST_DATA);
        }
    }

    private GridLayoutManager layoutManager;

    private void getExpressMailTotal() {

    }

    /**
     * 每一页展示多少条数据
     */
    private static final int REQUEST_COUNT = 10;

    @Override
    public void onEventMainThread(CityChangedEvent event) {
        mCurCityText.setText(PushApplication.getInstance().getProperty("user.city"));
        onRefresh();
    }

    public void onEventMainThread(UserChangedEvent event) {
        onRefresh();
        if (PushApplication.getInstance().isLogin()) {
            HttpUtil.getExpressMailTotal(getActivity());
        } else {
            badgeView.setBadgeCount(0);
            badgeView.setTargetView(scanView);
        }
    }

    public void onEventMainThread(ExpressMailEvent event) {
        badgeView.setBadgeCount(event.getTotal());
        badgeView.setTargetView(scanView);
    }

    public void onEventMainThread(ProductEvent event) {
        mGridAdapter.updateProduct(event.getId(), event.getCount());
    }

    public void onEventMainThread(ShopCartEvent event) {
        if (event.getCount() > 0) {
            mGridAdapter.updateProduct(event.getId(), event.getCount(), event.getShopCartId());
        } else {
            mGridAdapter.updateProduct(event.getShopCartId());
        }
    }

    private void initOneActivitiesView() {
        Log.d(TAG, "activitiesList.size() = " + activitiesList.size());
        if (activitiesList.size() > 0) {
            headerView.findViewById(R.id.activities_one).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.activities_view_top_line).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.activities_view_bottom_line).setVisibility(View.VISIBLE);
            for (int i = 0; i < activitiesList.size(); i++) {
                final Activities activities = activitiesList.get(i);
                String imageID = "ad_image_" + i;
                int imageResID = getResources().getIdentifier(imageID, "id", "com.haier.cabinet.customer");
                ImageView image = (ImageView) headerView.findViewById(imageResID);
                if (TextUtils.isEmpty(activities.imgUrl) || activities.imgUrl == null) {
                    image.setImageBitmap(Util.readBitMap(getActivity(), R.drawable.ic_product_default));
                } else {
                    ImageLoader.getInstance().displayImage(activities.imgUrl, image,
                            PushApplication.getInstance().getDefaultOptions3());
                }
                String titleID = "ad_title_" + i;
                int titleResID = getResources().getIdentifier(titleID, "id", getApplication().getPackageName());
                TextView titleText = (TextView) headerView.findViewById(titleResID);
                titleText.setText(activities.title);

                if (0 == i) {
                    String desID = "ad_description_" + i;
                    int desResID = getResources().getIdentifier(desID, "id", getApplication().getPackageName());
                    TextView desText = (TextView) headerView.findViewById(desResID);
                    desText.setText(activities.description);
                }

                String viewID = "ad_view_" + i;
                int viewResID = getResources().getIdentifier(viewID, "id", getApplication().getPackageName());
                View view = headerView.findViewById(viewResID);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StatAgent.initAction(getActivity(), "", "2", "2", "3", "", activities.title, "1", activities.clickUrl);

                        Bundle bundle = new Bundle();
                        bundle.putString("url", activities.clickUrl);
                        IntentUtil.startActivity(getActivity(), SalesPromotionActivity.class, bundle);
                    }
                });
            }

        } else {
            headerView.findViewById(R.id.activities_one).setVisibility(View.GONE);
            headerView.findViewById(R.id.activities_view_top_line).setVisibility(View.GONE);
            headerView.findViewById(R.id.activities_view_bottom_line).setVisibility(View.GONE);
        }


    }

    private void initRecommendActivitiesView() {
        if (recommendList.size() > 0) {
            headerView.findViewById(R.id.recommend_view).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.recommend_view_line).setVisibility(View.VISIBLE);
            for (int i = 0; i < recommendList.size(); i++) {
                final Activities activities = recommendList.get(i);
                String imageID = "recommend_image_" + i;
                int imageResID = getResources().getIdentifier(imageID, "id", getApplication().getPackageName());
                ImageView image = (ImageView) headerView.findViewById(imageResID);
                ImageLoader.getInstance().displayImage(activities.imgUrl, image,
                        PushApplication.getInstance().getDefaultOptions3());

                String titleID = "recommend_title_" + i;
                int titleResID = getResources().getIdentifier(titleID, "id", getApplication().getPackageName());
                TextView titleText = (TextView) headerView.findViewById(titleResID);
                titleText.setText(activities.title);

                String desID = "recommend_description_" + i;
                int desResID = getResources().getIdentifier(desID, "id", getApplication().getPackageName());
                TextView desText = (TextView) headerView.findViewById(desResID);
                desText.setText(activities.description);

                String viewID = "recommend_view_" + i;
                int viewResID = getResources().getIdentifier(viewID, "id", getApplication().getPackageName());
                View view = headerView.findViewById(viewResID);
                final int position = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        /*if (position == recommendList.size() - 1) {
                            bundle.putInt(Constant.FRAGMENT_DETAILS, Constant.FRAGMENT_COMPETITIVE_);
                            IntentUtil.startActivity(getActivity(), FragmentDetailsActivity.class, bundle);
                        } else {
                            bundle.putString("url", activities.clickUrl);
                            IntentUtil.startActivity(getActivity(), SalesPromotionActivity.class, bundle);
                        }*/
                    }
                });
            }

        } else {
            headerView.findViewById(R.id.recommend_view).setVisibility(View.GONE);
            headerView.findViewById(R.id.recommend_view_line).setVisibility(View.GONE);
        }

    }

    private void initSpecialityActivitiesView() {
        if (specialityList.size() > 0) {
            headerView.findViewById(R.id.specialty_view).setVisibility(View.VISIBLE);
            switch (specialityList.size()) {
                case 1:
                    headerView.findViewById(getResources().getIdentifier("specialty_image_1", "id", getApplication().getPackageName())).setVisibility(View.GONE);
                    headerView.findViewById(getResources().getIdentifier("specialty_image_2", "id", getApplication().getPackageName())).setVisibility(View.GONE);
                    break;
                case 2:
                    headerView.findViewById(getResources().getIdentifier("specialty_image_1", "id", getApplication().getPackageName())).setVisibility(View.VISIBLE);
                    headerView.findViewById(getResources().getIdentifier("specialty_image_2", "id", getApplication().getPackageName())).setVisibility(View.GONE);
                    break;
                case 3:
                    headerView.findViewById(getResources().getIdentifier("specialty_image_1", "id", getApplication().getPackageName())).setVisibility(View.VISIBLE);
                    headerView.findViewById(getResources().getIdentifier("specialty_image_2", "id", getApplication().getPackageName())).setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }

            for (int i = 0; i < specialityList.size(); i++) {
                final Activities activities = specialityList.get(i);
                String imageID = "specialty_image_" + i;
                int imageResID = getResources().getIdentifier(imageID, "id", getApplication().getPackageName());
                ImageView image = (ImageView) headerView.findViewById(imageResID);
                ImageLoader.getInstance().displayImage(activities.imgUrl, image,
                        PushApplication.getInstance().getDefaultOptions3());

               /* String titleID = "specialty_title_" + i ;
                int titleResID = getResources().getIdentifier(titleID, "id", getApplication().getPackageName());
                TextView titleText = (TextView) headerView.findViewById(titleResID);
                titleText.setText(activities.title);

                String desID = "specialty_description_" + i ;
                int desResID = getResources().getIdentifier(desID, "id", getApplication().getPackageName());
                TextView desText = (TextView) headerView.findViewById(desResID);
                desText.setText(activities.description);*/

                /*String viewID = "specialty_view_" + i ;
                int viewResID = getResources().getIdentifier(viewID, "id", getApplication().getPackageName());
                View view = headerView.findViewById(viewResID);*/
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StatAgent.initAction(getActivity(), "", "2", "2", "4", "", activities.title, "1", "");

                        Bundle bundle = new Bundle();
                        bundle.putInt("id", activities.id);
                        bundle.putString("title", activities.title);
                        IntentUtil.startActivity(getActivity(), SpecialityDetailActivity.class, bundle);

                    }
                });
            }

            headerView.findViewById(R.id.special_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatAgent.initAction(getActivity(), "", "2", "2", "4", "", "more", "1", "");

                    IntentUtil.startActivity(getActivity(), ProducerSupplyActivity.class);
                }
            });
        } else {
            headerView.findViewById(R.id.specialty_view).setVisibility(View.GONE);
        }

    }

    private void initQuickEntryView() {
        if (quickList.size() > 0) {
            headerView.findViewById(R.id.quick_entry_layout).setVisibility(View.VISIBLE);

            for (int i = 0; i < quickList.size(); i++) {
                final Activities activities = quickList.get(i);
                String textID = "quick_view_" + i;
                int textResID = getResources().getIdentifier(textID, "id", getApplication().getPackageName());
                final TextView textView = (TextView) headerView.findViewById(textResID);
                textView.setText(activities.title);

                ImageLoader.getInstance().loadImage(activities.imgUrl, PushApplication.getInstance().getDefaultOptions3(), new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        if (bitmap != null) {
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            DisplayMetrics mDisplayMetrics = new DisplayMetrics();
                            getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
                            int H = mDisplayMetrics.heightPixels;
                            if (H < 1500) {
                                drawable.setBounds(0, 0, 96, 96);
                            } else {
                                drawable.setBounds(0, 0, 129, 129);
                            }
                            textView.setCompoundDrawables(null, drawable, null, null);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });

                switch (i) {
                    case 0:
                        scanView = textView;
                        if (PushApplication.getInstance().isLogin()) {
                            HttpUtil.getExpressMailTotal(getActivity());
                        } else {
                            badgeView.setBadgeCount(0);
                            badgeView.setTargetView(scanView);
                        }
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                StatAgent.initAction(getActivity(), "", "2", "2", "2", "", textView.getText().toString(), "1", "");

                                Bundle bundle = new Bundle();
                                bundle.putInt(Constant.FRAGMENT_DETAILS, Constant.FRAGMENT_LIFE);
                                IntentUtil.startActivity(getActivity(), FragmentDetailsActivity.class, bundle);
                            }
                        });
                        break;
                    case 1:
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (TextUtils.isEmpty(activities.clickUrl) || activities.clickUrl.equals("null"))
                                    return;

                                StatAgent.initAction(getActivity(), "", "2", "2", "2", "", textView.getText().toString(), "1", "");

                                Bundle bundle = new Bundle();
                                bundle.putString("title", getString(R.string.appliance_service));
                                bundle.putString("url", activities.clickUrl);
                                IntentUtil.startActivity(getActivity(), SalesPromotionActivity.class, bundle);
                            }
                        });
                        break;
                    case 2:
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (TextUtils.isEmpty(activities.clickUrl) || activities.clickUrl.equals("null"))
                                    return;

                                StatAgent.initAction(getActivity(), "", "2", "2", "2", "", textView.getText().toString(), "1", "");

                                Bundle bundle = new Bundle();
                                bundle.putString("title", getString(R.string.shunxinbao));
                                bundle.putString("url", activities.clickUrl);
                                IntentUtil.startActivity(getActivity(), SalesPromotionActivity.class, bundle);
                            }
                        });
                        break;
                    case 3:
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (TextUtils.isEmpty(activities.clickUrl) || activities.clickUrl.equals("null"))
                                    return;

                                StatAgent.initAction(getActivity(), "", "2", "2", "2", "", textView.getText().toString(), "1", "");

                                Bundle bundle = new Bundle();
                                bundle.putString("title", getString(R.string.pa_insurance));
                                bundle.putString("url", activities.clickUrl);
                                IntentUtil.startActivity(getActivity(), SalesPromotionActivity.class, bundle);
                            }
                        });
                        break;
                    default:
                        break;
                }


            }
        } else {
            headerView.findViewById(R.id.quick_entry_layout).setVisibility(View.GONE);
        }

    }

    @Override
    @OnClick({R.id.current_city_text, R.id.tv_title, R.id.image_right, R.id.top_btn})

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_city_text:
                IntentUtil.startActivity(getActivity(), CityListActivity.class);
                StatAgent.initAction(getActivity(), "", "2", "2", "", "", mCurCityText.getText().toString(), "1", "");
                break;
            case R.id.tv_title:
                IntentUtil.startActivity(getActivity(), SearchActivity.class);
                StatAgent.initAction(getActivity(), "", "2", "2", "", "", keyTextView.getText().toString(), "1", "");
                break;
            case R.id.image_right:
                if (!PushApplication.getInstance().isLogin()) {
                    UIHelper.showLoginActivity(getActivity());
                    return;
                }
                IntentUtil.startActivity(getActivity(), PushMsgListActivity.class);
                StatAgent.initAction(getActivity(), "", "2", "2", "", "", msgTextView.getText().toString(), "1", "");
                break;
            case R.id.top_btn:
                mRecyclerView.scrollToPosition(0);
                toTopBtn.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void doAnim(int position) {
        int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        if (position - firstItemPosition >= 0) {
            //得到要更新的item的view
            View view = mRecyclerView.getChildAt(position - firstItemPosition + 1);
            if (null != mRecyclerView.getChildViewHolder(view)) {
                ProductsViewHolder viewHolder = (ProductsViewHolder) mRecyclerView.getChildViewHolder(view);
                int[] start_location = new int[2];
                viewHolder.productImage.getLocationInWindow(start_location);//获取点击商品图片的位置
                Drawable drawable = viewHolder.productImage.getDrawable();//复制一个新的商品图标
                if (!isClean) {
                    setAnim(position, drawable, start_location);
                } else {
                    try {
                        animation_viewGroup.removeAllViews();
                        isClean = false;
                        setAnim(position, drawable, start_location);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        isClean = true;
                    }
                }
            }


        }


    }

    /**
     * @param
     * @return void
     * @throws
     * @Description: 创建动画层
     */
    private FrameLayout createAnimLayout() {
        ViewGroup rootView = (ViewGroup) getActivity().getWindow().getDecorView();
        FrameLayout animLayout = new FrameLayout(getActivity());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    /**
     * @param vg       动画运行的层 这里是frameLayout
     * @param view     要运行动画的View
     * @param location 动画的起始位置
     * @return
     * @deprecated 将要执行动画的view 添加到动画层
     */
    private View addViewToAnimLayout(ViewGroup vg, View view, int[] location) {
        int x = location[0];
        int y = location[1];
        vg.addView(view);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                Util.dip2px(getActivity(), 90), Util.dip2px(getActivity(), 90));
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setPadding(5, 5, 5, 5);
        view.setLayoutParams(lp);

        return view;
    }

    /**
     * 动画效果设置
     *
     * @param drawable       将要加入购物车的商品
     * @param start_location 起始位置
     */
    private void setAnim(int position, Drawable drawable, int[] start_location) {
        Animation mScaleAnimation = new ScaleAnimation(1.5f, 0.0f, 1.5f, 0.0f, Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.1f);
        mScaleAnimation.setDuration(AnimationDuration);
        mScaleAnimation.setFillAfter(true);

        int[] end_location = new int[2];
        cart_btn.getLocationInWindow(end_location);
        int endX = end_location[0] + cart_btn.getWidth() / 4;
        if (position % 2 == 1) {
            endX = end_location[0] + cart_btn.getWidth() / 2;
        }

        int endY = end_location[1] - start_location[1];

        final ImageView iview = new ImageView(getActivity());
        iview.setImageDrawable(drawable);
        final View view = addViewToAnimLayout(animation_viewGroup, iview, start_location);
        view.setAlpha(0.6f);

        Animation mTranslateAnimation = new TranslateAnimation(0, endX, 0, endY);
        if (position % 2 == 1) {
            mTranslateAnimation = new TranslateAnimation(0, endX - start_location[0], 0, endY);
        }
        Animation mRotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        if (position % 2 == 1) {
            mRotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }

        mRotateAnimation.setDuration(AnimationDuration);
        mTranslateAnimation.setDuration(AnimationDuration);
        AnimationSet mAnimationSet = new AnimationSet(true);

        mAnimationSet.setFillAfter(true);
        mAnimationSet.addAnimation(mRotateAnimation);
        mAnimationSet.addAnimation(mScaleAnimation);
        mAnimationSet.addAnimation(mTranslateAnimation);

        mAnimationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                number++;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                number--;
                if (number == 0) {
                    isClean = true;
                    mHandler.sendEmptyMessage(MSG_CLEAR);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

        });
        view.startAnimation(mAnimationSet);

    }

    /**
     * 内存过低时及时处理动画产生的未处理冗余
     */
    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        isClean = true;
        try {
            animation_viewGroup.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
        isClean = false;
        super.onLowMemory();
    }

    private void requestData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!mSwipeRefreshLayout.isRefreshing()) {//取消刷新
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                mSwipeRefreshLayout.setRefreshing(false);
                mHandler.sendEmptyMessage(GET_LIST_DATA_FAILURE);
                Log.e(TAG, "获取数据异常 ", arg3);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                mSwipeRefreshLayout.setRefreshing(false);
                String json = new String(response);
                Log.d(TAG, "onSuccess " + json);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            mHandler.obtainMessage(UPDATE_LIST_DATA, json).sendToTarget();
                            break;
                        case 1002:
                            if (mGridAdapter.getItemCount() == 0) {
                                mHandler.sendEmptyMessage(NO_LIST_DATA);
                            } else {
                                mHandler.sendEmptyMessage(NO_MORE_LIST_DATA);
                            }

                            break;
                        case 2001:
                            mHandler.sendEmptyMessage(GET_LIST_DATA_FAILURE);
                            break;
                        default:
                            break;
                    }

                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (mSwipeRefreshLayout.isRefreshing()) {//取消刷新
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

    }


    private List<Activities> activitiesList = new ArrayList();//一区活动
    private List<Activities> recommendList = new ArrayList();//推荐活动
    private List<Activities> specialityList = new ArrayList();//特产活动
    private List<Activities> quickList = new ArrayList();//快捷入口

    private List<Product> getListByJosn(String json) {
        List<Product> list = new ArrayList<>();
        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONObject resultObject = jsonObject.getJSONObject("result");
            totalPage = resultObject.getInt("page_count");
            JSONObject listObject = resultObject.getJSONObject("list");
            if (mCurPageIndex == 1) {

                if ((listObject.isNull("type1") && mCurPageIndex == 1) || TextUtils.isEmpty(listObject.getString("type1"))) {
                    getActivity().findViewById(R.id.activities_one).setVisibility(View.GONE);
                } else {
                    JSONArray type1Array = listObject.getJSONArray("type1");
                    for (int i = 0; i < type1Array.length(); i++) {
                        JSONObject object = type1Array.getJSONObject(i);
                        Activities activities = new Activities();
                        activities.clickUrl = object.getString("url");
                        activities.imgUrl = object.getString("image");
                        activities.title = object.getString("title");
                        activities.description = object.getString("desc");
                        activitiesList.add(activities);
                    }
                    initOneActivitiesView();
                }

                if ((listObject.isNull("type2") && mCurPageIndex == 1) || TextUtils.isEmpty(listObject.getString("type2"))) {
                    //getActivity().findViewById(R.id.recommend_view).setVisibility(View.GONE);
                    //getActivity().findViewById(R.id.recommend_view_line).setVisibility(View.GONE);
                } else {
                    JSONArray type2Array = listObject.getJSONArray("type2");
                    for (int i = 0; i < type2Array.length(); i++) {
                        JSONObject object = type2Array.getJSONObject(i);
                        Activities activities = new Activities();
                        activities.clickUrl = object.getString("url");
                        activities.imgUrl = object.getString("image");
                        activities.title = object.getString("title");
                        activities.description = object.getString("desc");
                        recommendList.add(activities);
                    }
                    initRecommendActivitiesView();
                }

                if ((listObject.isNull("type3") && mCurPageIndex == 1) || TextUtils.isEmpty(listObject.getString("type3"))) {
                    getActivity().findViewById(R.id.specialty_view).setVisibility(View.GONE);
                } else {
                    JSONArray type3Array = listObject.getJSONArray("type3");
                    for (int i = 0; i < type3Array.length(); i++) {
                        JSONObject object = type3Array.getJSONObject(i);
                        Activities activities = new Activities();
                        activities.id = object.getInt("id");
                        activities.imgUrl = object.getString("image");
                        activities.title = object.getString("title");
                        activities.description = object.getString("desc");
                        specialityList.add(activities);
                    }
                    initSpecialityActivitiesView();
                }

                if ((listObject.isNull("type5") && mCurPageIndex == 1) || TextUtils.isEmpty(listObject.getString("type5"))) {
                    //getActivity().findViewById(R.id.specialty_view).setVisibility(View.GONE);
                } else {
                    JSONArray type5Array = listObject.getJSONArray("type5");
                    for (int i = 0; i < type5Array.length(); i++) {
                        JSONObject object = type5Array.getJSONObject(i);
                        Activities activities = new Activities();
                        activities.imgUrl = object.getString("imgurl");
                        activities.title = object.getString("uname");
                        activities.clickUrl = object.getString("url");
                        quickList.add(activities);
                    }
                    initQuickEntryView();
                }
            }

            JSONArray productsArray = listObject.getJSONArray("type4");
            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject object = productsArray.getJSONObject(i);
                Product product = new Product();
                product.id = object.getInt("goods_id");
                product.title = object.getString("goods_name");
                product.discountPrice = object.getDouble("goods_price");
                product.retailPrice = object.getDouble("goods_marketprice");
                product.thumbUrl = object.getString("goods_image");
                product.count = object.getInt("cart_num");
                product.shopCardId = object.getInt("cart_id");
                product.spec = object.getString("goods_guige");
                product.goods_storage = object.getInt("goods_storage");
//                product.boutique = object.getString("jingpin");
                list.add(product);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
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
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                mSwipeRefreshLayout.setRefreshing(false);
                String json = new String(response);
                Log.d(TAG, "json " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        HttpUtil.getShopCartTotal(getActivity());
                        // 添加购物车动画
                        doAnim(position);

                        product.shopCardId = JsonUtil.getShopCartId(json);

                        mGridAdapter.getDataList().set(position, product);
                        mGridAdapter.notifyItemChanged(position);
                        ShopCartFragment.isUpdate = true;
                    } else {
                        product.count--;
                        AppToast.showShortText(getActivity(), "添加购物车失败");
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
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                mSwipeRefreshLayout.setRefreshing(false);
                if (isAdded) {
                    product.count--;
                } else {
                    product.count++;
                }
                AppToast.showShortText(getActivity(), "修改购物车数量失败");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                mSwipeRefreshLayout.setRefreshing(false);
                String json = new String(response);
                Log.d(TAG, "json " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        if (isAdded) {
                            // 添加购物车动画
                            doAnim(position);
                        } else {
                            if (product.count == 0) {
                                int count = JsonUtil.getShopCartTotal(json);
                                PushApplication.getInstance().setCartTotal(count);
                                ((MainUIActivity) getActivity()).updateShopCartTotal();
                            }
                        }
                        mGridAdapter.getDataList().set(position, product);
                        mGridAdapter.notifyItemChanged(position);
                        ShopCartFragment.isUpdate = true;
                    } else {
                        if (isAdded) {
                            product.count--;
                        } else {
                            product.count++;
                        }
                        AppToast.showShortText(getActivity(), "修改购物车数量失败");
                    }

                }
            }

        });

    }

    private String getRequestUrl(boolean isStart) {
        if ((mGridAdapter.getItemCount() == 0) || isStart) {
            mCurPageIndex = 1;
        } else {
            ++mCurPageIndex;
        }
        String url = Constant.URL_HOME + "&page=" + mCurPageIndex
                + "&member_id=" + PushApplication.getInstance().getUserId()
                + "&city=" + PushApplication.getInstance().getProperty("user.city");
        //Log.d(TAG,"url " + url);
        return url;
    }
}
