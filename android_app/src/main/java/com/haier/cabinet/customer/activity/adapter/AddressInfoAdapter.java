package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ChooseAddressListActivity;
import com.haier.cabinet.customer.activity.UserAddressAddActvity;
import com.haier.cabinet.customer.activity.UserAddressListActivity;
import com.haier.cabinet.customer.base.ListBaseAdapter;
import com.haier.cabinet.customer.entity.AddressInfo;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.viewholder.AddressViewHolder;
import com.haier.common.util.IntentUtil;

import java.util.Collection;
import java.util.HashMap;

public class AddressInfoAdapter extends ListBaseAdapter<AddressInfo> {

    private HashMap<Integer, Boolean> isCheckMap = new HashMap<>();
    private String addressId;
    private Handler mHandler;
    private int mOperationtType;

    public AddressInfoAdapter(int operationtType, String addressId, Handler handler) {
        this.addressId = addressId;
        this.mHandler = handler;
        this.mOperationtType = operationtType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_user_adress_list_item, parent, false);
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).id.equals(addressId)) {
                isCheckMap.put(i, true);
                break;
            }
        }

        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final AddressViewHolder viewHolder = (AddressViewHolder) holder;

        final AddressInfo addressInfo = mDataList.get(position);
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

        if (TextUtils.isEmpty(addressId) && (mOperationtType == Constant.DISPLAY_ADDRESS_LIST)) {
            viewHolder.arrowImage.setVisibility(View.VISIBLE);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("address", addressInfo);
                    IntentUtil.startActivity((Activity) mContext,
                            UserAddressAddActvity.class, bundle);

                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setMessage("确定要删除该地址吗?")
                            .setPositiveButton("确定",
                                    new AlertDialog.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mHandler.obtainMessage(UserAddressListActivity.DELETE_ADDRESS, position, -1, addressInfo).sendToTarget();

                                        }
                                    })
                            .setNegativeButton("取消",
                                    new AlertDialog.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                        }
                                    }).create();// 创建
                    // 显示对话框
                    dialog.show();
                    return true;
                }
            });

        } else {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mHandler.obtainMessage(ChooseAddressListActivity.CHOOSE_ORDER_ADDRESS, addressInfo).sendToTarget();

                }
            });
            if (isCheckMap != null && isCheckMap.containsKey(position) && addressInfo.id.equals(addressId)) {
                viewHolder.selectCbx.setChecked(true);
                viewHolder.selectCbx.setVisibility(View.VISIBLE);
            } else {
                viewHolder.selectCbx.setVisibility(View.INVISIBLE);
            }

        }


    }

}
