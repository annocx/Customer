package com.haier.cabinet.customer.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.listener.ItemClickListener;

/**
 * Created by lzx on 2015/12/22.
 */
public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView productTitle;
    public ImageView productImage;
    public TextView priceText;//折扣价
    public TextView retailPriceText;//原价
    public TextView specText;//规格
    public EditText countText;
    public Button addBtn;
    public Button minusBtn;

    private ItemClickListener mListener;

    public ProductViewHolder(View itemView, ItemClickListener listener) {
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

        this.mListener = listener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(mListener != null){
            mListener.onItemClick(v,getPosition());
        }
    }
}
