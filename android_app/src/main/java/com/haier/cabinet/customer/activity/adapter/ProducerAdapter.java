package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.SpecialityDetailActivity;
import com.haier.cabinet.customer.base.BaseFragment;
import com.haier.cabinet.customer.base.BaseListFragment;
import com.haier.cabinet.customer.entity.Supply;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.HardRefSimpleImageLoadingListener;
import com.haier.common.util.IntentUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sunday.statagent.StatAgent;

import java.util.ArrayList;
import java.util.Collection;

public class ProducerAdapter extends RecyclerView.Adapter {

    private Context context;

    private int width;//图片宽度

    private int height;//图片高度

    private LayoutInflater mLayoutInflater;

    private ArrayList<Supply> mDataList = new ArrayList<>();

    public ProducerAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
        width= Util.getScreenWidth(context);
        this.height = (new Double(width*0.49)).intValue();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.adapter_producer, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Supply supply = mDataList.get(position);

        final ViewHolder viewHolder = (ViewHolder) holder;

        ImageLoader.getInstance().displayImage(supply.getImage(), viewHolder.img,
                PushApplication.getInstance().getDefaultOptions(), new HardRefSimpleImageLoadingListener(position) {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
                if (bitmap != null) {
                    ImageView imageView = (ImageView) view;
                    // 是否第一次显示
                    boolean firstDisplay = !BaseListFragment.displayedImages.contains(imageUri);
                    if (firstDisplay) {
                        // 图片淡入效果
                        FadeInBitmapDisplayer.animate(imageView, 500);
                        BaseListFragment.displayedImages.add(imageUri);
                    }else {
                        viewHolder.img.setImageBitmap(bitmap);
                    }
                }
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                super.onLoadingFailed(s, view, failReason);
                viewHolder.img.setImageResource(R.drawable.ic_product_default);

            }
        });

        //根据屏幕宽度动态设置每个图片的宽高
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(width,height);
        viewHolder.img.setLayoutParams(param);

        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatAgent.initAction(context, "", "2", "7", "", "", supply.getTitle(), "1", "");

                Activity activity = (Activity) context;
                Bundle bundle = new Bundle();
                bundle.putInt("id", supply.getId());
                bundle.putString("img", supply.getImage());
                bundle.putString("title", supply.getTitle());
                IntentUtil.startActivity(activity, SpecialityDetailActivity.class, bundle);
            }
        });

        /*FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (position % 2 == 0) {
            lp.setMargins(15, 15, 15, 15);
        } else if (position == mDataList.size()-1){
            lp.setMargins(15, 0, 15, 15);
        } else {
            lp.setMargins(15, 0, 15, 0);
        }
        viewHolder.itemView.setLayoutParams(lp);*/
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addAll(Collection<Supply> list) {
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

        private ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.producer_img);
        }
    }

}
