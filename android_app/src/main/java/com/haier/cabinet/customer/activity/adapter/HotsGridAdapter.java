package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ProductDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.base.BaseFragment;
import com.haier.cabinet.customer.base.BaseListFragment;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.fragment.HomeFragment;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.HardRefSimpleImageLoadingListener;
import com.haier.cabinet.customer.viewholder.ProductsViewHolder;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sunday.statagent.StatAgent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lzx on 2015/12/22.
 */
public class HotsGridAdapter extends RecyclerView.Adapter{
    private Context context=null;
    private Handler handler;
    private int width;//图片宽度
    private ArrayList<Product> dataList = new ArrayList<>();
    private AlbumGridViewAdapter.OnItemClickListener onItemClickListener;

    public HotsGridAdapter(Context context,Handler handler) {
        this.context = context;
        this.handler = handler;
        this.width=(Util.getScreenWidth(context) -(2 *  Util.dip2px(context, 8)))/2;
    }

    public void setDataList(Collection<Product> list) {
        this.dataList.clear();
        this.dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void addAll(Collection<Product> list) {
        int lastIndex = this.dataList.size();
        if (this.dataList.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size());
        }
    }

    public void clear(){
        this.dataList.clear();
        notifyDataSetChanged();
    }

    public List<Product> getDataList() {
        return this.dataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_product_list_item, parent, false);
        return new ProductsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ProductsViewHolder viewHolder = (ProductsViewHolder) holder;
        SpannableString msp = null;
        final Product product = this.dataList.get(position);
        viewHolder.productTitle.setText(product.title);
//        if(TextUtils.isEmpty(product.boutique) || product.boutique.equals("null")){
//            viewHolder.productTitle.setText(product.title);
//        }else{
//            msp = new SpannableString("12"+product.title);
//            Drawable drawable = context.getResources().getDrawable(R.drawable.bg_boutique);
//            drawable.setBounds(0, 0, drawable.getIntrinsicWidth()+5, drawable.getIntrinsicHeight()+5);
//            msp.setSpan(new ImageSpan(drawable,ImageSpan.ALIGN_BOTTOM), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            viewHolder.productTitle.setText(msp);
//        }
        viewHolder.specText.setText(product.spec);
        viewHolder.priceText.setText("¥" + product.discountPrice);
        viewHolder.retailPriceText.setText("¥" + product.retailPrice);
        viewHolder.retailPriceText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        viewHolder.retailPriceText.getPaint().setAntiAlias(true);
        viewHolder.countText.setText(String.valueOf(product.count));

        if (product.count > 0){
            viewHolder.minusBtn.setVisibility(View.VISIBLE);
            viewHolder.countText.setVisibility(View.VISIBLE);
        }else{
            viewHolder.minusBtn.setVisibility(View.GONE);
            viewHolder.countText.setVisibility(View.GONE);
        }

        viewHolder.addBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Util.isFastClick()) {
                    return;
                }

                if (product.count >= product.goods_storage) {
                    AppToast.showShortText(context,"对不起，库存不足!");
                    return;
                }

                int action = HomeFragment.ADD_TO_SHOPPING_CART;
                if(product.count > 0){
                    action = HomeFragment.INCREASE_FROM_SHOPPING_CART;
                }
                if (product.count >=999) {//单个商品数量不能超过999
                    AppToast.showShortText(context, "对不起，购买数量已达上限，另请下单!");
                    return;
                }
                product.count++;
                handler.obtainMessage(action,position,-1,product).sendToTarget();

                StatAgent.initAction(context, "", "2", "2", "5", "", "add product", "1", "");
            }
        });

        viewHolder.minusBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Util.isFastClick()) {
                    return;
                }
                if (product.count > 0) {
                    product.count--;
                    handler.obtainMessage(HomeFragment.REDUCE_FROM_SHOPPING_CART, position, -1, product).sendToTarget();

                    StatAgent.initAction(context, "", "2", "2", "5", "", "minus product", "1", "");
                }
            }
        });

        viewHolder.productImage.setImageResource(R.drawable.ic_product_default);
        ImageLoader.getInstance().displayImage(product.thumbUrl, viewHolder.productImage,
                PushApplication.getInstance().getDefaultOptions(), new HardRefSimpleImageLoadingListener(position) {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {

                        if (bitmap != null) {
                            ImageView imageView = (ImageView) view;
                            // 是否第一次显示
                            boolean firstDisplay = !BaseListFragment.displayedImages.contains(imageUri);
                            if (firstDisplay) {
                                // 图片淡入效果
                                FadeInBitmapDisplayer.animate(imageView, 500);
                                BaseListFragment.displayedImages.add(imageUri);
                            }else {
                                viewHolder.productImage.setImageBitmap(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        super.onLoadingFailed(s, view, failReason);
                        viewHolder.productImage.setImageResource(R.drawable.ic_product_default);

                    }

                });


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                StatAgent.initAction(context, "", "2", "2", "5", "", product.title, "1", "");

                Bundle bundle = new Bundle();
                bundle.putInt(SortResultActivity.RESULT_ID, dataList.get(position).id);
                IntentUtil.startActivity((Activity) context, ProductDetailsActivity.class, bundle);
            }
        });


        //根据屏幕宽度动态设置每个图片的宽高
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width,width);
        viewHolder.productImage.setLayoutParams(param);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin_vertical = context.getResources().getDimensionPixelSize(R.dimen.home_gird_item_vertical_margin);
        int margin_horizontal = context.getResources().getDimensionPixelSize(R.dimen.home_gird_item_horizontal_margin);
        if (position % 2 == 0) {
            lp.setMargins(margin_horizontal, margin_vertical,
                    margin_vertical, margin_vertical);
        } else {
            lp.setMargins(margin_vertical, margin_vertical, margin_horizontal, margin_vertical);
        }
        viewHolder.itemView.setLayoutParams(lp);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    /**
     * 修改商品数量
     * @param id
     * @param count
     */
    public void updateProduct(int id,int count){
        for(int i = 0; i< dataList.size();i++){
            Product product = dataList.get(i);
            if (product.id == id){
                product.count = count;
                dataList.set(i, product);
                notifyItemChanged(i);
            }

        }

    }

    /**
     * 修改商品数量
     * @param id
     * @param count
     */
    public void updateProduct(int id,int count, int shopCartId){
        for(int i = 0; i< dataList.size();i++){
            Product product = dataList.get(i);
            if (product.id == id){
                product.count = count;
                product.shopCardId = shopCartId;
                dataList.set(i, product);
                notifyItemChanged(i);
            }

        }

    }

    /**
     * 商品数量修改为0
     * @param shopCartId
     */
    public void updateProduct(int shopCartId ){
        for(int i = 0; i< dataList.size();i++){
            Product product = dataList.get(i);
            if (product.shopCardId == shopCartId){
                product.count = 0;
                dataList.set(i, product);
                notifyItemChanged(i);
            }
        }
    }
}
