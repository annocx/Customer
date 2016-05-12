package com.haier.cabinet.customer.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.event.ProductEvent;
import com.haier.cabinet.customer.listener.BaseViewInterface;
import com.sunday.statagent.StatAgent;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener,BaseViewInterface {

	@Bind(R.id.back_img)
	protected ImageView mBackBtn;
	@Bind(R.id.title_text)
	protected TextView mTitleText;

	protected Context mContext;
	protected LayoutInflater mInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getLayoutId() != 0) {
			setContentView(getLayoutId());
		}
		ButterKnife.bind(this);
		// Register
		EventBus.getDefault().register(this);
		mInflater = getLayoutInflater();
		mContext = this;
		PushApplication.addActivity(this);
		initView();
		initData();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		StatAgent.initWatcher(BaseActivity.this);
		if (null != mBackBtn) {
			mBackBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					onBackPressed();
				}
			});
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		StatAgent.stopWatcher(BaseActivity.this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		// Unregister
		EventBus.getDefault().unregister(this);
	}


	protected int getLayoutId() {
		return 0;
	}

	// 隐藏虚拟键盘
	protected void hideKeyboard(EditText editText) {
		InputMethodManager imm = (InputMethodManager) (getApplicationContext())
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(editText.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	//不做实现，解决出现的异常（Subscriber ****has no public methods called ）
	public void onEventMainThread(ProductEvent event)
	{
	}

	@Override
	@OnClick({R.id.back_img})
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
			case R.id.back_img:
				onBackPressed();
				break;
			default:
				break;
		}
	}
}
