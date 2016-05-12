package com.haier.cabinet.customer.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.CheckedTextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.event.MainUIEvent;
import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.cabinet.customer.fragment.CategoryFragment;
import com.haier.cabinet.customer.fragment.CustomPresentationPagerFragment;
import com.haier.cabinet.customer.fragment.HomeFragment;
import com.haier.cabinet.customer.fragment.LifeFragment;
import com.haier.cabinet.customer.fragment.ShopCartFragment;
import com.haier.cabinet.customer.fragment.UserCenterFragment;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.HttpUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.CustTabWidget;
import com.haier.cabinet.customer.view.CustTabWidget.onTabSelectedListener;
import com.haier.common.util.AppToast;
import com.haier.common.util.Utils;
import com.haier.common.view.BadgeView;
import com.sunday.statagent.StatAgent;
import com.umeng.onlineconfig.OnlineConfigAgent;
import com.umeng.socialize.UMShareAPI;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;

import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.android.api.JPushInterface;
import de.greenrobot.event.EventBus;

public class MainUIActivity extends AppCompatActivity implements onTabSelectedListener {

    public static final String ACTION_CURRETNTAB = "currentTab";

    private static final String TAG = "MainUIActivity";

    private int mIndex = Constant.HOME_FRAGMENT_INDEX;

    FragmentManager mFragmentManager;
    FragmentTransaction mTransaction;

    public static HomeFragment mHomeFragment;
    public static CategoryFragment mCategoryFragment;
    public static ShopCartFragment mShopCartFragment;
    public static UserCenterFragment mUserFragment;
    private static Context mContext;

    private CustTabWidget mTabWidget;
    private BadgeView badgeView;
    private CheckedTextView cartTipText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        PushApplication.addActivity(this);
        mContext = MainUIActivity.this;
        EventBus.getDefault().register(this);
        //注册
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.INTENT_ACTION_SHOP_CART_TOTAL_CHANGE);
        registerReceiver(mBroadcastReceiver, intentFilter);

        if (TextUtils.isEmpty(PushApplication.getInstance().getProperty("user.isFirstOpenApp"))) {
            showGuideUI();
        } else {
            showMainUI();
        }

    }


    private void showGuideUI() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new CustomPresentationPagerFragment());
        fragmentTransaction.commit();
    }

    private void showMainUI() {
        cartTipText = (CheckedTextView) findViewById(R.id.shop_cart_tab_text);

        mFragmentManager = getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();
        mIndex = getIntent().getIntExtra("currentTab", 0);
        //设置默认Fragment
        initData(mIndex);

        OnlineConfigAgent.getInstance().updateOnlineConfig(mContext);
        OnlineConfigAgent.getInstance().setDebugMode(false);//正式发布应用时，请务必将本开关关闭，避免影响用户正常使用APP。

        HttpUtil.getShopCartTotal(this);
        if (PushApplication.getInstance().isLogin()) {
            HttpUtil.pushBaiduChannleId(JPushInterface.getRegistrationID(this));
        }
        //PushManager.startWork(mContext, PushConstants.LOGIN_TYPE_API_KEY, Constant.API_KEY);
    }

    private void prepareUmengUpdate() {
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.setDeltaUpdate(true);//增量更新
        String upgrade_mode = OnlineConfigAgent.getInstance().getConfigParams(mContext, "upgrade_mode");
        if (TextUtils.isEmpty(upgrade_mode)) {
            return;
        }
        String[] upgrade_mode_array = upgrade_mode.split(";");

        for (String mode : upgrade_mode_array) {
            String versionName = Utils.getVersionNumber(this);
            versionName = versionName + "F";
            if (mode.equals(versionName)) {
                UmengUpdateAgent.forceUpdate(this);//这行如果是强制更新就一定加上
                // 强制更新
                UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {

                    @Override
                    public void onClick(int status) {
                        switch (status) {
                            case UpdateStatus.Update:
                                break;
                            default:
                                // close the app
                                AppToast.showShortText(MainUIActivity.this, "非常抱歉，您需要更新应用才能继续使用");
                                MainUIActivity.this.finish();
                                break;
                        }
                    }
                });

            } else {
                UmengUpdateAgent.update(this);
            }
        }

    }

    private void initData(int current) {
        mHomeFragment = new HomeFragment();
        mCategoryFragment = new CategoryFragment();
        mShopCartFragment = new ShopCartFragment();
        mUserFragment = new UserCenterFragment();

        mTabWidget = (CustTabWidget) findViewById(R.id.tab_widget);
        mTabWidget.setVisibility(View.VISIBLE);
        mTabWidget.setOnTabSelectedListener(this);

        onTabSelecete(current, true);
        mTabWidget.setTabDisplay(this, current);

        badgeView = new BadgeView(this);
        badgeView.setBadgeMargin(0, 2, 2, 0);
        badgeView.setTargetView(cartTipText);

//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Constant.INTENT_ACTION_SHOP_CART_TOTAL_CHANGE);
//        registerReceiver(mBroadcastReceiver, intentFilter);

    }

    public void updateShopCartTotal() {
        if (null != badgeView) {
            if (PushApplication.getInstance().getCartTotalNum() == 0) {
                badgeView.setVisibility(View.GONE);
            } else {
                badgeView.setVisibility(View.VISIBLE);
                badgeView.setText(PushApplication.getInstance().getCartTotal());
                badgeView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_anim));
            }
        }

    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constant.INTENT_ACTION_SHOP_CART_TOTAL_CHANGE)) {
                updateShopCartTotal();
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        updateShopCartTotal();
        if (!TextUtils.isEmpty(PushApplication.getInstance().getProperty("user.city"))
                && PushApplication.getInstance().isFirst()) {
            prepareUmengUpdate();
            PushApplication.getInstance().setFirst(false);
        }

        StatAgent.initWatcher(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatAgent.stopWatcher(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        // Unregister
        EventBus.getDefault().unregister(this);
        PushApplication.getInstance().setFirst(true);
    }

    @Override
    public void onTabSelecete(int index) {
        initTab(index);
        mIndex = index;
        isProduct = false;
    }

    boolean isProduct;//判断是否从商品进入的购物车

    private void onTabSelecete(int current, boolean isIntent) {
        isProduct = isIntent;
        initTab(current);
    }

    /**
     * 初始化控件
     *
     * @param index
     */
    private void initTab(int index) {
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        hideFragments(beginTransaction);
        switch (index) {
            case Constant.HOME_FRAGMENT_INDEX:
            /*if (null == mHomeFragment) {
                mHomeFragment = new HomeFragment();
				beginTransaction.add(R.id.content_frame, mHomeFragment);
			} else {
				beginTransaction.show(mHomeFragment);
			}*/
                if (!mHomeFragment.isAdded()) {
                    beginTransaction.add(R.id.content_frame, mHomeFragment);
                }
                beginTransaction.show(mHomeFragment);
                break;
            case Constant.CATEGOGR_FRAGMENT_INDEX:
                if (!mCategoryFragment.isAdded()) {
                    beginTransaction.add(R.id.content_frame, mCategoryFragment);
                }
                beginTransaction.show(mCategoryFragment);
                break;
            case Constant.SHOPC_CART_FRAGMENT_INDEX:
                if (!mShopCartFragment.isAdded()) {
                    beginTransaction.add(R.id.content_frame, mShopCartFragment);
                }
                beginTransaction.show(mShopCartFragment);
                if (mShopCartFragment.isUpdate) {
                    mShopCartFragment.onResume();
                }
                break;
            case Constant.USER_FRAGMENT_INDEX:
                if (!mUserFragment.isAdded()) {
                    beginTransaction.add(R.id.content_frame, mUserFragment);
                }
                beginTransaction.show(mUserFragment);
                break;

            default:
                break;
        }
        beginTransaction.commitAllowingStateLoss();

    }

    private void hideFragments(FragmentTransaction transaction) {
        if (null != mHomeFragment) {
            transaction.hide(mHomeFragment);
        }
        if (null != mCategoryFragment) {
            transaction.hide(mCategoryFragment);
        }
        if (null != mShopCartFragment) {
            mShopCartFragment.EditShopCart();
            transaction.hide(mShopCartFragment);
        }
        if (null != mUserFragment) {
            transaction.hide(mUserFragment);
        }
    }

    private static Boolean isExit = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mIndex != Constant.HOME_FRAGMENT_INDEX) {
                if (mIndex == Constant.SHOPC_CART_FRAGMENT_INDEX && isProduct) {//如果是从商品进入购物车的，返回商品界面
                    finish();
                } else {
                    onTabSelecete(Constant.HOME_FRAGMENT_INDEX);
                    mTabWidget.setTabDisplay(this, Constant.HOME_FRAGMENT_INDEX);
                }
            } else {
                if (isExit == false) {
                    isExit = true;
                    AppToast.showShortText(this, "再按一次退出程序");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            isExit = false;
                        }
                    }, 2000);
                } else {
                    PushApplication.finishAll();
//                    System.exit(0);
                }
            }
        }
        return false;
    }

    public void onEventMainThread(MainUIEvent event) {
        //切换到指定位置
        onTabSelecete(event.getPosition());
        switch (event.getPosition()) {
            case 0:
                onTabSelecete(Constant.HOME_FRAGMENT_INDEX);
                mTabWidget.setTabDisplay(this, Constant.HOME_FRAGMENT_INDEX);
                break;
            case -1:
                showMainUI();
                break;
            default:
                break;
        }

    }

    public void onEventMainThread(UserChangedEvent event) {
        if (PushApplication.getInstance().isLogin()) {
            Log.d(TAG, "UserChangedEvent pushBaiduChannleId");
            HttpUtil.pushBaiduChannleId(JPushInterface.getRegistrationID(this));
        } else {
            PushApplication.getInstance().setCartTotal(0);
            updateShopCartTotal();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** attention to this below ,must add this**/
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

}
