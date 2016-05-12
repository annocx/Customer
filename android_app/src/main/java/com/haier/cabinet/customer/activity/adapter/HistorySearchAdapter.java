package com.haier.cabinet.customer.activity.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.haier.cabinet.customer.R;

import java.util.ArrayList;


public class HistorySearchAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<String> item;

    public HistorySearchAdapter(Context context, ArrayList<String> item) {
        this.context = context;
        this.item = item;
    }

    @Override
    public Object getItem(int position) {
        if(position==item.size()){
            return  null;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_history_search, (ViewGroup) null, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(item.get(position).toString());

        return convertView;
    }

    class ViewHolder {
        TextView name;
    }
}
