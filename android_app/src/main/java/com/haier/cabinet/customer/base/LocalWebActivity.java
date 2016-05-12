package com.haier.cabinet.customer.base;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.umeng.analytics.MobclickAgent;

public class LocalWebActivity extends BaseActivity {

	private WebView mWebView;

	@Override
	protected int getLayoutId() {
		return R.layout.local_web_activity;
	}

	@Override
	public void initView() {
		mBackBtn.setVisibility(View.VISIBLE);

		mWebView = (WebView) findViewById(R.id.content_webview);

		initWebView();
	}

	public void initData() {
		String url = getIntent().getStringExtra("url");
		mTitleText.setText(getIntent().getStringExtra("title"));
		mWebView.loadUrl(url);
		mTitleText.setText(getIntent().getStringExtra("title"));
	}

	private void initWebView() {
		WebSettings mWebSetting = mWebView.getSettings();
		mWebSetting.setLoadWithOverviewMode(true);
		mWebSetting.setUseWideViewPort(true);
		// 设置不可以支持缩放
		mWebSetting.setSupportZoom(false);

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if( url.startsWith("http:") || url.startsWith("https:") ) {
					// 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
					view.loadUrl(url);
					return false;
				}

				// Otherwise allow the OS to handle things like tel, mailto, etc.
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity( intent );
				return true;

			}
		});
		mWebView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (!TextUtils.isEmpty(getIntent().getStringExtra("title"))) {
					mTitleText.setText(getIntent().getStringExtra("title"));
				} else {
					mTitleText.setText(title);
				}

			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
	}
}
