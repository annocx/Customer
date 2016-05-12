package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ProductDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.entity.OrderProduct2;
import com.haier.common.util.IntentUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailSubListAdapter extends BaseAdapter {
	private Context mContext;
	private List<OrderProduct2> dataList = new ArrayList<OrderProduct2>();
	private LayoutInflater inflater;
	private Handler mHandler;

	public OrderDetailSubListAdapter(Context context, List<OrderProduct2> dataList, Handler handler) {
		this.mContext = context;
		this.inflater=LayoutInflater.from(mContext);
		this.mHandler = handler;
		this.dataList = dataList;
	}
	
	@Override
	public int getCount() {
		return dataList!=null ? dataList.size():0;
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder ;
		if(convertView==null){
			convertView=inflater.inflate(R.layout.layout_my_order_detail_list_item, null);
			viewHolder=new ViewHolder();
			viewHolder.relative_order_detail_item = (RelativeLayout) convertView.findViewById(R.id.relative_order_detail_item);
			viewHolder.thumbImage=(ImageView)convertView.findViewById(R.id.thumb_image);
			viewHolder.titleText=(TextView)convertView.findViewById(R.id.pro_title_text);
			viewHolder.priceText=(TextView)convertView.findViewById(R.id.pro_price_text);
			viewHolder.countText=(TextView)convertView.findViewById(R.id.pro_count_text);
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		
		final OrderProduct2 product = dataList.get(position);
		
		viewHolder.titleText.setText(""+product.getShopName());
		viewHolder.countText.setText(String.format(Locale.CHINA, "×%s", product.getNum()));
		viewHolder.priceText.setText("￥" + product.getRetailPrice());//折扣价格

		SpannableString msp = null;

//		if (product.getJingpin().equals("精品")) {
//			msp = new SpannableString("12" + product.getShopName());
//			Drawable drawable = mContext.getResources().getDrawable(R.drawable.bg_boutique);
//			drawable.setBounds(0, 0, drawable.getIntrinsicWidth()+5, drawable.getIntrinsicHeight()+5);
//			msp.setSpan(new ImageSpan(drawable), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//			viewHolder.titleText.setText(msp);
//		}

		/*if ((Integer.valueOf(product.getCid()) == 99) && PushApplication.getInstance().isAuthenticated()){//是增值延保产品
			Bracket bracket = Util.getDiscount(product);
			if (bracket == null){
				viewHolder.priceText.setText("￥" + product.getDiscountPrice());//折扣价格

			}else {
				viewHolder.priceText.setText("￥" + bracket.price);//折扣价格
			}

		}else {
			viewHolder.priceText.setText("￥" + product.getDiscountPrice());//折扣价格
		}*/

		ImageLoader.getInstance().displayImage(product.getImgUrl(), viewHolder.thumbImage, PushApplication.getInstance().getDefaultOptions());
		viewHolder.relative_order_detail_item.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt(SortResultActivity.RESULT_ID, product.gcId);
				IntentUtil.startActivity((Activity) mContext, ProductDetailsActivity.class, bundle);
			}
		});
		return convertView;
	}
	
	
	class ViewHolder{
		ImageView thumbImage;
		TextView titleText;
		TextView priceText;
		TextView countText;
		RelativeLayout relative_order_detail_item;
	}
	
	
	public final List<OrderProduct2> getDataList() {
		return this.dataList;
	}

	public void setSelectedAll(boolean isChecked) {
		for (OrderProduct2 product : getDataList()) {
			product.isChecked = isChecked;
		}
		notifyDataSetChanged();
	}
	
	public boolean isSelectedAll() {
		if (0 == getDataList().size()) {
			return false;
		}
		for (OrderProduct2 product : getDataList()) {
			if (!product.isChecked)
				return false;
		}
		return true;
	}
	public List<OrderProduct2> getSelectedProducts() {
		List<OrderProduct2> list = new ArrayList<OrderProduct2>();
		for (OrderProduct2 product : getDataList()) {
			if (product.isChecked)
				list.add(product);
		}
		return list ;
	}
	
    private void updateList(int position, OrderProduct2 product){
    	getDataList().set(position, product);
    	notifyDataSetChanged();
    }
    
    public void deleteProduct(int position){
    	if (position <= getDataList().size()-1) {
    		getDataList().remove(position);
        	notifyDataSetChanged();
		}
    	
    }



}
