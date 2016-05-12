package com.haier.cabinet.customer.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AreaArrayWheelAdapter;
import kankan.wheel.widget.adapters.Area;
import kankan.wheel.widget.adapters.City;
import kankan.wheel.widget.adapters.CityArrayWheelAdapter;
import kankan.wheel.widget.adapters.Province;
import kankan.wheel.widget.adapters.ProvinceArrayWheelAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * 
 * @author zhy
 * 
 */
public class CitiesSetActivity extends Activity implements OnWheelChangedListener,OnClickListener
{
	private static final String TAG = "CitiesSetActivity";
	/**
	 * 把全国的省市区的信息以json的格式保存，解析完成后赋值为null
	 */
	private JSONObject mJsonObj;
	/**
	 * 省的WheelView控件
	 */
	private WheelView mProvince;
	/**
	 * 市的WheelView控件
	 */
	private WheelView mCity;
	/**
	 * 区的WheelView控件
	 */
	private WheelView mArea;

	/**
	 * 所有省
	 */
	private List<Province> mProvinceDatas;
	/**
	 * key - 省 value - 市s
	 */
	private Map<Integer, List<City>> mCitisDatasMap = new HashMap<Integer, List<City>>();
	/**
	 * key - 市 values - 区s
	 */
	private Map<Integer, List<Area>> mAreaDatasMap = new HashMap<Integer, List<Area>>();

	/**
	 * 当前省的名称
	 */
	private Province province;
	/**
	 * 当前市的名称
	 */
	private City city;
	
	private Button mOkBtn;
	private Button mCancelBtn;
	
	/**
	 * 当前区的名称
	 */
	private Area area;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_citys);
		PushApplication.addActivity(this);

		initJsonData();

		mProvince = (WheelView) findViewById(R.id.id_province);
		mCity = (WheelView) findViewById(R.id.id_city);
		mArea = (WheelView) findViewById(R.id.id_area);
		mOkBtn = (Button) findViewById(R.id.btn_pos);
		mCancelBtn = (Button) findViewById(R.id.btn_neg);

		initDatas();

		mProvince.setViewAdapter(new ProvinceArrayWheelAdapter(this, mProvinceDatas));
		// 添加change事件
		mProvince.addChangingListener(this);
		// 添加change事件
		mCity.addChangingListener(this);
		// 添加change事件
		mArea.addChangingListener(this);
		mOkBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);

		mProvince.setVisibleItems(5);
		mCity.setVisibleItems(5);
		mArea.setVisibleItems(5);
		updateCities();
		updateAreas();

	}

	/**
	 * 根据当前的市，更新区WheelView的信息
	 */
	private void updateAreas()
	{
		Log.d(TAG, "updateAreas");
		int pCurrent = mCity.getCurrentItem();
		city = mCitisDatasMap.get(province.id).get(pCurrent);
		Log.d(TAG, city.id + " city " + city.name);
		List<Area> areas = mAreaDatasMap.get(city.id);
		if (areas == null ) {
			areas = new ArrayList<Area>();
			Log.d(TAG,  "updateAreas  areas is null ");
			Area area = new Area();
			area.id = 10000;
			area.name = "";
			areas.add(area);
		}
		
		mArea.setViewAdapter(new AreaArrayWheelAdapter(this, areas));
		mArea.setCurrentItem(0);
		if(areas.size()!=0){
			area = areas.get(0);
		}else{
			area.name = "";
		}
		Log.d(TAG,  "updateAreas  area.name" + area.name);
	}

	/**
	 * 根据当前的省，更新市WheelView的信息
	 */
	private void updateCities()
	{
		int pCurrent = mProvince.getCurrentItem();
		province = mProvinceDatas.get(pCurrent);
		List<City> cities = mCitisDatasMap.get(province.id);
		
		mCity.setViewAdapter(new CityArrayWheelAdapter(this, cities));
		mCity.setCurrentItem(0);
		updateAreas();
	}

	/**
	 * 解析整个Json对象，完成后释放Json对象的内存
	 */
	int mum = 300;
	int num = 5000;
	private void initDatas()
	{
		try
		{
			JSONArray jsonArray = mJsonObj.getJSONArray("list");
			mProvinceDatas = new ArrayList<Province>();
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject jsonP = jsonArray.getJSONObject(i);// 每个省的json对象
				
				Province province = new Province();
				province.name = jsonP.getString("name");// 省名字
				province.id = jsonP.getInt("id");
				
				mProvinceDatas.add(province);

				
				JSONArray jsonCs = null;
				if (!jsonP.isNull("list")) {
					jsonCs = jsonP.getJSONArray("list");
					List<City> mCitiesDatas = new ArrayList<City>();
					for (int j = 0; j < jsonCs.length(); j++)
					{
						JSONObject jsonCity = jsonCs.getJSONObject(j);
						City city = new City();
						city.name = jsonCity.getString("name");// 市名字
						city.id = jsonCity.getInt("id");
						
						mCitiesDatas.add(city);
						if (!jsonCity.isNull("list")) {
							JSONArray jsonAreas = jsonCity.getJSONArray("list");
							List<Area> mAreasDatas = new ArrayList<Area>();// 当前市的所有区
							for (int k = 0; k < jsonAreas.length(); k++)
							{
								JSONObject jsonArea = jsonAreas.getJSONObject(k);
								Area area = new Area();
								area.name = jsonArea.getString("name");// 区域的名称
								area.id = jsonArea.getInt("id");
								
								mAreasDatas.add(area);
							}
							
							mAreaDatasMap.put(city.id, mAreasDatas);
						}
						
					}
					mCitisDatasMap.put(province.id, mCitiesDatas);
				}
				
				/*JSONArray jsonCs = null;
				try
				{
					*//**
					 * Throws JSONException if the mapping doesn't exist or is
					 * not a JSONArray.
					 *//*
					jsonCs = jsonP.getJSONArray("c");
					
				} catch (Exception e1)
				{
					continue;
				}
				List<City> mCitiesDatas = new ArrayList<City>();
				for (int j = 0; j < jsonCs.length(); j++)
				{
					JSONObject jsonCity = jsonCs.getJSONObject(j);
					
					City city = new City();
					city.name = jsonCity.getString("n");// 市名字
					city.id = 300+j;
					
					mCitiesDatas.add(city);
					
					JSONArray jsonAreas = null;
					try
					{
						*//**
						 * Throws JSONException if the mapping doesn't exist or
						 * is not a JSONArray.
						 *//*
						if (!jsonCity.isNull("a")) {
							jsonAreas = jsonCity.getJSONArray("a");
							List<Area> mAreasDatas = new ArrayList<Area>();// 当前市的所有区
							for (int k = 0; k < jsonAreas.length(); k++)
							{
								Area area = new Area();
								area.name = jsonAreas.getJSONObject(k).getString("s");// 区域的名称
								area.id = 5000 + k;
								
								mAreasDatas.add(area);
							}
							
							mAreaDatasMap.put(city.id, mAreasDatas);
						}
						
					} catch (Exception e)
					{
						Log.e(TAG, "jsonAreas is null " + city.name);
						//continue;
					}

					
				}*/

			}

		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		mJsonObj = null;
	}

	/**
	 * 从assert文件夹中读取省市区的json文件，然后转化为json对象
	 */
	private void initJsonData()
	{
		try
		{
			StringBuffer sb = new StringBuffer();
			InputStream is = getAssets().open("city.txt");
			int len = -1;
			byte[] buf = new byte[1024];
			while ((len = is.read(buf)) != -1)
			{
				sb.append(new String(buf, 0, len, "gbk"));
			}
			is.close();
			mJsonObj = new JSONObject(sb.toString());
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 0:
					break;
				case 1:
					break;
			}

		}
	};

	/**
	 * change事件的处理
	 */
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue)
	{
		if (wheel == mProvince)
		{
			updateCities();
		} else if (wheel == mCity)
		{
			updateAreas();
		} else if (wheel == mArea)
		{
			area = mAreaDatasMap.get(city.id).get(newValue);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_neg:
			CitiesSetActivity.this.finish();
			break;
		case R.id.btn_pos:
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("area", area);
			bundle.putSerializable("city", city);
			bundle.putSerializable("province", province);
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
			break;

		default:
			break;
		}
	}
	
}
