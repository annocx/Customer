package com.haier.cabinet.customer.activity;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.base.LocalWebActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.common.util.AppToast;
import com.haier.common.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;

public class UserRegisterActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "UserRegisterActivity";

    @Bind(R.id.get_verify_code)
    Button mGetVerifyCodeBtn;
    @Bind(R.id.next)
    Button mSubmitBtn;
    @Bind(R.id.protocol)
    TextView mProtocolText;
    @Bind(R.id.notice)
    TextView mNoticeText;
    @Bind(R.id.protocol_check)
    CheckBox mAgreeCheckbox;

    @Bind(R.id.account_editor)
    EditText mAccountText;
    @Bind(R.id.verify_code_editor)
    EditText mVerifyCodeText;
    @Bind(R.id.password_editor)
    EditText mPasswordText;
    @Bind(R.id.confirm_password_editor)
    EditText mConfirmPasswordText;

    private String checkCode;
    private Timer timer;// 计时器
    private int seconds = Constant.GET_VERIFY_CODE;

    long mGetVerifyCodeTime;
    long mNowTime;
    private boolean isAgreeProtocol;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_register;
    }

    public void initView() {
        mTitleText.setText(R.string.register);
        mBackBtn.setVisibility(View.VISIBLE);
    }

    public void initData() {
        isAgreeProtocol = mAgreeCheckbox.isChecked();
        mAgreeCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isAgreeProtocol = true;
                } else {
                    isAgreeProtocol = false;
                }
            }
        });
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void onGetVerifyCode(final String phoneNum) {
        Log.d(TAG, "onGetVerifyCode");
        String url = Constant.DOMAIN + "/user/getCode.json";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("phone", phoneNum);
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
//				Log.d(TAG, "onSuccess json = " + json);
                if (200 == statusCode) {
                    if (200 == JsonUtil.getStateFromServer(json)) {
                        String code = getVerifyVodeByJson(json);
                        Log.d(TAG, "code = " + code);
                        if (!TextUtils.isEmpty(code)) {
                            mGetVerifyCodeTime = System.currentTimeMillis();
                            checkCode = code;
                            String tip = getResources().getString(
                                    R.string.receive_sms_verify, phoneNum);
                            mNoticeText.setVisibility(View.VISIBLE);
                            mNoticeText.setText(tip);
                        }
                    } else {
                        AppToast.makeToast(UserRegisterActivity.this, "获取短信验证码失败，请稍后再试!");
                    }

                }
            }
        });
    }

    private void onRegister(final String phoneNum, final String password,
                            String verifyCode) {

        String url = Constant.DOMAIN + "/user/saveUser.json";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("phone", phoneNum);
        params.put("password", password);
        params.put("randomCode", verifyCode);
        params.put("gladEyeKey", Constant.GLADEYEKEY);
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                //Log.e(TAG, "onFailure " + new String(errorResponse));
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                Log.d(TAG, "onSuccess statusCode = " + statusCode);
                String json = new String(response);
//				Log.d(TAG, "onSuccess json = " + json);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromServer(json)) {
                        case 200:
                            Toast.makeText(UserRegisterActivity.this, "注册成功!",
                                    Toast.LENGTH_SHORT).show();
                            // 注册成功后自动登陆开关打开
                            HashMap<String, Object> map = new HashMap<String, Object>();
                            map.put("auto_login", true);
                            map.put("login_times", 0);
                            Utils.saveMsg(UserRegisterActivity.this,
                                    Constant.CABINET_FILE_NAME, map);

                            Intent intent = new Intent(UserRegisterActivity.this,
                                    UserLoginActivity.class);
                            intent.putExtra("username", phoneNum);
                            intent.putExtra("password", password);
                            startActivity(intent);
                            finish();
                            break;
                        case 701:
                            AppToast.showShortText(UserRegisterActivity.this, "该手机号已经被注册，请重新换一个！");
                            break;

                        default:
                            break;
                    }


                }
            }
        });
    }

    @Override
    @OnClick({R.id.protocol,R.id.next,R.id.get_verify_code})
    public void onClick(View v) {
        String phoneNum = mAccountText.getText().toString();
        String verifyCode = mVerifyCodeText.getText().toString();
        String password = mPasswordText.getText().toString();
        String confirmPassword = mConfirmPasswordText.getText().toString();
        switch (v.getId()) {
            case R.id.protocol:
                Intent intent = new Intent(this, LocalWebActivity.class);
                intent.putExtra("title", getResources().getString(R.string.user_register_protocol));
                intent.putExtra("url", Constant.URL_PRIVACY_POLICY);
                startActivity(intent);
                break;
            case R.id.next:

                if (TextUtils.isEmpty(phoneNum)) {
                    AppToast.makeToast(UserRegisterActivity.this, "手机号码不能为空");
                    return;
                }

                if (!verifyCode.equals(checkCode)) {
                    AppToast.makeToast(UserRegisterActivity.this, "短信验证码不正确");
                    return;
                }

                mNowTime = System.currentTimeMillis();
                if ((mNowTime - mGetVerifyCodeTime) > Constant.PERIOD_VERIFY_CODE) {
                    checkCode = null;
                    AppToast.showShortText(UserRegisterActivity.this, "短信验证码已经失效,请重新获取");
                    return;
                }

                if (TextUtils.isEmpty(password)
                        || TextUtils.isEmpty(confirmPassword)) {
                    AppToast.showShortText(UserRegisterActivity.this, "密码和验证密码不能为空");
                    return;
                }

                if ((password.length() < 6 || password.length() > 16)
                        || (confirmPassword.length() < 6 || confirmPassword.length() > 16)) {
                    AppToast.showShortText(UserRegisterActivity.this, "密码和验证密码长度必须在6-16位之间");
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    AppToast.showShortText(UserRegisterActivity.this, getText(R.string.psw_conflict));
                    return;
                }

                if (isAgreeProtocol) {
                    onRegister(phoneNum, password, verifyCode);
                } else {
                    AppToast.showShortText(UserRegisterActivity.this, "请同意我们的使用条款和隐私政策");
                }


                break;
            case R.id.get_verify_code:
                if (TextUtils.isEmpty(phoneNum)) {
                    AppToast.showShortText(UserRegisterActivity.this, "手机号不能为空");
                    return;
                } else {
                    if (Utils.isMobileNO(phoneNum)) {
                        onGetVerifyCode(phoneNum);
                        mGetVerifyCodeBtn.setEnabled(false);
                        timer = new Timer();
                        timer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                handler.sendEmptyMessage(seconds--);
                            }
                        }, 0, 1000);
                    } else {
                        AppToast.showShortText(UserRegisterActivity.this, "手机号无效，请重新输入");
                    }

                }

                break;

            default:
                break;
        }

    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                mGetVerifyCodeBtn.setEnabled(true);
                mGetVerifyCodeBtn.setText(R.string.get_verify_code);
                timer.cancel();
                seconds = Constant.GET_VERIFY_CODE;
            } else {
                String text = getResources().getString(
                        R.string.resend_verify_code, msg.what);
                mGetVerifyCodeBtn.setText(text);
            }
        }

        ;
    };

    @Override
    public void onDestroy() {
        if (timer != null)
            timer.cancel();
        super.onDestroy();
    }

    private String getVerifyVodeByJson(String json) {
//		Log.d(TAG, "getVerifyVodeByJson json = " + json);
        try {
            JSONObject object = new JSONObject(json);
            String code = object.getString("data");
            return code;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
