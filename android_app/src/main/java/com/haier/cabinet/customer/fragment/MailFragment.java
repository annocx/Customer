package com.haier.cabinet.customer.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.ExpressListAdapter;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.HeaderLayout;
import com.haier.cabinet.customer.view.PinnedSectionListView;
import com.haier.cabinet.customer.entity.Express;
import com.haier.cabinet.customer.entity.PackageBox;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.view.MailSlideShowView;
import com.haier.cabinet.customer.widget.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.haier.cabinet.customer.widget.recyclerview.RecyclerViewUtils;
import com.haier.common.util.AppToast;
import com.haier.common.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MailFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
	private String TAG = "MailFragment";
	private PinnedSectionListView mListView;
	private View mLoadView;

	static ExpressListAdapter mListAdapter;

	private ArrayList<Express> mListItems = new ArrayList<Express>();
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm",Locale.CHINA);
	private LayoutInflater mInflater;
	private View headerView;
	private MailSlideShowView slideShowView;
	private boolean mIsStart = false;
	private RecyclerView recyclerView;
	protected SwipeRefreshLayout swipeRefreshLayout;
	private HeaderAndFooterRecyclerViewAdapter mHeaderAndFooterRecyclerViewAdapter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mail, container, false);
		initView(view);
		return view;
	}

	private void initView(View view) {
		mListAdapter = new ExpressListAdapter(getActivity(), mListItems);
		recyclerView = (RecyclerView) view.findViewById(R.id.mail_refreshable_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mListAdapter);
		recyclerView.setAdapter(mHeaderAndFooterRecyclerViewAdapter);

		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
		swipeRefreshLayout.setProgressViewOffset(false, 0, Util.dip2px(getActivity(), 24));
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.setRefreshing(true);

		HeaderLayout headerView = new HeaderLayout(getActivity(), R.layout.layout_mail_header_view);
		RecyclerViewUtils.setHeaderView(recyclerView, headerView);

		slideShowView = (MailSlideShowView) headerView.findViewById(R.id.slideshowView);
		slideShowView.refreshView();
	}

	private boolean isRequestInProcess = false;
	@Override
	public void onResume() {
		Log.d(TAG, "onResume isRequestInProcess = " + isRequestInProcess);
		super.onResume();
//		if (PushApplication.getInstance().isLogin()) {
			if (!isRequestInProcess) {
				// 自动刷新  
				mHandler.sendEmptyMessage(GET_LONGITUDE_LATITUDE);
				mIsStart = true;
			}
			
//		}
	}

	private static final int GET_LONGITUDE_LATITUDE = 1000;
	private static final int GET_EXPRESS_LIST_DATA = 1001;
	private static final int UPDATE_EXPRESS_LIST = 1002;
	private static final int NO_EXPRESS_LIST_DATA = 1003;
	private static final int USER_TOKEN_TIMEOUT = 1004;
	private static final int GET_EXPRESS_LIST_DATA_FAILURE = 1005;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.d(TAG, "msg.what = " + msg.what);
			switch (msg.what) {
			case GET_LONGITUDE_LATITUDE:
				getBaiduLoaction();
				isRequestInProcess = true;
				break;
			case GET_EXPRESS_LIST_DATA:
				String location = (String) msg.obj;
				String url = getRequestUrl(location);
				requestExpressData(url);
				break;
			case UPDATE_EXPRESS_LIST:
				String json = (String) msg.obj;
				ArrayList<Express> data = getPostmanListByJosn(json);

				if (null == data) {
					return;
				}
				if (mListAdapter.getItemCount() > 0) {
					mListAdapter.clear();
				}
				mListAdapter.addAll(data);

				recyclerView.setVisibility(View.VISIBLE);
				mListAdapter.notifyDataSetChanged();
				mIsStart = false;
				break;
			case NO_EXPRESS_LIST_DATA:
			case GET_EXPRESS_LIST_DATA_FAILURE:
				recyclerView.setVisibility(View.VISIBLE);

				if (mListAdapter.getItemCount() > 0) {
					mListAdapter.clear();
				}
				mListAdapter.addAll(getExpressListByJosn());
				mListAdapter.notifyDataSetChanged();

				mIsStart = false;
				break;
			case USER_TOKEN_TIMEOUT:
				//token失效跳转到登陆界面
				AppToast.makeToast(getActivity(), "登录超时，请您重新登陆!");
				PushApplication.getInstance().logoutHaiUser();
				getActivity().finish();
				break;
			default:
				break;
			}
		}
	};

	private void requestExpressData(String url) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.setConnectTimeout(10);
		client.get(url, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				swipeRefreshLayout.setRefreshing(false);
				mHandler.sendEmptyMessage(GET_EXPRESS_LIST_DATA_FAILURE);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] response) {
				swipeRefreshLayout.setRefreshing(false);
				String json = new String(response);
				Log.d(json, "onSuccess json " + json);
				if (200 == statusCode) {
					switch (JsonUtil.getStateFromServer(json)) {
					case 200:
					case 201:
						mHandler.obtainMessage(UPDATE_EXPRESS_LIST, json).sendToTarget();
						break;
					case 502:
						mHandler.obtainMessage(NO_EXPRESS_LIST_DATA).sendToTarget();
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

	private ArrayList<Express> getPostmanListByJosn(String json) {
		Log.e(TAG, "getPostmanListByJosn json " + json);
		ArrayList<Express> list = new ArrayList<Express>();
		if (!TextUtils.isEmpty(json)) {
			try {
				
				JSONObject jsonObject = new JSONObject(json);
				if (!jsonObject.isNull("data")) {
					JSONArray expressArray = jsonObject.getJSONArray("data");
					Express express ;
					for (int i = 0; i < expressArray.length(); i++) {
						JSONObject expressObject = expressArray.getJSONObject(i);
						express = new Express();
						express.property = Express.POSTMAN;
						express.name = expressObject.getString("comName");
						express.username = expressObject.getString("nickName");
						express.phone =  expressObject.getString("userNo");
						express.icon_resId = R.drawable.ic_postman_avtar;
						list.add(express);
					}
					if (!list.isEmpty()) {
						express = new Express();
						express.content = getResources().getString(R.string.nearby_postman);
						express.type = PackageBox.SECTION;
						list.add(0, express);
					}
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG, "JSONException -- " + e.toString());
			}
		}
		
		list.addAll(getExpressListByJosn());
		return list;
	}

	private ArrayList<Express> getExpressListByJosn() {
		ArrayList<Express> list = new ArrayList<Express>();
		
		Resources resources = getActivity().getResources();
		String[] nameArray = resources.getStringArray(R.array.common_express_name);
		String[] phoneArray = resources.getStringArray(R.array.common_express_phone);
		TypedArray ar = resources.obtainTypedArray(R.array.common_express_logo);
		int len = ar.length();       
		int[] resIds = new int[len];       
		for (int i = 0; i < len; i++){
			resIds[i] = ar.getResourceId(i, 0);  
		}
		ar.recycle();    
		
		Express express = new Express();
		express.type = Express.SECTION;
		express.content = getResources().getString(R.string.common_express);
		list.add(express);
		for (int i = 0; i < resIds.length; i++) {
			express = new Express();
			express.property = Express.COMPANY;
			express.name = nameArray[i];
			express.phone = phoneArray[i];
			express.icon_resId = resIds[i];
			list.add(express);
		}
		
		return list;
	}

	@Override
	public void onStop() {
		if (null != mLocationClient) {
			mLocationClient.stop();
		}
		super.onStop();
	}
	
	private LocationClient mLocationClient;
	private void getBaiduLoaction() {
		mLocationClient = new LocationClient(getActivity());

		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Battery_Saving);// 设置定位模式
		option.setCoorType("gcj02");// 返回的定位结果是百度经纬度,默认值gcj02
		// 定位sdk提供2种定位模式，定时定位和app主动请求定位。 setScanSpan < 1000 则为 app主动请求定位；
		// setScanSpan>=1000,则为定时定位模式（setScanSpan的值就是定时定位的时间间隔））
		// 定时定位模式中，定位sdk会按照app设定的时间定位进行位置更新，定时回调定位结果。此种定位模式适用于希望获得连续定位结果的情况。
		// 对于单次定位类应用，或者偶尔需要一下位置信息的app，可采用app主动请求定位这种模式。
		option.setScanSpan(800);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		// 注册监听函数
		mLocationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceiveLocation(BDLocation location) {
				onLocationChanged(location);
			}
		});

		mLocationClient.start();
	}

	/**
	 * 当位置发生变化时触发此方法
	 * 
	 * @param location
	 *            当前位置
	 */
	public void onLocationChanged(BDLocation location) {
		if (location != null) {
			// 显示定位结果
			Log.d(TAG, location.getLongitude() + "  " + location.getLatitude());
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			/*if (mLatitude != latitude || mLongitude != longitude) {
				mLatitude = latitude;
				mLongitude = longitude;
				String newLocation = longitude + "," + latitude;
				newLocation = Utils.bd_encrypt(latitude, longitude);
				Log.i(TAG, "The baidulocation has changed.. " + newLocation);
		  		mHandler.obtainMessage(GET_EXPRESS_LIST_DATA, newLocation).sendToTarget();
			}*/
			String newLocation = Utils.bd_encrypt(latitude, longitude);
	  		mHandler.obtainMessage(GET_EXPRESS_LIST_DATA, newLocation).sendToTarget();
		}
	}
	
	private String getRequestUrl(String text) {
		if (!TextUtils.isEmpty(text) && text.contains(",")) {
			String[] array = text.split(",");
			String longitude = array[0];
			String latitude = array[1];
			
			String url = Constant.URL_NEARBY_POSTMAN
					+ "?longitude="+longitude
					+ "&latitude="+latitude
					+ "&distance=3"
					+ "&gladEyeKey=" + Constant.GLADEYEKEY;
//			Log.d(TAG, "url -- " + url);
			return url;
		}
		
		return null;
	}

	@Override
	public void onRefresh() {
		if (!mIsStart) {//防止多次下拉
			mIsStart = true;
			mHandler.sendEmptyMessage(GET_LONGITUDE_LATITUDE);
		}
	}
}
