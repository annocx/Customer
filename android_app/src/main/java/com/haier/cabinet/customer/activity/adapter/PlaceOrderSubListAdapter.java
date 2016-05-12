package com.haier.cabinet.customer.activity.adapter;

import java.math.BigDecimal;
import java.util.List;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.OrderProduct2;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.ShopCartItem;
import com.haier.cabinet.customer.util.MathUtil;
import com.haier.cabinet.customer.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PlaceOrderSubListAdapter extends BaseAdapter {

	private Context mContext;
	private List<Product> mDataList;
	private LayoutInflater inflater;

	public PlaceOrderSubListAdapter(Context context, List<Product> list) {
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
		SpannableString msp = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.layout_place_order_sub_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.productImage = (ImageView) convertView.findViewById(R.id.product_image);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.name_text);
			viewHolder.priceText = (TextView) convertView.findViewById(R.id.discount_price_text);
			viewHolder.countText = (TextView) convertView.findViewById(R.id.pro_count_text);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final Product product = mDataList.get(position);
		viewHolder.nameText.setText(product.title);
//		if (TextUtils.isEmpty(product.boutique) || product.boutique.equals("null")) {
//			viewHolder.nameText.setText(product.title);
//		} else {
//			msp = new SpannableString("12" + product.title);
//			Drawable drawable = mContext.getResources().getDrawable(R.drawable.bg_boutique);
//			drawable.setBounds(0, 0, drawable.getIntrinsicWidth()+5, drawable.getIntrinsicHeight()+5);
//			msp.setSpan(new ImageSpan(drawable), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//			viewHolder.nameText.setText(msp);
//		}
		viewHolder.countText.setText("×" + product.count);

		if (product.cid == 99 && PushApplication.getInstance().isAuthenticated()){//是增值延保产品
			Bracket bracket = Util.getDiscount(product);
			if (bracket == null){
				viewHolder.priceText.setText("￥" + product.discountPrice);//折扣价格

			}else {
				viewHolder.priceText.setText("￥" + bracket.price);//折扣价格
			}

		}else {
			viewHolder.priceText.setText("￥" + product.discountPrice);//折扣价格
		}

		ImageLoader.getInstance().displayImage(product.thumbUrl,
				viewHolder.productImage, PushApplication.getInstance().getDefaultOptions());

		return convertView;
	}

	class ViewHolder {
		ImageView productImage;
		TextView nameText;
		TextView priceText;
		TextView countText;
	}


}
