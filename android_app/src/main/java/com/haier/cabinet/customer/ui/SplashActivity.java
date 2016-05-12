package com.haier.cabinet.customer.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.UserLoginActivity;
import com.haier.cabinet.customer.api.HaierApi;
import com.haier.cabinet.customer.entity.HaierUser;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.StringUtils;
import com.haier.common.util.AppToast;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sunday.statagent.StatAgent;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.Header;

import butterknife.Bind;
import cn.jpush.android.api.JPushInterface;

public class SplashActivity extends Activity {
    protected String TAG = "SplashActivity";
    PushApplication mApplication;
    HaierUser user;
    ImageView mSplashItem_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mApplication = PushApplication.getInstance();
        initAnimation();
    }

    private void initAnimation() {
        // TODO Auto-generated method stub
        mSplashItem_iv = (ImageView) findViewById(R.id.splash_loading_item);
        Animation translate = AnimationUtils.loadAnimation(this,
                R.anim.splash_loading);
        translate.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
            }
        });
        mSplashItem_iv.setAnimation(translate);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        JPushInterface.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        JPushInterface.onPause(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //自动登录
        if (StringUtils.toBool(mApplication.getProperty("user.autoLogin"))) {
            user = mApplication.getLoginUser();
            if (null == user) {
                handler.sendEmptyMessageDelayed(ENTER_LOGIN_UI, 1200);
            } else {
                HaierApi.login(user.mobile, user.password, mHandler);
            }

        } else {
            handler.sendEmptyMessageDelayed(ENTER_LOGIN_UI, 1200);
        }

    }

    private AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
            String json = new String(bytes);
            if (200 == statusCode) {
                if (200 == JsonUtil.getStateFromServer(json)) {
                    String userToken = JsonUtil.getUserToken(json);
                    StatAgent.initMemberId(SplashActivity.this, userToken);
                    mApplication.setToken(userToken);
                    handler.sendEmptyMessageDelayed(USER_LOGIN_SUCCESS, 1200);
                } else {
                    handler.sendEmptyMessageDelayed(USER_LOGIN_FAILED, 1200);
                }
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            handler.sendEmptyMessageDelayed(USER_LOGIN_FAILED, 1200);
        }
    };


    private final int USER_LOGIN_SUCCESS = 1001;
    private final int USER_LOGIN_FAILED = 1002;
    private final int ENTER_LOGIN_UI = 1003;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case USER_LOGIN_SUCCESS:
                    mApplication.getInstance().setLogin(true);
                    mApplication.setUserId(user.mobile);
                    mApplication.setAuthenticateState(user.authentication_state);
                    // 进入主界面
                    gotoMainUI();
                    break;
                case USER_LOGIN_FAILED:
                case ENTER_LOGIN_UI:
                    gotoMainUI();
                    break;

                default:
                    break;
            }
        }

        ;
    };


    private void gotoMainUI() {
        startActivity(new Intent(SplashActivity.this, MainUIActivity.class));
        finish();
    }

    private void gotoLoginUI() {
        startActivity(new Intent(SplashActivity.this, UserLoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
}
