package com.haier.cabinet.customer.activity.adapter;

/**
 * Created by Administrator on 2016/3/21.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseFragment;
import com.haier.cabinet.customer.base.BaseListFragment;
import com.haier.cabinet.customer.view.HardRefSimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.List;

/**
 * 评论列表图片展示
 * Created by jinbiao.wu  on 2016/3/21.
 */
public class PhotoGridAdapter2 extends BaseAdapter {


    private Context mContext;
    private LayoutInflater inflater;
    private List<String> img_list;

    public PhotoGridAdapter2(Context context, List<String> img_list) {
        mContext = context;
        this.img_list = img_list;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return img_list.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_published_grida, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView
                    .findViewById(R.id.item_grida_image);
            //根据屏幕宽度动态设置每个图片的宽高
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(280, 280);
            holder.image.setLayoutParams(param);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageLoader.getInstance().displayImage(img_list.get(position), holder.image,
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
                            } else {
                                holder.image.setImageBitmap(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        super.onLoadingFailed(s, view, failReason);
                        holder.image.setImageResource(R.drawable.ic_product_default);

                    }

                });
        return convertView;
    }

    public class ViewHolder {
        public ImageView image;
    }
}
