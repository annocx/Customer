package com.haier.cabinet.customer.api;

import android.util.Log;

import com.haier.cabinet.customer.util.Constant;
import com.haier.common.util.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by lzx on 15/9/12.
 */
public class HaierApi {

    public static void login(String username, String password, AsyncHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("phone", username);
        params.put("password", Utils.getMD5Text(password));
        params.put("userType", Constant.USER_TYPE);
        String loginUrl = Constant.DOMAIN + "/user/userLoginNew.json";
        ApiHttpClient.get(loginUrl, params, handler);
    }

}
