package com.haier.cabinet.customer.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;

/**
 * Created by lzx on 2015/12/22.
 */
public class ShopCartEmptyViewHolder extends RecyclerView.ViewHolder {
    public Button lookBtn;
    public ShopCartEmptyViewHolder(View itemView) {
        super(itemView);
        lookBtn=(Button)itemView.findViewById(R.id.look_button);
    }

}
