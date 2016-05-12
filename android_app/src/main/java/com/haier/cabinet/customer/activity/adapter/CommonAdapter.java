package com.haier.cabinet.customer.activity.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.ApplianceProduct;
import com.haier.cabinet.customer.entity.Brand;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.ProductType;
import com.haier.cabinet.customer.entity.ServiceType;
import com.haier.cabinet.customer.entity.SubBrand;
import com.haier.cabinet.customer.util.Constant;


public class CommonAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	List<Object> mList;
	private int requestCode;
	public CommonAdapter(Context context, int requestCode, List<Object> objects) {
		this.inflater=LayoutInflater.from(context);
		this.requestCode = requestCode;
		this.mList = objects;
	}

	@Override
	public int getCount() {
		return mList.size();
	}


	@Override
	public Object getItem(int position) {
		return null;
	}


	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder=null;
		if(convertView==null){
			convertView=inflater.inflate(R.layout.common_list_item, null);
			viewHolder=new ViewHolder();
			viewHolder.name=(TextView)convertView.findViewById(R.id.name_text);

			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		
		String text = null;
		switch (requestCode) {
		case Constant.REQUEST_BRAND:
			Brand brand = (Brand) mList.get(position);
			text = brand.name;
			break;
		case Constant.REQUEST_SUB_BRAND:
			SubBrand subBrand = (SubBrand) mList.get(position);
			text = subBrand.getName();
			break;
		case Constant.REQUEST_PRODUCT_TYPE:
			ProductType productType = (ProductType) mList.get(position);
			text = productType.codeName;
			break;
		case Constant.REQUEST_PRODUCT:
			ApplianceProduct product = (ApplianceProduct) mList.get(position);
			text = product.codeName;
			break;
		case Constant.REQUEST_SERVICE_TYPE:
			ServiceType serviceType = (ServiceType) mList.get(position);
			text = serviceType.codeName;
			break;

		default:
			break;
		}
		
		viewHolder.name.setText(text);
		
		return convertView;
	}

	class ViewHolder{
		TextView name;
	}



	

}
