package com.haier.cabinet.customer.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.R;

/**
 * @author Jayce
 * @date 2015/7/29
 */
public class AccountInfoView extends RelativeLayout {

    private RelativeLayout mRl_center;
    private ImageView mIv_head;
    private View mUserRightPointsView;
    private TextView mTv_username, mTv_username_top,mPointsTextview,mPointsText,mRightPointsTextview,mRightPointsText;

    public static int MOVE_INIT_X;
    public static int MOVE_FINAL_X;
    public static int MAX_RANGE; //最大的scroll范围  转化为user_infoView控件对应的
    public static final int STANDARD_RANGE = 30;
    private int mRange;

    public static final int HEAD_GONE_VALUE = 15;//50 to 5 modify by lzx

    boolean isFirst = true;

    public AccountInfoView(Context context) {
        this(context, null);
    }

    public AccountInfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AccountInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.view_userinfo, this);
        setBackgroundResource(R.drawable.ic_user_info_bg);

        initView();
    }

    void initView() {
        mRl_center = (RelativeLayout) findViewById(R.id.main_center_layout);
        mTv_username = (TextView) findViewById(R.id.username_text);
        mPointsTextview = (TextView) findViewById(R.id.user_points_textview);
        mPointsText = (TextView) findViewById(R.id.user_points_text);
        mRightPointsTextview = (TextView) findViewById(R.id.user_right_points_textview);
        mRightPointsText = (TextView) findViewById(R.id.user_right_points_text);
        mIv_head = (ImageView) findViewById(R.id.business_avtar_image);
        mTv_username_top = (TextView) findViewById(R.id.username_top_text);
        mUserRightPointsView = findViewById(R.id.user_right_points_layout);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isFirst) {
                    MOVE_INIT_X = (int) mUserRightPointsView.getX();
                    MOVE_FINAL_X = getMeasuredWidth() - mUserRightPointsView.getMeasuredWidth() - getResources().getDimensionPixelSize(R.dimen.user_info_move_marginRight);

                    isFirst = false;
                }
            }
        });
    }

    public void setMaxRange(int maxRange) {
        MAX_RANGE = maxRange;
    }

    public void onChange(int range,boolean isLogin) {
        mRange = range * STANDARD_RANGE / MAX_RANGE;

        if (mRange <= HEAD_GONE_VALUE) {
            mRl_center.setVisibility(VISIBLE);

            int alpha = 255 - 255 * (mRange) / HEAD_GONE_VALUE;
            mIv_head.setAlpha((float) alpha);
            mTv_username.setTextColor(Color.argb(alpha, 255, 255, 255));
            mPointsTextview.setTextColor(Color.argb(alpha, 255, 255, 255));
            mPointsText.setTextColor(Color.argb(alpha, 255, 255, 255));
            float scale = (float) mRange / STANDARD_RANGE;
            mRl_center.setScaleX((1 - scale)*0.3f+0.7f);
            mRl_center.setScaleY((1 - scale)*0.3f+0.7f);

//            mTv_username.setScaleX(1-scale);
//            mTv_username.setScaleY(1-scale);
        } else {
            mRl_center.setVisibility(INVISIBLE);
        }

        if (mRange >= STANDARD_RANGE - HEAD_GONE_VALUE) {
            mTv_username_top.setVisibility(VISIBLE);
            if (isLogin){
                mUserRightPointsView.setVisibility(VISIBLE);
            }
            // modify by lzx
            int alpha = 255 * (STANDARD_RANGE - mRange) / HEAD_GONE_VALUE;
            mTv_username_top.setTextColor(Color.argb(255 - alpha, 255, 255, 255));
            mRightPointsTextview.setTextColor(Color.argb(255 - alpha, 255, 255, 255));
            mRightPointsText.setTextColor(Color.argb(255 - alpha, 255, 255, 255));
        } else {
            mTv_username_top.setVisibility(INVISIBLE);
            mUserRightPointsView.setVisibility(INVISIBLE);

            mPointsTextview.setVisibility(VISIBLE);
            mPointsText.setVisibility(VISIBLE);
        }

        //int x = mRange * (MOVE_FINAL_X - MOVE_INIT_X) / STANDARD_RANGE + MOVE_INIT_X;
        //mUserRightPointsView.setX(x);

    }
}
