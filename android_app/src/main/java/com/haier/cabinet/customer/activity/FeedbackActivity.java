package com.haier.cabinet.customer.activity;

import org.apache.http.Header;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.base.CommonWebActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.view.CustomDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.OnClick;

public class FeedbackActivity extends BaseActivity {

    private static final String TAG = "FeedbackActivity";

    @Bind(R.id.feedback_editor)
    EditText mFeedbackText;
    @Bind(R.id.right_text)
    TextView mSubmitText;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_feedback;
    }

    public void initView() {
        mTitleText.setText(R.string.user_feedback);
        mBackBtn.setVisibility(View.VISIBLE);
        mSubmitText.setText(R.string.submit_text);
    }

    public void initData() {
        mSubmitText.setOnClickListener(this);
    }


    @Override
    @OnClick({R.id.back_img})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.right_text:
                String content = mFeedbackText.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    AppToast.showShortText(this, "请您填写宝贵的意见！");
                    return;
                }
                if (content.length() < 5) {
                    AppToast.showShortText(this, "亲，写得太少了，再写点吧");
                    return;
                }
                if (content.length() > 200) {
                    AppToast.showShortText(this, "亲，反馈的内容超过字数限制了，精简下吧");
                    return;
                }
                submitFeedBack(content);
                break;
            default:
                break;
        }
    }

    private void submitFeedBack(String content) {
        String url = Constant.DOMAIN + "/user/feedback.json";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("comment", content);
        params.put("token", PushApplication.getInstance().getToken());
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                AppToast.showShortText(FeedbackActivity.this, "提交失败了，请稍后重试!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
//				Log.d(TAG, "json = " + json);
                if (200 == statusCode) {
                    if (200 == JsonUtil.getStateFromServer(json)) {
                        /*showSuccessDialog();*/
                        AppToast.showShortText(FeedbackActivity.this, "提交成功，感谢您的宝贵意见!");
                        FeedbackActivity.this.finish();
                    } else {
                        AppToast.showShortText(FeedbackActivity.this, "提交失败了，请稍后重试!");
                    }

                }
            }
        });
    }

    CustomDialog dialog = null;

    private void showSuccessDialog() {
        dialog = new CustomDialog(FeedbackActivity.this, R.style.MyDialog, new CustomDialog.CustomDialogListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.ok_text:
                        dialog.dismiss();
                        Bundle bundle = new Bundle();
                        bundle.putString("title", getResources().getString(R.string.points_explanation));
                        bundle.putString("url", Constant.URL_POINTS_EXPLANATION);
                        IntentUtil.startActivity(FeedbackActivity.this, CommonWebActivity.class, bundle);
                        FeedbackActivity.this.finish();
                        break;
                    case R.id.close_text:
                        dialog.dismiss();
                        FeedbackActivity.this.finish();
                        break;

                    default:
                        break;
                }
            }
        });
        dialog.getCustomView().findViewById(R.id.feedback_layout).setVisibility(View.VISIBLE);
        ((TextView) dialog.getCustomView().findViewById(R.id.ok_text)).setText(R.string.lookup_points);
        dialog.setCancelable(false);
        dialog.show();
    }

}
