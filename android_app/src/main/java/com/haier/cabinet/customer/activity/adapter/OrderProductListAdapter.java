package com.haier.cabinet.customer.activity.adapter;

import java.util.List;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.OrderDetailsActivity;
import com.haier.cabinet.customer.entity.OrderProduct2;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.util.Constant;
import com.haier.common.util.IntentUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OrderProductListAdapter extends BaseAdapter {

	private Context mContext;
	private List<OrderProduct2> mDataList;
	private LayoutInflater inflater;

	public OrderProductListAdapter(Context context, List<OrderProduct2> list) {
		this.mContext = context;
		this.mDataList = list;
		this.inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mDataList != null ? mDataList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.layout_my_order_product_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.productImage = (ImageView) convertView.findViewById(R.id.product_image);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.name_text);
			viewHolder.priceText = (TextView) convertView.findViewById(R.id.discount_price_text);
			viewHolder.countText = (TextView) convertView.findViewById(R.id.pro_count_text);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final OrderProduct2 product = mDataList.get(position);

		viewHolder.priceText.setText("¥" + product.getRetailPrice());
		viewHolder.countText.setText("×" + product.getNum());

		SpannableString msp = null;

//		if (product.getJingpin().equals("精品")) {
//			msp = new SpannableString("12" + product.getShopName());
//			Drawable drawable = mContext.getResources().getDrawable(R.drawable.bg_boutique);
//			drawable.setBounds(0, 0, drawable.getIntrinsicWidth()+5, drawable.getIntrinsicHeight()+5);
//			msp.setSpan(new ImageSpan(drawable), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//		} else {
//			msp = new SpannableString(product.getShopName());
//		}
//
//		viewHolder.nameText.setText(msp);
		viewHolder.nameText.setText(""+product.getShopName());
		ImageLoader.getInstance().displayImage(product.getImgUrl(), viewHolder.productImage, PushApplication.getInstance().getDefaultOptions());

		return convertView;
	}

	class ViewHolder {
		ImageView productImage;
		TextView nameText;
		TextView priceText;
		TextView countText;
	}

}
