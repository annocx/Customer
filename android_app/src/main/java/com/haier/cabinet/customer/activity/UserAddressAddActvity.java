package com.haier.cabinet.customer.activity;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import kankan.wheel.widget.adapters.Area;
import kankan.wheel.widget.adapters.City;
import kankan.wheel.widget.adapters.Province;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.AddressInfo;
import com.haier.cabinet.customer.event.AddressInfoEvent;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.common.util.AppToast;
import com.haier.common.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sunday.statagent.StatAgent;
import com.umeng.analytics.MobclickAgent;


public class UserAddressAddActvity extends BaseActivity {

    protected static final String TAG = "UserAddressAddActvity";
    @Bind(R.id.my_set_buyaddress_city)
    EditText cityText;
    @Bind(R.id.my_set_buyaddress_jiequ)
    EditText jiequText;
    @Bind(R.id.my_set_buyaddress_name)
    EditText nameText;
    @Bind(R.id.my_set_buyaddress_phone)
    EditText phoneText;
    @Bind(R.id.my_set_buyaddress_city_linear)
    LinearLayout cityLinear;
    @Bind(R.id.my_set_buyaddress_jiequ_linear)
    LinearLayout jiequLinear;
    @Bind(R.id.my_set_buyaddress_name_linear)
    LinearLayout nameLinear;
    @Bind(R.id.my_set_buyaddress_phone_linear)
    LinearLayout phoneLinear;
    @Bind(R.id.set_default_address_view)
    View defaultAddressView;

    @Bind(R.id.my_set_buyaddress_city_text)
    TextView cityTextView;
    @Bind(R.id.my_set_buyaddress_jiequ_text)
    TextView jiequTextView;
    @Bind(R.id.my_set_buyaddress_name_text)
    TextView nameTextView;
    @Bind(R.id.my_set_buyaddress_phone_text)
    TextView phoneTextView;

    @Bind(R.id.my_set_buyaddress_address_btn)
    Button postBtn;
    @Bind(R.id.my_set_address_checkbox)
    CheckBox defaultSwitch;
    private AddressInfo myAddress;


    private AddressInfo addressinfo;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_user_address;
    }

    public void initView() {
        StatAgent.initAction(this, "", "1", "23", "", "", "", "1", "");
        mBackBtn.setVisibility(View.VISIBLE);
        myAddress = new AddressInfo();
        initListener();
    }

    public void initData() {
        addressinfo = (AddressInfo) getIntent().getExtras().get("address");
        if (addressinfo != null) {// 修改地址信息
            mTitleText.setText(R.string.modify_community_address);
            phoneText.setVisibility(View.GONE);
            phoneText.setText(addressinfo.phone);
            phoneLinear.setVisibility(View.VISIBLE);
            phoneTextView.setText(addressinfo.phone);

            cityText.setText(addressinfo.getCurCity());

            jiequText.setText(addressinfo.street);
            nameText.setText(addressinfo.name);
            phoneText.setText(addressinfo.phone);

            jiequText.setVisibility(View.GONE);
            jiequLinear.setVisibility(View.VISIBLE);
            jiequTextView.setText(addressinfo.street);

            nameText.setVisibility(View.GONE);
            nameLinear.setVisibility(View.VISIBLE);
            nameTextView.setText(addressinfo.name);

            if (addressinfo.status) {
                defaultSwitch.setChecked(true);
            } else {
                defaultSwitch.setChecked(false);
            }

            if (myAddress.province != null) {
                addressinfo.province = myAddress.province;
                addressinfo.city = myAddress.city;
                addressinfo.area = myAddress.area;
            }

            cityLinear.setVisibility(View.VISIBLE);
            cityText.setVisibility(View.GONE);
            cityTextView.setText(addressinfo.getCurCity());

            myAddress = addressinfo;
        } else {
            Log.d(TAG, "bundle == null");
            mTitleText.setText(R.string.add_community_address);

            if (TextUtils.isEmpty(myAddress.getCurCity())) {
                cityText.setVisibility(View.VISIBLE);
                cityLinear.setVisibility(View.GONE);
            } else {
                cityText.setVisibility(View.GONE);
                cityLinear.setVisibility(View.VISIBLE);
                cityTextView.setText(myAddress.getCurCity());
            }

            if (getIntent().getIntExtra("size", 0) == 0) {
                defaultSwitch.setChecked(true);
                defaultSwitch.setEnabled(false);
                defaultAddressView.setOnClickListener(null);
            }
        }
    }

    private void initListener() {
        jiequText.setOnFocusChangeListener(focusChanger);
        nameText.setOnFocusChangeListener(focusChanger);
        phoneText.setOnFocusChangeListener(focusChanger);
    }

    private final int REQUEST_COMMUNITY = 1000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_COMMUNITY:
                if (resultCode == RESULT_OK) {
                    myAddress.provincialCityArea = "";//需置空，否则修改时一直有值导致getCurCity使用原值
                    Bundle bundle = data.getExtras();
                    myAddress.province = (Province) bundle.getSerializable("province");
                    myAddress.city = (City) bundle.getSerializable("city");
                    myAddress.area = (Area) bundle.getSerializable("area");
                    if (addressinfo != null) {// 修改地址信息
                        cityTextView.setText(myAddress.getCurCity());
                    } else {
                        cityText.setText(myAddress.getCurCity());
                    }
                }
                break;

            default:
                break;
        }
    }


    @Override
    @OnClick({R.id.my_set_buyaddress_city,R.id.my_set_buyaddress_city_linear,
            R.id.my_set_buyaddress_jiequ_linear,R.id.my_set_buyaddress_name_linear,
            R.id.my_set_buyaddress_phone_linear,R.id.my_set_buyaddress_address_btn,
            R.id.my_set_address_checkbox,R.id.set_default_address_view})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_set_buyaddress_city:
            case R.id.my_set_buyaddress_city_linear:
                Intent i = new Intent(UserAddressAddActvity.this, CitiesSetActivity.class);
                startActivityForResult(i, REQUEST_COMMUNITY);

			/*cityText.setVisibility(View.VISIBLE);
            cityLinear.setVisibility(View.GONE);*/
                break;
            case R.id.my_set_buyaddress_jiequ_linear:
                jiequText.setVisibility(View.VISIBLE);
                jiequLinear.setVisibility(View.GONE);

                jiequText.setFocusable(true);
                jiequText.setFocusableInTouchMode(true);

                jiequText.requestFocus();

                break;
            case R.id.my_set_buyaddress_name_linear:
                nameText.setVisibility(View.VISIBLE);
                nameLinear.setVisibility(View.GONE);

                nameText.setFocusable(true);
                nameText.setFocusableInTouchMode(true);

                nameText.requestFocus();
                break;
            case R.id.my_set_buyaddress_phone_linear:
                phoneText.setVisibility(View.VISIBLE);
                phoneLinear.setVisibility(View.GONE);

                phoneText.setFocusable(true);
                phoneText.setFocusableInTouchMode(true);

                phoneText.requestFocus();
                break;
            case R.id.my_set_buyaddress_address_btn:
                StatAgent.initAction(this, "", "2", "23", "", "", postBtn.getText().toString(), "1", "");

                myAddress.street = jiequText.getText().toString();
                myAddress.name = nameText.getText().toString();
                myAddress.phone = phoneText.getText().toString();

                if (myAddress.phone.length() > 0) {
                    phoneText.setVisibility(View.GONE);
                    phoneLinear.setVisibility(View.VISIBLE);
                    phoneTextView.setText(myAddress.phone);
                }

                if (!Utils.isMobileNO(myAddress.phone)) {
                    AppToast.showShortText(this, "手机号无效，请重新输入");
                    return;
                }

                postBtn.requestFocus();

                postBtn.setFocusable(true);
                postBtn.setFocusableInTouchMode(true);


                if ((myAddress.province == null)
                        || TextUtils.isEmpty(myAddress.street)
                        || TextUtils.isEmpty(myAddress.name)
                        || TextUtils.isEmpty(myAddress.phone)) {
                    AppToast.showShortText(this, "请完整填写地址");
                    return;
                }

                myAddress.status = defaultSwitch.isChecked();

                addOrModifyAddress();

                break;
            //case R.id.my_set_address_checkbox:
            case R.id.set_default_address_view:
                setDefaultAddress();
                break;
            default:
                break;
        }
    }

    private void setDefaultAddress() {
        if (addressinfo != null) {
            if (addressinfo.status) {//已经设置为默认地址，不能取消
                AppToast.showShortText(UserAddressAddActvity.this, "亲，必须设置一个默认地址！");
            } else {
//				setDefaultAddressInfo(addressinfo);
                myAddress.status = true;
            }
            defaultSwitch.setChecked(true);
        } else {
            defaultSwitch.setChecked(!defaultSwitch.isChecked());
            if (defaultSwitch.isChecked()) {
                myAddress.status = true;
            } else {
                myAddress.status = false;
            }
        }
    }

    OnFocusChangeListener focusChanger = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            myAddress.street = jiequText.getText().toString();
            myAddress.name = nameText.getText().toString();
            myAddress.phone = phoneText.getText().toString();

            switch (v.getId()) {
                case R.id.my_set_buyaddress_jiequ:
                    if (!hasFocus && myAddress.street.length() > 0) {
                        jiequText.setVisibility(View.GONE);
                        jiequLinear.setVisibility(View.VISIBLE);

                        jiequTextView.setText(myAddress.street);
                    }

                    if (hasFocus) {
                        jiequText.setSelectAllOnFocus(true);
                        Selection.setSelection(jiequText.getText(), jiequText.getText().length());
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(jiequText, 0);
                    }
                    break;
                case R.id.my_set_buyaddress_name:
                    if (!hasFocus && myAddress.name.length() > 0) {
                        nameText.setVisibility(View.GONE);
                        nameLinear.setVisibility(View.VISIBLE);

                        nameTextView.setText(myAddress.name);
                    }

                    if (hasFocus) {
                        nameText.setSelectAllOnFocus(true);
                        Selection.setSelection(nameText.getText(), nameText.getText().length());
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(nameText, 0);
                    }
                    break;
                case R.id.my_set_buyaddress_phone:
                    if (!hasFocus && myAddress.phone.length() > 0) {
                        phoneText.setVisibility(View.GONE);
                        phoneLinear.setVisibility(View.VISIBLE);

                        phoneTextView.setText(myAddress.phone);
                    }
                    if (hasFocus) {
                        phoneText.setSelectAllOnFocus(true);
                        Selection.setSelection(phoneText.getText(), phoneText.getText().length());
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(phoneText, 0);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private final int ADD_ADDRESS_SUCCESS = 1001;
    private final int MODIFY_ADDRESS_SUCCESS = 1002;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ADD_ADDRESS_SUCCESS:
                case MODIFY_ADDRESS_SUCCESS:
                    EventBus.getDefault().post(new AddressInfoEvent(myAddress));
                    finish();
                    break;

                default:
                    break;
            }
        }

    };

    private void modifyAddressInfo(AddressInfo addressinfo) {
        Log.d(TAG, "modifyAddressInfo");

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("address_id", addressinfo.id);
        params.put("member_id", PushApplication.getInstance().getUserId());
        params.put("true_name", addressinfo.name);
        params.put("mob_phone", addressinfo.phone);
        params.put("area_id", addressinfo.province.id);
        params.put("city_id", addressinfo.city.id);
        params.put("area_info", addressinfo.getCurCity());
        params.put("address", addressinfo.street);
        params.put("is_default", addressinfo.status ? 1 : 0);
        client.get(Constant.URL_MODIFY_USER_ADDRESS_, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(UserAddressAddActvity.this, getString(R.string.loading), true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onFailure : ", e);
                DialogHelper.stopProgressDlg();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                Log.d(TAG, "onSuccess json = " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        mHandler.obtainMessage(MODIFY_ADDRESS_SUCCESS).sendToTarget();
                    } else {
                        AppToast.showShortText(UserAddressAddActvity.this, "修改地址失败了!");
                    }
                }
            }
        });
    }

    private void addOrModifyAddress() {

        Log.d(TAG, "addOrModifyAddress");

        if (addressinfo != null) {
            modifyAddressInfo(myAddress);
        } else {
            addAddressInfo(myAddress);

        }

    }

    private void addAddressInfo(AddressInfo addressinfo) {
        Log.d(TAG, "addAddressInfo");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("member_id", PushApplication.getInstance().getUserId());
        params.put("true_name", addressinfo.name);
        params.put("mob_phone", addressinfo.phone);
        params.put("area_id", addressinfo.province.id);
        params.put("city_id", addressinfo.city.id);
        params.put("area_info", addressinfo.province.name + addressinfo.city.name + addressinfo.area.name);
        params.put("address", addressinfo.street);
        params.put("is_default", addressinfo.status ? 1 : 0);
        client.get(Constant.URL_ADD_USER_ADDRESS, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(UserAddressAddActvity.this, getString(R.string.loading), true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onFailure : ", e);
                DialogHelper.stopProgressDlg();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
//                Log.d(TAG, "onSuccess json = " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        mHandler.obtainMessage(ADD_ADDRESS_SUCCESS, json).sendToTarget();
                    } else {
                        AppToast.showShortText(UserAddressAddActvity.this, "添加地址失败了!");
                    }
                }
            }
        });
    }

    private void setDefaultAddressInfo(AddressInfo addressinfo) {
        Log.d(TAG, "setDefaultAddressInfo");

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", addressinfo.id);
        params.put("user_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_SET_USER_DEFAULT_ADDRESS, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onFailure : ", e);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                Log.d(TAG, "onSuccess statusCode = " + statusCode);
                String json = new String(response);
                Log.d(TAG, "onSuccess json = " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        mHandler.obtainMessage(MODIFY_ADDRESS_SUCCESS).sendToTarget();
                    } else {
                        AppToast.showShortText(UserAddressAddActvity.this, "设置默认地址失败!");
                    }
                }
            }
        });
    }

}
