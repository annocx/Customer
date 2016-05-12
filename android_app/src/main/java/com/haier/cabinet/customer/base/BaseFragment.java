package com.haier.cabinet.customer.base;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.event.CityChangedEvent;
import com.haier.cabinet.customer.event.ProductEvent;
import com.haier.cabinet.customer.interf.BaseFragmentInterface;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.widget.recyclerview.CustRecyclerView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;


/**
 * 碎片基类
 *
 * @author lzx
 */
public class BaseFragment extends Fragment implements BaseFragmentInterface {

    @Bind(R.id.title_text)
    protected TextView mTitleText;

    protected Activity mActivity;
    protected LayoutInflater mInflater;

    public Application getApplication() {
        return PushApplication.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.mInflater = inflater;
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void initView(View view) {

    }

    @Override
    public void initData() {

    }
}
