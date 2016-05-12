package com.haier.cabinet.customer.fragment;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.base.BaseSupplyFragment;
import com.haier.cabinet.customer.util.Constant;

public class SupplyListFragment extends BaseSupplyFragment implements SwipeRefreshLayout.OnRefreshListener{

    public SupplyListFragment() {
        super();
    }

    public static SupplyListFragment newInstance(int areaId) {
        
        Bundle bundle = new Bundle();
        
        SupplyListFragment fragment = new SupplyListFragment();
        bundle.putInt("areaId", areaId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public String getRequestUrl(boolean isStart) {
        if ((mListAdapter.getItemCount() == 0) || isStart) {
            mCurPageIndex = 1;
        } else {
            ++mCurPageIndex;
        }

        String url = Constant.SHOP_DOMAIN
                + "/appapi/index.php?act=activity&op=supply"
                + "&member_id=" + PushApplication.getInstance().getUserId()
                + "&areaid=" + getArguments().getInt("areaId")
                + "&page=" + mCurPageIndex;
        return url;
    }

}
