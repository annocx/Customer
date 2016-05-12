package com.haier.cabinet.customer.fragment;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.FragmentDetailsActivity;
import com.haier.cabinet.customer.activity.adapter.PackageBoxListAdapter;
import com.haier.cabinet.customer.base.BaseListFragment;
import com.haier.cabinet.customer.entity.PackageBox;
import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.UIHelper;
import com.haier.cabinet.customer.view.HeaderLayout;
import com.haier.cabinet.customer.view.LifeSlideShowView;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewUtils;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.view.BadgeView;
import com.haier.qr.code.CaptureCodeActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class LifeFragment extends BaseListFragment implements View.OnTouchListener {

    private static final String TAG = "LifeFragment";

    private PackageBoxListAdapter mListAdapter;

    private static int totalRecord = 0;
    private static int totalPage = 0;
    private int mCurPageIndex = 1;
    private static final int pageSize = 10;
    private LifeSlideShowView slideShowView;
    public static boolean isUpdate = false;

    View loginout_view;//未登录view
    @Bind(R.id.tv_login)
    TextView tv_login;

    TextView scanView;
    TextView mailView;

    private BadgeView badgeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_life, container, false);
        return view;
    }

    public void initView(View view) {
        super.initView(view);
        StatAgent.initAction(getActivity(), "", "1", "16", "", "", "", "1", "");
        mListAdapter = new PackageBoxListAdapter(getActivity());
        mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mListAdapter);
        mRecyclerView.setAdapter(mHeaderAndFooterRecyclerViewAdapter);
        mOnScrollListener.setSwipeRefreshLayout(mSwipeRefreshLayout);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        emptyView = (View) view.findViewById(R.id.empty_view);
        HeaderLayout headerView = new HeaderLayout(getActivity(), R.layout.layout_life_header_view);
        RecyclerViewUtils.setHeaderView(mRecyclerView, headerView);

        loginout_view = view.findViewById(R.id.loginout_view);

        scanView = (TextView) headerView.findViewById(R.id.scan_button);
        scanView.setOnClickListener(this);
        headerView.findViewById(R.id.mail_button).setOnClickListener(this);

        slideShowView = (LifeSlideShowView) headerView.findViewById(R.id.slideshowView);

        badgeView = new BadgeView(getActivity());
        if (Build.VERSION.SDK_INT >= 21) {
            badgeView.setBadgeMargin(0, 5, 65, 0);
        } else {
            badgeView.setBadgeMargin(0, 5, 60, 0);
        }
        badgeView.setTargetView(scanView);

        //emptyView.setOnTouchListener(this);

        isUpdate = true;
    }

    @Override
    protected void initLayoutManager() {
        super.initLayoutManager();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private RecyclerOnScrollListener mOnScrollListener = new RecyclerOnScrollListener() {

        public void onBottom() {

            LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(mRecyclerView);
            if (state == LoadingFooter.State.Loading) {
                return;
            }

            if (mCurPageIndex < totalPage) {
                // loading more
                RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, pageSize, LoadingFooter.State.Loading, null);
                mIsStart = false;
                mHandler.sendEmptyMessage(GET_PACKAGE_LIST_DATA);
            } else {
                //the end
                RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, pageSize, LoadingFooter.State.TheEnd, null);
            }
        }
    };

    private boolean isRequestInProcess = false;

    @Override
    public void onResume() {
        super.onResume();
        if (PushApplication.getInstance().isLogin()) {
            loginout_view.setVisibility(View.GONE);
            if (isUpdate && !isRequestInProcess) {
                onRefresh();
            }
        } else {
            mListAdapter.clear();
            mListAdapter.notifyDataSetChanged();
            badgeView.setBadgeCount(0);
            loginout_view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mSwipeRefreshLayout && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void onEventMainThread(UserChangedEvent event) {
        isUpdate = false;
        mIsStart = true;
        // 强制刷新
        mHandler.sendEmptyMessage(GET_PACKAGE_LIST_DATA);

    }

    private static final int GET_PACKAGE_LIST_DATA = 1001;
    private static final int UPDATE_PACKAGE_LIST = 1002;
    private static final int NO_PACKAGE_LIST_DATA = 1003;
    private static final int USER_TOKEN_TIMEOUT = 1004;
    private static final int GET_PACKAGE_LIST_DATA_FAILURE = 1005;
    private static final int NO_MORE_PACKAGE_LIST_DATA = 1006;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case GET_PACKAGE_LIST_DATA:
                    String url = getRequestUrl(mIsStart);
                    requestPackageData(url);
                    isRequestInProcess = true;
                    break;
                case UPDATE_PACKAGE_LIST:
                    String json = (String) msg.obj;
                    ArrayList<PackageBox> data = getPackageListByJosn(json);

                    if (null == data) {
                        return;
                    }

                    if (mIsStart) {
                        //清空数据
                        if (mListAdapter.getItemCount() > 0) {
                            mListAdapter.clear();
                        }
                        mListAdapter.addAll(data);
                    } else {
                        //if ()
                        RecyclerViewStateUtils.setFooterViewState(mRecyclerView, LoadingFooter.State.Normal);
                        mListAdapter.addAll(data);
                    }
                    mListAdapter.notifyDataSetChanged();

                    if ((mCurPageIndex == totalPage) && mIsStart) {
                        RecyclerViewStateUtils.setFooterViewState2(getActivity(), mRecyclerView, pageSize, LoadingFooter.State.TheEnd, null);
                    }
                    loginout_view.setVisibility(View.GONE);

                    badgeView.setBadgeCount(unPickListSize);

                    int num = 0;
                    if (unPickListSize == 0) {
                        if (totalRecord > 0) {
                            num = totalRecord + 1;
                        }
                    } else {
                        if (totalRecord == 0) {
                            num = totalRecord + unPickListSize + 1;
                        } else {
                            num = totalRecord + unPickListSize + 2;
                        }
                    }

                    if (isInLayout()) {
                        if (num == 0) {
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                        }
                    }
                    isUpdate = false;
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case NO_MORE_PACKAGE_LIST_DATA:
                    mListAdapter.notifyDataSetChanged();
                    isUpdate = false;
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case GET_PACKAGE_LIST_DATA_FAILURE:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, pageSize, LoadingFooter.State.NetWorkError, mFooterClick);
                    mListAdapter.notifyDataSetChanged();
                    isUpdate = false;
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case NO_PACKAGE_LIST_DATA:
                    //清空数据
                    if (mListAdapter.getItemCount() > 0) {
                        mListAdapter.clear();
                    }
                    mListAdapter.notifyDataSetChanged();

                    mRecyclerView.setVisibility(View.VISIBLE);
                    isUpdate = false;
                    mIsStart = false;
                    isRequestInProcess = false;
                    break;
                case USER_TOKEN_TIMEOUT:
                    //token失效跳转到登陆界面
                    PushApplication.getInstance().logoutHaiUser();
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnClickListener mFooterClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecyclerView, pageSize, LoadingFooter.State.Loading, null);
            mHandler.sendEmptyMessage(GET_PACKAGE_LIST_DATA);
        }
    };

    @Override
    @OnClick({R.id.tv_login})
    public void onClick(View v) {
        Bundle bundle;
        switch (v.getId()) {
            case R.id.mail_button:
                bundle = new Bundle();
                bundle.putInt(Constant.FRAGMENT_DETAILS, Constant.FRAGMENT_MAIL);
                IntentUtil.startActivity(getActivity(), FragmentDetailsActivity.class, bundle);
                break;
            case R.id.scan_button:
                if (PushApplication.getInstance().isLogin()) {
                    if (unPickListSize > 0) {
                        bundle = new Bundle();
                        bundle.putInt("total", unPickListSize);
                        bundle.putString("terminalNo", "");
                        bundle.putSerializable("unPickList", (Serializable) unPickList);
                        IntentUtil.startActivity(getActivity(), CaptureCodeActivity.class, bundle);
                    } else {
                        AppToast.showShortText(getActivity(), "亲，暂时没有您的未取快件哦");
                    }
                } else {
                    UIHelper.showLoginActivity(getActivity());
                }
                break;
            case R.id.tv_login:
                UIHelper.showLoginActivity(getActivity());
                break;
            default:
                break;
        }
    }

    private void requestPackageData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (mIsStart) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }

            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                Log.e(TAG, "获取数据异常 ", arg3);
                mHandler.sendEmptyMessage(GET_PACKAGE_LIST_DATA_FAILURE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String json = new String(response);
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromServer(json)) {
                        case 200:
                            mHandler.obtainMessage(UPDATE_PACKAGE_LIST, json).sendToTarget();
                            break;
                        case 201:
                            if (mListAdapter.getItemCount() == 0) {
                                mHandler.sendEmptyMessage(NO_PACKAGE_LIST_DATA);
                            } else {
                                mHandler.sendEmptyMessage(NO_MORE_PACKAGE_LIST_DATA);
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

    private int unPickListSize = 0;
    private List<PackageBox> unPickList = new ArrayList<>();

    private ArrayList<PackageBox> getPackageListByJosn(String json) {
        ArrayList<PackageBox> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject unPickListObject = (JSONObject) jsonObject.get("unPickOrderList");
            if (unPickListObject.isNull("data")) {
                if (mCurPageIndex == 1) {
                    unPickListSize = 0;
                    unPickList.clear();
                }
                Log.d(TAG, "unPickOrderList is null");
            } else {
                JSONArray boxesArray = unPickListObject.getJSONArray("data");
                unPickListSize = boxesArray.length();
                unPickList.clear();

                if (unPickListSize > 0) {
                    PackageBox box = new PackageBox();
                    box.content = "待取件 " + unPickListSize;
                    box.type = PackageBox.SECTION;
                    list.add(box);

                    for (int i = 0; i < boxesArray.length(); i++) {
                        JSONObject boxObject = boxesArray.getJSONObject(i);
                        box = new PackageBox();
                        box.cabinetNo = boxObject.getString("guiziNo");
                        box.cabinetName = boxObject.getString("guiziName");
                        box.cabinetAddress = boxObject.getString("location");
                        box.pickUpNo = boxObject.getString("openBoxKey");
                        box.packageNo = boxObject.getString("orderNo");
                        box.postmanMobile = boxObject.getString("courierPhone");
                        box.deliveredTime = boxObject.getString("storeTime");
                        box.pickTime = boxObject.getString("pickTime");
                        box.overdueTime = boxObject.getString("endTime");
                        box.tradeWaterNo = boxObject.getString("tradeWaterNo");
                        box.expressCompany = boxObject.getString("companyName");
                        box.boxNo = boxObject.getInt("boxNo");
                        box.corpType = boxObject.getString("corpType");
                        box.isTimeout = boxObject.getInt("isOverTime") == 1;
                        box.remainTime = boxObject.getString("isStoredTime");
                        box.type = PackageBox.ITEM;
                        box.packageStatus = boxObject.getInt("status");
                        list.add(box);
                        unPickList.add(box);
                    }
                }

            }
            mListAdapter.setUnPickListSize(unPickListSize);
            badgeView.setBadgeCount(unPickListSize);
            JSONObject pickedListObject = (JSONObject) jsonObject.get("pickedOrderList");
            if (pickedListObject.isNull("data")) {
                Log.d(TAG, "pickedOrderList is null");
            } else {
                JSONArray boxesArray = pickedListObject.getJSONArray("data");
                PackageBox box = null;

                if (mCurPageIndex == 1/* && totalRecord == 0*/) {
                    totalRecord = pickedListObject.getInt("size");
                    totalPage = totalRecord % 10 == 0 ? totalRecord / 10 : totalRecord / 10 + 1;
                    box = new PackageBox();
                    //box.content = "取件记录（" + totalRecord + "件）";
                    box.content = "取件记录 " + totalRecord;
                    box.type = PackageBox.SECTION;
                    list.add(box);
                }
                Log.d(TAG, "totalRecord " + totalRecord);
                Log.d(TAG, "totalPage " + totalPage);
                for (int i = 0; i < boxesArray.length(); i++) {
                    JSONObject boxObject = boxesArray.getJSONObject(i);
                    box = new PackageBox();
                    box.cabinetNo = boxObject.getString("guiziNo");
                    box.cabinetName = boxObject.getString("guiziName");
                    box.cabinetAddress = boxObject.getString("location");
                    box.pickUpNo = boxObject.getString("openBoxKey");
                    box.packageNo = boxObject.getString("orderNo");
                    box.postmanMobile = boxObject.getString("courierPhone");
                    box.deliveredTime = boxObject.getString("storeTime");
                    box.pickTime = boxObject.getString("pickTime");
                    box.overdueTime = boxObject.getString("endTime");
                    box.tradeWaterNo = boxObject.getString("tradeWaterNo");
                    box.expressCompany = boxObject.getString("companyName");
                    box.boxNo = boxObject.getInt("boxNo");
                    box.corpType = boxObject.getString("corpType");
                    box.isTimeout = boxObject.getInt("isOverTime") == 1;
                    box.remainTime = boxObject.getString("isStoredTime");
                    box.type = PackageBox.ITEM;
                    box.packageStatus = boxObject.getInt("status");
                    list.add(box);
                }
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
        String url = Constant.DOMAIN
                + "/order/customerMixtureOrderList.json?"
                + "start=" + (mCurPageIndex - 1) * pageSize
                + "&pageSize=" + pageSize
                + "&token=" + PushApplication.getInstance().getToken();
        return url;
    }

    @Override
    public void onRefresh() {
        if (!mIsStart) {//防止多次下拉
            Log.d(TAG, "onPullDownToRefresh");
            mIsStart = true;
            emptyView.setVisibility(View.GONE);
            slideShowView.refreshView();

            mHandler.sendEmptyMessage(GET_PACKAGE_LIST_DATA);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mListAdapter.getItemCount() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mHandler.sendEmptyMessage(GET_PACKAGE_LIST_DATA);
            }
        }
        return false;
    }
}
