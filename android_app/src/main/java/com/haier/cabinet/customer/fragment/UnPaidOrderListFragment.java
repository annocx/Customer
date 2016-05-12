package com.haier.cabinet.customer.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.base.BaseOrderFragment;
import com.haier.cabinet.customer.entity.Order;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UnPaidOrderListFragment extends BaseOrderFragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "UnPaidOrderListFragment";

    @Override
    public List<Order> getListByJosn(String json) {
        ArrayList<Order> list = new ArrayList<Order>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result") || TextUtils.isEmpty(jsonObject.getJSONObject("result").toString())) {
                return null;
            }
            JSONObject ordersObject = jsonObject.getJSONObject("result");
            totalRecord = ordersObject.getInt("page_count");
            JSONArray orderArray = ordersObject.getJSONArray("order_list");
            for (int i = 0; i < orderArray.length(); i++) {
                JSONObject orderObject = orderArray.getJSONObject(i);
                list.add(Util.getOrderByJosn(orderObject));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }
        return list;
    }

    @Override
    public String getRequestUrl(boolean isStart) {
        if ((mListAdapter.getItemCount() == 0) || isStart) {
            mCurPageIndex = 1;
        } else {
            ++mCurPageIndex;
        }
        String url = Constant.SHOP_DOMAIN
                + "/appapi/index.php?act=member_order&op=order_list&type=new&v=3"
                + "&member_id=" + PushApplication.getInstance().getUserId()
                + "&page=" + mCurPageIndex;
        return url;
    }
}
