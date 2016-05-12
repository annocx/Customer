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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ProductDetailsActivity;
import com.haier.cabinet.customer.activity.ShopDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.base.BaseFragment;
import com.haier.cabinet.customer.base.BaseListFragment;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.HardRefSimpleImageLoadingListener;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sunday.statagent.StatAgent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ShopProductListAdapter extends RecyclerView.Adapter {

    private int width;
    private Handler mHandler;
    private Context context;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Product> mDataList = new ArrayList<>();
    private HashMap<Integer, String> isCheckMap = new HashMap<Integer, String>();

    public ShopProductListAdapter(Context context, Handler mHandler) {
        mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.mHandler = mHandler;
        this.width = (Util.getScreenWidth(context) - (2 * Util.dip2px(context, 8))) / 2;
    }

    public void setDataList(Collection<Product> list) {
        this.mDataList.clear();
        this.mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Product> list) {
        int lastIndex = this.mDataList.size();
        if (this.mDataList.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size());
        }
    }

    public void updateList(int position, Product product) {
        getDataList().set(position, product);
    }

    public List<Product> getDataList() {
        return mDataList;
    }

    public Object getItem(int position) {
        return this.mDataList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.adapter_shop, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Product product = mDataList.get(position);
        SpannableString msp = null;
        final ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.title.setText(""+product.title);
//        if (TextUtils.isEmpty(product.boutique) || product.boutique.equals("null")) {
//            viewHolder.title.setText(product.title);
//        } else {
//            msp = new SpannableString("12" + product.title);
//            Drawable drawable = context.getResources().getDrawable(R.drawable.bg_boutique);
//            drawable.setBounds(0, 0, drawable.getIntrinsicWidth()+5, drawable.getIntrinsicHeight()+5);
//            msp.setSpan(new ImageSpan(drawable), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            viewHolder.title.setText(msp);
//        }
        viewHolder.productLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(SortResultActivity.RESULT_ID, product.id);
                IntentUtil.startActivity((Activity) context, ProductDetailsActivity.class, bundle);
            }
        });

        if (null != product.spec) {
            viewHolder.specText.setText("" + product.spec);
            viewHolder.specText.setVisibility(View.GONE);
        } else {
            viewHolder.specText.setVisibility(View.GONE);
        }

        viewHolder.specialPrice.setText("￥" + String.valueOf(product.discountPrice));

        viewHolder.originalPrice.setText("￥" + String.valueOf(product.retailPrice));
        viewHolder.originalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        viewHolder.originalPrice.getPaint().setAntiAlias(true);

        viewHolder.countText.setText(String.valueOf(product.count));

        if (product.count > 0) {
            viewHolder.minusProduct.setVisibility(View.VISIBLE);
            viewHolder.countText.setVisibility(View.VISIBLE);
            viewHolder.plusProduct.setVisibility(View.VISIBLE);
        } else {
            viewHolder.minusProduct.setVisibility(View.GONE);
            viewHolder.countText.setVisibility(View.GONE);
        }

        viewHolder.plusProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isFastClick()) {
                    return;
                }
                if (product.count >= product.goods_storage) {
                    AppToast.showShortText(context, "对不起，库存不足!");
                    return;
                }

                if (product.count >= 999) {
                    AppToast.showShortText(context, "对不起，购买数量已达上限，另请下单!");
                    return;
                }

                int action = ShopDetailsActivity.ADD_TO_SHOPPING_CART;
                if (product.count > 0) {
                    action = ShopDetailsActivity.PLUS_BADGE_ANIMATION;
                }

                product.count++;
                mHandler.obtainMessage(action, position, -1, product).sendToTarget();

                StatAgent.initAction(context, "", "2", "8", "", "", "add product", "1", "");
            }
        });

        viewHolder.minusProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isFastClick()) {
                    return;
                }

                if (product.count > 0) {
                    int action = ShopDetailsActivity.MINUS_BADGE_ANIMATION;

                    product.count--;
                    mHandler.obtainMessage(action, position, -1, product).sendToTarget();
                }
                StatAgent.initAction(context, "", "2", "8", "", "", "minus product", "1", "");
            }
        });
        viewHolder.img.setImageResource(R.drawable.ic_product_default);
        ImageLoader.getInstance().displayImage(product.thumbUrl, viewHolder.img,
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
                                viewHolder.img.setImageBitmap(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        super.onLoadingFailed(s, view, failReason);
                        viewHolder.img.setImageResource(R.drawable.ic_product_default);

                    }
                });

        //根据屏幕宽度动态设置每个图片的宽高
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width, width);
        viewHolder.img.setLayoutParams(param);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private View productLayout;
        private TextView specText;//规格
        private ImageView img;
        private TextView originalPrice;
        private TextView specialPrice;
        private EditText countText;
        private Button minusProduct;
        private Button plusProduct;
        private TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            productLayout = itemView.findViewById(R.id.product_layout);
            img = (ImageView) itemView.findViewById(R.id.product_img);
            title = (TextView) itemView.findViewById(R.id.product_name);
            specText = (TextView) itemView.findViewById(R.id.product_spec);
            originalPrice = (TextView) itemView.findViewById(R.id.retail_price_text);
            specialPrice = (TextView) itemView.findViewById(R.id.discount_price_text);
            countText = (EditText) itemView.findViewById(R.id.pro_count_text);
            minusProduct = (Button) itemView.findViewById(R.id.minusBtn);
            plusProduct = (Button) itemView.findViewById(R.id.addBtn);
        }
    }

    /**
     * 修改商品数量
     *
     * @param id
     * @param count
     */
    public void updateProduct(int id, int count) {
        for (int i = 0; i < getDataList().size(); i++) {
            Product product = getDataList().get(i);
            if (product.id == id) {
                product.count = count;
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
            Product product = getDataList().get(i);
            if (product.id == id) {
                product.count = count;
                product.shopCardId = shopCartId;
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
        for (int i = 0; i < getDataList().size(); i++) {
            Product product = getDataList().get(i);
            if (product.shopCardId == shopCartId) {
                product.count = 0;
                getDataList().set(i, product);
                notifyItemChanged(i);
            }

        }

    }


}
