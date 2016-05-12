package com.haier.cabinet.customer.activity;


import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.AppConfig;
import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.common.util.AppToast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import butterknife.Bind;
import butterknife.OnClick;

public class ModifyUserNickNameActivity extends BaseActivity implements OnClickListener {

    @Bind(R.id.right_bar_layout)
    View mRightBarView;
    @Bind(R.id.nick_name_editor)
    EditText mNickEditor;
    @Bind(R.id.back_img)
    ImageView mBack_img;
    private String srcNickName;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_modify_user_nickname;
    }

    public void initView() {
    }

    public void initData() {
        ((TextView) findViewById(R.id.right_text)).setText(R.string.save);
        mTitleText.setText("修改用户名");
        srcNickName = getIntent().getStringExtra("nickname");
        mNickEditor.setText(srcNickName);
        mRightBarView.setEnabled(true);
        mBack_img.setVisibility(View.VISIBLE);
        mNickEditor.addTextChangedListener(textWatcher);
        mNickEditor.setSelection(srcNickName.length());
    }


    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String result = editable.toString();
            if (!result.trim().equals(srcNickName)) {
                mRightBarView.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    @OnClick({R.id.back_img, R.id.right_bar_layout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_bar_layout:
                if (mNickEditor.getText().toString().equals(srcNickName)) {
                    AppToast.showShortText(this, "无修改内容!");
                    return;
                }
                modifyNickName(mNickEditor.getText().toString());
                break;


            default:
                break;
        }
    }

    private void modifyNickName(final String nickname) {
        String url = Constant.DOMAIN + "/user/updateDetail.json";
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("nickName", nickname + "");
        params.put("token", PushApplication.getInstance().getToken());
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
                Log.e("haipeng---", statusCode + "");
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromServer(json)) {
                        case 200:
                            AppToast.showShortText(ModifyUserNickNameActivity.this, "修改成功!");
                            AppConfig.getAppConfig(ModifyUserNickNameActivity.this).set("user.name", nickname);
                            ModifyUserNickNameActivity.this.finish();
                            break;
                        case 504:
                            AppToast.showShortText(ModifyUserNickNameActivity.this, "您的帐号在另一台终端登录，请重新登录!");
                            ModifyUserNickNameActivity.this.finish();
                            PushApplication.getInstance().logoutHaiUser();
                            break;
                        default:
                            break;
                    }

                }
            }
        });
    }
}
