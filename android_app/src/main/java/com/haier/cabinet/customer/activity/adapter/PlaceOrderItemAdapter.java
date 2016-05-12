package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.UserCouponsActivity;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.ShopCartItem;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.MathUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.common.util.IntentUtil;
import com.sunday.statagent.StatAgent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PlaceOrderItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<ShopCartItem> dataList = new ArrayList<>();
    private LayoutInflater inflater;

    public PlaceOrderItemAdapter(Context context, List<ShopCartItem> dataList) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(mContext);
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList != null ? dataList.size() : 0;
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
        ViewHolder viewHolder;
        double couponPrice = 0.00;
        double coupon_total = 0.00;
        boolean isBoutique = false;
        String goodsCouponId = "";
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_place_order_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.shopNameText = (TextView) convertView.findViewById(R.id.shop_name_text);
            viewHolder.freightText = (TextView) convertView.findViewById(R.id.order_freight_text);
            viewHolder.totalPriceText = (TextView) convertView.findViewById(R.id.order_total_price_text);
            viewHolder.listView = (ListView) convertView.findViewById(R.id.listView);
            viewHolder.listView.setDivider(mContext.getResources().getDrawable(R.color.app_bg));
            viewHolder.listView.setDividerHeight(15);
            viewHolder.listView.setCacheColorHint(mContext.getResources().getColor(android.R.color.transparent));
            viewHolder.relative_coupon = (RelativeLayout) convertView.findViewById(R.id.relative_coupon);
            viewHolder.tv_coupon = (TextView) convertView.findViewById(R.id.tv_coupon);
            viewHolder.ic_coupon_more = (ImageView) convertView.findViewById(R.id.ic_coupon_more);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ShopCartItem cartItem = dataList.get(position);

        PlaceOrderSubListAdapter adapter = new PlaceOrderSubListAdapter(mContext, cartItem.products);
        viewHolder.listView.setAdapter(adapter);

        viewHolder.shopNameText.setText(cartItem.shop.name);
        viewHolder.freightText.setText(String.valueOf(cartItem.freight));
        viewHolder.totalPriceText.setText("￥"+String.valueOf(calculatingOrderPrice(cartItem)));

        for (int i = 0; i < getDataList().get(position).products.size(); i++) {
            couponPrice = getDataList().get(position).products.get(i).couponPrice;//只要店铺中某个商品能够使用优惠劵，就显示1个优惠劵
            goodsCouponId = getDataList().get(position).products.get(i).goodsCouponId;//优惠劵返回的所有商品id
            coupon_total += couponPrice;
//            if (!TextUtils.isEmpty(getDataList().get(position).products.get(i).boutique)) {//如果有一个不是精品
//                isBoutique = true;
//            } /*else {
//                isBoutique = false;
//            }*/
        }
//        if (isBoutique) {
            if (coupon_total != 0) {
                viewHolder.tv_coupon.setBackgroundColor(mContext.getResources().getColor(R.color.common_color));
                viewHolder.tv_coupon.setTextColor(mContext.getResources().getColor(R.color.color_white));
                viewHolder.tv_coupon.setText(String.format(mContext.getString(R.string.produce_coupon), "" + coupon_total));
            } else {
                viewHolder.tv_coupon.setBackgroundColor(mContext.getResources().getColor(R.color.color_white));
                viewHolder.tv_coupon.setTextColor(mContext.getResources().getColor(R.color.light_grey));
                if (TextUtils.isEmpty(goodsCouponId)) {//如果商品数组是空的
                    viewHolder.tv_coupon.setText(mContext.getString(R.string.produce_select_coupon));
                } else {
                    viewHolder.tv_coupon.setText(mContext.getString(R.string.produce_no_select_coupon));
                }
            }
            viewHolder.relative_coupon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatAgent.initAction(mContext, "", "2", "15", "", "", "choose coupon", "1", "");

                    StringBuilder sb = new StringBuilder();
                    for (Product product : cartItem.products) {
                        sb.append(product.id).append("_");
                    }
                    String goods_id = sb.toString().substring(0, sb.length() - 1);
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constant.INTENT_KEY_FROM, 1);
                    bundle.putString(Constant.INTENT_KEY_GOODS_ID, goods_id);
                    bundle.putString(Constant.INTENT_KEY_COUPON_CODE, getAllCouponSn());
                    IntentUtil.startActivityResult((Activity) mContext, UserCouponsActivity.class, bundle, Constant.REQUEST_COUPON_PIRCE);
                }
            });
            viewHolder.ic_coupon_more.setVisibility(View.VISIBLE);
//        } else {
//            viewHolder.tv_coupon.setBackgroundColor(mContext.getResources().getColor(R.color.color_white));
//            viewHolder.tv_coupon.setTextColor(mContext.getResources().getColor(R.color.light_grey));
//            viewHolder.tv_coupon.setText(mContext.getString(R.string.produce_no_coupon));
//            viewHolder.ic_coupon_more.setVisibility(View.GONE);
//            viewHolder.relative_coupon.setOnClickListener(null);
//        }
        return convertView;
    }

    class ViewHolder {
        TextView shopNameText;
        ListView listView;
        TextView freightText;
        TextView totalPriceText;
        //优惠劵
        RelativeLayout relative_coupon;
        TextView tv_coupon;
        ImageView ic_coupon_more;
    }

    public List<ShopCartItem> getDataList() {
        return this.dataList;
    }

    /**
     * 不包含运费
     *
     * @param cartItem
     * @return
     */
    public double calculatingOrderPrice(ShopCartItem cartItem) {
        double total = 0.00;
        double coupon_price = 0.00;
        for (Product product : cartItem.products) {
            if (product.cid == 99) {
                Bracket bracket = Util.getDiscount(product);
                if (bracket == null) {
                    total += MathUtil.mul(product.count, product.discountPrice);
                } else {
                    total += MathUtil.mul(product.count, bracket.price);
                }
            } else {
                total += MathUtil.mul(product.count, product.discountPrice);
            }
//            coupon_price += product.couponPrice;
            coupon_price += product.couponPrice;
        }

        //double值保留 2 位小数,使用银行家舍入法
        return MathUtil.round(total + cartItem.freight - coupon_price, 2, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * 订单的价格
     *
     * @return
     */
    public double calculatingTotalOrderPrice(ShopCartItem cartItem, double orderPrice) {
        double total = orderPrice;
        total += getOrderFreight(cartItem, total);
        //double值保留 2 位小数,使用银行家舍入法
        return MathUtil.round(total, 2, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * 总订单的价格(包含运费)
     *
     * @return
     */
    public double calculatingTotalPrice() {
        double total = 0.00;
        double coupon_total = 0.00;//所有店铺商品优惠劵总额
        double couponPrice = 0.00;//每个商品优惠劵金额
        for (ShopCartItem cartItem : getDataList()) {
            double orderPrice = cartItem.orderTotalPirce;
            total += orderPrice;
        }
        for (int i = 0; i < getDataList().size(); i++) {
            for (int j = 0; j < getDataList().get(i).products.size(); j++) {
                couponPrice = getDataList().get(i).products.get(j).couponPrice;//只要店铺中某个商品能够使用优惠劵，就显示1个优惠劵
                coupon_total += couponPrice;
            }
        }
        //double值保留 2 位小数,使用银行家舍入法
        return MathUtil.round(total - coupon_total, 2, BigDecimal.ROUND_HALF_EVEN);
    }

    /*
     * 总订单的价格(不包含运费)
     * @return
     */
    public double calculatingTotalPriceNoFreight() {
        double total = 0.00;
        for (ShopCartItem cartItem : getDataList()) {
            double orderPrice = calculatingOrderPrice(cartItem);
            total += orderPrice;
        }

        //double值保留 2 位小数,使用银行家舍入法
        return MathUtil.round(total, 2, BigDecimal.ROUND_HALF_EVEN);
    }


    /**
     * 计算运费
     *
     * @return
     */
    public double getOrderFreight(ShopCartItem cartItem, double orderPrice) {
        double freight = cartItem.shop.fee;
        if (orderPrice >= cartItem.shop.free_delivery_pirce) {
            freight = 0.0;
        }
        return freight;
    }

    /**
     * 优惠劵
     */
    public void updateProduct(int id, double money) {
        for (int i = 0; i < getDataList().size(); i++) {
            List<Product> products = dataList.get(i).products;
            for (int j = 0; j < products.size(); j++) {
                Product product = products.get(j);
                if (product.id == id) {
                    product.couponPrice = money;
                    notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * 遍历所有优惠劵sn
     */
    public String getAllCouponSn() {
        StringBuilder sb = new StringBuilder();
        String couponSn_array = "";
        for (int i = 0; i < getDataList().size(); i++) {
            List<Product> products = dataList.get(i).products;
            for (int j = 0; j < products.size(); j++) {
                Product product = products.get(j);
                if (!TextUtils.isEmpty(product.couponCode)) {
                    sb.append(product.couponCode).append("_");
                }
            }
        }
        if (!TextUtils.isEmpty(sb.toString())) {
            couponSn_array = sb.toString().substring(0, sb.length() - 1);
        }
        return couponSn_array;
    }
}
