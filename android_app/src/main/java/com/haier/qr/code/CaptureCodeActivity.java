package com.haier.qr.code;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.HandleScanResultActivity;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.PackageBox;
import com.haier.cabinet.customer.util.Util;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.haier.common.util.Utils;
import com.haier.qr.code.zxing.camera.CameraManager;
import com.haier.qr.code.zxing.decoding.CaptureActivityHandler;
import com.haier.qr.code.zxing.decoding.FinishListener;
import com.haier.qr.code.zxing.decoding.InactivityTimer;
import com.haier.qr.code.zxing.view.ViewfinderView;
import com.sunday.statagent.StatAgent;

/**
 * Initial the camera
 * 
 * @author jdsjlzx
 */
public class CaptureCodeActivity extends BaseActivity implements Callback,
		View.OnClickListener {

	private static final String TAG = CaptureCodeActivity.class.getSimpleName();

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Map<DecodeHintType, ?> decodeHints;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private boolean isFlashlightOpen;
	private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
	/**
	 * 声音震动管理器。如果扫描成功后可以播放一段音频，也可以震动提醒，可以通过配置来决定扫描成功后的行为。
	 */
	private BeepManager beepManager;

	/**
	 * 闪光灯调节器。自动检测环境光线强弱并决定是否开启闪光灯
	 */
	private AmbientLightManager ambientLightManager;

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_capture;
	}

	@Override
	public void initView() {
		StatAgent.initAction(this, "", "1", "13", "", "", "", "1", "");
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		beepManager = new BeepManager(this);
		ambientLightManager = new AmbientLightManager(this);

		mTitleText = (TextView) findViewById(R.id.title_text);
		mTitleText.setText(R.string.scan_cabinet_text);

		mBackBtn = (ImageView) findViewById(R.id.back_img);
		mBackBtn.setVisibility(View.VISIBLE);

		// 按钮监听事件
		findViewById(R.id.capture_flashlight).setOnClickListener(this);
	}

	@Override
	public void initData() {

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		cameraManager = new CameraManager(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		viewfinderView.setVisibility(View.VISIBLE);

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			// 防止sdk8的设备初始化预览异常
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}
		decodeFormats = null;
		characterSet = null;

		// 加载声音配置，其实在BeemManager的构造器中也会调用该方法，即在onCreate的时候会调用一次
		beepManager.updatePrefs();

		// 启动闪光灯调节器
		ambientLightManager.start(cameraManager);

		// 恢复活动监控器
		inactivityTimer.onResume();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		beepManager.close();

		// 关闭摄像头
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();

		beepManager.playBeepSoundAndVibrate();

		String resultString = rawResult.getText();
//		Log.d(TAG, "handleDecode resultString = " + resultString);
		if (TextUtils.isEmpty(resultString)) {
			AppToast.showShortText(CaptureCodeActivity.this, "扫描出错!");
		} else {
			// 扫码完成，处理结果
			Map<String, String> map = Utils.getQRCodeContent(resultString);
			String terminalNo = getIntent().getStringExtra("terminalNo");// 传递过来的柜子编号
			/* String md5TerminalNo = Utils.getMD5Text(terminalNo); */
			String desTerminalNo = map.get("terminalNo");// 扫码得到的柜子编号
			List<PackageBox> unPickList = (List<PackageBox>) getIntent().getSerializableExtra("unPickList");
//			
			if (TextUtils.isEmpty(terminalNo)) {// 走快速开箱通道
				// 根据扫码得到的柜子编号来查询是否有件
				PackageBox box = null;
				String tip ;
				if (map.containsKey("boxNo")) {// 有箱子号
					String boxNoText = map.get("boxNo");
					if (!TextUtils.isEmpty(boxNoText)) {
						int boxNo = Integer.valueOf(boxNoText);
						box = getPackageBoxByTerminalNo(desTerminalNo, boxNo,
								unPickList);// 开指定的箱子
					}
					tip = "亲，您的快件不在这个箱子，请核实一下快件信息！";
				} else {
					box = getPackageBoxByTerminalNo(desTerminalNo, unPickList);// 随机开箱
					tip = "亲，此柜没有您的未取快件哦！";
				}
				
				if (box == null) {
					showConfirmDialog(tip);
				} else {
					// 开箱
					openBox(box, map);
				}
			} else {
				//http://203.130.41.104:8060/guizi-manager-diaochan/down_load.jsp?terminalNo=00176D01&corpType=hzdcdz&boxNo=15&arm=1
				if (!TextUtils.isEmpty(desTerminalNo)) {
					if (desTerminalNo.equals(terminalNo)) {
						PackageBox box = null;
						String tip = "亲，您的快件不在这个箱子，请核实一下快件信息！";

						if (map.containsKey("boxNo")) {//扫描静态二维码
							String boxNoText = map.get("boxNo");
							if (getIntent().hasExtra("packagebox")){//详情页面扫描开箱
								PackageBox box1 = (PackageBox) getIntent().getSerializableExtra("packagebox");
								if (!TextUtils.isEmpty(boxNoText)) {
									int boxNo = Integer.valueOf(boxNoText);
									if (boxNo == box1.boxNo) {
										box = box1;
									}
								}
							}
						} else {//扫描屏幕上的二维码
							box = (PackageBox) getIntent().getSerializableExtra("packagebox");
						}

						if (box == null) {
							showConfirmDialog(tip);
						} else {
							// 开箱
							openBox(box, map);
						}

					}else {
						showConfirmDialog("亲，您的快件不在这个柜子，请核实一下快件信息！");
					}
				} else {
					AppToast.showShortText(getApplicationContext(), "扫码失败，请重试！");
				}

			}

		}

	}

	private void openBox(PackageBox box, Map<String, String> map) {
		Bundle bundle = new Bundle();
		bundle.putInt("total", getIntent().getIntExtra("total", 0));
		if (map.containsKey("arm")) {
			bundle.putString("arm", map.get("arm"));
		}
		bundle.putSerializable("packagebox", box);
		IntentUtil.startActivity(CaptureCodeActivity.this, HandleScanResultActivity.class,bundle);
		CaptureCodeActivity.this.finish();
	}

	private PackageBox getPackageBoxByTerminalNo(String terminalNo, List<PackageBox> unPickList) {
		for (PackageBox packageBox : unPickList) {
			if (packageBox.cabinetNo.equals(terminalNo)) {
				return packageBox;
			}
		}
		return null;
		
	}

	private PackageBox getPackageBoxByTerminalNo(String terminalNo, int boxNo, List<PackageBox> unPickList) {
		for (PackageBox packageBox : unPickList) {
			if (packageBox.cabinetNo.equals(terminalNo) && packageBox.boxNo == boxNo) {
				return packageBox;
			}
		}
		return null;
		
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG,
					"initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(CaptureCodeActivity.this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}


		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}

	}

	private void displayFrameworkBugMessageAndExit() {
		if (Util.hasPermission(CaptureCodeActivity.this, Manifest.permission.CAMERA)){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.app_name));
			builder.setMessage(getString(R.string.msg_camera_framework_bug));
			builder.setPositiveButton(android.R.string.yes,
					new FinishListener(this));
			builder.setOnCancelListener(new FinishListener(this));
			builder.show();
		}else {
			ActivityCompat.requestPermissions(CaptureCodeActivity.this,
					new String[]{Manifest.permission.CAMERA},
					MY_PERMISSIONS_REQUEST_CAMERA);
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.capture_flashlight:
			if (isFlashlightOpen) {
				cameraManager.setTorch(false); // 关闭闪光灯
				isFlashlightOpen = false;
			} else {
				cameraManager.setTorch(true); // 打开闪光灯
				isFlashlightOpen = true;
			}
			break;
		default:
			break;
		}
	}

	/*CustomDialog dialog = null;

	private void showErrorDialog() {
		closeCamera();
		viewfinderView.setVisibility(View.GONE);
		dialog = new CustomDialog(CaptureCodeActivity.this, R.style.MyDialog,
				new CustomDialog.CustomDialogListener() {

					@Override
					public void onClick(View view) {
						switch (view.getId()) {
						case R.id.ok_text:
							dialog.dismiss();
							restartCamera();
							break;
						case R.id.close_text:
							dialog.dismiss();
							finish();
							break;

						default:
							break;
						}
					}
				});
		dialog.getCustomView().findViewById(R.id.failure_layout)
				.setVisibility(View.VISIBLE);
		((TextView) dialog.getCustomView().findViewById(R.id.ok_text))
				.setText(R.string.scan_qr_code_again);
		dialog.setCancelable(false);
		dialog.show();
	}*/

	void restartCamera() {
		Log.d(TAG, "hasSurface " + hasSurface);

		viewfinderView.setVisibility(View.VISIBLE);

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		initCamera(surfaceHolder);

		// 恢复活动监控器
		inactivityTimer.onResume();
	}

	void closeCamera() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();

		// 关闭摄像头
		cameraManager.closeDriver();
	}

	private void showConfirmDialog(String content) {
		closeCamera();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(content)
				.setPositiveButton("重新扫描",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								restartCamera();
							}
						})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}

				});
		AlertDialog ad = builder.create();
		ad.setCanceledOnTouchOutside(false); // 点击外面区域不会让dialog消失
		ad.show();
	}

}