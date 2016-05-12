package com.haier.cabinet.customer.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/3/29.
 */
public class AddressViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.username_text) public TextView userNameText;
    @Bind(R.id.address_text) public TextView userAddressText;
    @Bind(R.id.phone_text) public TextView phoneText;
    @Bind(R.id.default_text) public TextView defaultText;
    @Bind(R.id.right_arrow_image) public ImageView arrowImage;
    @Bind(R.id.select_checkbox) public CheckBox selectCbx;
    public AddressViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

}
