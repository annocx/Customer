package com.haier.cabinet.customer.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.SalesPromotionActivity;
import com.haier.cabinet.customer.activity.SettingsActivity;
import com.haier.cabinet.customer.activity.UserAddressListActivity;
import com.haier.cabinet.customer.activity.UserCouponsActivity;
import com.haier.cabinet.customer.activity.UserInfoActivity;
import com.haier.cabinet.customer.activity.UserOrderListActivity;
import com.haier.cabinet.customer.base.CommonWebActivity;
import com.haier.cabinet.customer.base.EmptyFragment;
import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.ShareUtil;
import com.haier.cabinet.customer.util.UIHelper;
import com.haier.cabinet.customer.view.AccountInfoView;
import com.haier.cabinet.customer.view.PullZoomScrollView;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.NetUtil;
import com.haier.common.view.BadgeView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;

public class UserCenterFragment extends EmptyFragment implements View.OnClickListener {
    private static final String TAG = "UserCenterFragment";
    @Bind(R.id.user_info_layout)
    View mUserInfoView;
    @Bind(R.id.user_coupons_layout)
    View mUserCouponsView;
    @Bind(R.id.user_points_layout)
    View mUserPointsView;
    @Bind(R.id.user_right_points_layout)
    View mUserPointsRightView;
    @Bind(R.id.invite_friends_layout)
    View mInviteFriendsView;
    @Bind(R.id.referral_code_layout)
    View mReferralCodeView;
    @Bind(R.id.settings_layout)
    View mSettingsView;
    @Bind(R.id.user_shopping_cart_layout)
    View mShoppingCartView;
    @Bind(R.id.user_refuse_to_use_layout)
    View mRefuseToUseView;//大件拒投
    @Bind(R.id.user_all_order)
    View allOrderLayout;
    @Bind(R.id.user_points_text)
    TextView mUserPointsText;
    @Bind(R.id.user_right_points_text)
    TextView mUserRightPointsText;
    @Bind(R.id.username_text)
    TextView mUserNameText;
    @Bind(R.id.username_top_text)
    TextView mUserNameTopText;
    @Bind(R.id.user_unpaid)
    TextView unPaidTextView;
    @Bind(R.id.user_paid)
    TextView paidTextView;
    @Bind(R.id.user_unreceived)
    TextView unReceivedTextView;
    @Bind(R.id.user_completed)
    TextView completedTextView;
    @Bind(R.id.user_login_layout)
    View mUserLoginView;//登陆后显示的view
    @Bind(R.id.pull_scrollView)
    PullZoomScrollView mPullZoomScrollview;
    @Bind(R.id.account_layout)
    AccountInfoView mAccountLayout;

    private BadgeView unPaidBadgeView, paidBadgeView, unReceivedBadgeView, completedBadgeView;
    private ShareUtil mShareUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_center, container, false);
        return view;
    }

    @Override
    public void initView(View view) {
        super.initView(view);

        mPullZoomScrollview.bindView(mAccountLayout);
        mPullZoomScrollview.setOnScrollChangeListener(new PullZoomScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(int scrollY) {
                mAccountLayout.onChange(scrollY, PushApplication.getInstance().isLogin());
            }
        });

        initBadgeView();
    }

    @Override
    public void initData() {
        mShareUtil = new ShareUtil();
        mShareUtil.init(getActivity(), "乐家，让生活更美好！",
                "乐家，让生活更美好！",
                "http://m.rrslj.com/h5/pages/shopstatic/down_load.html", ShareUtil.FRIEND_SHARE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PushApplication.getInstance().isLogin()) {
            mUserLoginView.setVisibility(View.VISIBLE);
            mUserPointsView.setVisibility(View.VISIBLE);

            if (mUserNameTopText.getVisibility() == View.VISIBLE) {
                mUserPointsRightView.setVisibility(View.VISIBLE);
            }

            mUserNameText.setText(PushApplication.getInstance().getProperty("user.mobile"));
            mUserNameTopText.setText(PushApplication.getInstance().getProperty("user.mobile"));
            requestUserPoints();
            requestOrderCount();
        } else {
            mUserNameText.setText(getString(R.string.login));
            mUserNameTopText.setText(getString(R.string.login));
            mUserPointsView.setVisibility(View.GONE);
            mUserPointsRightView.setVisibility(View.INVISIBLE);

            ArrayList<Integer> removeBadgeView = new ArrayList<>();
            removeBadgeView.add(0);
            removeBadgeView.add(0);
            removeBadgeView.add(0);
            removeBadgeView.add(0);
            showOrderCount(removeBadgeView);
        }

    }


    private void initBadgeView() {
        unPaidBadgeView = new BadgeView(getActivity());
        unPaidBadgeView.setBadgeMargin(0, 0, 20, 0);

        paidBadgeView = new BadgeView(getActivity());
        paidBadgeView.setBadgeMargin(0, 0, 20, 0);

        unReceivedBadgeView = new BadgeView(getActivity());
        unReceivedBadgeView.setBadgeMargin(0, 0, 20, 0);

        completedBadgeView = new BadgeView(getActivity());
        completedBadgeView.setBadgeMargin(0, 0, 20, 0);
    }

    public void onEventMainThread(UserChangedEvent event) {
        onResume();
    }

    @Override
    @OnClick({R.id.user_info_layout, R.id.user_coupons_layout,
            R.id.user_shopping_cart_layout, R.id.user_points_layout,
            R.id.user_refuse_to_use_layout, R.id.invite_friends_layout,
            R.id.referral_code_layout, R.id.settings_layout,
            R.id.username_top_text, R.id.user_login_layout,
            R.id.user_all_order, R.id.user_unpaid, R.id.user_paid, R.id.user_unreceived,
            R.id.user_completed,R.id.user_right_points_layout,R.id.account_layout})
    public void onClick(View v) {
        if (!PushApplication.getInstance().isLogin()) {
            UIHelper.showLoginActivity(getActivity());
            return;
        }
        Bundle bundle;
        switch (v.getId()) {
            case R.id.user_info_layout:
                IntentUtil.startActivity(getActivity(), UserInfoActivity.class);
                break;
            case R.id.user_coupons_layout:
                bundle = new Bundle();
                bundle.putInt(Constant.INTENT_KEY_FROM, 0);
                IntentUtil.startActivity(getActivity(), UserCouponsActivity.class, bundle);
                break;
            case R.id.user_shopping_cart_layout:
                IntentUtil.startActivity(getActivity(), UserAddressListActivity.class);
                break;
            case R.id.user_points_layout:
            case R.id.user_right_points_layout:
                bundle = new Bundle();
                bundle.putString("title", getResources().getString(R.string.points_explanation));
                bundle.putString("url", Constant.URL_POINTS_EXPLANATION);
                IntentUtil.startActivity(getActivity(), CommonWebActivity.class, bundle);
                break;
            case R.id.user_refuse_to_use_layout:
                String url = Constant.URL_REFUSE_TO_USE + "?userName=" + PushApplication.getInstance().getUserId();
                bundle = new Bundle();
                bundle.putString("title", getString(R.string.user_refuse_deliver));
                bundle.putString("url", url);
                IntentUtil.startActivity(getActivity(), CommonWebActivity.class, bundle);
                break;
            case R.id.invite_friends_layout:
                //mShareUtil.startShare();
                String urlInviteFriends = Constant.URL_INVITE_FRIENDS + "?userId=" + PushApplication.getInstance().getUserId();
                bundle = new Bundle();
                bundle.putString("url", urlInviteFriends);
                bundle.putBoolean("isNeedShare", true);
                IntentUtil.startActivity(getActivity(), SalesPromotionActivity.class, bundle);
                break;
            case R.id.referral_code_layout:
                String urlReferralCode = Constant.URL_INVITE_CODE+ "?userId=" + PushApplication.getInstance().getUserId();
                bundle = new Bundle();
                bundle.putString("title", getString(R.string.input_referral_code));
                bundle.putString("url", urlReferralCode);
                IntentUtil.startActivity(getActivity(), CommonWebActivity.class, bundle);
                break;
            case R.id.settings_layout:
                IntentUtil.startActivity(getActivity(), SettingsActivity.class);
                break;
            /*case R.id.user_coupon:
            AppToast.showShortText(getActivity(),"暂无可用优惠券，敬请期待！");
			break;
		case R.id.user_order_text:
			IntentUtil.startActivity(getActivity(), UserOrderListActivity.class);
			break;*/
            case R.id.username_top_text:
            case R.id.user_login_layout:
                if (!PushApplication.getInstance().isLogin()) {
                    UIHelper.showLoginActivity(getActivity());
                }
                break;
            case R.id.user_all_order:
                bundle = new Bundle();
                bundle.putInt("order_state", 0);
                IntentUtil.startActivity(getActivity(), UserOrderListActivity.class, bundle);
                break;
            case R.id.user_unpaid:
                bundle = new Bundle();
                bundle.putInt("order_state", 1);
                IntentUtil.startActivity(getActivity(), UserOrderListActivity.class, bundle);
                break;
            case R.id.user_paid:
                bundle = new Bundle();
                bundle.putInt("order_state", 2);
                IntentUtil.startActivity(getActivity(), UserOrderListActivity.class, bundle);
                break;
            case R.id.user_unreceived:
                bundle = new Bundle();
                bundle.putInt("order_state", 3);
                IntentUtil.startActivity(getActivity(), UserOrderListActivity.class, bundle);
                break;
            case R.id.user_completed:
                bundle = new Bundle();
                bundle.putInt("order_state", 4);
                IntentUtil.startActivity(getActivity(), UserOrderListActivity.class, bundle);
                break;
            case R.id.account_layout:
                //不做处理
                break;
            default:
                break;
        }
    }

    private void requestOrderCount() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_ORDER_COUNT, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                Log.e(TAG, "onFailure arg3 " + arg3);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                String json = new String(response);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromJdbServer(json)) {
                        case 1001:
                            ArrayList<Integer> counts = JsonUtil.getOrderCount(json);
                            showOrderCount(counts);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void requestUserPoints() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("userIsAdmin", Constant.USER_TYPE);
        params.put("token", PushApplication.getInstance().getToken());
        client.get(Constant.URL_USER_POINTS, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                Log.e(TAG, "onFailure arg3 " + arg3);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                String json = new String(response);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromServer(json)) {
                        case 200:
                            String userPoints = JsonUtil.getUserPoints(json);
                            mUserPointsText.setText(userPoints);
                            mUserRightPointsText.setText(userPoints);
                            break;
                        case 504:
                            PushApplication.getInstance().logoutHaiUser();
                            break;
                        default:
                            break;
                    }

                }
            }

        });

    }

    private void showOrderCount(ArrayList<Integer> counts) {
        unPaidBadgeView.setBadgeCount(counts.get(0));
        unPaidBadgeView.setTargetView(unPaidTextView);

        paidBadgeView.setBadgeCount(counts.get(1));
        paidBadgeView.setTargetView(paidTextView);

        unReceivedBadgeView.setBadgeCount(counts.get(2));
        unReceivedBadgeView.setTargetView(unReceivedTextView);

        completedBadgeView.setBadgeCount(counts.get(3));
        //completedBadgeView.setTargetView(completedTextView);
    }

}
