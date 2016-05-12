package com.haier.cabinet.customer.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.entity.DeliverInfo;
import com.haier.cabinet.customer.view.CustomTextView;

import java.util.ArrayList;
import java.util.List;

public class LogisticsAdapter extends BaseAdapter {

    private List<DeliverInfo> list;
    private Context context;
    private List<Float> nodeRadiusDistances;

    public LogisticsAdapter(List<DeliverInfo> list, Context context) {
        this.list = list;
        this.context = context;
        this.nodeRadiusDistances = new ArrayList<Float>();
            for (int i = 0; i < list.size() + 2; i++) {
                // 倒数第二个用于装载item顶部内边距,倒数第一个用于装载listview分割线高度
                this.nodeRadiusDistances.add(0.0f);
            }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        //这里只所以不用复用，是因为使用了复用之后测量出item的高度就不准确了
        convertView = LayoutInflater.from(context).inflate(
                R.layout.person_logistics_info_item_line, null);
        CustomTextView info_txt = (CustomTextView) convertView
                .findViewById(R.id.info_txt);
        CustomTextView dateTime_txt = (CustomTextView) convertView
                .findViewById(R.id.dateTime_txt);
        info_txt.setText(list.get(position).context);
        dateTime_txt.setText(list.get(position).time);
        //监听View的伸展，以便测量高度
        ViewTreeObserver vto = info_txt.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new MyGlobalLayoutListener(info_txt,
                dateTime_txt, position));
//        if (position == 0) {
//            info_txt.setTextColor(context.getResources().getColor(R.color.common_color));
//            dateTime_txt.setTextColor(context.getResources().getColor(R.color.common_color));
//        }
        return convertView;
    }

    class ViewHolder {
        CustomTextView info_txt;
        CustomTextView dateTime_txt;
    }

    public List<Float> getNodeRadiusDistances() {
        return nodeRadiusDistances;
    }

    /**
     * 监听TextView高度的改变
     *
     * @author zad
     */
    private class MyGlobalLayoutListener implements OnGlobalLayoutListener {

        // private ViewHolder holder;
        private int position;
        private CustomTextView info_txt;
        private CustomTextView dateTime_txt;

        public MyGlobalLayoutListener(CustomTextView info_txt,
                                      CustomTextView dateTime_txt, int position) {
            // this.holder = holder;
            this.position = position;
            this.info_txt = info_txt;
            this.dateTime_txt = dateTime_txt;
            // 倒数第二个位置装载顶部内边距
            nodeRadiusDistances.set(nodeRadiusDistances.size() - 2,
                    (float) info_txt.getParentPaddingTop());
        }

        @Override
        public void onGlobalLayout() {
            // TODO Auto-generated method stub
            /**
             * 获取子项的真实高度
             */
            float realHeight = info_txt.getTxtHeight()
                    + info_txt.getParentPaddingTop()
                    + info_txt.getParentPaddingBotton()
                    + dateTime_txt.getTxtHeight();
            LayoutParams childparams = (LayoutParams) info_txt
                    .getLayoutParams();
            realHeight += childparams.bottomMargin;
            if (realHeight > nodeRadiusDistances.get(position)) {
                nodeRadiusDistances.set(position, realHeight);
            }
        }
    }
}
