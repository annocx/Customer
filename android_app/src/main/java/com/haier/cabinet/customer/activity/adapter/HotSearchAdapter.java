package com.haier.cabinet.customer.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.haier.cabinet.customer.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/24.
 */
public class HotSearchAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<String> item;

    public HotSearchAdapter(Context context, ArrayList<String> item) {
        this.context = context;
        this.item = item;
    }

    @Override
    public Object getItem(int position) {
        if (position == item.size()) {
            return null;
        }
        return item.get(position);
    }

    @Override
    public int getCount() {
        return item.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.vw_lable_bg, (ViewGroup) null, false);
            viewHolder.tv_lable = (TextView) convertView.findViewById(R.id.tv_lable);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_lable.setText(item.get(position).toString());

        return convertView;
    }

    class ViewHolder {
        TextView tv_lable;
    }
}