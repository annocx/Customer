package com.haier.cabinet.customer.activity;

import org.apache.http.Header;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.api.HaierApi;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.HaierUser;
import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.cabinet.customer.ui.MainUIActivity;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.StringUtils;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sunday.statagent.StatAgent;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class UserLoginActivity extends BaseActivity implements View.OnClickListener {

	protected String TAG = "UserLoginActivity";

	@Bind(R.id.register) TextView mRegisterText;
	@Bind(R.id.forgot_psw) TextView mForgetPasswordText;
	@Bind(R.id.login) Button mLoginBtn;

	@Bind(R.id.account_editor) TextInputLayout accountText;
	@Bind(R.id.psw_editor) TextInputLayout passwordText;

	@Override
	protected int getLayoutId() {
		return R.layout.activity_user_login;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		hideKeyboard(accountText.getEditText());
	}

	public void initView() {
		mTitleText.setText(R.string.login);
		mBackBtn.setVisibility(View.VISIBLE);

		accountText.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 11) passwordText.requestFocus();
			}
		});

	}

	public void initData() {

		//自动登录
		if(StringUtils.toBool(PushApplication.getInstance().getProperty("user.autoLogin"))){
			autologin();
		}
	}

	private void autologin() {
		HaierUser user = PushApplication.getInstance().getLoginUser();
		if(null == user){
			return;
		}

		String username = user.mobile;
		String password = user.password;
		accountText.getEditText().setText(username);
		passwordText.getEditText().setText(password);
		
		if (TextUtils.isEmpty(username)) {
			accountText.getEditText().requestFocus();
		}else {
			passwordText.getEditText().requestFocus();
		}
		
		if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
			HaierApi.login(username, password, mHandler);
		}
	}

	private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onStart() {
			super.onStart();
			if(!isFinishing()){
				DialogHelper.showDialogForLoading(UserLoginActivity.this, "正在登录，请您耐心等待...", true);
			}
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
			DialogHelper.stopProgressDlg();

			String json = new String(bytes);
			if (200 == statusCode) {
				switch (JsonUtil.getStateFromServer(json)) {
					case 200:
						String userToken = JsonUtil.getUserToken(json);
						int state = JsonUtil.getAuthenticationState(json);
						PushApplication.getInstance().setToken(userToken);
						StatAgent.initMemberId(UserLoginActivity.this, userToken);
						/*String phoneNum = mAccountText.getText().toString();
						String userPass = mPasswordText.getText().toString();*/
						String phoneNum = accountText.getEditText().getText().toString();
						String userPass = passwordText.getEditText().getText().toString();
						HaierUser user = new HaierUser();
						user.mobile = phoneNum;
						user.password = userPass;
						user.authentication_state = state;
						handLoginUserInfo(user);

						break;
					case 702:
						accountText.setError("该手机号未注册，请先注册");
						//AppToast.showShortText(UserLoginActivity.this, "该手机号未注册，请你先注册");
						break;
					case 706:
						passwordText.setError("登录密码错误，请重新输入！");
						//AppToast.showShortText(UserLoginActivity.this, "登录密码错误，请重新输入！");
						break;

					default:
						break;
				}
			}
		}

		@Override
		public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
			DialogHelper.stopProgressDlg();
			//mLoginBtn.setError("登录失败了，请稍后重试");
			AppToast.showShortText(UserLoginActivity.this, "登录失败了，请稍后重试");
		}
	};

	/**
	 *  保存用户信息
	 * @param user
	 */
	private void handLoginUserInfo(HaierUser user){
		PushApplication.getInstance().saveUserInfo(user);
		EventBus.getDefault().post(new UserChangedEvent());
		finish();
	}

	@Override
	@OnClick({R.id.back_img,R.id.register,R.id.forgot_psw,R.id.login})
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_img:
			onBackPressed();
			break;
		case R.id.register:
			IntentUtil.startActivity(this, UserRegisterActivity.class);
			break;
		case R.id.forgot_psw:
			IntentUtil.startActivity(this, ForgetUserPwdActivity.class);
			break;
		case R.id.login:

			accountText.setError(null);
			passwordText.setError(null);

			/*final String phoneNum = mAccountText.getText().toString();
			final String userPass = mPasswordText.getText().toString();*/

			final String phoneNum = accountText.getEditText().getText().toString();
			final String userPass = passwordText.getEditText().getText().toString();
			if (TextUtils.isEmpty(phoneNum)) {
				accountText.setError("用户名不能为空");
				accountText.requestFocus();
				return;
			}

			if (TextUtils.isEmpty(userPass)) {
				passwordText.setError("密码不能为空");
				passwordText.requestFocus();
				return;
			}

			if (passwordText.getEditText().getText().toString().length() < 6) {
				passwordText.setError("密码过短，请重新输入!");
				passwordText.requestFocus();
				return;
			}

			accountText.setErrorEnabled(false);
			passwordText.setErrorEnabled(false);

			HaierApi.login(phoneNum, userPass, mHandler);
			break;

		default:
			break;
		}
	}

}
