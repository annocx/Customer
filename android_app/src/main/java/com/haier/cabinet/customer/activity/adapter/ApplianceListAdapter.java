package com.haier.cabinet.customer.activity.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ProductDetailsActivity;
import com.haier.cabinet.customer.activity.ShopDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.entity.FreshExpress;
import com.haier.cabinet.customer.entity.Shop;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.HardRefSimpleImageLoadingListener;
import com.haier.cabinet.customer.view.PinnedSectionListView.PinnedSectionListAdapter;
import com.haier.cabinet.customer.entity.Product;
import com.haier.common.util.IntentUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class ApplianceListAdapter extends BaseAdapter implements PinnedSectionListAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	List<FreshExpress> mDataList ;
	public ApplianceListAdapter(Context context, List<FreshExpress> list) {
		this.mContext = context;
		this.mDataList = list;
		this.mInflater = LayoutInflater.from(mContext);
	}
	
	@Override
	public int getCount() {
		return mDataList.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_appliance_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.sectionView = convertView.findViewById(R.id.section_layout);
			viewHolder.advertisementView = convertView.findViewById(R.id.advertisement_layout);
			viewHolder.productView = convertView.findViewById(R.id.product_layout);
			viewHolder.shopView = convertView.findViewById(R.id.shop_layout);
			viewHolder.sectionText = (TextView) convertView.findViewById(R.id.section_text);
			viewHolder.bannerImage = (ImageView) convertView.findViewById(R.id.banner_image);
			viewHolder.proNameText = (TextView) convertView.findViewById(R.id.product_name_text);
			viewHolder.priceText = (TextView) convertView.findViewById(R.id.discount_price_text);
			viewHolder.retailPriceText = (TextView) convertView.findViewById(R.id.retail_price_text);
			viewHolder.buyBtn = (Button)convertView.findViewById(R.id.buy_button);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		FreshExpress freshExpress = mDataList.get(position);

		if (freshExpress.type == FreshExpress.SECTION) {
			viewHolder.advertisementView.setVisibility(View.GONE);
			viewHolder.sectionView.setVisibility(View.VISIBLE);
			viewHolder.sectionText.setText(freshExpress.content);

		}else{
			viewHolder.sectionView.setVisibility(View.GONE);
			viewHolder.advertisementView.setVisibility(View.VISIBLE);

			if (freshExpress.category_type == FreshExpress.CATEGORY_PRODUCT){
				viewHolder.productView.setVisibility(View.VISIBLE);
				viewHolder.shopView.setVisibility(View.GONE);

				final Product product =  Util.convertFreshExpress2Product(freshExpress);
				viewHolder.proNameText.setText(product.title);
				viewHolder.priceText.setText("¥" + product.discountPrice + "");
				viewHolder.retailPriceText.setText("¥" + product.retailPrice);
				viewHolder.retailPriceText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);

				viewHolder.buyBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Bundle bundle = new Bundle();
						bundle.putSerializable("product", product);
						IntentUtil.startActivity(((Activity)mContext), ProductDetailsActivity.class,bundle);
					}
				});

				if (!TextUtils.isEmpty(product.imgUrl)) {
					ImageLoader.getInstance().displayImage(product.imgUrl, viewHolder.bannerImage,
							PushApplication.getInstance().getDefaultOptions2());
				}
			}else if (freshExpress.category_type == FreshExpress.CATEGORY_SHOP) {
				viewHolder.productView.setVisibility(View.GONE);
				viewHolder.shopView.setVisibility(View.VISIBLE);
				final Shop shop =  Util.convertFreshExpress2Shop(freshExpress);
				viewHolder.proNameText.setText(shop.name);

				viewHolder.buyBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Bundle bundle = new Bundle();
						bundle.putInt(SortResultActivity.RESULT_ID, shop.id);
						IntentUtil.startActivity(((Activity) mContext), ShopDetailsActivity.class, bundle);
					}
				});

				if (!TextUtils.isEmpty(shop.imgUrl)) {
					ImageLoader.getInstance().displayImage(shop.imgUrl, viewHolder.bannerImage,
							PushApplication.getInstance().getDefaultOptions2());
				}

			}




		}
		
		return convertView;
	}
	class ViewHolder {
		View sectionView;
		View advertisementView;
		View productView;
		View shopView;
		TextView sectionText;
		TextView proNameText;
		ImageView bannerImage;
		TextView priceText;
		TextView retailPriceText;//原价
		Button buyBtn;
	}

	@Override
	public boolean isItemViewTypePinned(int viewType) {
		return viewType == FreshExpress.SECTION;//0是标题，1是内容
	}

	@Override
	public int getViewTypeCount() {
		return 2;//2种view的类型 baseAdapter中得方法
	}

	@Override
	public int getItemViewType(int position) {
		return ((FreshExpress)getItem(position)).type;
	}

}
