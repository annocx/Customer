package com.haier.cabinet.customer.activity;

import org.apache.http.Header;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.HaierUser;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.UIHelper;
import com.haier.common.util.AppToast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.OnClick;

public class ModifyUserPwdActivity extends BaseActivity {

    private String TAG = "ModifyPasswordFragment";

    @Bind(R.id.next) Button mNextBtn;
    @Bind(R.id.psw_editor) EditText mPasswordText;
    @Bind(R.id.new_psw_editor) EditText mNewPasswordText;
    @Bind(R.id.confirm_psw_editor) EditText mRePasswordText;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_modify_password;
    }

    public void initView() {
        mTitleText = (TextView) findViewById(R.id.title_text);
        mTitleText.setText(R.string.reset_password);

        mBackBtn = (ImageView) findViewById(R.id.back_img);
        mBackBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void initData() {

    }

    @Override
    @OnClick({R.id.back_img,R.id.next})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.next:
                final String password = mPasswordText.getText().toString();
                final String newPassword = mNewPasswordText.getText().toString();
                final String rePassword = mRePasswordText.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    AppToast.showShortText(this, "旧密码不能为空");
                    return;
                }
                if (TextUtils.isEmpty(newPassword)) {
                    AppToast.showShortText(this, "新密码不能为空");
                    return;
                }
                if (TextUtils.isEmpty(rePassword)) {
                    AppToast.showShortText(this, "确认密码不能为空");
                    return;
                }
                if ((newPassword.length() < 6 || newPassword.length() > 16)
                        || (rePassword.length() < 6 || rePassword.length() > 16)) {
                    AppToast.showShortText(this, "新密码和确认密码长度必须在6-16位之间");
                    return;
                }

                if (!newPassword.equals(rePassword)) {
                    AppToast.showShortText(this, getText(R.string.psw_conflict));
                    return;
                }
                modifyPassword(password, newPassword);

                break;

            default:
                break;
        }

    }

    private void modifyPassword(String password, final String newPassword) {
        Log.d(TAG, "modifyPassword");
        String url = Constant.DOMAIN + "/user/updatePassword.json";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("oldPassword", password);
        params.put("password", newPassword);
        params.put("token", PushApplication.getInstance().getToken());
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                Log.d(TAG, "onSuccess statusCode = " + statusCode);
                String json = new String(response);
//				Log.d(TAG, "onSuccess json = " + json);
                if (200 == statusCode) {
                    if (200 == JsonUtil.getStateFromServer(json)) {
                        AppToast.makeShortToast(ModifyUserPwdActivity.this, getText(R.string.reset_password_success).toString());
                        HaierUser user = PushApplication.getInstance().getLoginUser();
                        if (null == user) {
                            return;
                        }

                        user.password = newPassword;
                        //user.authentication_state = state;
                        PushApplication.getInstance().saveUserInfo(user);
                        UIHelper.showLoginActivity(ModifyUserPwdActivity.this);
                        finish();
                    } else {
                        AppToast.makeToast(ModifyUserPwdActivity.this, "修改密码失败!");
                    }

                }
            }
        });
    }
}
