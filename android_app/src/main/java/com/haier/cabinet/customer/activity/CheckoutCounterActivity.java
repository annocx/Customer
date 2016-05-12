package com.haier.cabinet.customer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.entity.Order;
import com.haier.cabinet.customer.entity.PayResult;
import com.haier.cabinet.customer.event.OrderEvent;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.MD5;
import com.haier.cabinet.customer.util.MathUtil;
import com.haier.cabinet.customer.util.SignUtils;
import com.haier.cabinet.customer.util.Util;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 乐家收银台
 */
public class CheckoutCounterActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "CheckoutCounterActivity";
    @Bind(R.id.weixin_checkbox)
    CheckBox weiXinPayCbx;
    @Bind(R.id.alipay_checkbox)
    CheckBox alipayPayCbx;
    @Bind(R.id.order_total_money_text)
    TextView mOrderTotalMoneyText;
    @Bind(R.id.pay_btn)
    Button mPayBtn;

    private Intent mIntent;
    private String paySn;
    private String orderSn;
    private Double mMoney;

    private Order order;

    private String orderId;

    public int payOrderWay = Constant.PAY_FROM_SHOPCART;

    public static final int WEIXIN_PAY = 0;//微信支付
    public static final int ALIPAY_PAY = 1;//支付宝支付

    public int payment_mode = WEIXIN_PAY;//支付宝支付

    PayReq req;
    final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);

    Map<String, String> resultunifiedorder;
    StringBuffer sb;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constant.INTENT_ACTION_PAY_SUCCESS)) {
                payOrderSuccessfully();
            } else if (action.equals(Constant.INTENT_ACTION_PAY_FAILED)) {
                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                AppToast.showShortText(CheckoutCounterActivity.this, "支付失败");
                payOrderFailed();
            }

        }
    };
    @Override
    protected int getLayoutId() {
        return R.layout.activity_checkout_counter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PushApplication.addActivity(this);
        initView();
        initData();
    }

    public void initView() {
        mTitleText.setText("乐家收银台");
        mBackBtn.setVisibility(View.VISIBLE);
    }

    public void initData() {
        mIntent = getIntent();
        mMoney = mIntent.getDoubleExtra("money", 0.0);
        orderSn = mIntent.getStringExtra("order_sn");//多个订单用","隔开
        paySn = mIntent.getStringExtra("pay_sn");//多个订单用","隔开
        payOrderWay = mIntent.getIntExtra("pay_src", Constant.PAY_FROM_SHOPCART);
        orderId = mIntent.getStringExtra(Constant.INTENT_KEY_ORDER_ID);

        if (payOrderWay == Constant.PAY_FROM_SHOPCART) {
            orderSn = paySn;
        }

        mOrderTotalMoneyText.setText("￥" + mMoney);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.INTENT_ACTION_PAY_SUCCESS);
        intentFilter.addAction(Constant.INTENT_ACTION_PAY_FAILED);
        registerReceiver(mBroadcastReceiver, intentFilter);

        //微信请求相关
        req = new PayReq();
        sb = new StringBuffer();
        msgApi.registerApp(Constant.weixin_appID);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    @OnClick({R.id.back_img,R.id.weixin_checkbox, R.id.weixin_pay_layout, R.id.alipay_checkbox, R.id.alipay_layout,
            R.id.pay_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.weixin_checkbox:
            case R.id.weixin_pay_layout:
                payment_mode = WEIXIN_PAY;
                weiXinPayCbx.setChecked(true);
                alipayPayCbx.setChecked(false);
                break;
            case R.id.alipay_checkbox:
            case R.id.alipay_layout:
                payment_mode = ALIPAY_PAY;
                alipayPayCbx.setChecked(true);
                weiXinPayCbx.setChecked(false);
                break;
            case R.id.pay_btn:
                mHandler.sendEmptyMessage(GO_TO_PAY);
                break;

            default:
                break;
        }
    }

    private static final int GO_TO_PAY = 1001;
    private static final int SDK_PAY_FLAG = 1002;
    private static final int SDK_CHECK_FLAG = 1003;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case SDK_PAY_FLAG:
                    PayResult payResult = new PayResult((String) msg.obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();
                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        /*if (PushApplication.getInstance().isLogin() && !TextUtils.isEmpty(mOrderNo)) {
                            HaierApi.modifyOrderPayState(PushApplication.getInstance().getUserId(), mOrderNo, responseHandler);
                        }*/
                        AppToast.showShortText(CheckoutCounterActivity.this, "支付成功");
                        payOrderSuccessfully();
                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            AppToast.showShortText(CheckoutCounterActivity.this, "支付结果确认中");

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            AppToast.showShortText(CheckoutCounterActivity.this, "支付失败");
                            //支付失败
                            payOrderFailed();
                        }
                    }
                    break;
                case SDK_CHECK_FLAG:
                    Toast.makeText(CheckoutCounterActivity.this, "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                case GO_TO_PAY:
                    if (payment_mode == WEIXIN_PAY) {// 微信支付
                        GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
                        getPrepayId.execute();
                    } else if (payment_mode == ALIPAY_PAY) {// 支付宝支付
                        payByAlipay();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void payOrderSuccessfully() {
        UserOrderListActivity.isUpdate = true;

        EventBus.getDefault().post(new OrderEvent(getIntent().getIntExtra("position", 0), getIntent().getIntExtra("type", 1)));

        Bundle bundle = new Bundle();
        bundle.putString(Constant.INTENT_KEY_PAY_SN, paySn);
        bundle.putString(Constant.INTENT_KEY_ORDER_ID, orderId);
        bundle.putInt("pay_src", payOrderWay);
        IntentUtil.startActivity(CheckoutCounterActivity.this, PaySuccessActivity.class, bundle);
        CheckoutCounterActivity.this.finish();
    }

    private void payOrderFailed() {
        Bundle bundle = new Bundle();
        bundle.putInt("order_state", 1);// 1:成功 0:失败
        IntentUtil.startActivity(CheckoutCounterActivity.this, UserOrderListActivity.class, bundle);
        CheckoutCounterActivity.this.finish();
    }

    private AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
            String json = new String(bytes);
            Log.e(TAG, "modify pay state onSuccess json = " + json);
            if (200 == statusCode) {
                switch (JsonUtil.getStateFromShopServer(json)) {
                    case 1001:
                        AppToast.showShortText(CheckoutCounterActivity.this, "支付成功");
                        payOrderSuccessfully();

                        break;

                    default:
                        AppToast.showShortText(CheckoutCounterActivity.this, "支付成功");
                        payOrderSuccessfully();
                        break;
                }
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            Log.e(TAG, "modify pay state onFailure  ", throwable);
            AppToast.showShortText(CheckoutCounterActivity.this, "网络繁忙，订单支付状态稍候更新！");
            payOrderSuccessfully();
        }
    };

    //支付宝相关的方法 begin

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void payByAlipay() {
        // 订单
        String orderInfo = null;

        if (payOrderWay == Constant.PAY_FROM_SHOPCART) {
            orderInfo = getOrderInfo("乐家订单-" + orderSn, "all", String.valueOf(mMoney));
        } else if (payOrderWay == Constant.PAY_FROM_ORDER) {
            orderInfo = getOrderInfo("乐家订单-" + orderSn, paySn, String.valueOf(mMoney));
        }

        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();
        /*Log.d(TAG, "payByAlipay payInfo " + payInfo);*/
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(CheckoutCounterActivity.this);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * check whether the device has authentication alipay account.
     * 查询终端设备是否存在支付宝认证账户
     */
    public void check(View v) {
        Runnable checkRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask payTask = new PayTask(CheckoutCounterActivity.this);
                // 调用查询接口，获取查询结果
                boolean isExist = payTask.checkAccountIfExist();

                Message msg = new Message();
                msg.what = SDK_CHECK_FLAG;
                msg.obj = isExist;
                mHandler.sendMessage(msg);
            }
        };

        Thread checkThread = new Thread(checkRunnable);
        checkThread.start();

    }

    /**
     * get the sdk version. 获取SDK版本号
     */
    public void getSDKVersion() {
        PayTask payTask = new PayTask(this);
        String version = payTask.getVersion();
        Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
    }

    /**
     * create the order info. 创建订单信息
     */
    public String getOrderInfo(String subject, String body, String price) {
        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + Constant.PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + Constant.SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + orderSn + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + Constant.URL_ALIPAY_PAY_NOTIFY
                + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    public String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    public String sign(String content) {
        return SignUtils.sign(content, Constant.RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }
    //支付宝相关的方法 end


    //微信相关的方法 begin

    /**
     * 生成签名
     */
    private String genPackageSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constant.API_KEY_WEIXIN_PAY);

        String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        return packageSign;
    }

    private String genAppSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constant.API_KEY_WEIXIN_PAY);

        this.sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        Log.e(TAG, "appSign " + appSign);
        return appSign;
    }

    private String toXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<" + params.get(i).getName() + ">");


            sb.append(params.get(i).getValue());
            sb.append("</" + params.get(i).getName() + ">");
        }
        sb.append("</xml>");

        Log.e("orion", sb.toString());
        return sb.toString();
    }

    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected void onPreExecute() {
            DialogHelper.showDialogForLoading(CheckoutCounterActivity.this, "正在处理数据，请稍候", true);
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            DialogHelper.stopProgressDlg();

            sb.append("prepay_id\n" + result.get("prepay_id") + "\n\n");
            resultunifiedorder = result;

            //支付申请
            genPayReq();
            sendPayReq();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {

            String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            String entity = genProductArgs();

            Log.e("orion", entity);

            byte[] buf = Util.httpPost(url, entity);

            String content = new String(buf);
            Log.e("orion", content);
            Map<String, String> xml = decodeXml(content);

            return xml;
        }
    }


    public Map<String, String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if ("xml".equals(nodeName) == false) {
                            //实例化student对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            Log.e("orion", e.toString());
        }
        return null;

    }


    private String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }


    private String genOutTradNo() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }


    private String genProductArgs() {
        StringBuffer xml = new StringBuffer();

        try {
            String nonceStr = genNonceStr();

            xml.append("</xml>");
            List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
            packageParams.add(new BasicNameValuePair("appid", Constant.weixin_appID));
            if (payOrderWay == Constant.PAY_FROM_SHOPCART) {
                packageParams.add(new BasicNameValuePair("attach", "all"));
            } else if (payOrderWay == Constant.PAY_FROM_ORDER) {
                packageParams.add(new BasicNameValuePair("attach", paySn));
            }
            packageParams.add(new BasicNameValuePair("body", "乐家订单-" + orderSn)); //商品描述
            packageParams.add(new BasicNameValuePair("device_info", "ANDROID"));
            packageParams.add(new BasicNameValuePair("mch_id", Constant.MCH_ID));
            packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
            packageParams.add(new BasicNameValuePair("notify_url", Constant.URL_WEIXIN_PAY_NOTIFY));

            packageParams.add(new BasicNameValuePair("out_trade_no", orderSn));
            packageParams.add(new BasicNameValuePair("spbill_create_ip", "127.0.0.1"));
            packageParams.add(new BasicNameValuePair("total_fee", MathUtil.double2String(mMoney)));//商品金额,以分为单位
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));

            String sign = genPackageSign(packageParams);
            packageParams.add(new BasicNameValuePair("sign", sign));


            String xmlstring = toXml(packageParams);
            xmlstring = new String(xmlstring.getBytes("UTF-8"), "ISO-8859-1");
            return xmlstring;

        } catch (Exception e) {
            Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
            return null;
        }


    }

    private void genPayReq() {

        req.appId = Constant.weixin_appID;
        req.partnerId = Constant.MCH_ID;
        req.prepayId = resultunifiedorder.get("prepay_id");
        req.packageValue = "Sign=WXPay";
        req.nonceStr = genNonceStr();
        req.timeStamp = String.valueOf(genTimeStamp());


        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

        req.sign = genAppSign(signParams);
        sb.append("sign\n" + req.sign + "\n\n");
        Log.e("orion", signParams.toString());

    }

    private void sendPayReq() {

        msgApi.registerApp(Constant.weixin_appID);
        msgApi.sendReq(req);
    }
    //微信相关的方法 end
}
