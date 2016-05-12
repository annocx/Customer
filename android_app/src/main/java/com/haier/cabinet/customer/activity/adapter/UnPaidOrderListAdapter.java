package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.OrderDetailsActivity;
import com.haier.cabinet.customer.entity.Order;
import com.haier.cabinet.customer.fragment.UnPaidOrderListFragment;
import com.haier.cabinet.customer.util.Constant;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.Utils;

import java.util.List;

public class UnPaidOrderListAdapter extends BaseAdapter {

	private Context mContext;
	private List<Order> mDataList;
	private LayoutInflater inflater;
	private Handler mHandler;

	public UnPaidOrderListAdapter(Context context, List<Order> list,Handler handler) {
		this.mContext = context;
		this.mDataList = list;
		this.inflater = LayoutInflater.from(mContext);
		this.mHandler = handler;
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
			convertView = inflater.inflate(R.layout.layout_unpaid_order_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.place_order_time_text);
			viewHolder.totalPriceText = (TextView) convertView.findViewById(R.id.total_price_text);
			viewHolder.countText = (TextView) convertView.findViewById(R.id.total_count_text);
			viewHolder.payBtn = (Button) convertView.findViewById(R.id.pay_btn);
			viewHolder.listView = (ListView) convertView.findViewById(R.id.listView);;
			viewHolder.listView.setDivider(mContext.getResources().getDrawable(android.R.color.white));
			viewHolder.listView.setDividerHeight(5);
			viewHolder.listView.setCacheColorHint(mContext.getResources().getColor(android.R.color.transparent));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final Order order = mDataList.get(position);
		/*OrderProductListAdapter listAdapter = new OrderProductListAdapter(mContext, order.dataList);
		viewHolder.listView.setAdapter(listAdapter);
		viewHolder.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Bundle bundle = new Bundle();
				bundle.putSerializable(Constant.INTENT_KEY_ORDER_DETAIL, order);
				Activity activity = (Activity) mContext;
				IntentUtil.startActivity(activity, OrderDetailsActivity.class, bundle);
			}
		});
		
		viewHolder.totalPriceText.setText("合计：¥" + order.totalPrice + " (含运费：¥" + 0.00 + ")");
		viewHolder.countText.setText("共" + order.dataList.size()+"件商品");
		viewHolder.nameText.setText(Utils.getDateText(Long.parseLong(order.time)*1000));

		viewHolder.payBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Message msg = new Message();
				msg.what = UnPaidOrderListFragment.GO_TO_PAY;
				Bundle bundle = new Bundle();
				bundle.putString("orderNo",order.orderNo);
				bundle.putDouble("money", order.totalPrice);
				msg.setData(bundle);
				mHandler.sendMessage(msg);
			}
		});*/
		return convertView;
	}

	class ViewHolder {
		TextView nameText;
		TextView totalPriceText;
		TextView countText;
		ListView listView;
		Button payBtn;
	} 

}
