package com.haier.cabinet.customer.util;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 定义一个倒计时的内部类
 * 
 * @author jinbiao.wu
 */
public class TimeCount extends CountDownTimer {
	private TextView tv_hour;
	private TextView tv_min;
	private TextView tv_sec;
    private LinearLayout ll;
    private boolean hasLinearLayout;
	public TimeCount(LinearLayout ll,TextView tv_hour, TextView tv_min,TextView tv_sec, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		hasLinearLayout = true;
		this.ll = ll;
	}
	public TimeCount(TextView tv_hour, TextView tv_min,TextView tv_sec,long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		this.tv_hour = tv_hour;
		this.tv_min = tv_min;
		this.tv_sec = tv_sec;
	}
	@Override
	public void onFinish() {// 计时完毕时触发

		tv_hour.setText("00");
		tv_min.setText("00");
		tv_sec.setText("00");
	}

	@Override
	public void onTick(long millisUntilFinished) {// 计时过程显示
		// 当前时间减去测试时间 这个的除以1000得到秒，相应的60000得到分，3600000得到小时
		tv_hour.setText(""+(millisUntilFinished / 3600000 < 10 ? "0"
				+ millisUntilFinished / 3600000
				: (millisUntilFinished / 3600000)));
		tv_min.setText(""+(millisUntilFinished / 60000 % 60 < 10 ? "0"
				+ millisUntilFinished / 60000 % 60
				: millisUntilFinished / 60000 % 60));
		tv_sec.setText(""+(millisUntilFinished % 60000 / 1000 < 10 ? "0"
				+ millisUntilFinished % 60000 / 1000
				: millisUntilFinished % 60000 / 1000));
	}
}
