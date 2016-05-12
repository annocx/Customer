package com.haier.cabinet.customer.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.PhotoView;
import com.haier.cabinet.customer.util.PhotoViewAttacher;
import com.haier.cabinet.customer.util.ViewPagerFixed;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

/**
 * 评论列表显示照片
 * Created by jinbiao.wu on 2016/3/21.
 */
public class ImagePagerActivity extends Activity {
    public static final String COMMENT_LIST_DATA = "COMMENT_LIST_DATA";
    public static final String STATE_POSITION = "STATE_POSITION";

    DisplayImageOptions options;

    ViewPagerFixed pager;

    private ArrayList<View> listViews = null;

    ArrayList<String> imageUrls = new ArrayList<String>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_page);
        Bundle bundle = getIntent().getExtras();
        imageUrls = bundle.getStringArrayList(COMMENT_LIST_DATA);
        // 当前显示View的位置
        int pagerPosition = bundle.getInt(STATE_POSITION, 0);

        // 如果之前有保存用户数据
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);
        }
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_product_default)
                .showImageOnFail(R.drawable.ic_product_default)
                .resetViewBeforeLoading(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        pager = (ViewPagerFixed) findViewById(R.id.image_page);
        pager.setAdapter(new ImagePagerAdapter(imageUrls));
        pager.setCurrentItem(pagerPosition);    // 显示当前位置的View
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // 保存用户数据
        outState.putInt(STATE_POSITION, pager.getCurrentItem());
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private ArrayList images;
        private LayoutInflater inflater;

        ImagePagerAdapter(ArrayList images) {
            this.images = images;
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPagerFixed) container).removeView((View) object);
        }

        @Override
        public void finishUpdate(View container) {
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            PhotoView imageView = (PhotoView) imageLayout.findViewById(R.id.image);
            ImageLoader.getInstance().displayImage(images.get(position).toString(), imageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    DialogHelper.showDialogForLoading(ImagePagerActivity.this, getString(R.string.loading), true);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String message = null;
                    switch (failReason.getType()) {     // 获取图片失败类型
                        case IO_ERROR:              // 文件I/O错误
                            message = "Input/Output error";
                            break;
                        case DECODING_ERROR:        // 解码错误
                            message = "Image can't be decoded";
                            break;
                        case NETWORK_DENIED:        // 网络延迟
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:         // 内存不足
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:               // 原因不明
                            message = "Unknown error";
                            break;
                    }
                    Log.d("wjb","message:"+message);
                    DialogHelper.stopProgressDlg();
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    DialogHelper.stopProgressDlg();
                }
            });
            try {
                ((ViewPagerFixed) view).addView(imageLayout, 0);

            } catch (Exception e) {
            }
            imageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    finish();
                }
            });
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View container) {
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return false;
    }
}
