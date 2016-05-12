package com.haier.cabinet.customer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.haier.cabinet.customer.api.ApiHttpClient;
import com.haier.cabinet.customer.entity.HaierUser;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.CyptoUtils;
import com.haier.cabinet.customer.util.StringUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.sunday.statagent.StatAgent;
import com.umeng.socialize.PlatformConfig;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class PushApplication extends Application {

    private String userId;
    private boolean isReceiveMessage = true;   // 默认打开
    private boolean isLogin = false;
    private boolean isFirst = true;//是否刚打开app
    private static String token = null;
    private int authenticateState = 3;
    private int cartTotal = 0;//购物车数量
    private static final String PREFS_NAME = "MyUserToken";

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    SharedPreferences sp;

    public String getCartTotal() {
        if (cartTotal > 99) {
            return "99+";
        }
        return String.valueOf(cartTotal);
    }

    public int getCartTotalNum() {
        return cartTotal;
    }

    public void setCartTotal(int cartTotal) {
        this.cartTotal = cartTotal;
    }

    public int getAuthenticateState() {
        return authenticateState;
    }

    public void setAuthenticateState(int authenticateState) {
        this.authenticateState = authenticateState;
    }

    public String getToken() {
        if (token == null) {
            token = getUserToken();
        }
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        saveUserToken(token);
    }

    public boolean isReceiveMessage() {
        return isReceiveMessage;
    }

    public void setReceiveMessage(boolean receiveMessage) {
        isReceiveMessage = receiveMessage;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean isLogin) {

        this.isLogin = isLogin;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAuthenticated() {
        return authenticateState == 1;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public static Context applicationContext;
    private static PushApplication instance;
    // 记录打开的Activity
    public static Stack<Activity> Acts = new Stack<Activity>();


    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance = this;
        //fix bug: Could not find method android.view.Window.setStatusBarColor, referenced from method
        MultiDex.install(this);

        // 埋点
        StatAgent.init(this, "2", false); //1:乐家H5 2.乐家APP 3.乐家微信

        init();
        initImageLoader(this);
        sp = getSharedPreferences(PREFS_NAME, 0);

    }

    public static PushApplication getInstance() {
        return instance;
    }

    private void init() {
        // 初始化网络请求
        AsyncHttpClient client = new AsyncHttpClient();
        ApiHttpClient.setHttpClient(client);

        //微信 appid appsecret
        PlatformConfig.setWeixin(Constant.weixin_appID, Constant.weixin_appSecret);
        // QQ和Qzone appid appkey
        PlatformConfig.setQQZone(Constant.qq_appID, Constant.qq_appSecret);

//        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
//        Set<String> tags = new HashSet<>();
//        tags.add("Test");
//        JPushInterface.setTags(getApplicationContext(), tags, new TagAliasCallback() {
//            @Override
//            public void gotResult(int i, String s, Set<String> set) {
//
//            }
//        });
    }

    public void setProperties(Properties props) {
        AppConfig.getAppConfig(this).setProps(props);
    }

    public void setProperty(String key, String value) {
        AppConfig.getAppConfig(this).set(key, value);
    }

    public void getProperties() {
        AppConfig.getAppConfig(this).getProperties();
    }

    public String getProperty(String key) {
        return AppConfig.getAppConfig(this).get(key);
    }

    public void removeProperty(String... keys) {
        AppConfig.getAppConfig(this).remove(keys);
    }

    public void saveReceiveMessageStatus(boolean isReceiveMessage) {
        SharedPreferences.Editor spEd = sp.edit();
        spEd.putBoolean("mReceiveMessage", isReceiveMessage);
        spEd.commit();
    }

    public boolean getReceiveMessageStatus() {
        boolean mReceiveMessage = sp.getBoolean("mReceiveMessage", true);
        return mReceiveMessage;
    }


    /**
     * 保存token
     */

    public void saveUserToken(String token) {
        // 写入配置文件
        SharedPreferences.Editor spEd = sp.edit();
        spEd.putString("mToken", token);
        spEd.commit();
    }

    public String getUserToken() {
        String mToken = sp.getString("mToken", "");
        return mToken;
    }

    /**
     * 保存登录信息
     *
     * @param user
     */
    public void saveUserInfo(final HaierUser user) {
        this.isLogin = true;
        this.userId = user.mobile;
        this.authenticateState = user.authentication_state;
        setProperty("user.name", user.name);
        setProperty("user.mobile", user.mobile);
        setProperty("user.pwd", CyptoUtils.encode("custmoerApp", user.password));
        setProperty("user.authenticationState", String.valueOf(user.authentication_state));
        setProperty("user.autoLogin", String.valueOf(true));
        setProperty("user.isFirstOpenApp", "customer");
    }

    /**
     * 更新用户信息
     *
     * @param user
     */
    public void updateUserInfo(final HaierUser user) {
        setProperties(new Properties() {
            {
                setProperty("user.pwd", CyptoUtils.encode("custmoerApp", user.password));
            }
        });
    }

    public HaierUser getLoginUser() {
        HaierUser user = new HaierUser();
        user.name = getProperty("user.name");
        user.mobile = getProperty("user.mobile");
        user.password = CyptoUtils.decode("custmoerApp", getProperty("user.pwd"));
        user.authentication_state = StringUtils.toInt(getProperty("user.authenticationState"), 3);
        return user;
    }

    /**
     * 清除登录信息
     */
    public void cleanLoginInfo() {
        removeProperty("user.pwd", "user.name", "user.mobile", "user.autoLogin");
    }

    /**
     * 退出登录,清空数据
     */
    public void logoutHaiUser() {
        Log.d("lzx", "logoutHaiUser");
        this.userId = "";
        this.isLogin = false;
        this.cartTotal = 0;
        cleanLoginInfo();
    }

    /**
     * ImageLoader 图片组件初始化
     *
     * @param context
     */
    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public DisplayImageOptions getDefaultOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false) // default
                .delayBeforeLoading(100)
                /*.showImageForEmptyUri(R.drawable.default_cover)
                .showImageOnFail(R.drawable.default_cover)*/
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true) // default 不缓存至手机SDCard
//                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)// default
//                .bitmapConfig(Bitmap.Config.ARGB_8888) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build();
        return options;
    }

    public DisplayImageOptions getDefaultOptions2() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false) // default
                .delayBeforeLoading(100)
                .showImageOnLoading(R.drawable.ic_product_default)
                .showImageForEmptyUri(R.drawable.ic_product_default)
                .showImageOnFail(R.drawable.ic_product_default)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true) // default 不缓存至手机SDCard
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)// default
                .bitmapConfig(Bitmap.Config.ARGB_8888) // default
                .build();
        return options;
    }

    public DisplayImageOptions getDefaultOptions3() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false) // default
                .delayBeforeLoading(100)
                .showImageOnLoading(R.drawable.ic_product_default)
                .showImageForEmptyUri(R.drawable.ic_product_default)
                .showImageOnFail(R.drawable.ic_product_default)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true) // default 不缓存至手机SDCard
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)// default
                .bitmapConfig(Bitmap.Config.ARGB_8888) // default
                .build();
        return options;
    }

    public static void addActivity(Activity activity) {
        Acts.add(activity);
    }

    public static void finishAll() {
        for (Activity activity : Acts) {
            activity.finish();
        }
        System.exit(0);
    }

}
