package com.haier.cabinet.customer.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.event.CouponEvent;
import com.haier.common.util.AppToast;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import de.greenrobot.event.EventBus;

/**
 * 分享方法
 * Created by jinbiao.wu on 2015/12/3.
 */
public class ShareUtil {
    /**
     * 程序的Context对象
     */

    public static final int PRODUCT_SHARE = 00000001;
    public static final int FRIEND_SHARE = 00000002;
    public static final int COUPON_SHARE = 00000003;//优惠券分享
    public static final int COUPON_INVITE_FRIENDS = 00000004;//优惠券分享
    private Context mContext;
    private String mTitle;

    private String mContent;

    private String mUrl;

    private UMImage image;

    private int mType;//判断是分享好友还是分享商品

    private String coupon_template_code;//分享页面对应的优惠券code

    public ShareUtil() {
    }

    public void init(Context ctx, String title, String content, String url, int type) {
        mContext = ctx;
        mTitle = title;
        mContent = content;
        mUrl = url;
        image = new UMImage(mContext, BitmapFactory.decodeResource(ctx.getResources(), R.drawable.icon));
        this.mType = type;
    }

    /**
     * 设置当前分享页面对应的优惠券code
     * @param code
     */
    public void setCode(String code){
        coupon_template_code = code;
    }

    /**
     * 开始分享
     */
    public void startShare() {
        new ShareAction((Activity) mContext).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
                .withMedia(image)
                .withTitle(mTitle)
                .withText(mContent)
                .withTargetUrl(mUrl)
                .setListenerList(umShareListener, umShareListener)
                .open();
    }

    private UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onResult(SHARE_MEDIA platform) {
            switch (mType) {
                case PRODUCT_SHARE:
                    if (platform.toString().equals("QQ")) {
                        HttpUtil.qqShareSucess();
                    } else if (platform.toString().equals("WEIXIN") || platform.toString().equals("WEIXIN_CIRCLE")) {
                        HttpUtil.weixinShareSucess();
                    }
                    AppToast.showShortText(mContext,  "分享成功");
                    break;
                case FRIEND_SHARE:
                    if (platform.toString().equals("QQ")) {
                        HttpUtil.inviteQQFriendSucess();
                    } else if (platform.toString().equals("WEIXIN") || platform.toString().equals("WEIXIN_CIRCLE")) {
                        HttpUtil.inviteWeixinFriendSucess();
                    }
                    AppToast.showShortText(mContext, "分享成功");
                    break;
                case COUPON_SHARE:
                    HttpUtil.inviteIncreaseSucess(coupon_template_code);
                    EventBus.getDefault().post(new CouponEvent(1));
                    AppToast.showShortText(mContext, "分享成功");
                    break;
                case COUPON_INVITE_FRIENDS:
                    AppToast.showShortText(mContext, "邀请成功");
                    break;
                default:
                    break;
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
