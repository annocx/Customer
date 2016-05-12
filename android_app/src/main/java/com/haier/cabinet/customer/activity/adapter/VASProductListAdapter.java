package com.haier.cabinet.customer.activity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.HardRefSimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class VASProductListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Product> mDataList;
    private LayoutInflater inflater;

    public VASProductListAdapter(Context context, List<Product> list) {
        this.mContext = context;
        this.mDataList = list;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mDataList != null ? mDataList.size() : 0;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_home_product_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.productImage = (ImageView) convertView.findViewById(R.id.product_image);
            viewHolder.nameText = (TextView) convertView.findViewById(R.id.product_name);
            viewHolder.priceText = (TextView) convertView.findViewById(R.id.discount_price_text);
            viewHolder.retailPriceText = (TextView) convertView.findViewById(R.id.retail_price_text);
            viewHolder.product_spec = (TextView) convertView.findViewById(R.id.product_spec);
            viewHolder.countText = (EditText) convertView.findViewById(R.id.pro_count_text);
            viewHolder.addBtn = (Button) convertView.findViewById(R.id.addBtn);
            viewHolder.minusBtn = (Button) convertView.findViewById(R.id.minusBtn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Product product = mDataList.get(position);

        viewHolder.nameText.setText(product.title);
        viewHolder.retailPriceText.setText("¥" + product.retailPrice);
        viewHolder.product_spec.setText("" + product.spec);
        viewHolder.retailPriceText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        viewHolder.retailPriceText.getPaint().setAntiAlias(true);
        viewHolder.minusBtn.setVisibility(View.GONE);
        viewHolder.countText.setVisibility(View.GONE);
        viewHolder.addBtn.setVisibility(View.GONE);


        if (PushApplication.getInstance().isAuthenticated()) {//是认证用户,享受阶梯价
            Bracket bracket = Util.getDiscount(product);
            if (bracket == null) {
                viewHolder.priceText.setText("￥" + product.discountPrice);//原价价格
            } else {
                viewHolder.priceText.setText("￥" + bracket.price);//折扣价格

            }
        } else {
            viewHolder.priceText.setText("￥" + product.discountPrice);//原价价格
        }
        if(!product.imgUrl.equals("")){
            ImageLoader.getInstance().loadImage(product.imgUrl,
                    PushApplication.getInstance().getDefaultOptions(), new HardRefSimpleImageLoadingListener(position) {
                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            super.onLoadingComplete(s, view, bitmap);
                            if (bitmap != null) {
                                if (identifier != position) {
                                    return;
                                }
//                                viewHolder.productImage.setImageBitmap(bitmap);
                            }
                        }
                    });
        }
        return convertView;
    }

    public void updateList(int position, Product product) {
        getDataList().set(position, product);
        notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView productImage;
        TextView nameText;
        TextView priceText;
        TextView retailPriceText;//原价
        TextView product_spec;
        EditText countText;
        Button addBtn;
        Button minusBtn;
    }

    public final List<Product> getDataList() {
        return this.mDataList;
    }
}
