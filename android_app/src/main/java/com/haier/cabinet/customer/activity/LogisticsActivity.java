package com.haier.cabinet.customer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.LogisticsAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.DeliverInfo;
import com.haier.cabinet.customer.entity.LogisticsInfo;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.GsonUtils;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.CustomNodeListView;
import com.haier.common.util.AppToast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 物流查询
 * Created by jinbiao.wu on 2015/12/18.
 */
public class LogisticsActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.scrollView) ScrollView scrollView;
    @Bind(R.id.linear_no_result) LinearLayout linear_no_result;
    @Bind(R.id.listview) CustomNodeListView mListView;
    @Bind(R.id.iv_product) ImageView iv_product;//商品图片
    @Bind(R.id.iv_not_result) ImageView iv_not_result;//暂无物流信息图片
    //快递公司,物流状态,订单号
    @Bind(R.id.tv_name) TextView tv_name;
    @Bind(R.id.tv_status) TextView tv_status;
    @Bind(R.id.tv_code) TextView tv_code;

    private LogisticsAdapter adapter;
    private String mOrderNo;//商品id
    private static final int GET_LIST_DATA = 1001;//请求数据
    private static final int UPDATE_LIST_DATA = 1002;//刷新数据
    private static final int NO_LIST_DATA = 1003;//无数据
    private static final int ERRO_LIST_DATA = 1004;//数据异常

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_LIST_DATA:
                    requestLogisticsData(Constant.URL_LOGISTICS);
                    break;
                case UPDATE_LIST_DATA:
                    String json = (String) msg.obj;
                    initData(json);
                    break;
                case NO_LIST_DATA:
                case ERRO_LIST_DATA:
                    scrollView.setVisibility(View.GONE);
                    linear_no_result.setVisibility(View.VISIBLE);
                    tv_name.setText("暂无");
                    tv_code.setText("暂无");
                    tv_status.setText("暂无");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_logistics;
    }

    public void initView() {
        StatAgent.initAction(this, "", "1", "25", "", "", "", "1", "");
        mOrderNo = getIntent().getStringExtra("orderNo");
        mTitleText.setText("物流查询");
        mBackBtn.setVisibility(View.VISIBLE);
        iv_not_result.setImageBitmap(Util.readBitMap(LogisticsActivity.this, R.drawable.ic_comment_fail));
        mHandler.sendEmptyMessage(GET_LIST_DATA);
    }

    @Override
    public void initData() {

    }

    private void initData(String json) {
        List<DeliverInfo> datas = new ArrayList<>();
        LogisticsInfo info = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result")) {
                return;
            }
            info = new LogisticsInfo();
            JSONObject resultObject = jsonObject.getJSONObject("result");
            info.express_name = resultObject.getString("express_name");
            info.express_status_name = resultObject.getString("express_status_name");
            info.shipping_code = resultObject.getString("shipping_code");
            info.goods_image = resultObject.getString("goods_image");
            datas = GsonUtils.jsonToList(resultObject.getString("deliver_info"), DeliverInfo.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ImageLoader.getInstance().displayImage(info.goods_image,
                iv_product, PushApplication.getInstance().getDefaultOptions());
        tv_status.setText("" + info.express_status_name);
        tv_name.setText("" + info.express_name);
        tv_code.setText("" + info.shipping_code);
        if(datas.size()!=0){
            adapter = new LogisticsAdapter(datas, this);
            mListView.setAdapter(adapter);
        }else {
            mHandler.sendEmptyMessage(ERRO_LIST_DATA);
        }

    }

    @Override
    @OnClick({R.id.back_img})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                finish();
                break;
        }
    }

    private void requestLogisticsData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("order_id", ""+mOrderNo);
        params.add("member_id", ""+PushApplication.getInstance().getUserId());
//        params.add("order_id", "1411");
//        params.add("member_id", "18053277898");
        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(LogisticsActivity.this, getString(R.string.loading), true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                DialogHelper.stopProgressDlg();
                mHandler.sendEmptyMessage(ERRO_LIST_DATA);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            mHandler.obtainMessage(UPDATE_LIST_DATA, json).sendToTarget();
                            break;
                        case 1002:
                            mHandler.sendEmptyMessage(NO_LIST_DATA);
                            break;
                        case 2001:
                            mHandler.sendEmptyMessage(ERRO_LIST_DATA);
                            break;
                        default:
                            break;
                    }

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iv_not_result.setImageResource(0);
        System.gc();
    }
}