package com.haier.cabinet.customer.activity.adapter;

import java.util.HashMap;
import java.util.List;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.AddressInfo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class AddressAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	List<AddressInfo> address;
	String addressId;

	HashMap<Integer, Boolean> isCheckMap =  new HashMap<Integer, Boolean>();

	public AddressAdapter(Context context, String addressId, List<AddressInfo> address) {
		this.context = context;
		this.address = address;
		this.addressId = addressId;
		this.inflater = LayoutInflater.from(context);
		for (int i = 0; i < address.size(); i++){
			if (!address.get(i).id.equals(addressId)) {
				isCheckMap.put(i, false);
			}
		}

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return address != null ? address.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.layout_user_adress_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.userNameText = (TextView) convertView.findViewById(R.id.username_text);
			viewHolder.userAddressText = (TextView) convertView.findViewById(R.id.address_text);
			viewHolder.phoneText = (TextView) convertView.findViewById(R.id.phone_text);
			viewHolder.defaultText = (TextView) convertView.findViewById(R.id.default_text);
			viewHolder.arrowImage = (ImageView) convertView.findViewById(R.id.right_arrow_image);
			viewHolder.selectCbx=(CheckBox)convertView.findViewById(R.id.select_checkbox);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final AddressInfo addressInfo = address.get(position);
		viewHolder.userNameText.setText(addressInfo.name);
		StringBuilder sb = new StringBuilder();
		sb.append(addressInfo.provincialCityArea).append(addressInfo.street);
		viewHolder.userAddressText.setText(sb.toString());
		viewHolder.phoneText.setText(addressInfo.phone);

		if (addressInfo.status) {
			viewHolder.defaultText.setVisibility(View.VISIBLE);
		} else {
			viewHolder.defaultText.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(addressId)) {
			viewHolder.arrowImage.setVisibility(View.VISIBLE);
		} else {

			if (isCheckMap!=null && !isCheckMap.containsKey(position)) {
				viewHolder.selectCbx.setChecked(true);
				viewHolder.selectCbx.setVisibility(View.VISIBLE);
			} else {
				viewHolder.selectCbx.setVisibility(View.INVISIBLE);
			}

		}

		return convertView;
	}

	class ViewHolder {
		TextView userNameText, userAddressText, phoneText, defaultText;
		ImageView arrowImage;
		CheckBox selectCbx;
	}

}
