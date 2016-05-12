package com.haier.cabinet.customer.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.SalesPromotionActivity;
import com.haier.cabinet.customer.entity.Advertisement;
import com.haier.cabinet.customer.util.Constant;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * ViewPager实现的轮播图广告自定义视图，如京东首页的广告轮播图效果； 既支持自动轮播页面也支持手势滑动切换页面
 * 
 *
 */

public class SlideShowView extends FrameLayout {
	private String TAG = "SlideShowView";
	//轮播图图片数量
	private final static int IMAGE_COUNT = 5;
	//自动轮播的时间间隔
	private final static int TIME_INTERVAL = 5;
	//自动轮播启用开关
	private final static boolean isAutoPlay = true;

	//自定义轮播图的资源
	private String[] imageUrls;
	//放轮播图片的ImageView 的list
	private List<ImageView> imageViewsList;
	//放圆点的View的list
	private List<View> dotViewsList;

	private ViewPager viewPager;
	//当前轮播页
	private int currentItem  = 0;
	//定时任务
	private ScheduledExecutorService scheduledExecutorService;

	private Context context;
	private LinearLayout dotLayout;
	private List<Advertisement> adverList;
	private MyPagerAdapter mAdapter;

	// Handler
	private static final int UPDATE_VIEWPAGER_ITEM = 1001;
	private static final int FRESH_BANNER_DATA = 1002;
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case UPDATE_VIEWPAGER_ITEM:
					viewPager.setCurrentItem(currentItem);
					break;
				case FRESH_BANNER_DATA:
					List<Advertisement> list = (List<Advertisement>) msg.obj;
					Log.d(TAG, "lzx 从服务器获取数据 size = " + list.size());
					adverList.addAll(list);
					Log.d(TAG, "lzx  size = " + list.size());
					initUI(context);
					break;

				default:
					break;
			}

		};

	};

	public SlideShowView(Context context) {
		this(context,null);
	}
	public SlideShowView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public SlideShowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initView();
		initData();
		if(isAutoPlay){
			startPlay();
		}

	}

	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.layout_slideshow, this,
				true);
		dotLayout = (LinearLayout) findViewById(R.id.dotLayout);

		adverList = new ArrayList<>();
		imageViewsList = new ArrayList<>();
		dotViewsList = new ArrayList<>();

		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setFocusable(true);

		mAdapter = new MyPagerAdapter(adverList);
		viewPager.setAdapter(mAdapter);

		viewPager.setOnPageChangeListener(new MyPageChangeListener());

	}

	public void refreshView(){
		Log.d(TAG, "refresh bannerView");
		if (adverList.size() > 0) {
			adverList.clear();
			imageViewsList.clear();
			dotViewsList.clear();

			mAdapter.notifyDataSetChanged();
		}
		initData();
		if (isAutoPlay) {
			startPlay();
		}
	}

	/**
	 * 开始轮播图切换
	 */
	private void startPlay(){
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), (long) 1.5, 4, TimeUnit.SECONDS);
	}
	/**
	 * 停止轮播图切换
	 */
	private void stopPlay(){
		scheduledExecutorService.shutdown();
	}
	/**
	 * 初始化相关Data
	 */
	private void initData(){

		// 异步任务获取图片
		processData();
	}
	/**
	 * 初始化Views等UI
	 */
	private void initUI(Context context) {
		if (adverList == null || adverList.size() == 0)
			return;

		dotLayout.removeAllViews();

		// 热点个数与图片特殊相等
		for (int i = 0; i < adverList.size(); i++) {
			ImageView view = new ImageView(context);
			view.setTag(adverList.get(i).imgUrl);
			if (i == 0)// 给一个默认图
				view.setBackgroundResource(R.drawable.ic_product_default);
			view.setScaleType(ScaleType.FIT_XY);
			imageViewsList.add(view);

			ImageView dotView = new ImageView(context);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = 4;
			params.rightMargin = 4;
			dotLayout.addView(dotView, params);
			dotViewsList.add(dotView);
		}


		mAdapter.notifyDataSetChanged();

	}

	/**
	 * 填充ViewPager的页面适配器
	 *
	 */
	private class MyPagerAdapter  extends PagerAdapter{
		List<Advertisement> list;
		public MyPagerAdapter(List<Advertisement> list) {
			this.list = list;
		}
		@Override
		public void destroyItem(View container, int position, Object object) {
			// TODO Auto-generated method stub
			//((ViewPag.er)container).removeView((View)object);
			((ViewPager)container).removeView(imageViewsList.get(position));
		}

		@Override
		public Object instantiateItem(View container, final int position) {
			final ImageView imageView = imageViewsList.get(position);

			if (position < list.size()) {
				ImageLoader.getInstance().displayImage(String.valueOf(imageView.getTag()), imageView, PushApplication.getInstance().getDefaultOptions());
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						/*Bundle bundle = new Bundle();
						bundle.putString("url", "file:///android_asset/error.html");
						IntentUtil.startActivity((Activity)context, SalesPromotionActivity.class, bundle);*/
					}
				});

				((ViewPager) container).addView(imageView);
			}
			return imageViewsList.get(position);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return imageViewsList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}
		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public Parcelable saveState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void finishUpdate(View arg0) {
			// TODO Auto-generated method stub

		}

	}
	/**
	 * ViewPager的监听器
	 * 当ViewPager中页面的状态发生改变时调用
	 *
	 */
	private class MyPageChangeListener implements OnPageChangeListener{

		boolean isAutoPlay = false;

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			switch (arg0) {
				case 1:// 手势滑动，空闲中
					isAutoPlay = false;
					break;
				case 2:// 界面切换中
					isAutoPlay = true;
					break;
				case 0:// 滑动结束，即切换完毕或者加载完毕
					// 当前为最后一张，此时从右向左滑，则切换到第一张
					if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
						viewPager.setCurrentItem(0);
					}
					// 当前为第一张，此时从左向右滑，则切换到最后一张
					else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {
						viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
					}
					break;
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int pos) {
			// TODO Auto-generated method stub

			currentItem = pos;
			for(int i=0;i < dotViewsList.size();i++){
				if(i == pos){
					((View)dotViewsList.get(pos)).setBackgroundResource(R.drawable.dot_focus);
				}else {
					((View)dotViewsList.get(i)).setBackgroundResource(R.drawable.dot_blur);
				}
			}
		}

	}

	/**
	 *执行轮播图切换任务
	 *
	 */
	private class SlideShowTask implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			synchronized (viewPager) {
				currentItem = (currentItem+1)%imageViewsList.size();
				mHandler.sendEmptyMessage(UPDATE_VIEWPAGER_ITEM);
			}
		}

	}

	/**
	 * 销毁ImageView资源，回收内存
	 *
	 */
	private void destoryBitmaps() {

		for (int i = 0; i < IMAGE_COUNT; i++) {
			ImageView imageView = imageViewsList.get(i);
			Drawable drawable = imageView.getDrawable();
			if (drawable != null) {
				//解除drawable对view的引用
				drawable.setCallback(null);
			}
		}
	}


	private void processData() {
		String url = Constant.DOMAIN + "/version/findAdvertImg.json?attType=5";
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
								  Throwable arg3) {

			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String json = new String(arg2);
				List<Advertisement> list = getAdvertisementListByJosn(json);
				mHandler.obtainMessage(FRESH_BANNER_DATA, list).sendToTarget();
			}

		});

	}

	private List<Advertisement> getAdvertisementListByJosn(String json) {
		List<Advertisement> list = new ArrayList<>();

		try {
			JSONObject jsonObject = new JSONObject(json);
			JSONArray listArray = jsonObject.getJSONArray("data");
			for (int i = 0; i < listArray.length(); i++) {
				JSONObject adObject = (JSONObject) listArray.get(i);
				Advertisement advertisement = new Advertisement();
				advertisement.imgUrl = adObject.getString("attPathUrl");
				advertisement.clickUrl = adObject.getString("attUrl");

				list.add(advertisement);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}
}