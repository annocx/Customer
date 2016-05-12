package com.haier.cabinet.customer.activity;

import java.io.Serializable;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.CommonAdapter;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;

public class CommonListActivity extends Activity {

	@Bind(R.id.title_text)
	TextView titleText;
	@Bind(R.id.listview) ListView mListView;

	private int requestCode = -1;
	private CommonAdapter mAdapter;
	List<Object> mDataList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_list_layout);
		PushApplication.addActivity(this);
		Intent intent = getIntent();
		if (intent != null) {
			String title = intent.getStringExtra("title");
			titleText.setText(title);

			requestCode = intent.getIntExtra("request_code", -1);

			Bundle extras = intent.getExtras();
			mDataList = (List<Object>) extras.getSerializable("data");
		}
		mAdapter = new CommonAdapter(this, requestCode, mDataList);

		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object object = mDataList.get(position);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("result", (Serializable) object);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
