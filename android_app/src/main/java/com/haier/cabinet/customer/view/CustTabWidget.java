package com.haier.cabinet.customer.view;

import java.util.ArrayList;
import java.util.List;

import com.haier.cabinet.customer.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CustTabWidget extends LinearLayout {


	private int[] mDrawableIds = new int[] { R.drawable.bg_home,
			R.drawable.bg_life, R.drawable.bg_cart, R.drawable.bg_person };
	//CheckedTextView
	private List<CheckedTextView> mCheckedList = new ArrayList<CheckedTextView>();

	private List<View> mViewList = new ArrayList<View>();

	private List<ImageView> mIndicateImgs = new ArrayList<ImageView>();
	

	private CharSequence[] mLabels;
	
	public CustTabWidget(Context context) {
		super(context);
		initView(context);
	}
	
	public CustTabWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		/*TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TabWidget, defStyle, 0);
		
		mLabels = typedArray.getTextArray(R.styleable.TabWidget_bottom_labels);
		typedArray.recycle();*/
		mLabels = context.getResources().getTextArray(R.array.bottom_bar_labels);
		initView(context);
	}
	
	private void initView(final Context context) {
		this.setOrientation(LinearLayout.HORIZONTAL);
		//底部导航栏的高度
		this.setBackgroundResource(R.drawable.index_bottom_bar);

		LayoutInflater inflater = LayoutInflater.from(context);
		LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		params.weight = 1.0f;
		params.gravity = Gravity.CENTER;
		
		for (int i = 0; i < mLabels.length; i++) {
			final int index = i;
			View tabView = inflater.inflate(R.layout.tab_item, null);
			CheckedTextView tabText = (CheckedTextView) tabView.findViewById(R.id.item_name);
			tabText.setText(mLabels[i]);
			tabText.setPadding(0, 10, 25, 0);
			tabText.setCompoundDrawablesWithIntrinsicBounds(null, context
					.getResources().getDrawable(mDrawableIds[i]), null, null);

			if(i == 2){//设置购物车CheckedTextView的id
				tabText.setId(R.id.shop_cart_tab_text);
			}

			//ImageView indicateImage = (ImageView) tabView.findViewById(R.id.indicate_img);
			
			addView(tabView, params);
			if (i == 0) {
//				View dividerView = new View(context);
				LinearLayout dividerView = new LinearLayout(context);
				LinearLayout.LayoutParams dividerParams = new LayoutParams(2, 0);
//				dividerView.setBackgroundColor(Color.rgb(106, 106, 106));
				addView(dividerView, dividerParams);
			}
			
			
			tabText.setTag(i);
			
			mCheckedList.add(tabText);
			//mIndicateImgs.add(indicateImage);
			mViewList.add(tabView);
			
			
			if (i == 0) {
				tabText.setChecked(true);
				tabText.setTextColor(Color.rgb(197, 1, 58));
				/*tabView.setBackgroundColor(Color.rgb(63, 63, 63));*/
			} else {
				tabText.setChecked(false);
				tabText.setTextColor(Color.rgb(51, 51, 51));
				/*tabView.setBackgroundColor(Color.rgb(63, 63, 63));*/
			}

			if(i == 2){//设置购物车id
				tabView.setId(R.id.shop_cart_btn);
			}

			tabView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					setTabDisplay(context, index);
					if (null != mTabSelectedListener) {
						mTabSelectedListener.onTabSelecete(index);
					}
				}
			});
		}		
	}
	
	public void setTabDisplay(Context context, int index){
		for (int i = 0; i < mCheckedList.size(); i++) {
			CheckedTextView tabText = mCheckedList.get(i);
			if ((Integer) (tabText.getTag()) == index) {
				tabText.setChecked(true);
				tabText.setTextColor(Color.rgb(197, 1, 58));
				/*mViewList.get(i).setBackgroundColor(Color.rgb(63, 63, 63));*/
			} else {
				tabText.setChecked(false);
				tabText.setTextColor(Color.rgb(51, 51, 51));
				/*mViewList.get(i).setBackgroundColor(Color.rgb(63, 63, 63));*/
			}
		}
	}
	
	/**
	 * 消息提示
	 * 
	 * @param context
	 * @param position
	 * @param visible
	 */
	public void setIndicateDisplay(Context context, int position,
			boolean visible) {
		int size = mIndicateImgs.size();
		if (size <= position) {
			return;
		}
		ImageView indicateImg = mIndicateImgs.get(position);
		indicateImg.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}

	
	public interface onTabSelectedListener{
		void onTabSelecete(int index);
	}
	
	private onTabSelectedListener mTabSelectedListener;
	public void setOnTabSelectedListener(onTabSelectedListener listener){
		this.mTabSelectedListener = listener;
	}
}
