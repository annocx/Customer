package com.haier.cabinet.customer.wxapi;


import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.util.Constant;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private static final String TAG = "WXPayEntryActivity";
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        
    	api = WXAPIFactory.createWXAPI(this, Constant.weixin_appID);

        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (resp.errCode == 0) {
				notifyCheckoutCounter(true);
			} else {
				notifyCheckoutCounter(false);
			}
			WXPayEntryActivity.this.finish();
		}
	}

	private void notifyCheckoutCounter(boolean isSuccessfull){
		Intent intent;
		if (isSuccessfull){
			intent = new Intent(Constant.INTENT_ACTION_PAY_SUCCESS);
		}else {
			intent = new Intent(Constant.INTENT_ACTION_PAY_FAILED);
		}
		sendBroadcast(intent);
	}

}