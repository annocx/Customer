package com.haier.cabinet.customer.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.ShareAdapter;
import com.haier.cabinet.customer.entity.PackageBox;
import com.haier.cabinet.customer.event.CouponEvent;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.HttpUtil;
import com.haier.common.util.AppToast;
import com.umeng.analytics.social.UMSocialService;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.handler.SmsHandler;
import com.umeng.socialize.handler.UMQQSsoHandler;
import com.umeng.socialize.media.SmsShareContent;
import com.umeng.socialize.media.UMImage;

import de.greenrobot.event.EventBus;


public class SharePopupWindow extends PopupWindow {

    private Context mContext;
    private PackageBox mPackagebox;
    private UMImage image;
    String mTitle;
    String mContent;
    String mUrl;

    public SharePopupWindow(Context context, PackageBox packagebox) {
        this.mContext = context;
        mPackagebox = packagebox;
        initSocialSDK();
    }

    /**
     * 初始化SDK，添加一些平台
     */
    private void initSocialSDK() {
        image = new UMImage(mContext, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon));
        //设置分享文字
        mContent = String.format(mContext.getResources().getString(R.string.fetching_express_content),
                "运单号", mPackagebox.packageNo, mPackagebox.cabinetName, mPackagebox.pickUpNo, mPackagebox.postmanMobile);
        mTitle = mContext.getResources().getString(R.string.app_name);
        mUrl = Constant.URL_SHARE_FETCHING_EXPRESS + mPackagebox.tradeWaterNo;
    }

    public void showShareWindow() {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.activity_share_layout, null);
        GridView gridView = (GridView) view.findViewById(R.id.share_gridview);
        ShareAdapter adapter = new ShareAdapter(mContext);
        gridView.setAdapter(adapter);

        // 设置SelectPicPopupWindow的View
        this.setContentView(view);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);

        gridView.setOnItemClickListener(new ShareItemClickListener(this));

    }

    private class ShareItemClickListener implements OnItemClickListener {
        private PopupWindow pop;

        public ShareItemClickListener(PopupWindow pop) {
            this.pop = pop;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            share(position);
            pop.dismiss();

        }
    }

    /**
     * @param position
     */
    private void share(int position) {
        switch (position) {
            case 0:// 短信
                new ShareAction((Activity) mContext).setPlatform(SHARE_MEDIA.SMS)
                        .withText(mContent)
                        .setCallback(umShareListener)
                        .share();
                break;
            case 1:// 微信
                new ShareAction((Activity) mContext).setPlatform(SHARE_MEDIA.WEIXIN)
                        .withMedia(image)
                        .withTitle(mTitle)
                        .withText("帮我取下快件，拜托啦！")
                        .withTargetUrl(mUrl)
                        .setCallback(umShareListener)
                        .share();
                break;
            case 2:// QQ好友
                new ShareAction((Activity) mContext).setPlatform(SHARE_MEDIA.QQ)
                        .withMedia(image)
                        .withTitle(mTitle)
                        .withText("帮我取下快件，拜托啦！")
                        .withTargetUrl(mUrl)
                        .setCallback(umShareListener)
                        .share();
                break;

            default:
                break;
        }
    }


    private UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onResult(SHARE_MEDIA platform) {
            if (platform.toString().equals("QQ")) {
                HttpUtil.qqShareSucess();
            } else if (platform.toString().equals("WEIXIN") || platform.toString().equals("WEIXIN_CIRCLE")) {
                HttpUtil.weixinShareSucess();
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            AppToast.showShortText(mContext, "分享失败");
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            AppToast.showShortText(mContext, "分享失败");
        }
    };
}
