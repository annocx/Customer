package com.haier.cabinet.customer.activity.adapter;

import com.haier.cabinet.customer.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ShareAdapter extends BaseAdapter {

	private static String[] shareNames = new String[] { "手机", "微信好友", "QQ好友" };
	private int[] shareIcons = new int[] { R.drawable.ic_short_message,
			R.drawable.ic_sns_weixin, R.drawable.ic_sns_qqfriends};

	private LayoutInflater inflater;

	public ShareAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return shareNames.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.layout_share_item, null);
		}
		ImageView shareIcon = (ImageView) convertView
				.findViewById(R.id.share_icon);
		TextView shareTitle = (TextView) convertView
				.findViewById(R.id.share_title);
		shareIcon.setImageResource(shareIcons[position]);
		shareTitle.setText(shareNames[position]);

		return convertView;
	}
}
