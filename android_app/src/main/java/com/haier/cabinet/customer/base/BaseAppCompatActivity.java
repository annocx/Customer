package com.haier.cabinet.customer.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.haier.cabinet.customer.interf.BaseActivityInterface;
import com.haier.cabinet.customer.util.Constant;
import com.haier.common.widget.PullToRefreshGridView;
import com.sunday.statagent.StatAgent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class BaseAppCompatActivity extends BaseActivity implements BaseActivityInterface {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.INTENT_ACTION_SHOP_CART_TOTAL_CHANGE);
		registerReceiver(mBroadcastReceiver, intentFilter);
		
	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.INTENT_ACTION_SHOP_CART_TOTAL_CHANGE)){
				notifyShopCartNumChanged();
			}

		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
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
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	public void notifyShopCartNumChanged() {

	}

	@Override
	public void initView() {

	}

	@Override
	public void initData() {

	}
}
