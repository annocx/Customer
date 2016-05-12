package com.haier.cabinet.customer.push;

import android.app.Notification;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushManager;
import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.PushMsgListAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.PushMsg;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.haier.cabinet.customer.widget.recyclerview.LoadingFooter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerOnScrollListener;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewStateUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PushMsgListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = PushMsgListActivity.class.getSimpleName();

	private View emptyView;
	private HeaderAndFooterRecyclerViewAdapter mHeaderAndFooterRecyclerViewAdapter = null;
	private PushMsgListAdapter mListAdapter;
	private ArrayList<PushMsg> mListItems;
	private boolean mIsStart = false;
	private static int totalRecord = 0;
	private int mCurPageIndex = 1;
	private static final int REQUEST_COUNT = 10;
	private RecyclerView recyclerView;
	private SwipeRefreshLayout swipeRefreshLayout;

	@Override
	protected int getLayoutId() {
		return R.layout.activity_push_msg_list;
	}

	public void initView() {
		mTitleText.setText(R.string.person_push_msg_text);
		mBackBtn.setVisibility(View.VISIBLE);

		emptyView = findViewById(R.id.empty_view);
		emptyView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (mListAdapter.getItemCount() == 0) {
						mIsStart = true;
						mHandler.sendEmptyMessage(GET_PUSH_MSG_LIST_DATA);
					}
				}
				return false;
			}
		});

		mListItems = new ArrayList<>();

		recyclerView = (RecyclerView) findViewById(R.id.refreshable_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		mListAdapter = new PushMsgListAdapter(this);
		mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mListAdapter);
		recyclerView.setAdapter(mHeaderAndFooterRecyclerViewAdapter);
		mOnScrollListener.setSwipeRefreshLayout(swipeRefreshLayout);
		recyclerView.addOnScrollListener(mOnScrollListener);

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
		swipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(this, 24));
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.setRefreshing(true);

		mIsStart = true;
		mHandler.sendEmptyMessage(GET_PUSH_MSG_LIST_DATA);
    }

	public void initData() {
		Resources resource = this.getResources();
		String pkgName = this.getPackageName();
		// Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
		// PushManager.enableLbs(getApplicationContext());

		// Push: 设置自定义的通知样式，具体API介绍见用户手册，如果想使用系统默认的可以不加这段代码
		// 请在通知推送界面中，高级设置->通知栏样式->自定义样式，选中并且填写值：1，
		// 与下方代码中 PushManager.setNotificationBuilder(this, 1, cBuilder)中的第二个参数对应
		CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
				resource.getIdentifier(
						"notification_custom_builder", "layout", pkgName),
				resource.getIdentifier("notification_icon", "id", pkgName),
				resource.getIdentifier("notification_title", "id", pkgName),
				resource.getIdentifier("notification_text", "id", pkgName));
		cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
		cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND
				| Notification.DEFAULT_VIBRATE);
		cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
		cBuilder.setLayoutDrawable(resource.getIdentifier(
				"simple_notification_icon", "drawable", pkgName));
		PushManager.setNotificationBuilder(this, 1, cBuilder);
	}
    
    @Override
    public void onResume() {
        super.onResume();
//		if (!PushApplication.getInstance().isLogin()) {
//			UIHelper.showLoginActivity(this);
//			return;
//		}
    }

    private void showEmptyDataView(){
		recyclerView.setVisibility(View.GONE);
		emptyView.setVisibility(View.VISIBLE);
	}

	private RecyclerOnScrollListener mOnScrollListener = new RecyclerOnScrollListener() {

		public void onBottom() {

			LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(recyclerView);
			if(state == LoadingFooter.State.Loading) {
				return;
			}

			if (mListAdapter.getItemCount() < totalRecord) {
				// loading more
				RecyclerViewStateUtils.setFooterViewState(PushMsgListActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
				mHandler.sendEmptyMessage(GET_PUSH_MSG_LIST_DATA);
			} else {
				//the end
				RecyclerViewStateUtils.setFooterViewState(PushMsgListActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.TheEnd, null);
			}
		}
	};
    
    private static final int GET_PUSH_MSG_LIST_DATA = 1001;
	private static final int UPDATE_PUSH_MSG_LIST = 1002;
	private static final int NO_PUSH_MSG_DATA = 1003;
	private static final int GET_PUSH_MSG_DATA_FAILURE = 1004;
	private static final int NO_MORE_PUSH_MSG_DATA = 1005;
	private static final int USER_TOKEN_TIMEOUT = 1006;
    private Handler mHandler = new Handler() {
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case GET_PUSH_MSG_LIST_DATA:
				String url = getRequestUrl(mIsStart);
				requestPushMsgData(url);
				break;
			case UPDATE_PUSH_MSG_LIST:
				String json = (String) msg.obj;
				ArrayList<PushMsg> data = getPushMsgListByJosn(json);

				if (null == data) {
					return;
				}

				if (mIsStart) {
					//清空数据
					if (mListAdapter.getItemCount() > 0) {
						mListAdapter.clear();
					}
					mListAdapter.addAll(data);
				} else {
					mListAdapter.addAll(data);
					RecyclerViewStateUtils.setFooterViewState(recyclerView, LoadingFooter.State.Normal);
				}

				recyclerView.setVisibility(View.VISIBLE);
				mListAdapter.notifyDataSetChanged();

				mIsStart = false;
				break;
			case NO_PUSH_MSG_DATA:
				//清空数据
				if (mListAdapter.getItemCount() > 0) {
					mListAdapter.clear();
				}
				mListAdapter.notifyDataSetChanged();
				mIsStart = false;
				break;
			case NO_MORE_PUSH_MSG_DATA:
				mListAdapter.notifyDataSetChanged();
				mIsStart = false;
				break;
			case GET_PUSH_MSG_DATA_FAILURE:
				if(mListAdapter.getItemCount() > 0){
					RecyclerViewStateUtils.setFooterViewState(PushMsgListActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.NetWorkError, mFooterClick);
					mListAdapter.notifyDataSetChanged();
				}else {
					showEmptyDataView();
				}
				mIsStart = false;
				break;
			case USER_TOKEN_TIMEOUT:
				//token失效跳转到登陆界面
				PushApplication.getInstance().logoutHaiUser();
				PushMsgListActivity.this.finish();
				break;
			default:
				break;
			}
    	};
    };

	private View.OnClickListener mFooterClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			RecyclerViewStateUtils.setFooterViewState(PushMsgListActivity.this, recyclerView, REQUEST_COUNT, LoadingFooter.State.Loading, null);
			mHandler.sendEmptyMessage(GET_PUSH_MSG_LIST_DATA);
		}
	};

    private void requestPushMsgData(String url) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, null, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				swipeRefreshLayout.setRefreshing(false);
				mHandler.sendEmptyMessage(GET_PUSH_MSG_DATA_FAILURE);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] response) {
				swipeRefreshLayout.setRefreshing(false);
				String json = new String(response);
				if (200 == statusCode) {
					switch (JsonUtil.getStateFromServer(json)) {
					case 200:
						mHandler.obtainMessage(UPDATE_PUSH_MSG_LIST, json).sendToTarget();
						break;
					case 201:
						if (mListAdapter.getItemCount() == 0) {
							mHandler.sendEmptyMessage(NO_PUSH_MSG_DATA);
						}else {
							mHandler.sendEmptyMessage(NO_MORE_PUSH_MSG_DATA);
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
    
    private ArrayList<PushMsg> getPushMsgListByJosn(String json) {
		ArrayList<PushMsg> list = new ArrayList<PushMsg>();
		try {
			JSONObject jsonObject = new JSONObject(json);
			if (jsonObject.isNull("data")) {
				return null;
			}
			JSONArray boxesArray = jsonObject.getJSONArray("data");
			totalRecord = jsonObject.getInt("size");
			for (int i = 0; i < boxesArray.length(); i++) {
				JSONObject msgObject = boxesArray.getJSONObject(i);
				PushMsg msg = new PushMsg();
				msg.title = msgObject.getString("title");
				msg.content = msgObject.getString("content");
				msg.createTime = msgObject.getString("createTime");
				msg.proId = msgObject.getString("proId");
				list.add(msg);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG, "JSONException -- " + e.toString());
		}
		return list;
	}
    
    private String getRequestUrl(boolean isStart) {
		if ((mListAdapter.getItemCount() == 0) || isStart) {
			mCurPageIndex = 1;
		} else {
			++mCurPageIndex;
		}
		//目前使用的是测试服务器数据，等到正式服务器数据部署完之后进行更改
		String url = Constant.DOMAIN
				//String url = "http://203.130.41.108:8011/guizi-app-jiqimao/haier"
				+ "/userMessage/getUserMessage.json?"
				+ "start="+(mCurPageIndex-1)*REQUEST_COUNT
				+ "&pageSize="+REQUEST_COUNT
				+ "&token=" + PushApplication.getInstance().getToken();
//		Log.d("wjb","url:"+url);
		return url;
	}

	@Override
	public void onRefresh() {
		if (!mIsStart) {//防止多次下拉
			mIsStart = true;
			mHandler.sendEmptyMessage(GET_PUSH_MSG_LIST_DATA);
		}
	}

}
