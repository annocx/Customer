package com.haier.cabinet.customer.activity.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.PinnedSectionListView.PinnedSectionListAdapter;
import com.haier.cabinet.customer.entity.Express;
import com.haier.cabinet.customer.entity.PackageBox;
import com.haier.common.util.AppToast;

public class ExpressListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<Express> mDataList;
    private LayoutInflater inflater;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    public ExpressListAdapter(Context context, ArrayList<Express> list) {
        this.mContext = context;
        this.mDataList = list;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.layout_express_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        final Express express = mDataList.get(position);

        if (express.type == PackageBox.SECTION) {
            viewHolder.sectionView.setVisibility(View.VISIBLE);
            viewHolder.sectionText.setText(express.content);
            viewHolder.expressView.setVisibility(View.GONE);
        } else {
            viewHolder.sectionView.setVisibility(View.GONE);
        }

        viewHolder.expressImage.setImageResource(express.icon_resId);
        viewHolder.expressText.setText(express.name);

        if (express.property == Express.POSTMAN) {
            viewHolder.usernameText.setVisibility(View.VISIBLE);
            viewHolder.expressText.setTextColor(mContext.getResources().getColor(R.color.gray_text_one));
            viewHolder.expressText.setTextSize(14.0f);
            viewHolder.usernameText.setText(express.username);
            viewHolder.phoneText.setText("电话：" + express.phone);
        } else if (express.property == Express.COMPANY) {
            viewHolder.usernameText.setVisibility(View.GONE);
            viewHolder.phoneText.setText("客服电话：" + express.phone);
            viewHolder.expressText.setTextSize(18.0f);
            viewHolder.expressText.setTextColor(mContext.getResources().getColor(android.R.color.black));
        }

        viewHolder.callBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                callPostman(mContext, express.phone);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addAll(ArrayList<Express> list) {
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
    public long getItemId(int position) {
        return position;
    }

    private void callPostman(Context context, String number) {
        //用intent启动拨打电话
        Activity activity = (Activity) context;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
            return;
        }
        activity.startActivity(intent);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private View sectionView;
        private View expressView;
        private TextView sectionText;
        private ImageView expressImage;
        private TextView expressText;
        private TextView usernameText;
        private TextView phoneText;
        private Button callBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            sectionView = itemView.findViewById(R.id.section_layout);
            sectionText = (TextView) itemView.findViewById(R.id.section_text);
            expressView = itemView.findViewById(R.id.express_layout);
            expressImage = (ImageView) itemView.findViewById(R.id.express_avater);
            usernameText = (TextView) itemView.findViewById(R.id.name_text);
            expressText = (TextView) itemView.findViewById(R.id.express_text);
            phoneText = (TextView) itemView.findViewById(R.id.phone_text);
            callBtn = (Button) itemView.findViewById(R.id.call_btn);
        }
    }

}
