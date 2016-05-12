package com.haier.cabinet.customer.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.haier.cabinet.customer.AppConfig;

import java.util.ArrayList;

/**
 * 搜索历史记录
 * Created by jinbiao.wu on 2015/11/20.
 */
public class AsSearchHistory {

    public final static String SEARCH_HISTORY = "search_history";

    private static AsSearchHistory INSTANCE;

    private Context mContext;

    public AsSearchHistory(Context context) {
        mContext = context;
    }

    public void setProperty(String key, String value) {
        AppConfig.getAppConfig(mContext).set(key, value);
    }

    public String getProperty(String key) {
        return AppConfig.getAppConfig(mContext).get(key);
    }

    public void removeProperty() {
        setProperty(SEARCH_HISTORY, "");
    }

    /**
     * 保存数据
     */
    public void setSearchHistory(String current_hostory) {
        ArrayList<String> historyList = new ArrayList<String>();
        // 搜索历史记录字符串
        String historys = getProperty(SEARCH_HISTORY);
        // 解析历史记录字符串，放到ArrayList中
        int end = 0;
        int start = 0;
        historyList.add(current_hostory);
        if (!TextUtils.isEmpty(historys)) {
            while ((end = historys.indexOf(";", start)) > -1) {
                // 如果关键字已经存在，就不在加入到ArrayList中
                if (!current_hostory.equals(historys.substring(start, end))) {
                    historyList.add(historys.substring(start, end));
                }

                start = end + 1;
            }
        }
        historys = "";

        for (int i = 0; i < historyList.size(); i++) {
            if (i >= 15) {
                break;
            }

            historys += historyList.get(i) + ";";

        }
        // 保存搜索历史记录字符串
        setProperty(SEARCH_HISTORY, historys);
    }

    /**
     * 查找历史记录
     *
     * @return
     */
    public ArrayList<String> getSearchHistory() {
        ArrayList<String> his = new ArrayList<String>();
        int end = 0;
        int start = 0;

        String history = getProperty(SEARCH_HISTORY);

        if (!TextUtils.isEmpty(history)) {
            while ((end = history.indexOf(";", start)) > -1) {
                his.add(history.substring(start, end));
                start = end + 1;
            }
        }
        return his;
    }
}
