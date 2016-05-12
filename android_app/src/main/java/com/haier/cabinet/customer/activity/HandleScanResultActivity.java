package com.haier.cabinet.customer.activity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.MyBoxListAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.PackageBox;
import com.haier.cabinet.customer.fragment.HomeFragment;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import butterknife.Bind;

public class HandleScanResultActivity extends BaseActivity {
    private static final String TAG = "HandleScanResultActivity";
    @Bind(R.id.listView)
    ListView mListView;
    @Bind(R.id.box_no_text)
    TextView boxNoText;
    @Bind(R.id.box_pickup_no_text)
    TextView pickUpNoText;
    @Bind(R.id.open_box_success_text)
    TextView openBoxSuccessText;
    @Bind(R.id.open_box_failed_text)
    TextView openBoxFailedText;
    @Bind(R.id.express_tip_text)
    TextView tipText;
    @Bind(R.id.open_package_box_success_layout)
    View openBoxSuccessView;
    @Bind(R.id.open_package_box_failed_layout)
    View openBoxFailedView;

    private LinkedList<PackageBox> mListItems;
    private MyBoxListAdapter mListAdapter;
    private PackageBox mPackageBox;
    private int mCurPageIndex = 1;
    private static final int pageSize = 10;
    private String arm = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_open_packagebox_result;
    }

    public void initView() {
        mTitleText.setText("开箱");
        mBackBtn.setVisibility(View.VISIBLE);
        mListView.setDivider(getResources().getDrawable(R.color.app_bg));
        mListView.setDividerHeight(10);
    }

    public void initData() {

        mListItems = new LinkedList<PackageBox>();
        mListAdapter = new MyBoxListAdapter(this, mHandler, mListItems);
        mListView.setAdapter(mListAdapter);

        mPackageBox = (PackageBox) getIntent().getSerializableExtra("packagebox");
        arm = getIntent().getStringExtra("arm");
        // 开箱
        if (TextUtils.isEmpty(arm)) {
            openPackageBox(mPackageBox);
        } else {
            if (arm.equals("1")) {//arm开箱
                openPackageBox_ARM(mPackageBox);
            }
        }

        boxNoText.setText(String.valueOf(mPackageBox.boxNo));

        mHandler.sendEmptyMessage(GET_PACKAGE_LIST_DATA);

    }

    private static final int GET_PACKAGE_LIST_DATA = 1001;
    private static final int UPDATE_PACKAGE_LIST = 1002;
    private static final int NO_PACKAGE_LIST_DATA = 1003;
    private static final int USER_TOKEN_TIMEOUT = 1004;
    private static final int GET_PACKAGE_LIST_DATA_FAILURE = 1005;
    private static final int NO_MORE_PACKAGE_LIST_DATA = 1006;
    public static final int SCAN_CABINET_QR_CODE = 1007;
    public static final int FETCHING_EXPRESS = 1008;
    public static final int TRY_OPEN_EXPRESS = 1009;
    public static final int SHOW_EVALUATION_DIALOG = 1010;
    private Handler mHandler = new Handler() {
        @SuppressLint("LongLogTag")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_PACKAGE_LIST_DATA:
                    String url = getRequestUrl();
                    requestPackageData(url);
                    break;
                case UPDATE_PACKAGE_LIST:
                    String json = (String) msg.obj;
                    List<PackageBox> data = getPackageListByJosn(json);
                    Log.d(TAG, "data size = " + data.size());
                    if (data.isEmpty()) {
                        return;
                    }

                    mListItems.addAll(data);
                    mListAdapter.notifyDataSetChanged();
                    String tip = String.format(
                            getResources().getString(R.string.express_tip),
                            getIntent().getIntExtra("total", 0));
                    tipText.setText(tip);
                    break;

                case NO_PACKAGE_LIST_DATA:
                    // 清空数据
                    if (!mListItems.isEmpty()) {
                        mListItems.clear();
                    }
                    mListAdapter.notifyDataSetChanged();
                    String empty_tip = String.format(
                            getResources().getString(R.string.empty_express_tip),
                            getIntent().getIntExtra("total", 0));
                    tipText.setText(empty_tip);
                    break;
                case TRY_OPEN_EXPRESS:
                    mPackageBox = (PackageBox) msg.obj;
                    if (TextUtils.isEmpty(getIntent().getStringExtra("arm"))) {
                        openPackageBox(mPackageBox);
                    } else {
                        if (getIntent().getStringExtra("arm").equals("1")) {
                            Log.d(TAG, "TRY_OPEN_EXPRESS");
                            openPackageBox_ARM(mPackageBox);
                        }
                    }

                    break;
                case SHOW_EVALUATION_DIALOG:
                    Bundle bundle = new Bundle();
                    bundle.putString("orderNo", mPackageBox.packageNo);
                    IntentUtil.startActivity(HandleScanResultActivity.this, EvaluationServiceActivity.class, bundle);
                    break;
                case USER_TOKEN_TIMEOUT:
                    // token失效跳转到登陆界面
                    AppToast.showShortText(HandleScanResultActivity.this, "登录超时，请您重新登录!");
                    PushApplication.getInstance().logoutHaiUser();
                    HandleScanResultActivity.this.finish();

                    break;

                default:
                    break;
            }
        }
    };

    private void openPackageBox_ARM(final PackageBox box) {
        String url = Constant.URL_OPEN_PACKAGEBOX_ARM;
//		Log.d(TAG, "openPackageBox_ARM url -- " + url);
//		Log.d(TAG, "boxNo -- " + box.boxNo);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("openBoxKey", box.pickUpNo);
        params.put("guiziNo", box.cabinetNo);
        params.put("tradeWaterNo", box.tradeWaterNo);
        params.put("operateType", "1");// 1:App用户开箱;2:App快递员开箱;
        params.put("arm", "1");
        params.put("token", PushApplication.getInstance().getToken());
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(HandleScanResultActivity.this, "箱门正在打开，请您不要远离柜子...", true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                DialogHelper.stopProgressDlg();
                openBoxResponse(box.boxNo, false);
                Log.e(TAG, "开箱异常，onFailure ", arg3);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
//				Log.d(TAG, "openPackageBox_ARM onSuccess json －－　" + json);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromServer(json)) {
                        case 200:
                            openBoxResponse(box.boxNo, true);
                            break;
                        case 504:
                            mHandler.sendEmptyMessage(USER_TOKEN_TIMEOUT);
                            break;
                        case 600:
                            openBoxResponse(box.boxNo, false);
                            break;

                        default:
                            break;
                    }

                }

            }

        });
    }

    private void openPackageBox(final PackageBox box) {
        String url = Constant.URL_OPEN_PACKAGEBOX;
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("terminalNo", box.cabinetNo);
        params.put("customerMobile", box.customerMobile);
        params.put("storedTime", box.deliveredTime);
        params.put("corpType", box.corpType);
        params.put("packageNo", box.packageNo);
        params.put("tradeWaterNo", box.tradeWaterNo);
        params.put("token", PushApplication.getInstance().getToken());
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(HandleScanResultActivity.this, "箱门正在打开，请您不要远离柜子...", true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                DialogHelper.stopProgressDlg();
                openBoxResponse(box.boxNo, false);
                Log.e(TAG, "开箱异常，onFailure ", arg3);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
//				Log.d(TAG, "openPackageBox onSuccess json －－　" + json);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromServer(json)) {
                        case 200:
                            openBoxResponse(box.boxNo, true);
                            break;
                        case 504:
                            mHandler.sendEmptyMessage(USER_TOKEN_TIMEOUT);
                            break;
                        case 600:
                            openBoxResponse(box.boxNo, false);
                            break;

                        default:
                            break;
                    }

                }

            }

        });
    }

    private void openBoxResponse(int boxNo, boolean isSuccess) {
        mListAdapter.updateItem(boxNo, isSuccess);
        boxNoText.setText(String.valueOf(boxNo));
        if (isSuccess) {
            openBoxFailedText.setVisibility(View.GONE);
            openBoxFailedView.setVisibility(View.GONE);
            openBoxSuccessView.setVisibility(View.VISIBLE);
            openBoxSuccessText.setVisibility(View.VISIBLE);
            HomeFragment.isUpdate = true;
            mHandler.sendEmptyMessage(SHOW_EVALUATION_DIALOG);
        } else {
            pickUpNoText.setText(mPackageBox.pickUpNo);
            openBoxFailedText.setVisibility(View.VISIBLE);
            openBoxFailedView.setVisibility(View.VISIBLE);
            openBoxSuccessText.setVisibility(View.GONE);
            openBoxSuccessView.setVisibility(View.GONE);
        }
    }

    private void requestPackageData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                Log.e(TAG, "获取数据异常 ", arg3);
                mHandler.sendEmptyMessage(GET_PACKAGE_LIST_DATA_FAILURE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {

                String json = new String(response);
//				Log.d(TAG, "requestPackageData onSuccess " + json);

                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromServer(json)) {
                        case 200:
                            mHandler.obtainMessage(UPDATE_PACKAGE_LIST, json)
                                    .sendToTarget();
                            break;
                        case 201:
                            if (mListAdapter.getCount() == 0) {
                                mHandler.sendEmptyMessage(NO_PACKAGE_LIST_DATA);
                            } else {
                                mHandler.sendEmptyMessage(NO_MORE_PACKAGE_LIST_DATA);
                            }

                            break;
                        case 504:
                            mHandler.sendEmptyMessage(USER_TOKEN_TIMEOUT);
                            break;
                        default:
                            break;
                    }

                }
            }

        });

    }

    private List<PackageBox> getPackageListByJosn(String json) {
        List<PackageBox> list = new ArrayList<PackageBox>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("data")) {
                return null;
            }
            JSONArray boxesArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < boxesArray.length(); i++) {
                JSONObject boxObject = boxesArray.getJSONObject(i);
                PackageBox box = new PackageBox();
                box.cabinetNo = boxObject.getString("terminalNo");
                box.cabinetName = boxObject.getString("terminalName");
                box.pickUpNo = boxObject.getString("openBoxKey");
                box.packageNo = boxObject.getString("packageNo");
                box.postmanMobile = boxObject.getString("courierName");
                box.deliveredTime = boxObject.getString("storedTime");
                box.overdueTime = boxObject.getString("endTime");
                box.tradeWaterNo = boxObject.getString("tradeWaterNo");
                box.expressCompany = boxObject.getString("companyName");
                box.boxNo = boxObject.getInt("boxNo");
                box.corpType = boxObject.getString("corpType");
                box.isTimeout = Utils.getOvertime(box.overdueTime) > 0;

                list.add(box);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }
        return list;
    }

    private String getRequestUrl() {
        mCurPageIndex = 1;
        String url = Constant.DOMAIN + "/order/customerUnGetList.json?"
                + "start=" + (mCurPageIndex - 1) * pageSize + "&pageSize="
                + pageSize + "&guiziNo=" + mPackageBox.cabinetNo
                + "&isTimeout=-1" + "&token="
                + PushApplication.getInstance().getToken();

        return url;
    }
}
