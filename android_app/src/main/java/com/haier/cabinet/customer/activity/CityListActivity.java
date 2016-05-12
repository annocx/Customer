package com.haier.cabinet.customer.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.db.DBHelper;
import com.haier.cabinet.customer.db.DatabaseHelper;
import com.haier.cabinet.customer.event.CityChangedEvent;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.PingYinUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.MyLetterListView;


import com.haier.cabinet.customer.R;
import com.haier.common.util.AppToast;
import com.sunday.statagent.StatAgent;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import kankan.wheel.widget.adapters.City;


public class CityListActivity extends BaseActivity implements OnScrollListener {
    private static final String TAG = "CityListActivity";
    private static final int UPDATE_LIST = 1001;//刷新列表
    @Bind(R.id.list_view)
    ListView cityListView;
    @Bind(R.id.search_result)
    ListView resultList;
    @Bind(R.id.letterListView)
    MyLetterListView letterListView; // A-Z listview
    @Bind(R.id.sh)
    EditText sh;
    @Bind(R.id.tv_noresult)
    TextView tv_noresult;
    private BaseAdapter adapter;
    private ResultListAdapter resultListAdapter;
    private TextView overlay; // 对话框首字母textview
    private HashMap<String, Integer> alphaIndexer;// 存放存在的汉语拼音首字母和与之对应的列表位置
    private String[] sections;// 存放存在的汉语拼音首字母
    private Handler handler;
    private OverlayThread overlayThread; // 显示首字母对话框
    private ArrayList<City> allCity_lists; // 所有城市列表
    private ArrayList<City> city_lists;// 城市列表
    private ArrayList<City> city_hot;
    private ArrayList<City> city_result;
    private ArrayList<String> city_history;
    private LocationClient mLocationClient;
    private String currentCity; // 用于保存定位到的城市
    private int locateProcess = 1; // 记录当前定位的状态 正在定位-定位成功-定位失败
    private boolean isNeedFresh;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private DatabaseHelper helper;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what = " + msg.what);
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_LIST:
                    cityListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_city_list;
    }

    @Override
    public void initView() {
        StatAgent.initAction(this, "", "1", "29", "", "", "", "1", "");
        PushApplication.addActivity(this);
        mTitleText.setText("选择城市");
        mBackBtn.setVisibility(View.VISIBLE);
        allCity_lists = new ArrayList<City>();
        city_hot = new ArrayList<City>();
        city_result = new ArrayList<City>();
        city_history = new ArrayList<String>();
        helper = new DatabaseHelper(this);
        sh.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (Util.CheckString(s.toString())) {
                    letterListView.setVisibility(View.VISIBLE);
                    cityListView.setVisibility(View.VISIBLE);
                    resultList.setVisibility(View.GONE);
                    tv_noresult.setVisibility(View.GONE);
                } else {
                    city_result.clear();
                    letterListView.setVisibility(View.GONE);
                    cityListView.setVisibility(View.GONE);
                    getResultCityList(s.toString());
                    if (city_result.size() <= 0) {
                        tv_noresult.setVisibility(View.VISIBLE);
                        resultList.setVisibility(View.GONE);
                    } else {
                        tv_noresult.setVisibility(View.GONE);
                        resultList.setVisibility(View.VISIBLE);
                        resultListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                StatAgent.initAction(CityListActivity.this, "", "2", "29", "", "", s.toString(), "1", "");
            }
        });
        letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
        alphaIndexer = new HashMap<String, Integer>();
        handler = new Handler();
        overlayThread = new OverlayThread();
        isNeedFresh = true;
        cityListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position >= 4) {
                    String cityName = allCity_lists.get(position).getName();
                    InsertCity(cityName);
                    notifyCityChanged(cityName);
                }
            }
        });
        locateProcess = 1;
        cityListView.setAdapter(adapter);
        cityListView.setOnScrollListener(this);
        resultListAdapter = new ResultListAdapter(this, city_result);
        resultList.setAdapter(resultListAdapter);
        resultList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String cityName = city_result.get(position).getName();
                InsertCity(cityName);
                notifyCityChanged(cityName);

            }
        });
        initOverlay();
        cityInit();
        hotCityInit();
        hisCityInit();
        setAdapter(allCity_lists, city_hot, city_history);
        getLocation();//获取位置权限

        mBackBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!isCityNull()) {
                    finish();
                }
            }
        });
    }

    public void getLocation() {
        if (Build.VERSION.SDK_INT >= 23) {//6.0版本
            if (ActivityCompat.checkSelfPermission(CityListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CityListActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
        }else {
            getBaiduLoaction();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    getBaiduLoaction();
                } else {
                    // Permission Denied
                    AppToast.showShortText(CityListActivity.this, "需要获取位置权限以便能更好的为您服务");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void initData() {

    }

    public void InsertCity(String name) {
        PushApplication.getInstance().setProperty("user.city", name);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from recentcity where name = '"
                + name + "'", null);
        if (cursor.getCount() > 0) { //
            db.delete("recentcity", "name = ?", new String[]{name});
        }
        db.execSQL("insert into recentcity(name, date) values('" + name + "', "
                + System.currentTimeMillis() + ")");
        db.close();
        Log.d(TAG, " InsertCity city = " + PushApplication.getInstance().getProperty("user.city"));
    }

    private void getBaiduLoaction() {
        mLocationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);// 设置定位模式
        option.setCoorType("gcj02");// 返回的定位结果是百度经纬度,默认值gcj02
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
     * @param location 当前位置
     */
    public void onLocationChanged(BDLocation location) {
        if (location != null) {
            currentCity = location.getCity();
            Log.d(TAG, "onLocationChanged ==" + currentCity);
            if (!isNeedFresh) {
                return;
            }
            isNeedFresh = false;
            if (currentCity == null) {
                locateProcess = 3; // 定位失败
                mHandler.sendEmptyMessage(UPDATE_LIST);
                return;
            }
            currentCity = currentCity.substring(0,
                    currentCity.length() - 1);
            locateProcess = 2; // 定位成功
            mHandler.sendEmptyMessage(UPDATE_LIST);

            if (TextUtils.isEmpty(PushApplication.getInstance().getProperty("user.city"))) {
                InsertCity(currentCity);
                EventBus.getDefault().post(new CityChangedEvent());
            }

        }
    }

    private void cityInit() {
        City city = new City("定位", "0"); // 当前定位城市
        allCity_lists.add(city);
        city = new City("最近", "1"); // 最近访问的城市
        allCity_lists.add(city);
        city = new City("热门", "2"); // 热门城市
        allCity_lists.add(city);
        city = new City("全部", "3"); // 全部城市
        allCity_lists.add(city);
        city_lists = getCityList();
        allCity_lists.addAll(city_lists);
    }

    /**
     * 热门城市
     */
    public void hotCityInit() {
        City city = new City("上海", "2");
        city_hot.add(city);
        city = new City("北京", "2");
        city_hot.add(city);
        city = new City("广州", "2");
        city_hot.add(city);
        city = new City("深圳", "2");
        city_hot.add(city);
        city = new City("武汉", "2");
        city_hot.add(city);
        city = new City("天津", "2");
        city_hot.add(city);
        city = new City("西安", "2");
        city_hot.add(city);
        city = new City("南京", "2");
        city_hot.add(city);
        city = new City("杭州", "2");
        city_hot.add(city);
        city = new City("成都", "2");
        city_hot.add(city);
        city = new City("重庆", "2");
        city_hot.add(city);
    }

    private void hisCityInit() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getReadableDatabase();
            cursor = db.rawQuery(
                    "select * from recentcity order by date desc limit 0, 3", null);
            while (cursor.moveToNext()) {
                city_history.add(cursor.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<City> getCityList() {
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<City> list = new ArrayList<>();
        try {
            dbHelper.createDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("select * from city", null);
            City city;
            while (cursor.moveToNext()) {
                city = new City(cursor.getString(1), cursor.getString(2));
                list.add(city);
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(list, comparator);
        return list;
    }

    @SuppressWarnings("unchecked")
    private void getResultCityList(String keyword) {
        DBHelper dbHelper = new DBHelper(this);
        try {
            dbHelper.createDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery(
                    "select * from city where name like \"%" + keyword
                            + "%\" or pinyin like \"%" + keyword + "%\"", null);
            City city;
            Log.e("info", "length = " + cursor.getCount());
            while (cursor.moveToNext()) {
                city = new City(cursor.getString(1), cursor.getString(2));
                city_result.add(city);
            }
            cursor.close();
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(city_result, comparator);
    }

    /**
     * a-z排序
     */
    @SuppressWarnings("rawtypes")
    Comparator comparator = new Comparator<City>() {
        @Override
        public int compare(City lhs, City rhs) {
            String a = lhs.getPinyin().substring(0, 1);
            String b = rhs.getPinyin().substring(0, 1);
            int flag = a.compareTo(b);
            if (flag == 0) {
                return a.compareTo(b);
            } else {
                return flag;
            }
        }
    };

    private void setAdapter(List<City> list, List<City> hotList,
                            List<String> hisCity) {
        adapter = new ListAdapter(this, list, hotList, hisCity);
        cityListView.setAdapter(adapter);
    }

    private class ResultListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList<City> results = new ArrayList<City>();

        public ResultListAdapter(Context context, ArrayList<City> results) {
            inflater = LayoutInflater.from(context);
            this.results = results;
        }

        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView
                        .findViewById(R.id.name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.name.setText(results.get(position).getName());
            return convertView;
        }

        class ViewHolder {
            TextView name;
        }
    }

    public class ListAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<City> list;
        private List<City> hotList;
        private List<String> hisCity;
        final int VIEW_TYPE = 5;

        public ListAdapter(Context context, List<City> list,
                           List<City> hotList, List<String> hisCity) {
            this.inflater = LayoutInflater.from(context);
            this.list = list;
            this.context = context;
            this.hotList = hotList;
            this.hisCity = hisCity;
            alphaIndexer = new HashMap<String, Integer>();
            sections = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                // 当前汉语拼音首字母
                String currentStr = getAlpha(list.get(i).getPinyin());
                // 上一个汉语拼音首字母，如果不存在为" "
                String previewStr = (i - 1) >= 0 ? getAlpha(list.get(i - 1)
                        .getPinyin()) : " ";
                if (!previewStr.equals(currentStr)) {
                    String name = getAlpha(list.get(i).getPinyin());
                    alphaIndexer.put(name, i);
                    sections[i] = name;
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE;
        }

        @Override
        public int getItemViewType(int position) {
            return position < 4 ? position : 4;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        ViewHolder holder;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView city;
            int viewType = getItemViewType(position);
            if (viewType == 0) { // 定位
                convertView = inflater.inflate(R.layout.frist_list_item, null);
                TextView locateHint = (TextView) convertView.findViewById(R.id.locateHint);
                city = (TextView) convertView.findViewById(R.id.lng_city);
                city.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (locateProcess == 2) {
                            Toast.makeText(getApplicationContext(), city.getText().toString(), Toast.LENGTH_SHORT).show();
                        } else if (locateProcess == 3) {
                            locateProcess = 1;
                            mHandler.sendEmptyMessage(UPDATE_LIST);
                            mLocationClient.stop();
                            isNeedFresh = true;
                            //getBaiduLoaction();
                            currentCity = "";
                            mLocationClient.start();
                        }
                    }
                });
                ProgressBar pbLocate = (ProgressBar) convertView.findViewById(R.id.pbLocate);
                if (locateProcess == 1) { // 正在定位
                    locateHint.setText("正在定位");
                    city.setVisibility(View.GONE);
                    pbLocate.setVisibility(View.VISIBLE);
                } else if (locateProcess == 2) { // 定位成功
                    locateHint.setText("当前定位城市");
                    city.setVisibility(View.VISIBLE);
                    city.setText(currentCity);
                    mLocationClient.stop();
                    pbLocate.setVisibility(View.GONE);

                    city.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            InsertCity(currentCity);
                            notifyCityChanged(currentCity);
                        }
                    });
                } else if (locateProcess == 3) {
                    locateHint.setText("未定位到城市,请选择");
                    city.setVisibility(View.VISIBLE);
                    city.setText("重新选择");
                    pbLocate.setVisibility(View.GONE);
                }
            } else if (viewType == 1) { // 最近访问城市
                convertView = inflater.inflate(R.layout.recent_city, null);
                GridView rencentCity = (GridView) convertView.findViewById(R.id.recent_city);
                rencentCity.setAdapter(new HitCityAdapter(context, this.hisCity));
                rencentCity.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {


                        String cityName = hisCity.get(position);
                        InsertCity(cityName);
                        notifyCityChanged(cityName);

                    }
                });
                TextView recentHint = (TextView) convertView.findViewById(R.id.recentHint);
                recentHint.setText("最近访问的城市");
            } else if (viewType == 2) {
                convertView = inflater.inflate(R.layout.recent_city, null);
                GridView hotCity = (GridView) convertView.findViewById(R.id.recent_city);
                hotCity.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {


                        String cityName = city_hot.get(position).getName();
                        InsertCity(cityName);
                        notifyCityChanged(cityName);

                    }
                });
                hotCity.setAdapter(new HotCityAdapter(context, this.hotList));
                TextView hotHint = (TextView) convertView.findViewById(R.id.recentHint);
                hotHint.setText("热门城市");
            } else if (viewType == 3) {
                convertView = inflater.inflate(R.layout.total_item, null);
            } else {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.list_item, null);
                    holder = new ViewHolder();
                    holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
                    holder.alphaLayout = convertView.findViewById(R.id.alpha_layout);
                    holder.name = (TextView) convertView.findViewById(R.id.name);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                if (position >= 1) {
                    holder.name.setText(list.get(position).getName());
                    String currentStr = getAlpha(list.get(position).getPinyin());
                    String previewStr = (position - 1) >= 0 ? getAlpha(list.get(position - 1).getPinyin()) : " ";
                    if (!previewStr.equals(currentStr)) {
                        holder.alphaLayout.setVisibility(View.VISIBLE);
                        holder.alpha.setVisibility(View.VISIBLE);
                        holder.alpha.setText(currentStr);
                    } else {
                        holder.alphaLayout.setVisibility(View.GONE);
                    }
                }
            }
            return convertView;
        }

        private class ViewHolder {
            TextView alpha; // 首字母标题
            TextView name; // 城市名字
            View alphaLayout;
        }
    }


    @Override
    protected void onStop() {
        if (null != mLocationClient) {
            mLocationClient.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        windowManager.removeView(overlay);
        StatAgent.initAction(this, "", "2", "29", "", "", "back", "2", "");

    }

    class HotCityAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<City> hotCitys;

        public HotCityAdapter(Context context, List<City> hotCitys) {
            this.context = context;
            inflater = LayoutInflater.from(this.context);
            this.hotCitys = hotCitys;
        }

        @Override
        public int getCount() {
            return hotCitys.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_city, null);
            TextView city = (TextView) convertView.findViewById(R.id.city);
            city.setText(hotCitys.get(position).getName());
            return convertView;
        }
    }

    class HitCityAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<String> hotCitys;

        public HitCityAdapter(Context context, List<String> hotCitys) {
            this.context = context;
            inflater = LayoutInflater.from(this.context);
            this.hotCitys = hotCitys;
        }

        @Override
        public int getCount() {
            return hotCitys.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_city, null);
            TextView city = (TextView) convertView.findViewById(R.id.city);
            city.setText(hotCitys.get(position));
            return convertView;
        }
    }

    private boolean mReady;
    WindowManager windowManager;

    // 初始化汉语拼音首字母弹出提示框
    private void initOverlay() {
        mReady = true;
        LayoutInflater inflater = LayoutInflater.from(this);
        overlay = (TextView) inflater.inflate(R.layout.overlay, null);
        overlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        windowManager.addView(overlay, lp);
        //windowManager.removeView(overlay);
    }


    private boolean isScroll = false;

    private class LetterListViewListener implements MyLetterListView.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            isScroll = false;
            if (alphaIndexer.get(s) != null) {
                int position = alphaIndexer.get(s);
                cityListView.setSelection(position);
                overlay.setText(s);
                overlay.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);
                // 延迟一秒后执行，让overlay为不可见
                handler.postDelayed(overlayThread, 1000);
            }
        }
    }

    // 设置overlay不可见
    private class OverlayThread implements Runnable {
        @Override
        public void run() {
            overlay.setVisibility(View.GONE);
        }
    }

    // 获得汉语拼音首字母
    private String getAlpha(String str) {
        if (str == null) {
            return "#";
        }
        if (str.trim().length() == 0) {
            return "#";
        }
        char c = str.trim().substring(0, 1).charAt(0);
        // 正则表达式，判断首字母是否是英文字母
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        if (pattern.matcher(c + "").matches()) {
            return (c + "").toUpperCase();
        } else if (str.equals("0")) {
            return "定位";
        } else if (str.equals("1")) {
            return "最近";
        } else if (str.equals("2")) {
            return "热门";
        } else if (str.equals("3")) {
            return "全部";
        } else {
            return "#";
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL
                || scrollState == SCROLL_STATE_FLING) {
            isScroll = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (!isScroll) {
            return;
        }

        if (mReady) {
            String text;
            String name = allCity_lists.get(firstVisibleItem).getName();
            String pinyin = allCity_lists.get(firstVisibleItem).getPinyin();
            if (firstVisibleItem < 4) {
                text = name;
            } else {
                text = PingYinUtil.converterToFirstSpell(pinyin)
                        .substring(0, 1).toUpperCase();
            }
            overlay.setText(text);
            overlay.setVisibility(View.VISIBLE);
            handler.removeCallbacks(overlayThread);
            // 延迟一秒后执行，让overlay为不可见
            handler.postDelayed(overlayThread, 1000);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isCityNull()) {
                return true;
            } else {
                finish();
            }
        }
        return false;
    }

    private boolean isCityNull() {
        boolean flag = false;
        String localCity = PushApplication.getInstance().getProperty("user.city");
        if (TextUtils.isEmpty(localCity)) {
            flag = true;
            AppToast.showShortText(CityListActivity.this, "为了更好的为您服务，请您选择当前所在的城市！");
        }
        return flag;
    }

    /**
     * 城市切换
     *
     * @param city
     */
    private void notifyCityChanged(String city) {
        String localCity = PushApplication.getInstance().getProperty("user.city");
        Log.d(TAG, " notifyCityChanged city = " + localCity);
        if (localCity != null) {
            EventBus.getDefault().post(new CityChangedEvent());
            CityListActivity.this.finish();
        }
    }

}