package com.haier.cabinet.customer.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.listener.BaseViewInterface;
import com.sunday.statagent.StatAgent;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class BaseFragmentActivity extends FragmentActivity implements BaseViewInterface {
	@Bind(R.id.back_img)
	protected ImageView mBackBtn;
	@Bind(R.id.title_text)
	protected TextView mTitleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (getLayoutId() != 0) {
			setContentView(getLayoutId());
		}
		ButterKnife.bind(this);

		PushApplication.addActivity(this);
		initView();
		initData();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onResume(this);       //统计时长
		StatAgent.stopWatcher(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		StatAgent.initWatcher(this);
		mBackBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	protected int getLayoutId() {
		return 0;
	}

	@Override
	public void initView() {

	}

	@Override
	public void initData() {

	}
}
