package com.haier.cabinet.customer.activity;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.util.Constant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;

public class EvaluationServiceActivity extends Activity {

    WebView mWebView;
    Button mCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation_service);
        mWebView = (WebView) findViewById(R.id.content_webview);
        mCancelBtn = (Button) findViewById(R.id.btn_neg);
        PushApplication.addActivity(this);
        initWebView();

        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        String orderNo = intent.getStringExtra("orderNo");
        String url = Constant.URL_EVALUATION_SERVICE + "?orderNo=" + orderNo + "&gladEyeKey=" + Constant.GLADEYEKEY;
        mWebView.loadUrl(url);

        mCancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                EvaluationServiceActivity.this.finish();
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        // 支持JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //设置加载进来的页面自适应手机屏幕
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        // 支持保存数据
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setDomStorageEnabled(true);
        // 清除缓存
        mWebView.clearCache(true);
        // 清除历史记录
        mWebView.clearHistory();

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return false;
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWebView.destroy();
    }
}
