package com.haier.cabinet.customer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.HistorySearchAdapter;
import com.haier.cabinet.customer.activity.adapter.HotSearchAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.util.AsSearchHistory;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.JsonParser;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.MyGridView;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sunday.statagent.StatAgent;

import org.apache.http.Header;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemClick;


/**
 * 搜索类
 */
public class SearchActivity extends BaseActivity implements TextWatcher,
        AdapterView.OnItemClickListener, View.OnKeyListener {

    public static final String PREFER_NAME = "com.iflytek.setting";
    //搜索，删除历史记录
    @Bind(R.id.btn_search)
    Button btn_search;
    @Bind(R.id.et_search)
    EditText et_search;//输入框
    //删除按钮,语音按钮
    @Bind(R.id.iv_del)
    ImageView iv_del;
    @Bind(R.id.iv_voice)
    ImageView iv_voice;
    @Bind(R.id.history_list) ListView historyListView;

    @Bind(R.id.tv_voice) TextView tv_voice;

    @Bind(R.id.linear_history) LinearLayout linear_history;//历史记录布局

    AsSearchHistory as;//历史记录

    HistorySearchAdapter historySearchAdapter;

    public View view_del;
    public Button btn_del;
    // 语音听写对象
    public SpeechRecognizer mSpeech;

    private RecognizerDialog recognizerDialog;// 语音

    private SharedPreferences mSharedPreferences;

    @Bind(R.id.gv_hot_lable) MyGridView gv_hot_lable;

    private HotSearchAdapter mAdapter;

    private ArrayList<String> data = new ArrayList<String>();//热门搜索假数据

    InputMethodManager imm;


    private static final int GET_LIST_DATA = 1001;//请求数据
    private static final int UPDATE_LIST_DATA = 1002;//刷新数据

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_LIST_DATA:
                    getHotSearch();
                    break;
                case UPDATE_LIST_DATA:
                    String json = (String) msg.obj;
                    data = getHotData(json);
                    mAdapter = new HotSearchAdapter(SearchActivity.this, data);
                    gv_hot_lable.setAdapter(mAdapter);
                    break;
            }
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    public void initData() {
        StatAgent.initAction(this, "", "1", "3", "", "", "", "1", "");
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        PushApplication.addActivity(this);
        as = new AsSearchHistory(this);
        SpeechUtility.createUtility(this, "appid=53e3250d");// 讯飞初始化
        updateHistory();
        mHandler.sendEmptyMessage(GET_LIST_DATA);
    }

    @Override
    @OnClick({R.id.btn_search,R.id.iv_del,R.id.iv_voice,R.id.back_img})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                if (et_search.getText().toString().equals("")) {
                    AppToast.showShortText(SearchActivity.this, "请输入搜索内容");
                    return;
                }
                if (Util.CheckString(et_search.getText().toString())) {
                    AppToast.showShortText(SearchActivity.this, "搜索内容中含有非法字符");
                    return;
                }
                doSearch();

                StatAgent.initAction(SearchActivity.this, "", "2", "3", "", "", btn_search.getText().toString(), "1", "");
                break;
            case R.id.iv_del:
                et_search.setText("");
                break;
            case R.id.iv_voice:
                // // 设置参数
                setParam();
                boolean isShowDialog = mSharedPreferences.getBoolean("iat_show",
                        true);
                if (isShowDialog) {
                    // 显示听写对话框
                    recognizerDialog.setListener(recognizerDialogListener);
                    recognizerDialog.show();
                }
                break;
            case R.id.btn_del:
                as.removeProperty();
                linear_history.setVisibility(View.GONE);
                break;
            case R.id.back_img:
                finish();
                break;
            default:
                break;
        }
    }

    public void initView() {
        view_del = View.inflate(SearchActivity.this,
                R.layout.vw_search_del, null);
        btn_del = (Button) view_del.findViewById(R.id.btn_del);
        historyListView.addFooterView(view_del);
        InitListener();
        // // 初始化识别对象
        mSpeech = SpeechRecognizer.createRecognizer(this, mInitListener);
        // // 初始化听写Dialog,如果只使用有UI听写功能,无需创建SpeechRecognizer
        recognizerDialog = new RecognizerDialog(this, mInitListener);
        mSharedPreferences = getSharedPreferences(PREFER_NAME,
                Activity.MODE_PRIVATE);
    }

    private void InitListener() {
        btn_del.setOnClickListener(this);
        et_search.addTextChangedListener(this);
        et_search.setOnKeyListener(this);
    }

    private void updateHistory() {
        if (as.getSearchHistory().size() != 0) {
            linear_history.setVisibility(View.VISIBLE);
            historySearchAdapter = new HistorySearchAdapter(this, as.getSearchHistory());
            historyListView.setAdapter(historySearchAdapter);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        iv_del.setVisibility(s.toString().length() == 0 ? View.GONE : View.VISIBLE);
        iv_voice.setVisibility(s.toString().length() == 0 ? View.VISIBLE : View.GONE);
        et_search.setSelection(s.toString().length());
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!TextUtils.isEmpty(s.toString()))
            StatAgent.initAction(SearchActivity.this, "", "2", "3", "", "", s.toString(), "1", "");
    }

    /**
     * 初始化监听器。
     */
    private final InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code == ErrorCode.SUCCESS) {
            }
        }
    };

    /**
     * 听写UI监听器
     */
    private final RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
            if (et_search != null) {
                et_search.append(text);
            }

        }

        /**
         * 识别回调错误.
         */
        @Override
        public void onError(SpeechError error) {

        }

    };

    @Override
    @OnItemClick({R.id.history_list,R.id.gv_hot_lable})
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.history_list:
                et_search.setText(as.getSearchHistory().get(position).toString());
                break;
            case R.id.gv_hot_lable:
                StatAgent.initAction(this, "", "2", "3", "", "", data.get(position).toString(), "1", "");

                et_search.setText(data.get(position).toString());
                as.setSearchHistory(et_search.getText().toString());
                updateHistory();
                break;
        }
        doSearch();
    }

    /**
     * 获取热门搜索数据
     */
    private void getHotSearch() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constant.URL_HOT_SEARCH, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(SearchActivity.this, getString(R.string.loading), true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] errorResponse, Throwable e) {
                DialogHelper.stopProgressDlg();
                mHandler.obtainMessage(UPDATE_LIST_DATA, "").sendToTarget();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        String data_hot = JsonUtil.getResultFromJson(json);
                        mHandler.obtainMessage(UPDATE_LIST_DATA, data_hot).sendToTarget();
                    } else {
                        mHandler.obtainMessage(UPDATE_LIST_DATA, "").sendToTarget();
//                        AppToast.showShortText(SearchActivity.this, "获取热门数据失败了!");
                    }

                }
            }
        });

    }

    /**
     * 搜索方法
     */
    private void doSearch() {
        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
        as.setSearchHistory(et_search.getText().toString());
        updateHistory();
        Bundle bundle = new Bundle();
        bundle.putString(SortResultActivity.RESULT_NAME, et_search.getText().toString());
        bundle.putInt(SortResultActivity.RESULT_TYPE, SortResultActivity.RESULT_SEARCH);
        IntentUtil.startActivity(this, SortResultActivity.class, bundle);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_ENTER
                && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
            // 先隐藏键盘
            String text = et_search.getText().toString().trim();
            if (text.equals("")) {
                AppToast.showShortText(SearchActivity.this, "请输入搜索内容");
                return false;
            }

            if (Util.CheckString(text)) {
                AppToast.showShortText(SearchActivity.this, "搜索内容中含有非法字符");
                return false;
            }
            imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
            doSearch();
        }
        return false;
    }

    /**
     * 热门搜索数据解析
     *
     * @return
     */
    public ArrayList<String> getHotData(String str_data) {
        ArrayList<String> mList = new ArrayList<String>();
        int end = 0;
        int start = 0;
        if (!TextUtils.isEmpty(str_data)) {//如果数据不为空
            String replace_data = str_data.replace("[", "").replace("]", ",").replace("\"", "");
            while ((end = replace_data.indexOf(",", start)) > -1) {
                mList.add(replace_data.substring(start, end));
                start = end + 1;
            }
        } else {
            mList.add("青岛特产");
            mList.add("乐家特产");
            mList.add("太原特产");
            mList.add("大礼包");
        }
        return mList;
    }

    public void setParam() {
        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mSpeech.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mSpeech.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mSpeech.setParameter(SpeechConstant.ACCENT, lag);
        }
        // 设置语音前端点
        mSpeech.setParameter(SpeechConstant.VAD_BOS,
                mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        // 设置语音后端点
        mSpeech.setParameter(SpeechConstant.VAD_EOS,
                mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        // 设置标点符号
        mSpeech.setParameter(SpeechConstant.ASR_PTT,
                mSharedPreferences.getString("iat_punc_preference", "0"));
        // 设置音频保存路径
        mSpeech.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                "/sdcard/iflytek/wavaudio.pcm");
    }
}
