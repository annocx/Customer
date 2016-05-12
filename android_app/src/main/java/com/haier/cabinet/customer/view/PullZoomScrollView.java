package com.haier.cabinet.customer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.haier.cabinet.customer.R;

/**
 * @author Jayce
 * @date 2015/7/29
 */
public class PullZoomScrollView extends ScrollView {

    public static final String TAG = "MyScrollView";

    public static int USERINFO_MIN_HEIGHT;
    public static int USERINFO_COMMON_HEIGHT;
    public static int USERINFO_MAX_HEIGHT;

    public static int MIN_SCROLLY;
    public static int MAX_SCROLLY;

    private View mOperateView; //可操控的View

    private OnScrollChangeListener mOnScrollChangeListener;

    boolean isFirst = true;

    public static final int MSG_SCROLL = 1;


    public PullZoomScrollView(Context context) {
        this(context, null);
    }

    public PullZoomScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        USERINFO_MIN_HEIGHT = getResources().getDimensionPixelSize(R.dimen.user_info_min_height);
        USERINFO_COMMON_HEIGHT = getResources().getDimensionPixelOffset(R.dimen.user_info_common_height);
        USERINFO_MAX_HEIGHT = getResources().getDimensionPixelOffset(R.dimen.user_info_max_height);

        MIN_SCROLLY = USERINFO_MAX_HEIGHT - USERINFO_COMMON_HEIGHT;
        MAX_SCROLLY = USERINFO_MAX_HEIGHT - USERINFO_MIN_HEIGHT;
    }

    public void bindView(View view) {
        mOperateView = view;

        mOperateView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                //当控件绘制完成

                if (isFirst) {
                    smoothScrollTo(0, PullZoomScrollView.MIN_SCROLLY);

                    ((AccountInfoView) mOperateView).setMaxRange(USERINFO_COMMON_HEIGHT - USERINFO_MIN_HEIGHT);
                    isFirst = false;
                }
            }
        });
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        int scrollY = t < MAX_SCROLLY ? t : MAX_SCROLLY;  //限定在滚动范围内

        if (mOnScrollChangeListener != null) {
            if (scrollY - MIN_SCROLLY > 0) {
                mOnScrollChangeListener.onScrollChange(scrollY - MIN_SCROLLY);  //对外部可见的滚动范围在COMMON_HEIGHT 和 MIN_Height之间
            }else{
                mOnScrollChangeListener.onScrollChange(0);
            }

            if (mOperateView != null) {
                ViewGroup.LayoutParams params = mOperateView.getLayoutParams();
                params.height = PullZoomScrollView.USERINFO_COMMON_HEIGHT + PullZoomScrollView.MIN_SCROLLY - scrollY;
                mOperateView.setLayoutParams(params);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int scrollY = getScrollY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                if (scrollY < MIN_SCROLLY + (MAX_SCROLLY- MIN_SCROLLY)/4) {
                    smoothScrollTo(0, MIN_SCROLLY);
                }else if(scrollY <MAX_SCROLLY){
                    smoothScrollTo(0,MAX_SCROLLY);
                }


                return true;
        }
        return super.onTouchEvent(ev);
    }

    public void setOnScrollChangeListener(OnScrollChangeListener mOnScrollChangeListener) {
        this.mOnScrollChangeListener = mOnScrollChangeListener;
    }

    public interface OnScrollChangeListener {
        void onScrollChange(int scrollY);
    }
}
