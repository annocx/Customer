package com.haier.cabinet.customer.activity.adapter;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.PackageBoxDetailsActivity;
import com.haier.cabinet.customer.entity.Supply;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.PinnedSectionListView.PinnedSectionListAdapter;
import com.haier.cabinet.customer.entity.PackageBox;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.Utils;

public class PackageBoxListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<PackageBox> mPackagetList = new ArrayList<>();
    private Resources mResources;

    private int unPickListSize = 0;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    public PackageBoxListAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mResources = context.getResources();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mPackagetList.size();
    }

    public void addAll(ArrayList<PackageBox> list) {
        int lastIndex = this.mPackagetList.size();
        if (this.mPackagetList.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size());
        }
    }

    public void clear() {
        if (mPackagetList.size() > 0) {
            mPackagetList.clear();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.layout_cabinet_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        final PackageBox packageBox = mPackagetList.get(position);

        if (packageBox.type == PackageBox.SECTION) {
            viewHolder.packageBoxView.setVisibility(View.GONE);
            viewHolder.sectionView.setVisibility(View.VISIBLE);
            viewHolder.sectionText.setText(packageBox.content);
        } else {
            viewHolder.sectionView.setVisibility(View.GONE);
            viewHolder.packageBoxView.setVisibility(View.VISIBLE);
            //packageStatus 0:在箱正常;5.投递员取回;6.逾期回收;7.异常回收;8.已取件9.管理员取回
            if (packageBox.packageStatus == 0) {//待取件
                viewHolder.unTakePackageBoxView.setVisibility(View.VISIBLE);
                viewHolder.takenPackageBoxView.setVisibility(View.GONE);

                if (packageBox.isTimeout) {
                    viewHolder.expressStateText.setText("已超期");
                    viewHolder.expressStateText.setTextColor(mContext.getResources().getColor(R.color.red_light));

                } else {
                    viewHolder.expressStateText.setText("待取");
                }

                /*viewHolder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("total", getUnPickListSize());
                        bundle.putSerializable("packageBox", packageBox);
                        Activity activity = (Activity) mContext;
                        IntentUtil.startActivity(activity, PackageBoxDetailsActivity.class, bundle);
                    }
                });*/

            } else {
                viewHolder.unTakePackageBoxView.setVisibility(View.GONE);
                viewHolder.takenPackageBoxView.setVisibility(View.VISIBLE);
                viewHolder.pickTimeText.setText(packageBox.pickTime);

                switch (packageBox.packageStatus) {
                    case 8:
                        viewHolder.pickTimeText.setTextColor(mContext.getResources().getColor(R.color.common_black_text));
                        viewHolder.callTakenPostmanBtn.setVisibility(View.GONE);
                        break;
                    case 5:
                    case 6:
                    case 7:
                    case 9:
                        viewHolder.callTakenPostmanBtn.setVisibility(View.VISIBLE);
                        if (packageBox.isTimeout) {
                            viewHolder.pickTimeText.setText("已超时回收，请尽快联系快递员");
                            viewHolder.pickTimeText.setTextColor(Color.RED);
                        } else {
                            viewHolder.pickTimeText.setTextColor(mContext.getResources().getColor(R.color.common_black_text));
                        }
                        break;

                    default:
                        break;
                }

            }

            String packageBoxNo = String.format(mResources.getString(R.string.box_no), packageBox.boxNo);
            final String content = packageBox.cabinetAddress + "  " + packageBoxNo;
            SpannableString spanText = new SpannableString(content);
            spanText.setSpan(new ForegroundColorSpan(mResources.getColor(R.color.blue_text)), packageBox.cabinetAddress.length() + 2, spanText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            viewHolder.cabinetNameText.setText(spanText);
            viewHolder.takenCabinetNameText.setText(spanText);

            viewHolder.pickupNoText.setText(packageBox.pickUpNo);
            viewHolder.expressNoText.setText(packageBox.packageNo);
            /* expressCompanyText.setText(packageBox.expressCompany);
			 expressCompanyText.setText("运单号");*/
            viewHolder.postmanMobileText.setText(packageBox.postmanMobile);
            viewHolder.takenPostmanMobileText.setText(packageBox.postmanMobile);
            viewHolder.deliveryTimeText.setText(packageBox.deliveredTime);

            viewHolder.callPostmanBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Utils.call(mContext, packageBox.postmanMobile);
                    callPostman(mContext, packageBox.postmanMobile);
                }
            });
            viewHolder.callTakenPostmanBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Utils.call(mContext, packageBox.postmanMobile);
                    callPostman(mContext, packageBox.postmanMobile);
                }
            });
            viewHolder.packageBoxLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PackageBox box = mPackagetList.get(position);
                    if (box.type == PackageBox.ITEM) {
                        if (box.packageStatus == 0) {//待取件
                            Bundle bundle = new Bundle();
                            bundle.putInt("total", getUnPickListSize());
                            bundle.putSerializable("packageBox", box);
                            Activity activity = (Activity) mContext;
                            IntentUtil.startActivity(activity, PackageBoxDetailsActivity.class, bundle);
                        }

                    }
                }
            });

            if (unPickListSize > 0) {
                if (position == unPickListSize || position == mPackagetList.size()) {
                    viewHolder.separationView.setVisibility(View.GONE);
                } else {
                    viewHolder.separationView.setVisibility(View.GONE);
                }
            } else {
                if (position == mPackagetList.size()) {
                    viewHolder.separationView.setVisibility(View.GONE);
                } else {
                    viewHolder.separationView.setVisibility(View.GONE);
                }
            }

        }
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

    public int getUnPickListSize() {
        return unPickListSize;
    }

    public void setUnPickListSize(int unPickListSize) {
        this.unPickListSize = unPickListSize;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private View packageBoxLayout;
        private View sectionView;
        private View packageBoxView;
        private View unTakePackageBoxView;//待取件视图
        private View takenPackageBoxView;//已取件视图
        private View separationView;//分隔线
        private TextView sectionText;
        private TextView cabinetNameText;
        private TextView packageBoxNoText;
        private TextView pickupNoText;
        private TextView expressNoText;
        private TextView expressCompanyText;
        private TextView postmanMobileText;
        private TextView takenPostmanMobileText;
        private TextView takenCabinetNameText;
        private TextView deliveryTimeText;
        private TextView pickTimeText;
        private TextView expressStateText;
        private Button callPostmanBtn;
        private Button callTakenPostmanBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            sectionView = itemView.findViewById(R.id.section_layout);
            packageBoxView = itemView.findViewById(R.id.packagebox_layout);
            unTakePackageBoxView = itemView.findViewById(R.id.untake_layout);
            takenPackageBoxView = itemView.findViewById(R.id.taken_layout);
            separationView = itemView.findViewById(R.id.separation_line_view);
            sectionText = (TextView) itemView.findViewById(R.id.section_text);
            cabinetNameText = (TextView) itemView.findViewById(R.id.cabinet_location_text);
            takenCabinetNameText = (TextView) itemView.findViewById(R.id.taken_address_text);
            pickupNoText = (TextView) itemView.findViewById(R.id.pickup_no_text);
            expressNoText = (TextView) itemView.findViewById(R.id.express_no_text);
            postmanMobileText = (TextView) itemView.findViewById(R.id.postman_telephone_text);
            takenPostmanMobileText = (TextView) itemView.findViewById(R.id.taken_postman_telephone_text);
            deliveryTimeText = (TextView) itemView.findViewById(R.id.delivery_time_text);
            pickTimeText = (TextView) itemView.findViewById(R.id.pickup_time_text);
            expressStateText = (TextView) itemView.findViewById(R.id.express_state_text);
            callPostmanBtn = (Button) itemView.findViewById(R.id.call_postman_btn);
            callTakenPostmanBtn = (Button) itemView.findViewById(R.id.call_taken_postman_btn);
            packageBoxLayout = itemView.findViewById(R.id.package_box_layout);
        }
    }

}
