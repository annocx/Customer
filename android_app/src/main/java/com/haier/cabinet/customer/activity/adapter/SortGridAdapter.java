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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ProductDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.base.BaseFragment;
import com.haier.cabinet.customer.base.BaseListFragment;
import com.haier.cabinet.customer.entity.BProduct;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.HardRefSimpleImageLoadingListener;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sunday.statagent.StatAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzx on 15/11/28.
 */
public class SortGridAdapter extends RecyclerView.Adapter {
    private ArrayList<BProduct> mDataList = new ArrayList<>();
    private Context context = null;
    private LayoutInflater mLayoutInflater = null;
    private Handler mHandler;
    private int wh;
    private String pageType = "5";

    public SortGridAdapter(Context context, Handler handler, String pageType) {
        mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.mHandler = handler;
        this.pageType = pageType;
        this.wh = (Util.getScreenWidth(context) - (2 * Util.dip2px(context, 8))) / 2;
    }

    public void addAll(ArrayList<BProduct> list) {
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
        return new ViewHolder(mLayoutInflater.inflate(R.layout.layout_home_product_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final BProduct product = mDataList.get(position);
        SpannableString msp = null;
        final ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.productTitle.setText(""+product.goods_name);
//        if (TextUtils.isEmpty(product.jingpin) || product.jingpin.equals("null")) {
//            viewHolder.productTitle.setText(product.goods_name);
//        } else {
//            msp = new SpannableString("12" + product.goods_name);
//            Drawable drawable = context.getResources().getDrawable(R.drawable.bg_boutique);
//            drawable.setBounds(0, 0, drawable.getIntrinsicWidth()+5, drawable.getIntrinsicHeight()+5);
//            msp.setSpan(new ImageSpan(drawable), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            viewHolder.productTitle.setText(msp);
//        }
        viewHolder.priceText.setText("¥" + product.goods_price);
        viewHolder.retailPriceText.setText("¥" + product.goods_marketprice);
        viewHolder.retailPriceText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        viewHolder.retailPriceText.getPaint().setAntiAlias(true);
        viewHolder.product_spec.setText("" + product.goods_guige);
        viewHolder.countText.setText(product.cart_num + "");

        if (product.cart_num > 0) {
            viewHolder.minusBtn.setVisibility(View.VISIBLE);
            viewHolder.countText.setVisibility(View.VISIBLE);
        } else {
            viewHolder.minusBtn.setVisibility(View.GONE);
            viewHolder.countText.setVisibility(View.GONE);
        }

        viewHolder.addBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Util.isFastClick()) {
                    return;
                }
                if (product.cart_num >= product.goods_storage) {
                    AppToast.showShortText(context, "对不起，库存不足!");
                    return;
                }

                if (product.cart_num >=999) {
                    AppToast.showShortText(context, "对不起，购买数量已达上限，另请下单!");
                    return;
                }

                int action = SortResultActivity.ADD_TO_SHOPPING_CART;
                if (product.cart_num > 0) {
                    action = SortResultActivity.INCREASE_FROM_SHOPPING_CART;
                }
                product.cart_num++;
                mHandler.obtainMessage(action, position, -1, product).sendToTarget();

                StatAgent.initAction(context, "", "2", pageType, "", "", "add product", "1", "");
            }
        });

        viewHolder.minusBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Util.isFastClick()) {
                    return;
                }
                if (product.cart_num > 0) {
                    product.cart_num--;
                    mHandler.obtainMessage(SortResultActivity.REDUCE_FROM_SHOPPING_CART, position, -1, product).sendToTarget();
                }

                StatAgent.initAction(context, "", "2", pageType, "", "", "minus product", "1", "");
            }
        });

        viewHolder.productImage.setImageResource(R.drawable.ic_product_default);
        ImageLoader.getInstance().displayImage(product.goods_image, viewHolder.productImage,
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
                            } else {
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
        //根据屏幕宽度动态设置每个cell的宽高
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(wh, wh);
        viewHolder.productImage.setLayoutParams(param);
        viewHolder.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(SortResultActivity.RESULT_ID, product.goods_id);
//                bundle.putInt(ProductDetailsActivity.PRODUCT_TYPE, ProductDetailsActivity.ACTION_JPTJ);
                Activity activity = (Activity) context;
                IntentUtil.startActivity(activity, ProductDetailsActivity.class, bundle);

                StatAgent.initAction(context, "", "2", pageType, "", "", product.goods_name, "1", "");
            }
        });

        /*FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        if (position % 2 == 0) {
            lp.setMargins(16, 8, 8, 8);
        } else {
            lp.setMargins(8, 8, 16, 8);
        }
        viewHolder.itemView.setLayoutParams(lp);*/
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

        private TextView productTitle;
        private ImageView productImage;
        private TextView priceText;
        private TextView retailPriceText;//原价
        private TextView product_spec;
        private EditText countText;
        private Button addBtn;
        private Button minusBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            productTitle = (TextView) itemView.findViewById(R.id.product_name);
            productImage = (ImageView) itemView.findViewById(R.id.product_img);
            priceText = (TextView) itemView.findViewById(R.id.discount_price_text);
            retailPriceText = (TextView) itemView.findViewById(R.id.retail_price_text);
            product_spec = (TextView) itemView.findViewById(R.id.product_spec);
            countText = (EditText) itemView.findViewById(R.id.pro_count_text);
            addBtn = (Button) itemView.findViewById(R.id.addBtn);
            minusBtn = (Button) itemView.findViewById(R.id.minusBtn);
        }
    }

    private List<BProduct> getDataList() {
        return mDataList;
    }

    public void updateList(int position, BProduct product) {
        getDataList().set(position, product);
    }

    /**
     * 修改商品数量
     *
     * @param id
     * @param count
     */
    public void updateProduct(int id, int count) {
        for (int i = 0; i < getDataList().size(); i++) {
            BProduct product = getDataList().get(i);
            if (product.goods_id == id) {
                product.cart_num = count;
                getDataList().set(i, product);
                notifyItemChanged(i);
            }

        }

    }

    /**
     * 修改商品数量
     *
     * @param id
     * @param count
     */
    public void updateProduct(int id, int count, int shopCartId) {
        for (int i = 0; i < getDataList().size(); i++) {
            BProduct product = getDataList().get(i);
            if (product.goods_id == id) {
                product.cart_num = count;
                product.cart_id = shopCartId;
                getDataList().set(i, product);
                notifyItemChanged(i);
            }

        }

    }

    /**
     * 商品数量修改为0
     *
     * @param shopCartId
     */
    public void updateProduct(int shopCartId) {
        Log.d("lzx", "updateProduct shopCartId = " + shopCartId);
        for (int i = 0; i < getDataList().size(); i++) {
            BProduct product = getDataList().get(i);
            if (product.cart_id == shopCartId) {
                product.cart_num = 0;
                getDataList().set(i, product);
                notifyItemChanged(i);
            }
        }
    }
}
