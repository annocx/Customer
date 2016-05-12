package com.haier.cabinet.customer.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ProductDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.event.CouponEvent;
import com.haier.cabinet.customer.util.UIHelper;
import com.haier.common.util.IntentUtil;
import com.haier.common.widget.PullToRefreshBase;
import com.haier.common.widget.PullToRefreshWebView;
import com.sunday.statagent.StatAgent;
import com.umeng.socialize.UMShareAPI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CommonWebActivity extends BaseWebViewActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sales_promotion;
    }

    public void initView() {
        mBackBtn.setVisibility(View.VISIBLE);

        mProgressbar = (ProgressBar) findViewById(R.id.progressbar);
        mPullWebView = (PullToRefreshWebView) findViewById(R.id.pull_webview);
        mWebView = mPullWebView.getRefreshableView();

        mPullWebView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<WebView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<WebView> refreshView) {
                loadUrl();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<WebView> refreshView) {
            }
        });
        mPullWebView.setPullRefreshEnabled(false);
    }

    public void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("title")) {
            if (bundle.getString("title").equals("顺心宝")) {
                StatAgent.initAction(this, "", "1", "14", "", "", "", "1", "");
            } else if (bundle.getString("title").equals("家电维修")) {
                StatAgent.initAction(this, "", "1", "17", "", "", "", "1", "");
            } else if (bundle.getString("title").equals(getResources().getString(R.string.points_explanation))) {
                StatAgent.initAction(this, "", "1", "27", "", "", "", "1", "");
            } else if (bundle.getString("title").equals(getResources().getString(R.string.customer_service))) {
                StatAgent.initAction(this, "", "1", "27", "", "", getString(R.string.customer_service), "1", "");
            }
        }

        browserUrl = getIntent().getStringExtra("url");
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
        // 联网载入
        loadUrl();

        // 设置
        mWebView.setWebViewClient(new WebViewClient() {

            /** 开始载入页面 */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setProgressBarIndeterminateVisibility(true);// 设置标题栏的滚动条开始
                browserUrl = url;
                super.onPageStarted(view, url, favicon);
            }

            /** 捕获点击事件 */
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                    view.loadUrl(url);
                    return false;
                }

                // Otherwise allow the OS to handle things like tel, mailto, etc.
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            /** 页面载入完毕 */
            @Override
            public void onPageFinished(WebView view, String url) {
                //将过滤到的url加入历史栈中
                addUrl(url);
                mPullWebView.onPullDownRefreshComplete();
                setLastUpdateTime();
                super.onPageFinished(view, url);
            }

        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int newProgress) {
                // 设置标题栏的进度条的百分比
                mProgressbar.setProgress(newProgress);
                if (newProgress == 100) {
                    //将过滤到的url加入历史栈中
                    mProgressbar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }

            /** 设置标题 */
            public void onReceivedTitle(WebView view, String title) {
                mTitleText.setText(title);
                super.onReceivedTitle(view, title);
            }
        });
        mWebView.addJavascriptInterface(new WebViewInterface(CommonWebActivity.this), "Android");
    }


    private void loadUrl() {
        loadHistoryUrls.clear();
        mWebView.loadUrl(browserUrl);
        setLastUpdateTime();
    }


    public class WebViewInterface {
        Context mContext;
        /**
         * Instantiate the interface and set the context
         */
        WebViewInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void finish() {
            CommonWebActivity.this.finish();
        }
    }

}
