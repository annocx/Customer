package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.entity.Categorize;
import com.haier.common.util.IntentUtil;
import com.sunday.statagent.StatAgent;

import java.util.ArrayList;

public class CategorizeAdapter extends RecyclerView.Adapter {

    private Context context;

    private LayoutInflater mLayoutInflater;

    private ArrayList<Categorize> mDataList = new ArrayList<>();

    public CategorizeAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.adapter_categorize, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Categorize categorize = mDataList.get(position);

        ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.title.setText(categorize.getTitle());
        viewHolder.describe.setText(categorize.getDescribe());

        switch (categorize.getId()) {
            case 1101:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.hot_categorize));
                break;
            case 1102:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.fish_categorize));
                break;
            case 1103:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.dry_fruit_categorize));
                break;
            case 1104:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.food_categorize));
                break;
            case 1105:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.fast_food_categorize));
                break;
            case 1106:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.gift_categorize));
                break;
            case 1107:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.fresh_fruit_categorize));
                break;
            case 1108:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.cooked_categorize));
                break;
            case 1109:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.appliance_categorize));
                break;
            case 1110:
                viewHolder.categorizeImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.other_categorize));
                break;
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(SortResultActivity.RESULT_ID, categorize.getId());
                bundle.putInt(SortResultActivity.RESULT_TYPE, SortResultActivity.RESULT_CLASSIFY);
                bundle.putString(SortResultActivity.RESULT_NAME, categorize.getTitle());
                Activity activity = (Activity) context;
                IntentUtil.startActivity(activity, SortResultActivity.class, bundle);

                StatAgent.initAction(context, "", "2", "4", "", "", categorize.getTitle(), "1", "");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addAll(ArrayList<Categorize> list) {
        int lastIndex = this.mDataList.size();
        if (this.mDataList.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size());
        }
    }

    public void clear() {
        if (mDataList.size() > 0) {
            mDataList.clear();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private TextView describe;

        private TextView title;

        private ImageView categorizeImg;

        private View cardViewLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            categorizeImg = (ImageView) itemView.findViewById(R.id.categorize_img);
            title = (TextView) itemView.findViewById(R.id.title);
            describe = (TextView) itemView.findViewById(R.id.describe);
            cardViewLayout = itemView.findViewById(R.id.card_view);
        }
    }

}
