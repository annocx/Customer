package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ShopDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.OrderDetailItem;
import com.haier.cabinet.customer.entity.OrderProduct2;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.ShopCartItem;
import com.haier.cabinet.customer.util.MathUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OrderDetailAdapter extends BaseAdapter {
	private Context mContext;
	private List<OrderDetailItem> dataList = new ArrayList<OrderDetailItem>();
	private LayoutInflater inflater;
	private Handler mHandler;

	public OrderDetailAdapter(Context context, Handler handler) {
		this.mContext = context;
		this.inflater=LayoutInflater.from(mContext);
		this.mHandler = handler;
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
			convertView=inflater.inflate(R.layout.layout_order_detail_list_item, null);
			viewHolder=new ViewHolder();
			viewHolder.listView = (ListView) convertView.findViewById(R.id.listView);
			viewHolder.listView.setDivider(mContext.getResources().getDrawable(R.color.app_bg));
			viewHolder.listView.setDividerHeight(15);
			viewHolder.listView.setCacheColorHint(mContext.getResources().getColor(android.R.color.transparent));
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		
		final OrderDetailItem cartItem = dataList.get(position);

		OrderDetailSubListAdapter adapter = new OrderDetailSubListAdapter(mContext, cartItem.products,mHandler);
		viewHolder.listView.setAdapter(adapter);

		return convertView;
	}

	class ViewHolder{
		ListView listView;
	}

	public void setDataList(Collection<OrderDetailItem> dataList) {
		this.dataList.clear();
		this.dataList.addAll(dataList);
		notifyDataSetChanged();
	}

	public List<OrderDetailItem> getDataList() {
		return this.dataList;
	}

	public void setSelectedAll(boolean isChecked) {
		for (OrderDetailItem cartItem : dataList) {
			for (OrderProduct2 product : cartItem.products) {
				product.isChecked = isChecked;
			}
		}
		notifyDataSetChanged();
	}

	public boolean isSelectedAll() {
		if (0 == dataList.size()) {
			return false;
		}
		for (OrderDetailItem cartItem : dataList) {
			for (OrderProduct2 product : cartItem.products) {
				if (!product.isChecked)
					return false;
			}
		}

		return true;
	}

	public List<OrderDetailItem> getSelectedProducts() {
		List<OrderDetailItem> cartItems = new ArrayList<>();
		for (OrderDetailItem cartItem : dataList) {
			OrderDetailItem shopCartItem = new OrderDetailItem();
			shopCartItem.shop = cartItem.shop;
			for (OrderProduct2 product : cartItem.products) {
				if (product.isChecked)
					shopCartItem.products.add(product);
			}
			cartItems.add(shopCartItem);
		}
		return cartItems ;
	}
}
