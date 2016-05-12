package com.haier.cabinet.customer.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.Header;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.PackageBox;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.ShareUtil;
import com.haier.cabinet.customer.view.SharePopupWindow;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.Utils;
import com.haier.qr.code.CaptureCodeActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.umeng.socialize.UMShareAPI;

import butterknife.Bind;
import butterknife.OnClick;

public class PackageBoxDetailsActivity extends BaseActivity implements OnClickListener {

    @Bind(R.id.express_state_image)
    ImageView expressStateImage;
    @Bind(R.id.cabinet_location_text)
    TextView cabinetNameText;
    @Bind(R.id.pickup_no_text)
    TextView pickupNoText;
    @Bind(R.id.express_no_text)
    TextView expressNoText;
    @Bind(R.id.postman_telephone_text)
    TextView postmanMobileText;
    @Bind(R.id.delivery_time_text)
    TextView deliveryTimeText;
    @Bind(R.id.remain_duration_text)
    TextView remainTimeText;
    @Bind(R.id.hotline_counseling_text)
    TextView hotlineText;
    @Bind(R.id.call_postman_btn)
    Button callPostmanBtn;
    @Bind(R.id.scan_button)
    Button scanBtn;
    @Bind(R.id.share_button)
    Button shareBtn;

    private PackageBox packageBox;
    private ShareUtil mShareUtil;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_packagebox_details;
    }

    public void initView() {
        mTitleText = (TextView) findViewById(R.id.title_text);
        mBackBtn = (ImageView) findViewById(R.id.back_img);
        mTitleText.setText("快件详情");
        mBackBtn.setVisibility(View.VISIBLE);
    }

    public void initData() {
        mShareUtil = new ShareUtil();
        packageBox = (PackageBox) getIntent().getSerializableExtra("packageBox");

        String packageBoxNo = String.format(getResources().getString(R.string.box_no), packageBox.boxNo);
        String content = packageBox.cabinetAddress + "  " + packageBoxNo;
        SpannableString spanText = new SpannableString(content);
        spanText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue_text)), packageBox.cabinetAddress.length() + 2, spanText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        cabinetNameText.setText(spanText);

        pickupNoText.setText(packageBox.pickUpNo);
        expressNoText.setText(packageBox.packageNo);
        postmanMobileText.setText(packageBox.postmanMobile);
        deliveryTimeText.setText(packageBox.deliveredTime);

        if (packageBox.isTimeout) {
            expressStateImage.setImageResource(R.drawable.ic_express_overtime);
            remainTimeText.setText(packageBox.remainTime + "  已超期，请您尽快取件");
            remainTimeText.setTextColor(Color.RED);
        } else {
            remainTimeText.setText(packageBox.remainTime);
        }
    }

    @Override
    @OnClick({R.id.scan_button, R.id.share_button, R.id.hotline_counseling_text, R.id.call_postman_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_button:

                Bundle bundle = new Bundle();
                bundle.putInt("total", getIntent().getIntExtra("total", 0));
                bundle.putString("terminalNo", packageBox.cabinetNo);
                bundle.putSerializable("packagebox", packageBox);
            /*IntentUtil.startActivity(PackageBoxDetailsActivity.this, HandleScanResultActivity.class,bundle);*/
                IntentUtil.startActivity(PackageBoxDetailsActivity.this, CaptureCodeActivity.class, bundle);
                PackageBoxDetailsActivity.this.finish();
                break;
            case R.id.share_button:
                otherPersonPickup(packageBox);
                break;
            case R.id.hotline_counseling_text:
                Intent intent_hot = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + hotlineText.getText().toString()));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    return;
                }
                this.startActivity(intent_hot);
                break;
            case R.id.call_postman_btn:
                Intent intent_postman = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + packageBox.postmanMobile));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    return;
                }
                this.startActivity(intent_postman);
                break;
            default:
                break;
        }
    }

    private void otherPersonPickup(final PackageBox box) {
        String url = Constant.URL_OTHER_PEOPLE_FETCHING_EXPRESS;
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("pickAddress", box.cabinetName);
        params.put("couriesPhone", box.postmanMobile);
        params.put("company", box.expressCompany);
        params.put("pickKey", box.pickUpNo);
        params.put("packageNo", box.packageNo);
        params.put("tradeWaterNo", box.tradeWaterNo);
        params.put("token", PushApplication.getInstance().getToken());
        Log.e("haipeng---",url+params);
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(PackageBoxDetailsActivity.this, "正在处理快件信息...", true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                AppToast.showShortText(PackageBoxDetailsActivity.this, "处理快件信息失败，请稍后再试");
                DialogHelper.stopProgressDlg();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                Log.e("haipeng---",json);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromServer(json)) {
                        case 200:
                            Message msg = new Message();
                            msg.what = SHOW_SHARE_DIALOG;
                            msg.obj = box;
                            mHandler.sendMessageDelayed(msg, 500);
                            break;
                        case 504:
                            // token失效跳转到登陆界面
                            AppToast.showShortText(PackageBoxDetailsActivity.this, "登录超时，请您重新登录!");
                            PushApplication.getInstance().logoutHaiUser();
                            PackageBoxDetailsActivity.this.finish();
                            break;

                        default:
                            break;
                    }

                }

            }

        });
    }

    private void showSharePopupWindow(PackageBox packagebox) {
        SharePopupWindow sharePopWindow = new SharePopupWindow(PackageBoxDetailsActivity.this, packagebox);
        sharePopWindow.showShareWindow();
        // 显示窗口 (设置layout在PopupWindow中显示的位置)
        sharePopWindow.showAtLocation(PackageBoxDetailsActivity.this.findViewById(R.id.root_layout),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private final int SHOW_SHARE_DIALOG = 1001;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_SHARE_DIALOG:
                    PackageBox packagebox = (PackageBox) msg.obj;
                    showSharePopupWindow(packagebox);
                    break;

                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** attention to this below ,must add this**/
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

    }
}
