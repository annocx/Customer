package com.haier.common.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.haier.common.R;

public class IntentUtil {
    public static void startActivity(Activity activity, Class<?> cls) {
        Intent intent = new Intent();
        intent.setClass(activity, cls);
        activity.startActivity(intent);
    }

    public static void startActivity(Activity activity, Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(activity, cls);
        activity.startActivity(intent);
    }

    public static void startActivityResult(Activity activity, Class<?> cls, Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(activity, cls);
        activity.startActivityForResult(intent, requestCode);

    }
}
