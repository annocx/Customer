package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.Coupon;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DateUtil;
import com.haier.common.util.AppToast;
import com.sunday.statagent.StatAgent;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by SK on 2016/2/18.
 */
public class
CouponsAdapter extends RecyclerView.Adapter {

    private Context context;

    private int from = -1;

    private ArrayList<Coupon> mDataList = new ArrayList<>();

    public CouponsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_coupons, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        final Coupon coupon = mDataList.get(position);

        if (null != coupon) {

            viewHolder.couponSummary.setText(coupon.getCoupon_Show_Summary());
            viewHolder.discountName.setText(coupon.getDiscountShowName().replace(" ", "\n"));
            viewHolder.couponName.setText(coupon.getCouponShowName());

            if (Integer.valueOf(coupon.getDiscount()) > 99) {
                viewHolder.money.setTextSize(40);
                viewHolder.money.setPadding(0, 40, 0, 0);
            }
            viewHolder.money.setText(coupon.getDiscount());

            viewHolder.beginTime.setText("开始日期：" + DateUtil.getYYMMDD(coupon.getUseFromTime()));
            viewHolder.vaildTime.setText("结束日期：" + DateUtil.getYYMMDD(coupon.getCoupon_use_time()));

            if (coupon.getShowStatus().equals("可使用")) {
                viewHolder.status.setBackgroundResource(R.drawable.ic_coupon_usable);
                setRedBackgroundColor(viewHolder);
            } else if (coupon.getShowStatus().equals("未开始")) {
                viewHolder.status.setBackgroundResource(R.drawable.ic_coupon_notstarted);
                setGreyBackgroundColor(viewHolder);
            } else if (coupon.getShowStatus().equals("已使用")) {
                viewHolder.status.setBackgroundResource(R.drawable.ic_coupon_used);
                setGreyBackgroundColor(viewHolder);
            } else if (coupon.getShowStatus().equals("已过期")) {
                viewHolder.status.setBackgroundResource(R.drawable.ic_coupon_expired);
                setGreyBackgroundColor(viewHolder);
            }

        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (from == 0) return;

                if (!coupon.getShowStatus().equals("可使用")) return;

                StatAgent.initAction(context, "", "2", "18", "", "", coupon.getCoupon_sn(), "1", "");

                Intent intent = new Intent();
                intent.putExtra(Constant.INTENT_KEY_COUPON_CODE, coupon.getCoupon_sn()); // 优惠券code
                intent.putExtra(Constant.INTENT_KEY_GOODS_ID, coupon.getCoupon_good_id()); // 优惠商品ID
                intent.putExtra(Constant.INTENT_KEY_COUPON_DISCOUNT, Double.valueOf(coupon.getDiscount()));  // 优惠券金额
                Activity activity = (Activity) context;
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            }
        });
    }

    private void setRedBackgroundColor(ViewHolder viewHolder) {
        viewHolder.leftSideImg.setImageResource(R.drawable.ic_coupon_red_left);
        viewHolder.leftSideLayout.setBackgroundColor(Color.parseColor("#f23039"));
        viewHolder.dotLineImg.setBackgroundColor(Color.parseColor("#f23039"));
        viewHolder.rightSideLayout.setBackgroundColor(Color.parseColor("#e72932"));
        viewHolder.rightSideImg.setImageResource(R.drawable.ic_coupon_red_right);
    }

    private void setGreyBackgroundColor(ViewHolder viewHolder) {
        viewHolder.leftSideImg.setImageResource(R.drawable.ic_coupon_grey_left);
        viewHolder.leftSideLayout.setBackgroundColor(Color.parseColor("#acacac"));
        viewHolder.dotLineImg.setBackgroundColor(Color.parseColor("#acacac"));
        viewHolder.rightSideLayout.setBackgroundColor(Color.parseColor("#a3a2a2"));
        viewHolder.rightSideImg.setImageResource(R.drawable.ic_coupon_grey_right);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private TextView money;

        private TextView status;

        private TextView vaildTime;
        private TextView beginTime;

        private TextView couponName, discountName, couponSummary;

        private ImageView leftSideImg, rightSideImg;

        private View leftSideLayout, rightSideLayout;

        private ImageView dotLineImg;

        public ViewHolder(View itemView) {
            super(itemView);
            money = (TextView) itemView.findViewById(R.id.money);
            status = (TextView) itemView.findViewById(R.id.status);
            vaildTime = (TextView) itemView.findViewById(R.id.valid_time);
            beginTime = (TextView) itemView.findViewById(R.id.begin_time);
            leftSideImg = (ImageView) itemView.findViewById(R.id.left_side_img);
            leftSideLayout = itemView.findViewById(R.id.left_side_layout);
            rightSideImg = (ImageView) itemView.findViewById(R.id.right_side_img);
            rightSideLayout = itemView.findViewById(R.id.right_side_layout);
            dotLineImg = (ImageView) itemView.findViewById(R.id.dot_line);
            couponName = (TextView) itemView.findViewById(R.id.coupon_name);
            discountName = (TextView) itemView.findViewById(R.id.discount_name);
            couponSummary = (TextView) itemView.findViewById(R.id.coupon_summary);
        }
    }

    public void addAll(Collection<Coupon> list) {
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

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

}
