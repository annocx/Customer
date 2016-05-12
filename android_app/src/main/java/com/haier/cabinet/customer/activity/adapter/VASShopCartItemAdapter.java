package com.haier.cabinet.customer.activity.adapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.OrderProduct2;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.ShopCartItem;
import com.haier.cabinet.customer.util.MathUtil;
import com.haier.cabinet.customer.util.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class VASShopCartItemAdapter extends BaseAdapter {
	private Context mContext;
	private List<ShopCartItem> dataList = new ArrayList<>();
	private LayoutInflater inflater;
	private Handler mHandler;
	
	public VASShopCartItemAdapter(Context context, Handler handler) {
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
			convertView=inflater.inflate(R.layout.layout_shopping_cart_list_item, null);
			viewHolder=new ViewHolder();
			viewHolder.shopNameText = (TextView)convertView.findViewById(R.id.shop_name_text);
			viewHolder.listView = (ListView) convertView.findViewById(R.id.listView);
			viewHolder.listView.setDivider(mContext.getResources().getDrawable(R.color.app_bg));
			viewHolder.listView.setDividerHeight(0);
			viewHolder.listView.setCacheColorHint(mContext.getResources().getColor(android.R.color.transparent));
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		
		final ShopCartItem cartItem = dataList.get(position);

		final ShopCartSubListAdapter adapter = new ShopCartSubListAdapter(mContext, position,cartItem.products,mHandler);
		viewHolder.listView.setAdapter(adapter);
		viewHolder.listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
										   final int position, long arg3) {

				AlertDialog dialog = new AlertDialog.Builder(mContext)
						.setMessage("确认要删除该商品吗?")
						.setPositiveButton("确定",
								new AlertDialog.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
														int which) {

									}
								})
						.setNegativeButton("取消",
								new AlertDialog.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
														int which) {
									}
								}).create();
				// 显示对话框
				dialog.show();

				return true;
			}
		});
		viewHolder.shopNameText.setText(cartItem.shop.name);

		return convertView;
	}


	class ViewHolder{
		TextView shopNameText;
		ListView listView;
	}

	public void setDataList(Collection<ShopCartItem> dataList) {
		this.dataList.clear();
		this.dataList.addAll(dataList);
		notifyDataSetChanged();
	}

	public List<ShopCartItem> getDataList() {
		return this.dataList;
	}

	public double calculatingTotalPrice() {
		double total = 0.00;
		for (ShopCartItem cartItem : dataList) {
			for (Product product : cartItem.products) {
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
		}

		//double值保留 2 位小数,使用银行家舍入法
		return MathUtil.round(total, 2, BigDecimal.ROUND_HALF_EVEN) ;
	}

	public void setSelectedAll(boolean isChecked) {
		for (ShopCartItem cartItem : dataList) {
			for (Product product : cartItem.products) {
				product.isChecked = isChecked;
			}
		}
		notifyDataSetChanged();
	}

	public boolean isSelectedAll() {
		if (0 == dataList.size()) {
			return false;
		}
		for (ShopCartItem cartItem : dataList) {
			for (Product product : cartItem.products) {
				if (!product.isChecked)
					return false;
			}
		}

		return true;
	}

	public List<ShopCartItem> getSelectedProducts() {
		List<ShopCartItem> cartItems = new ArrayList<>();
		for (ShopCartItem cartItem : dataList) {
			ShopCartItem shopCartItem = new ShopCartItem();
			shopCartItem.shop = cartItem.shop;
			for (Product product : cartItem.products) {

				if (product.isChecked){
					shopCartItem.products.add(product);
				}
			}
			if(shopCartItem.products.size() > 0){
				cartItems.add(shopCartItem);
			}
		}
		return cartItems ;
	}

	/**
	 * 删除购物车商品
	 * @param shopId
	 * @param position
	 */
	public void deleteProduct(int shopId, int position){
		for (int i = 0; i < dataList.size(); i++) {
			ShopCartItem cartItem = dataList.get(i);
			if(cartItem.shop.id == shopId){
				cartItem.products.remove(position);
			}
			updateShopCart(cartItem,i);
		}

		notifyDataSetChanged();
	}

	private void updateShopCart(ShopCartItem cartItem,int position){
		if(cartItem.products.size() == 0){
			dataList.remove(position);
		}
	}

}
