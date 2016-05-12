package com.haier.common.view;

import com.haier.common.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class CustomDialog extends AlertDialog implements View.OnClickListener {

	private TextView mShareText;
	private TextView mCloseText;
	private View mView;
	private LayoutInflater inflater;

	private CustomDialogListener listener;

	public interface CustomDialogListener {
		public void onClick(View view);
	}

	public CustomDialog(Context context) {
		super(context);
		inflater = LayoutInflater.from(context);
		mView = inflater.inflate(R.layout.activity_cust_dialog, null);
	}

	public CustomDialog(Context context, int theme,
			CustomDialogListener listener) {
		super(context);
		this.listener = listener;
		inflater = LayoutInflater.from(context);
		mView = inflater.inflate(R.layout.activity_cust_dialog, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mView);
		mShareText = (TextView) findViewById(R.id.ok_text);
		mCloseText = (TextView) findViewById(R.id.close_text);

		mShareText.setOnClickListener(this);
		mCloseText.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		listener.onClick(v);
	}

	public View getCustomView() {
		return mView;
	}

	public void setFailureViewVisible() {

		/*
		 * findViewById(R.id.success_layout).setVisibility(View.GONE);
		 * findViewById(R.id.failure_layout).setVisibility(View.VISIBLE);
		 */
		mShareText.setText(R.string.scan_qr_code_again);
	}
}
