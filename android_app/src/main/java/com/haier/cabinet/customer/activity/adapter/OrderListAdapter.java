package com.haier.cabinet.customer.activity.adapter;

import java.math.BigDecimal;
import java.util.List;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.util.MathUtil;
import com.haier.cabinet.customer.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OrderListAdapter extends BaseAdapter {

	private Context mContext;
	private List<Product> mDataList;
	private LayoutInflater inflater;

	public OrderListAdapter(Context context, List<Product> list) {
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
			convertView = inflater.inflate(R.layout.layout_order_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.productImage = (ImageView) convertView.findViewById(R.id.product_image);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.name_text);
			viewHolder.priceText = (TextView) convertView.findViewById(R.id.discount_price_text);
			viewHolder.totalPriceText = (TextView) convertView.findViewById(R.id.pro_total_price_text);
			viewHolder.countText = (TextView) convertView.findViewById(R.id.pro_count_text);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final Product product = mDataList.get(position);

		viewHolder.nameText.setText(product.title);
		viewHolder.countText.setText("×" + product.count);

		if ((product.cid==99) && PushApplication.getInstance().isAuthenticated()){//是增值延保产品
			Bracket bracket = Util.getDiscount(product);
			if (bracket == null){
				viewHolder.priceText.setText("￥" + product.discountPrice);//折扣价格
				viewHolder.totalPriceText.setText("¥" + product.discountPrice * product.count);

			}else {
				viewHolder.priceText.setText("￥" + bracket.price);//折扣价格
				viewHolder.totalPriceText.setText("¥" + bracket.price * product.count);
			}

		}else {
			viewHolder.priceText.setText("￥" + product.discountPrice);//折扣价格
			viewHolder.totalPriceText.setText("¥" + product.discountPrice * product.count);
		}

		ImageLoader.getInstance().displayImage(product.thumbUrl,
				viewHolder.productImage, PushApplication.getInstance().getDefaultOptions());

		return convertView;
	}

	class ViewHolder {
		ImageView productImage;
		TextView nameText;
		TextView priceText;
		TextView totalPriceText;
		TextView countText;
	}

	public double calculatingTotalPrice() {
		double total = 0.00;
		for (Product product : mDataList) {
			if (product.isChecked){
				if (product.cid == 99){
					Bracket bracket = Util.getDiscount(product);
					if (bracket == null){
						total += MathUtil.mul(product.count, product.discountPrice);
					}else {
						total += MathUtil.mul(product.count, bracket.price);
					}
				}else {
					total += MathUtil.mul(product.count, product.discountPrice);
				}
			}
		}
		//double值保留 2 位小数,使用银行家舍入法
		return MathUtil.round(total, 2, BigDecimal.ROUND_HALF_EVEN) ;
	}
}
