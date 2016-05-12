package com.haier.cabinet.customer.util;

import android.os.CountDownTimer;
import android.os.Handler;

public class RemainingTimer extends CountDownTimer {

	private static Handler mHandler;
	public static final int IN_RUNNING = 1001;
	public static final int END_RUNNING = 1002;
	
	/**
	 * @param millisInFuture
	 *            // 倒计时的时长
	 * @param countDownInterval
	 *            // 间隔时间
	 * @param handler
	 *            // 通知进度的Handler
	 */
	public RemainingTimer(long millisInFuture, long countDownInterval,
			Handler handler) {
		super(millisInFuture, countDownInterval);
		mHandler = handler;
	}

	@Override
	public void onFinish() {
		if (mHandler != null)
			mHandler.sendEmptyMessage(END_RUNNING);
	}

	@Override
	public void onTick(long millisUntilFinished) {
		if (mHandler != null)
			mHandler.obtainMessage(IN_RUNNING,
					(int) (millisUntilFinished / 1000),-1).sendToTarget();
	}

}
