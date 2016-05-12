package com.haier.cabinet.customer.util;

import android.app.Activity;
import android.content.Context;

import com.haier.cabinet.customer.activity.UserLoginActivity;
import com.haier.common.util.IntentUtil;

/**
 * Created by lzx on 15/12/5.
 */
public class UIHelper {

    /**
     * 显示登陆界面
     *
     * @param context
     */
    public static void showLoginActivity(Context context) {
        IntentUtil.startActivity(((Activity) context), UserLoginActivity.class);
    }
}
