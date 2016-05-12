package com.haier.common.util;

import com.haier.common.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AppToast {

	protected static final String TAG = "AppToast";
	public static Toast toast;
	/**
	 * 信息提示
	 * 
	 * @param context
	 * @param content
	 */
	public static void makeToast(Context context, String content) {
		if(context==null)return;
		if(toast != null)
			toast.cancel();
		toast = Toast.makeText(context, content, Toast.LENGTH_LONG);
		toast.show();
	}

	public static void makeShortToast(Context context, String content) {
		if(context==null)return;
		if(toast != null)
			toast.cancel();
		toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
		toast.show();
	}

	public static void showShortText(Context context, int resId) {
		try {
			if(context==null)return;
			if(toast != null)
				toast.cancel();

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.layout_custom_toast, null);
			TextView contentText = (TextView) view.findViewById(R.id.toast_message);
			contentText.setText(resId);
			toast = new Toast(context);
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM, 0, 300);
			toast.setView(view);
			toast.show();
		} catch (Exception e) {
			AppLog.e(TAG,e.getMessage());
		}
	}
	public static void showShortText(Context context, CharSequence text) {
		if(context==null)return;
		if(toast != null)
			toast.cancel();
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.layout_custom_toast, null);
		TextView contentText = (TextView) view.findViewById(R.id.toast_message);
		contentText.setText(text);
		toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, 300);
		toast.setView(view);
		toast.show();
	}
	
	public static void showLongText(Context context, int resId) {
		try {
			if(context==null)return;
			if(toast != null)
				toast.cancel();
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.layout_custom_toast, null);
			TextView contentText = (TextView) view.findViewById(R.id.toast_message);
			contentText.setText(resId);
			toast = new Toast(context);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setGravity(Gravity.BOTTOM, 0, 300);
			toast.setView(view);
			toast.show();
			
		} catch (Exception e) {
			AppLog.e(TAG,e.getMessage());
		}
	}

	public static void showLongText(Context context, CharSequence text) {
		if(context==null)return;
		if(toast != null)
			toast.cancel();
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.layout_custom_toast, null);
		TextView contentText = (TextView) view.findViewById(R.id.toast_message);
		contentText.setText(text);
		toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setGravity(Gravity.BOTTOM, 0, 300);
		toast.setView(view);
		toast.show();
	}
	
	public static void showRequestDataFailureTip(Context context) {
		try {
			if (context == null)
				return;
			if (toast != null)
				toast.cancel();
			if (NetUtil.isNetConnected(context)) {
				toast = Toast.makeText(context, R.string.get_data_failure,
						Toast.LENGTH_SHORT);
			} else {
				toast = Toast.makeText(context, R.string.no_network,
						Toast.LENGTH_SHORT);
			}
			toast.show();

		} catch (Exception e) {
			AppLog.e(TAG, e.getMessage());
		}
	}
	
}
