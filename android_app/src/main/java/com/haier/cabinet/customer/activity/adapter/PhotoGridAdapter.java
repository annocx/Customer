package com.haier.cabinet.customer.activity.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.util.Bimp;

/**
 * 照片选择适配器
 * Created by jinbiao.wu  on 2016/3/16.
 */
public class PhotoGridAdapter extends BaseAdapter {


    private Context mContext;
    private LayoutInflater inflater;

    public PhotoGridAdapter(Context context) {
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        if (Bimp.tempSelectBitmap.size() == 3) {
            return 3;
        }
        return (Bimp.tempSelectBitmap.size() + 1);
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_published_grida, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView
                    .findViewById(R.id.item_grida_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == Bimp.tempSelectBitmap.size()) {
            holder.image.setImageBitmap(BitmapFactory.decodeResource(
                    mContext.getResources(), R.drawable.icon_addpic_unfocused));
            if (position == 3) {
                holder.image.setVisibility(View.GONE);
            }
        } else {
            holder.image.setImageBitmap(Bimp.tempSelectBitmap.get(position).getBitmap());
        }

        return convertView;
    }

    public class ViewHolder {
        public ImageView image;
    }
}
