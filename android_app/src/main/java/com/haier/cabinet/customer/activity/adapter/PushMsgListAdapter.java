package com.haier.cabinet.customer.activity.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.LogisticsActivity;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.PushMsg;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.Utils;

public class PushMsgListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<PushMsg> mDataList = new ArrayList<>();
    private LayoutInflater inflater;

    public PushMsgListAdapter(Context context) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(mContext);
    }

    public void addAll(ArrayList<PushMsg> list) {
        int lastIndex = this.mDataList.size();
        if (this.mDataList.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size());
        }
    }

    public void clear() {
        if (mDataList.size() > 0) {
            mDataList.clear();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.layout_push_msg_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        final PushMsg msg = mDataList.get(position);
        viewHolder.titleText.setText(msg.title);
        viewHolder.timeText.setText(Utils.getDateText(Long.parseLong(msg.createTime)));
        viewHolder.contentText.setText(msg.content);
        if (msg.proId.equals("null") || TextUtils.isEmpty(msg.proId)) {
            viewHolder.iv_arrow.setVisibility(View.GONE);
            viewHolder.linear_msg.setOnClickListener(null);
        }else {
            viewHolder.iv_arrow.setVisibility(View.VISIBLE);
            viewHolder.linear_msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("orderNo", msg.proId);
                    Activity activity = (Activity) mContext;
                    IntentUtil.startActivity(activity, LogisticsActivity.class, bundle);
                }
            });
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private TextView titleText;
        private TextView timeText;
        private TextView contentText;
        private LinearLayout linear_msg;
        private ImageView iv_arrow;

        public ViewHolder(View itemView) {
            super(itemView);
            linear_msg = (LinearLayout) itemView.findViewById(R.id.linear_msg);
            titleText = (TextView) itemView.findViewById(R.id.msg_title_text);
            timeText = (TextView) itemView.findViewById(R.id.msg_time_text);
            contentText = (TextView) itemView.findViewById(R.id.msg_content_text);
            iv_arrow = (ImageView) itemView.findViewById(R.id.iv_arrow);
        }
    }
}
