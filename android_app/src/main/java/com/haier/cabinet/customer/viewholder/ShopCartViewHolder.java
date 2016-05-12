package com.haier.cabinet.customer.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;

/**
 * Created by lzx on 15/12/31.
 */
public class ShopCartViewHolder extends RecyclerView.ViewHolder {
    public TextView shopNameText;
    public ListView listView;

    public ShopCartViewHolder(View itemView) {
        super(itemView);
        shopNameText = (TextView) itemView.findViewById(R.id.shop_name_text);
        listView = (ListView) itemView.findViewById(R.id.listView);
    }


}
