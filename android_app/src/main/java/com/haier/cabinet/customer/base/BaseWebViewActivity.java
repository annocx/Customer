package com.haier.cabinet.customer.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.common.widget.PullToRefreshWebView;
import com.sunday.statagent.StatAgent;
import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;


public class BaseWebViewActivity extends BaseActivity {

	protected WebView mWebView;

	protected ProgressBar mProgressbar;
	/** 当前url地址 */
	protected String browserUrl ;
	protected PullToRefreshWebView mPullWebView;
	protected SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");

	protected ArrayList<String> loadHistoryUrls = new ArrayList<String>();

	@Override
	protected void onDestroy() {
		super.onDestroy();
		loadHistoryUrls.clear();
	}

	protected void setLastUpdateTime() {
		String text = formatDateTime(System.currentTimeMillis());
		mPullWebView.setLastUpdatedLabel(text);
	}

	protected String formatDateTime(long time) {
		if (0 == time) {
			return "";
		}

		return mDateFormat.format(new Date(time));
	}

	protected void addUrl(String url){
		if(!loadHistoryUrls.contains(url)){
			loadHistoryUrls.add(url);
		}
	}

	public void onEventMainThread(UserChangedEvent event) {
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ) {

			if(mWebView.canGoBack()){
				if(getIntent().getBooleanExtra("filterable",false)){
					//过滤是否为重定向后的链接
					if(loadHistoryUrls.size() > 3 && loadHistoryUrls.get(loadHistoryUrls.size() - 2).contains("buyInfo.html")){
						loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls.size() - 2));
					}
				}
				loadHistoryUrls.remove(loadHistoryUrls.get(loadHistoryUrls.size() - 1));
				if(loadHistoryUrls.size() > 0){
					//加载重定向之前的页
					mWebView.loadUrl(loadHistoryUrls.get(loadHistoryUrls.size() - 1));
					return true;
				}

			}

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void initView() {

	}

	@Override
	public void initData() {

	}
}
