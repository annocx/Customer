package com.haier.cabinet.customer.viewholder;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.util.Util;

/**
 * Created by lzx on 2015/12/22.
 */
public class ProductsViewHolder extends RecyclerView.ViewHolder {
    public TextView productTitle;
    public ImageView productImage;
    public TextView priceText;//折扣价
    public TextView retailPriceText;//原价
    public TextView specText;//规格
    public EditText countText;
    public Button addBtn;
    public Button minusBtn;

    public ProductsViewHolder(View itemView) {
        super(itemView);
        productTitle = (TextView) itemView.findViewById(R.id.text);
        specText=(TextView) itemView.findViewById(R.id.product_spec);
        productTitle=(TextView) itemView.findViewById(R.id.product_name);
        productImage=(ImageView) itemView.findViewById(R.id.product_img);
        priceText = (TextView) itemView.findViewById(R.id.discount_price_text);
        retailPriceText = (TextView) itemView.findViewById(R.id.retail_price_text);
        countText=(EditText)itemView.findViewById(R.id.pro_count_text);
        addBtn=(Button)itemView.findViewById(R.id.addBtn);
        minusBtn=(Button)itemView.findViewById(R.id.minusBtn);
    }

}
