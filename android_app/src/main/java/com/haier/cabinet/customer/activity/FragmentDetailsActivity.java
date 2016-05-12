package com.haier.cabinet.customer.activity;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseFragmentActivity;
import com.haier.cabinet.customer.fragment.LifeFragment;
import com.haier.cabinet.customer.fragment.MailFragment;
import com.haier.cabinet.customer.util.Constant;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

public class FragmentDetailsActivity extends BaseFragmentActivity {


	@Override
	protected int getLayoutId() {
		return R.layout.activity_details;
	}

	@Override
	public void initView() {
		super.initView();

		mBackBtn.setVisibility(View.VISIBLE);
		Fragment fragment = null;
		int extra = getIntent().getExtras().getInt(Constant.FRAGMENT_DETAILS, Constant.FRAGMENT_MAIL);
		switch (extra) {
			case Constant.FRAGMENT_MAIL:
				mTitleText.setText("寄件");
				fragment = new MailFragment();
				break;
			case Constant.FRAGMENT_LIFE:
				mTitleText.setText("生活+");
				fragment = new LifeFragment();
				break;

			default:
				break;
		}
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.add(R.id.fragment, fragment);
		transaction.commit();
		mBackBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
	}
}
