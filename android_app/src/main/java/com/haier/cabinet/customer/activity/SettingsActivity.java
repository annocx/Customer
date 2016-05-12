package com.haier.cabinet.customer.activity;

import org.apache.http.Header;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.base.CommonWebActivity;
import com.haier.cabinet.customer.base.LocalWebActivity;
import com.haier.cabinet.customer.event.UserChangedEvent;
import com.haier.cabinet.customer.ui.MainUIActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.Utils;
import com.haier.common.view.ActionSheetDialog;
import com.haier.common.view.ActionSheetDialog.OnSheetItemClickListener;
import com.haier.common.view.ActionSheetDialog.SheetItemColor;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sunday.statagent.StatAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

import butterknife.Bind;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;
import de.greenrobot.event.EventBus;


public class SettingsActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.modify_pwd_layout) View mModifyPasswordView;
    @Bind(R.id.logout_layout) View mLogoutView;
    @Bind(R.id.update_app_layout) View mUpdateAppView;//检查版本
    @Bind(R.id.questionnaire_layout) View mQuestionnaireView;//调查问卷
    @Bind(R.id.feedback_layout) View mFeedbackView;//反馈
    @Bind(R.id.app_version_text) TextView mVersionText;
    @Bind(R.id.about_us_layout) View mAboutUsView;
    @Bind(R.id.right_arrow_image3) ToggleButton jpushToggleBtn;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_settings;
    }

    public void initView() {
        StatAgent.initAction(this, "", "1", "24", "", "", "", "1", "");
        mTitleText.setText(R.string.settings);
        mBackBtn.setVisibility(View.VISIBLE);

        jpushToggleBtn.setChecked(PushApplication.getInstance().getReceiveMessageStatus());
        jpushToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    JPushInterface.resumePush(getApplicationContext());
                    PushApplication.getInstance().setReceiveMessage(isChecked);
                    PushApplication.getInstance().saveReceiveMessageStatus(isChecked);
                } else {
                    final boolean isC = isChecked;
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setMessage("提醒：消息通知关闭之后，物流以及快件消息将无法收到！")
                            .setPositiveButton("保留通知",
                                    new AlertDialog.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            buttonView.setChecked(true);
                                        }
                                    })
                            .setNegativeButton("残忍关闭",
                                    new AlertDialog.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            JPushInterface.stopPush(getApplicationContext());
                                            PushApplication.getInstance().setReceiveMessage(isC);
                                            PushApplication.getInstance().saveReceiveMessageStatus(isC);
                                        }
                                    }).create();// 创建
                    // 显示对话框
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
        });
        mVersionText.setText(Utils.getVersion(this));
    }

    public void initData() {
        if (PushApplication.getInstance().isLogin()) {
            mLogoutView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    @OnClick({R.id.back_img,R.id.modify_pwd_layout,R.id.update_app_layout,R.id.questionnaire_layout,
            R.id.feedback_layout,R.id.about_us_layout,R.id.logout_layout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.modify_pwd_layout:
                if (PushApplication.getInstance().isLogin()) {
                    IntentUtil.startActivity(SettingsActivity.this, ModifyUserPwdActivity.class);
                } else {
                    IntentUtil.startActivity(SettingsActivity.this, UserLoginActivity.class);
                }

                break;
            case R.id.update_app_layout:
                StatAgent.initAction(this, "", "2", "24", "", "", "新版本检测", "1", "");
                checkNewVersion();
                break;
            case R.id.questionnaire_layout:
                String url = Constant.URL_QUESTIONNAIRE
                        + "?userName=" + PushApplication.getInstance().getUserId()
                        + "&userIsAdmin=" + Constant.USER_TYPE;
                Bundle bundle = new Bundle();
                bundle.putString("title", getResources().getString(R.string.questionnaire));
                bundle.putString("url", url);
                IntentUtil.startActivity(SettingsActivity.this, CommonWebActivity.class, bundle);
                break;
            case R.id.feedback_layout:
                IntentUtil.startActivity(SettingsActivity.this, FeedbackActivity.class);
                break;
            case R.id.about_us_layout:
                bundle = new Bundle();
                bundle.putString("title", getResources().getString(R.string.about_us));
                bundle.putString("url", Constant.URL_ABOUT_US);
                IntentUtil.startActivity(SettingsActivity.this, LocalWebActivity.class, bundle);
                break;
            case R.id.logout_layout:
                StatAgent.initAction(this, "", "2", "24", "", "", "退出登录", "1", "");

                logout();
                break;
            default:
                break;
        }
    }

    private void logout() {

        new ActionSheetDialog(SettingsActivity.this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem(getText(android.R.string.yes).toString(), SheetItemColor.White,
                        new OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                //注销用户
                                logoutUser();
                            }
                        }).show();

    }

    private void checkNewVersion() {
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus,
                                         UpdateResponse updateInfo) {
                switch (updateStatus) {
                    case 0: // has update
                        UmengUpdateAgent
                                .showUpdateDialog(SettingsActivity.this, updateInfo);
                        break;
                    case 1: // has no update
                    case 2: // none wifi
                        AppToast.showShortText(SettingsActivity.this, "已经是最新版本！^_^");
                        break;
                    case 3: // time out
                        AppToast.showShortText(SettingsActivity.this, "网络超时,请检查网络！");
                        break;
                }
            }
        });

        UmengUpdateAgent.update(SettingsActivity.this);
    }

    private void logoutUser() {
        String url = Constant.DOMAIN + "/user/logout.json";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("token", PushApplication.getInstance().getToken());
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                handUserout();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
                if (200 == statusCode) {
                    handUserout();
                }
            }
        });
    }

    /**
     * 注销用户
     */
    private void handUserout(){
        StatAgent.initMemberId(SettingsActivity.this,"");
        PushApplication.getInstance().logoutHaiUser();
        Bundle bundle = new Bundle();
        bundle.putInt(MainUIActivity.ACTION_CURRETNTAB, Constant.HOME_FRAGMENT_INDEX);
        EventBus.getDefault().post(new UserChangedEvent());
        SettingsActivity.this.finish();
    }
}
