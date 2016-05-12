package com.haier.cabinet.customer.activity;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.OnClick;

public class ForgetUserPwdActivity extends BaseActivity implements
		View.OnClickListener {

	private static final String TAG = "ForgetUserPwdActivity";
	@Bind(R.id.get_verify_code)
	Button mGetVerifyCodeBtn;
	@Bind(R.id.next) Button mSubmitBtn;
	@Bind(R.id.account_editor) EditText mAccountText;
	@Bind(R.id.verify_code_editor) EditText mVerifyCodeText;
	@Bind(R.id.password_editor) EditText mPasswordText;
	@Bind(R.id.confirm_password_editor) EditText mConfirmPasswordText;
	@Bind(R.id.notice) TextView mNoticeText;

	private String checkCode;
	private Timer timer;// 计时器
	private int seconds = Constant.GET_VERIFY_CODE;

	@Override
	protected int getLayoutId() {
		return R.layout.activity_forget_password;
	}

	public void initView() {
		mTitleText.setText(R.string.forgot_psw_title);
		mBackBtn.setVisibility(View.VISIBLE);
	}

	public void initData() {
//		mGetVerifyCodeBtn.setOnClickListener(this);
//		mSubmitBtn.setOnClickListener(this);
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
//				Log.d(TAG, "onFailure json = " + errorResponse);
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
							checkCode = code;
							String tip = getResources().getString(
									R.string.receive_sms_verify, phoneNum);
							mNoticeText.setVisibility(View.VISIBLE);
							mNoticeText.setText(tip);
						}
					} else {
						AppToast.makeToast(ForgetUserPwdActivity.this,
								"获取验证码失败，请稍后重试!");
					}

				}
			}
		});
	}

	private void onReSetPassword(String phoneNum, String password,
			String verifyCode) {

		String url = Constant.DOMAIN + "/user/forgotPassword.json";
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("phone", phoneNum);
		params.put("password", password);
		params.put("randomCode", verifyCode);
		params.put("userType", Constant.USER_TYPE);
		params.put("gladEyeKey", Constant.GLADEYEKEY);
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {
				AppToast.showShortText(ForgetUserPwdActivity.this, "找回密码失败!");
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {

				String json = new String(response);
				Log.d(TAG, "onSuccess json = " + json);
				if (200 == statusCode) {
					switch (JsonUtil.getStateFromServer(json)) {
					case 200:
						AppToast.showShortText(ForgetUserPwdActivity.this, "找回密码成功!");
						IntentUtil.startActivity(ForgetUserPwdActivity.this,
								UserLoginActivity.class);
						ForgetUserPwdActivity.this.finish();
						break;
					case 702:
						AppToast.showShortText(ForgetUserPwdActivity.this, "该手机号未注册，请先注册");
						break;

					default:
						break;
					}

				}
			}
		});
	}

	@Override
	@OnClick({R.id.next,R.id.get_verify_code})
	public void onClick(View v) {
		String phoneNum = mAccountText.getText().toString();
		String verifyCode = mVerifyCodeText.getText().toString();
		String newPassword = mPasswordText.getText().toString();
		String rePassword = mConfirmPasswordText.getText().toString();
		switch (v.getId()) {
		case R.id.next:
			String inputCodeText = mVerifyCodeText.getText().toString();

			if (!inputCodeText.equals(checkCode)) {
				AppToast.showShortText(this, "验证码不正确");
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
			if ((newPassword.length()<6 || newPassword.length()>16)
					|| (rePassword.length()<6 || rePassword.length()>16)) {
				AppToast.showShortText(this, "新密码和确认密码长度必须在6-16位之间");
				return;
			}
			
			if (!newPassword.equals(rePassword)) {
				AppToast.showShortText(this, getText(R.string.psw_conflict));
				return;
			}
			
			onReSetPassword(phoneNum, newPassword, verifyCode);

			break;
		case R.id.get_verify_code:
			if (TextUtils.isEmpty(phoneNum)) {
				AppToast.showShortText(ForgetUserPwdActivity.this, "手机号不能为空！");
				return;
			} else {
				if (Utils.isMobileNO(phoneNum)) {
					onGetVerifyCode(phoneNum);
					mGetVerifyCodeBtn.setEnabled(false);
					timer = new Timer();
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							mHandler.obtainMessage(REMAING_SECONDS, seconds--, -1)
									.sendToTarget();
						}
					}, 0, 1000);
				} else {
					AppToast.showShortText(ForgetUserPwdActivity.this, "手机号无效，请重新输入!");
				}
				
			}

			break;

		default:
			break;
		}

	}

	private static final int REMAING_SECONDS = 1001;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REMAING_SECONDS:
				int remainSeconds = msg.arg1;
				if (remainSeconds == 0) {
					mGetVerifyCodeBtn.setEnabled(true);
					mGetVerifyCodeBtn.setText(R.string.get_verify_code);
					timer.cancel();
					seconds = Constant.GET_VERIFY_CODE;
				} else {
					String text = getResources().getString(
							R.string.resend_verify_code, remainSeconds);
					mGetVerifyCodeBtn.setText(text);
				}
				break;

			default:
				break;
			}
		}
	};
	private String getVerifyVodeByJson(String json) {
		Log.d(TAG, "getVerifyVodeByJson json = " + json);
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
