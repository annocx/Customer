package com.haier.cabinet.customer.activity;

import org.apache.http.Header;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.base.CommonWebActivity;
import com.haier.cabinet.customer.entity.AddressInfo;
import com.haier.cabinet.customer.entity.HaierUser;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sunday.statagent.StatAgent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import kankan.wheel.widget.adapters.Area;
import kankan.wheel.widget.adapters.City;
import kankan.wheel.widget.adapters.Province;

public class UserInfoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "UserInfoActivity";
    @Bind(R.id.user_name_text)
    TextView mUserNameText;//用户姓名
    @Bind(R.id.user_mobile_text)
    TextView mUserMobileText;//用户手机号
    @Bind(R.id.user_hometown_layout)
    View mUserHometown;
    @Bind(R.id.user_hometown_text)
    TextView mUserHometownText;//用户家乡
    @Bind(R.id.right_bar_layout)
    View mRightBarView;
    @Bind(R.id.user_name_layout)
    View mUserNameView;
    @Bind(R.id.user_address_layout)
    View mUserAddressView;
    @Bind(R.id.user_identity_authentication_layout)
    View mdentityAuthenticationView;//身份认证
    @Bind(R.id.user_identity_authentication_text)
    TextView mAuthenticationText;//身份认证状态

    private AddressInfo myAddress;
    private AddressInfo addressinfo;

    private HaierUser mUser;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_info;
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestIdentificationState();
        getUserInfo();
    }

    public void initView() {
        StatAgent.initAction(this, "", "1", "28", "", "", "", "1", "");
        mTitleText.setText("个人资料");
        mBackBtn.setVisibility(View.VISIBLE);
        myAddress = new AddressInfo();
    }

    public void initData() {
//        addressinfo = (AddressInfo) getIntent().getExtras().get("address");
//        if (addressinfo != null) {// 修改地址信息
//            mUserHometownText.setText(addressinfo.getCurCity());
//        }
        mRightBarView.setEnabled(false);
        mUser = PushApplication.getInstance().getLoginUser();
    }

    private void updateData() {

        mUserMobileText.setText(mUser.mobile);
        if (mUser.name.equals("null")) {
            mUser.name = "";
        } else if (mUser.name.length() > 8) {
            String name = mUser.name.substring(0, 8);
            mUserNameText.setText(name + "...");
        } else {
            mUserNameText.setText(mUser.name);
        }
    }
    private final int REQUEST_COMMUNITY = 1000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_COMMUNITY:
                if (resultCode == RESULT_OK) {
                    myAddress.provincialCityArea = "";//需置空，否则修改时一直有值导致getCurCity使用原值
                    Bundle bundle = data.getExtras();
                    myAddress.province = (Province) bundle.getSerializable("province");
                    myAddress.city = (City) bundle.getSerializable("city");
                    myAddress.area = (Area) bundle.getSerializable("area");
                    if (addressinfo != null) {// 修改地址信息
                        mUserHometownText.setText(myAddress.getCurCity());
                    } else {
                        mUserHometownText.setText(myAddress.getCurCity());
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    @OnClick({R.id.back_img, R.id.user_name_layout,R.id.user_hometown_layout, R.id.user_address_layout, R.id.user_identity_authentication_layout})
    public void onClick(View v) {
        Bundle bundle;
        switch (v.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.user_name_layout:
                bundle = new Bundle();
                bundle.putString("nickname", mUser.name);
                IntentUtil.startActivity(this, ModifyUserNickNameActivity.class, bundle);
                break;
            case R.id.user_address_layout:
                IntentUtil.startActivity(this, UserAddressListActivity.class);
                break;
            case R.id.user_hometown_layout://家乡
                Intent i = new Intent(UserInfoActivity.this, CitiesSetActivity.class);
                startActivityForResult(i, REQUEST_COMMUNITY);
                break;
            case R.id.user_identity_authentication_layout:
                String url = Constant.URL_IDENTITY_AUTHENTICATION + "?userName=" + mUser.mobile;
                bundle = new Bundle();
                bundle.putString("title", "身份认证");
                bundle.putString("url", url);
                IntentUtil.startActivity(this, CommonWebActivity.class, bundle);

                break;
            default:
                break;
        }
    }


    private void getUserInfo() {
        String url = Constant.DOMAIN + "/user/checkMyDetail.json";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("token", PushApplication.getInstance().getToken());
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                if (null != errorResponse) {
                    Log.e(TAG, "onFailure " + new String(errorResponse));
                    AppToast.showShortText(UserInfoActivity.this, "获取个人资料失败");
                }

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
//				Log.d(TAG, "getUserInfo onSuccess json = " + json);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromServer(json)) {
                        case 200:
                            Map<String, String> userInfo = JsonUtil.getUserInfo(json);
                            mUser.name = userInfo.get("nickName");
                            updateData();
                            break;

                        default:
                            break;
                    }


                }
            }
        });
    }


    private void requestIdentificationState() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("userName", PushApplication.getInstance().getUserId());
        params.put("gladEyeKey", Constant.GLADEYEKEY);
        client.get(Constant.URL_GET_IDENTITY_AUTHENTICATION_STATE, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                Log.e(TAG, "onFailure arg3 " + arg3);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {

                String json = new String(response);
//				Log.d(TAG, "onSuccess json " + json);
                if (200 == statusCode) {
                    if (200 == JsonUtil.getStateFromServer(json)) {
                        int state = JsonUtil.getIdentificationState(json);
                        String text = "未认证";
                        switch (state) {
                            case 0:
                                text = "认证中";
                                break;
                            case 1:
                                text = "已认证";
                                break;
                            case 2:
                                text = "认证失败";
                                break;
                            case 3:
                                text = "未认证";
                                break;
                            default:
                                break;
                        }
                        mAuthenticationText.setText(text);
                    }

                }
            }

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StatAgent.initAction(this, "", "2", "28", "", "", "back", "2", "");
    }
}
