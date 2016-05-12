package com.haier.cabinet.customer.util;

import org.apache.http.Header;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.event.ExpressMailEvent;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import de.greenrobot.event.EventBus;

public class HttpUtil {

	private static final String TAG = "HttpUtil";
	public static void weixinShareSucess(){
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("token", PushApplication.getInstance().getToken());
		client.get(Constant.URL_SHARE_WEIXIN, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String data = new String(arg2);
				Log.d(TAG, "weixinShareSucess onSuccess data " + data);
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				
			}
		});
	}
	
	public static void pushBaiduChannleId(final String channelId){
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("channelId", channelId);
		params.put("phoneType", String.valueOf(Constant.PHONE_TYPE));
		params.put("token", PushApplication.getInstance().getToken());
		client.get(Constant.URL_PUSH_CHANNELID, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String data = new String(arg2);
				Log.d(TAG, "pushBaiduChannleId onSuccess data " + data);
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Log.e(TAG, " onFailure " + arg3);
			}
		});
	}
	
	public static void qqShareSucess(){
		Log.d(TAG, "qqShareSucess");
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("token", PushApplication.getInstance().getToken());
		client.get(Constant.URL_SHARE_QQ, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String data = new String(arg2);
				Log.d(TAG, "qqShareSucess onSuccess data " + data);
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				String data = new String(arg2);
				Log.d(TAG, "qqShareFailure onSuccess data " + data);
			}
		});
	}
	
	public static void inviteQQFriendSucess(){
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("token", PushApplication.getInstance().getToken());
		client.get(Constant.URL_SHARE_QQ_INVITATION, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String data = new String(arg2);
				Log.d(TAG, "inviteQQFriendSucess onSuccess data " + data);
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				
			}
		});
	}
	
	public static void inviteWeixinFriendSucess(){
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("token", PushApplication.getInstance().getToken());
		client.get(Constant.URL_SHARE_WEIXIN_INVITATION, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String data = new String(arg2);
				Log.d(TAG, "inviteWeixinFriendSucess onSuccess data " + data);
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				
			}
		});
	}

	public static void getExpressMailTotal(final Context context){
		final AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("token", PushApplication.getInstance().getToken());
		client.post(Constant.URL_EXPRESS_MAIL_COUNT, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
								  byte[] response) {
				String json = new String(response);
				Log.d(TAG,"json " + json);
				if (200 == statusCode) {
					switch (JsonUtil.getExpressMailFromServer(json)) {
						case 1001:
							int count = JsonUtil.getIdentificationState(json);
							EventBus.getDefault().post(new ExpressMailEvent(count));
							break;
						default:
							break;
					}

				}
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Log.e("sk", arg3.getMessage());
			}
		});
	}

	public static void getShopCartTotal(final Context context){
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("member_id", PushApplication.getInstance().getUserId());
		client.get(Constant.URL_SHOPPING_CART_COUNT, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
								  byte[] response) {
				String json = new String(response);
				Log.d(TAG,"json " + json);
				if (200 == statusCode) {
					switch (JsonUtil.getStateFromShopServer(json)) {
						case 1001:
							int count = JsonUtil.getShopCartTotal(json);
							PushApplication.getInstance().setCartTotal(count);
							Intent intnet = new Intent(Constant.INTENT_ACTION_SHOP_CART_TOTAL_CHANGE);
							context.sendBroadcast(intnet);
							break;
						default:
							break;
					}

				}
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			}
		});
	}

	public static void inviteIncreaseSucess(String code){
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("coupon_template_code", code);
		params.put("member_id", PushApplication.getInstance().getUserId());
//		Log.d(TAG,Constant.URL_INCREASE+"?coupon_template_code="+code+"&member_id="+ PushApplication.getInstance().getUserId());
		client.post(Constant.URL_INCREASE, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String data = new String(arg2);
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				String data = new String(arg2);
			}
		});
	}
}
