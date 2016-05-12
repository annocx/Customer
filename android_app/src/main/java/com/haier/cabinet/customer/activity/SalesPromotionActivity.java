package com.haier.cabinet.customer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.base.BaseWebViewActivity;
import com.haier.cabinet.customer.event.CouponEvent;
import com.haier.cabinet.customer.util.ShareUtil;
import com.haier.cabinet.customer.util.UIHelper;
import com.haier.common.util.IntentUtil;
import com.haier.common.widget.PullToRefreshBase;
import com.haier.common.widget.PullToRefreshBase.OnRefreshListener;
import com.haier.common.widget.PullToRefreshWebView;
import com.sunday.statagent.StatAgent;
import com.umeng.socialize.UMShareAPI;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;

/**
 * 优惠活动通用界面
 */
public class SalesPromotionActivity extends BaseWebViewActivity {
    
    private ShareUtil mShareUtil;

    @Bind(R.id.right_text) TextView shareView;

    private static final int ACTION_SHARE = 1001;
    private static final int ACTION_INVITE_FRIENDS = 1002;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what){
                case ACTION_SHARE:
                    mShareUtil.init(SalesPromotionActivity.this, "优惠优质农产品",
                            "优惠券来袭，速速来领！从源头把控，产地直供，放心、优质农产品尽在乐家驿站！",
                            data.getString("share_url"), ShareUtil.COUPON_SHARE);

                    mShareUtil.setCode(data.getString("coupon_code"));
                    mShareUtil.startShare();
                    break;
                case ACTION_INVITE_FRIENDS:
                    mShareUtil.init(SalesPromotionActivity.this, "邀约您共享美食",
                            "不发邀请码，您都不知道好吃的就在这里！在这里！！在这里！！！快来一起搜寻各地美食吧～",
                            data.getString("share_url"), ShareUtil.COUPON_INVITE_FRIENDS);
                    mShareUtil.startShare();
                    break;
                default:
                    break;

            }
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_sales_promotion;
    }

    public void initView() {
        StatAgent.initAction(this, "", "1", "9", "", "", "", "1", "");
        mBackBtn.setVisibility(View.VISIBLE);
        shareView.setText(R.string.share);
        if (getIntent().getBooleanExtra("isNeedShare", false)) {
            shareView.setVisibility(View.VISIBLE);
        } else {
            shareView.setVisibility(View.GONE);
        }
        shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.loadUrl("javascript:inviteShare()");
            }
        });

        mProgressbar = (ProgressBar) findViewById(R.id.progressbar);
        mPullWebView = (PullToRefreshWebView) findViewById(R.id.pull_webview);
        mWebView = mPullWebView.getRefreshableView();

        mPullWebView.setOnRefreshListener(new OnRefreshListener<WebView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<WebView> refreshView) {
                loadUrl();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<WebView> refreshView) {
            }
        });
    }

    public void initData() {
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

            /** 页面载入完毕 */
            @Override
            public void onPageFinished(WebView view, String url) {
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
                if(newProgress==100){
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

        mWebView.addJavascriptInterface(new WebViewInterface(SalesPromotionActivity.this), "Android");
        mShareUtil = new ShareUtil();

    }

    private void loadUrl() {
        mWebView.loadUrl(browserUrl);
        setLastUpdateTime();
    }


    String mUrl;
    public class WebViewInterface {
        Context mContext;
        /** Instantiate the interface and set the context */
        WebViewInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void showProductDetails(int categoryId, int proId) {

            Bundle bundle = new Bundle();
            bundle.putInt(ProductDetailsActivity.PRODUCT_TYPE, categoryId);
            bundle.putInt(SortResultActivity.RESULT_ID, proId);
            IntentUtil.startActivity((Activity)mContext, ProductDetailsActivity.class,bundle);
        }

        @JavascriptInterface
        public void shareCoupon(String couponCode,String url) {
            Message msg = new Message();
            msg.what = ACTION_SHARE;
            Bundle data = new Bundle();
            data.putString("coupon_code",couponCode);
            data.putString("share_url",url);
            msg.setData(data);
            mHandler.sendMessage(msg);
        }

        @JavascriptInterface
        public void inviteFriends(String url) {
            mUrl = url;
            Message msg = new Message();
            msg.what = ACTION_INVITE_FRIENDS;
            Bundle data = new Bundle();
            data.putString("share_url",url);
            msg.setData(data);
            mHandler.sendMessage(msg);
        }

        @JavascriptInterface
        public String getUserId() {
            return PushApplication.getInstance().getUserId();
        }

        @JavascriptInterface
        public void goToLogin() {
            UIHelper.showLoginActivity(mContext);
        }

    }

    public void onEventMainThread(CouponEvent event) {
        mWebView.loadUrl("javascript:shareResult("+event.getResult()+")");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** attention to this below ,must add this**/
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StatAgent.initAction(this, "", "2", "9", "", "", "back", "2", "");
    }
}
