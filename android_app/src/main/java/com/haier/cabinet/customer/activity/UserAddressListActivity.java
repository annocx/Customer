package com.haier.cabinet.customer.activity;

import java.util.List;

import org.apache.http.Header;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.AddressInfoAdapter;
import com.haier.cabinet.customer.base.BaseListActivity;
import com.haier.cabinet.customer.base.ListBaseAdapter;
import com.haier.cabinet.customer.entity.AddressInfo;
import com.haier.cabinet.customer.event.AddressInfoEvent;
import com.haier.cabinet.customer.event.DeleteAddressEvent;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sunday.statagent.StatAgent;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class UserAddressListActivity extends BaseListActivity<AddressInfo> {

    private final String TAG = "UserAddressListActivity";


    TextView addAddressText;

    @Override
    protected ListBaseAdapter<AddressInfo> getListAdapter() {
        return new AddressInfoAdapter(Constant.DISPLAY_ADDRESS_LIST,null,mHandler);
    }


    public void initView() {
        super.initView();
        mTitleText.setText(R.string.manage_address);
        mBackBtn.setVisibility(View.VISIBLE);

        float height = getResources().getDimension(R.dimen.btn_next_height);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) height);

        lp.setMargins(16,16,16,16);
        mDynamicLayout.setVisibility(View.VISIBLE);
        View view = mInflater.inflate(R.layout.layout_add_address_textview,null);
        mDynamicLayout.addView(view,lp);

        addAddressText = (TextView) findViewById(R.id.add_address);
        addAddressText.setOnClickListener(this);

    }

    public void initData() {

    }

    @Override
    protected void sendRequestData() {
        getAddress();
    }

    @Override
    protected void initLayoutManager() {
        super.initLayoutManager();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_address:
                StatAgent.initAction(this, "", "2", "22", "", "", addAddressText.getText().toString(), "1", "");

                Bundle bundle = new Bundle();
                bundle.putInt("size", mListAdapter.getDataList().size());
                IntentUtil.startActivity(UserAddressListActivity.this, UserAddressAddActvity.class, bundle);
                break;
            default:
                break;
        }
    }


    public static final int DELETE_ADDRESS = 1001;
    private final int UPDATE_ADDRESS_LIST = 1003;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DELETE_ADDRESS:
                    int position = msg.arg1;
                    deleteAddress(position,(AddressInfo)msg.obj);
                    break;
                case UPDATE_ADDRESS_LIST:
                    String json = (String) msg.obj;
                    List<AddressInfo> data = Util.getAddressListByJosn(json);

                    if (mCurrentPage == 1) {
                        mListAdapter.setDataList(data);

                    } else {
                        RecyclerViewStateUtils.setFooterViewState(mRecyclerView, LoadingFooter.State.Normal);
                        mListAdapter.addAll(data);
                    }

                    break;

                default:
                    break;
            }
        }
    };

    private void deleteAddress(final int position, final AddressInfo address) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("address_id", address.id);
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_DELETE_USER_ADDRESS_, params, new AsyncHttpResponseHandler() {

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                String json = new String(response);
				Log.d(TAG, "content " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        mListAdapter.deleteData(position);
                        EventBus.getDefault().post(new DeleteAddressEvent(address));
                    } else {
                        AppToast.showShortText(UserAddressListActivity.this, "删除地址失败了!");
                    }

                }
            }

        });

    }

    private void getAddress() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_USER_ADDRESS_LIST, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if(mCurrentPage == 1){
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                mSwipeRefreshLayout.setRefreshing(false);
                String json = new String(response);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        mHandler.obtainMessage(UPDATE_ADDRESS_LIST, json).sendToTarget();
                    } else {
                        AppToast.showShortText(UserAddressListActivity.this, "获取地址失败了!");
                    }

                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if(mSwipeRefreshLayout.isRefreshing()){
                   mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StatAgent.initAction(this, "", "2", "22", "", "", "back", "2", "");
    }

    public void onEventMainThread(AddressInfoEvent event)
    {
        onRefresh();
    }
}
