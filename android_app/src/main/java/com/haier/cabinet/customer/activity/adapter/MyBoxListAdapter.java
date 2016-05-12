package com.haier.cabinet.customer.activity.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.HandleScanResultActivity;
import com.haier.cabinet.customer.entity.PackageBox;

public class MyBoxListAdapter extends BaseAdapter {

	private Context mContext;
	private List<PackageBox> mDataList;
	private LayoutInflater inflater;
	private Handler mHandler;
	
	private int boxNo = 0;
	private boolean isSuccess = false;
	
	public MyBoxListAdapter(Context context, Handler handler, List<PackageBox> list) {
		this.mContext = context;
		this.mDataList = list;
		this.inflater=LayoutInflater.from(mContext);
		this.mHandler = handler;
	}
	
	@Override
	public int getCount() {
		return mDataList!=null ? mDataList.size():0;
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
		ViewHolder viewHolder=null;
		if(convertView==null){
			convertView=inflater.inflate(R.layout.layout_user_box_list_item, null);
			viewHolder=new ViewHolder();
			viewHolder.boxNoText=(TextView)convertView.findViewById(R.id.box_no_text);
			viewHolder.openBtn = (Button)convertView.findViewById(R.id.open_box_button);

			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		
		final PackageBox packageBox = mDataList.get(position);
		viewHolder.boxNoText.setText(String.valueOf(packageBox.boxNo));
		
		if (packageBox.boxNo == boxNo) {
			if (isSuccess) {
				viewHolder.openBtn.setText("已开箱");
				viewHolder.openBtn.setTextColor(mContext.getResources().getColor(R.color.button_text_gray));
				viewHolder.openBtn.setEnabled(false);
			} else {
				viewHolder.openBtn.setText("重新开箱");
			}
		}
		
		
		viewHolder.openBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				msg.obj = packageBox;
				msg.what = HandleScanResultActivity.TRY_OPEN_EXPRESS;
				mHandler.sendMessage(msg);
			}
		});
		return convertView;
	}

	class ViewHolder{
		TextView boxNoText;
		TextView timeText;
		Button openBtn;
	}

	public void updateItem(int boxNo, boolean isSuccess) {
		this.boxNo = boxNo;
		this.isSuccess = isSuccess;
		notifyDataSetChanged();
	}
}
