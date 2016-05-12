package com.haier.cabinet.customer.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.haier.cabinet.customer.entity.AddressInfo;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.Categorize;
import com.haier.cabinet.customer.entity.Coupon;
import com.haier.cabinet.customer.entity.FreshExpress;
import com.haier.cabinet.customer.entity.Order;
import com.haier.cabinet.customer.entity.OrderProduct2;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.Shop;
import com.haier.cabinet.customer.entity.Supply;
import com.haier.common.util.NetUtil;

import kankan.wheel.widget.adapters.City;
import kankan.wheel.widget.adapters.Province;

public class Util {
    private static final String TAG = "Util";

    public static byte[] httpGet(final String url) {
        if (url == null || url.length() == 0) {
            Log.e(TAG, "httpGet, url is null");
            return null;
        }

        HttpClient httpClient = getNewHttpClient();
        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse resp = httpClient.execute(httpGet);
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.e(TAG, "httpGet fail, status code = " + resp.getStatusLine().getStatusCode());
                return null;
            }

            return EntityUtils.toByteArray(resp.getEntity());

        } catch (Exception e) {
            Log.e(TAG, "httpGet exception, e = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] httpPost(String url, String entity) {
        if (url == null || url.length() == 0) {
            Log.e(TAG, "httpPost, url is null");
            return null;
        }

        HttpClient httpClient = getNewHttpClient();

        HttpPost httpPost = new HttpPost(url);

        try {
            httpPost.setEntity(new StringEntity(entity));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse resp = httpClient.execute(httpPost);
            if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.e(TAG, "httpGet fail, status code = " + resp.getStatusLine().getStatusCode());
                return null;
            }

            return EntityUtils.toByteArray(resp.getEntity());
        } catch (Exception e) {
            Log.e(TAG, "httpPost exception, e = " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static class SSLSocketFactoryEx extends SSLSocketFactory {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    private static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public static double getTotal(double total) {
        return MathUtil.round(total, 2, BigDecimal.ROUND_HALF_EVEN);
    }

    private static long lastClickTime;

    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    //计算折扣价
    public static Bracket getDiscount(Product product) {

        int size = product.bracketList.size();
        for (int i = 0; i < size; i++) {

            for (int j = 1; j <= size; j++) {//这个地方需要注意
                if (product.count < product.bracketList.get(0).num) {
                    return null;
                }
                if (product.count >= product.bracketList.get(size - 1).num) {
                    return product.bracketList.get(size - 1);
                }
                if (product.bracketList.get(i).num > product.count && product.count < product.bracketList.get(j).num) {
                    return product.bracketList.get(i - 1);
                }
            }
        }
        return null;
    }

    // 检测是否拥有此权限
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    //  获取收货地址列表数据
    public static List<AddressInfo> getAddressListByJosn(String json) {
        List<AddressInfo> list = new ArrayList<AddressInfo>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result")) {
                return null;
            }
            JSONObject resultObject = jsonObject.getJSONObject("result");
            JSONArray listArray = resultObject.getJSONArray("address_list");
            for (int i = 0; i < listArray.length(); i++) {
                AddressInfo address = new AddressInfo();
                JSONObject object = listArray.getJSONObject(i);
                address.name = object.getString("true_name");
                address.phone = object.getString("mob_phone");
                address.id = object.getString("address_id");
                Province province = new Province();
                province.id = object.getInt("area_id");
                address.province = province;
                City city = new City();
                city.id = object.getInt("city_id");
                address.city = city;

                address.provincialCityArea = object.getString("area_info");
                address.street = object.getString("address");

                int state = object.getInt("is_default");
                address.status = (state == 1);
                list.add(address);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }
        return list;
    }

    // 获取产地列表数据
    public static ArrayList<Supply> getSupplyListByJosn(String json) {
        ArrayList<Supply> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result") || TextUtils.isEmpty(jsonObject.getJSONObject("result").toString())) {
                return null;
            }
            JSONObject supplysObject = jsonObject.getJSONObject("result");
            JSONArray orderArray = supplysObject.getJSONArray("supply_list");
            for (int i = 0; i < orderArray.length(); i++) {
                JSONObject supplyObject = orderArray.getJSONObject(i);
                list.add(Util.getSupplyByJson(supplyObject));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }
        return list;
    }

    //  获取产地 特产数据
    public static Supply getSupplyByJson(JSONObject supplyObject) {
        Supply supply = null;
        try {
            supply = new Supply();
            supply.setId(supplyObject.getInt("id"));
            supply.setTitle(supplyObject.getString("title"));
            supply.setDesc(supplyObject.getString("desc"));
            supply.setImage(supplyObject.getString("image"));
        } catch (Exception e) {
            Log.e(TAG, "JSONException -- " + e.toString());
        }
        return supply;
    }

    //  获取分类数据
    public static Categorize getCategorizeByJson(JSONObject categorizeObject) {
        Categorize categorize = null;
        try {
            categorize = new Categorize();
            categorize.setId(categorizeObject.getInt("gc_id"));
            categorize.setTitle(categorizeObject.getString("gc_name"));
            categorize.setDescribe(categorizeObject.getString("gc_description"));
            //categorize.setUrl(categorizeObject.getString("image"));
        } catch (Exception e) {
            Log.e(TAG, "JSONException -- " + e.toString());
        }
        return categorize;
    }

    // 获取订单数据
    public static Order getOrderByPaySn(String json) {
        Order order = new Order();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result") || TextUtils.isEmpty(jsonObject.getJSONObject("result").toString())) {
                return null;
            }

            JSONObject orderDetailsObject = jsonObject.getJSONObject("result");

            JSONArray orderDetailsArray = orderDetailsObject.getJSONArray("order_list");

            for (int i = 0; i < orderDetailsArray.length(); i++) {
                JSONObject orderDetailObject = orderDetailsArray.getJSONObject(i);

                order.setReciverName(orderDetailObject.getString("reciver_name"));
                order.setAddress(orderDetailObject.getString("reciver_address"));
                order.setPhone(orderDetailObject.getString("reciver_phone"));
                order.setOrderAmount(orderDetailObject.getString("order_amount"));
                order.setEvaluationState(orderDetailObject.getString("evaluation_state"));
                order.setOrderState(orderDetailObject.getString("order_state"));
                order.setShippingCode(orderDetailObject.getString("shipping_code"));
                order.setOrderId(orderDetailObject.getString("order_id"));
                order.setShippingFee(orderDetailObject.getString("shipping_fee"));
                order.setStoreName(orderDetailObject.getString("store_name"));
                order.setOrderSn(orderDetailObject.getString("order_sn"));
                order.setCouponUse(orderDetailObject.getString("coupon_use"));
                order.setCouponDiscount(orderDetailObject.getString("coupon_amt"));

                List<OrderProduct2> proList = new ArrayList<OrderProduct2>();
                JSONArray itemArray = orderDetailObject.getJSONArray("goods_list");
                for (int j = 0; j < itemArray.length(); j++) {
                    JSONObject shopObject = itemArray.getJSONObject(j);

                    //店铺信息
                    OrderProduct2 product = new OrderProduct2();
                    product.setGcId(shopObject.getInt("goods_id"));
                    product.setShopName(shopObject.getString("goods_name"));
                    product.setRetailPrice(shopObject.getString("goods_price"));
                    product.setDiscountPrice(shopObject.getString("goods_pay_price"));
                    product.setImgUrl(shopObject.getString("goods_image"));
                    product.setNum(shopObject.getString("goods_num"));
                    product.setJingpin(shopObject.getString("jingpin"));
                    proList.add(product);
                    order.setDataList(proList);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }
        return order;
    }

    // 获取订单数据
    public static Order getOrder(String json) {
        Order order = new Order();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.isNull("result") || TextUtils.isEmpty(jsonObject.getJSONObject("result").toString())) {
                return null;
            }

            JSONObject orderDetailObject = jsonObject.getJSONObject("result");

            order.setReciverName(orderDetailObject.getString("reciver_name"));
            order.setAddress(orderDetailObject.getString("reciver_address"));
            order.setPhone(orderDetailObject.getString("reciver_phone"));
            order.setOrderAmount(orderDetailObject.getString("order_amount"));
            order.setEvaluationState(orderDetailObject.getString("evaluation_state"));
            order.setOrderState(orderDetailObject.getString("order_state"));
            order.setShippingCode(orderDetailObject.getString("shipping_code"));
            order.setOrderId(orderDetailObject.getString("order_id"));
            order.setShippingFee(orderDetailObject.getString("shipping_fee"));
            order.setStoreName(orderDetailObject.getString("store_name"));
            order.setOrderSn(orderDetailObject.getString("order_sn"));
            order.setPaySn(orderDetailObject.getString("pay_sn"));
            order.setCouponUse(orderDetailObject.getString("coupon_use"));
            order.setCouponDiscount(orderDetailObject.getString("coupon_amt"));
            order.setPayState(orderDetailObject.getString("pay_state"));

            List<OrderProduct2> proList = new ArrayList<OrderProduct2>();
            JSONArray itemArray = orderDetailObject.getJSONArray("goods_list");
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject shopObject = itemArray.getJSONObject(i);

                //店铺信息
                OrderProduct2 product = new OrderProduct2();
                product.setGcId(shopObject.getInt("goods_id"));
                product.setShopName(shopObject.getString("goods_name"));
                product.setRetailPrice(shopObject.getString("goods_price"));
                product.setDiscountPrice(shopObject.getString("goods_pay_price"));
                product.setImgUrl(shopObject.getString("goods_image"));
                product.setNum(shopObject.getString("goods_num"));
                product.setJingpin(shopObject.getString("jingpin"));
                proList.add(product);
                order.setDataList(proList);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }
        return order;
    }

    // 通过json获取订单数据
    public static Order getOrderByJosn(JSONObject orderObject) {

        List<OrderProduct2> proList = new ArrayList<OrderProduct2>();
        Order order = null;
        try {
            order = new Order();
            order.setStoreId(orderObject.getString("store_id"));
            order.setStoreName(orderObject.getString("store_name"));
            order.setShippingCode(orderObject.getString("shipping_code"));
            order.setBuyerName(orderObject.getString("buyer_name"));
            order.setPhone(orderObject.getString("reciver_phone"));
            order.setShippingFee(orderObject.getString("shipping_fee"));
            order.setOrderAmount(orderObject.getString("order_amount"));
            order.setAddress(orderObject.getString("reciver_address"));
            order.setOrderSn(orderObject.getString("order_sn"));
            order.setPaySn(orderObject.getString("pay_sn"));
            order.setFinishTime(orderObject.getString("finnshed_time"));
            order.setReciverName(orderObject.getString("reciver_name"));
            order.setOrderState(orderObject.getString("order_state"));
            order.setPayState(orderObject.getString("pay_state"));
            order.setEvaluationState(orderObject.getString("evaluation_state"));
            order.setOrderId(orderObject.getString("order_id"));
            order.setCouponUse(orderObject.getString("coupon_use"));
            order.setCouponDiscount(orderObject.getString("coupon_amt"));

            JSONArray productArray = orderObject.getJSONArray("goods_list");
            for (int i = 0; i < productArray.length(); i++) {
                JSONObject proObject = productArray.getJSONObject(i);

                OrderProduct2 product = new OrderProduct2();
                product.setId(proObject.getString("gc_id"));
                product.setShopName(proObject.getString("goods_name"));
                product.setDiscountPrice(proObject.getString("goods_pay_price"));
                product.setRetailPrice(proObject.getString("goods_price"));
                product.setImgUrl(proObject.getString("goods_image"));
                product.setNum(proObject.getString("goods_num"));
                product.setJingpin(proObject.getString("jingpin"));
                proList.add(product);
            }
            order.setDataList(proList);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException -- " + e.toString());
        }
        return order;
    }

    // 通过json获取订单数据
    public static Coupon getCouponByJosn(JSONObject couponObject, int from) {

        Coupon coupon = null;
        try {
            coupon = new Coupon();
            coupon.setCoupon_sn(couponObject.getString("couponSn"));
            coupon.setMember_phone(couponObject.getString("memberMobile"));
            coupon.setCoupon_receive_time(couponObject.getString("receiveTime"));
            coupon.setUseFromTime(couponObject.getString("useFromTime"));

            if (from == 1) coupon.setCoupon_good_id(couponObject.getString("goods"));

            coupon.setTemplate_Code(couponObject.getString("templateCode"));
            coupon.setCoupon_Show_Summary(couponObject.getString("couponShowSummary"));
            coupon.setCouponShowName(couponObject.getString("couponShowName"));
            coupon.setDiscountShowName(couponObject.getString("discountShowName"));
            coupon.setDiscount(couponObject.getString("discount"));
            coupon.setDiscount_Level(couponObject.getString("discountLevel"));
            coupon.setShowStatus(couponObject.getString("showStatus"));
            coupon.setDiscount_Type(couponObject.getString("discountType"));
            coupon.setCoupon_use_time(couponObject.getString("useToTime"));
            coupon.setSponsor(couponObject.getString("sponsor"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return coupon;
    }

    public static Product convertFreshExpress2Product(FreshExpress freshExpress) {
        Product product = new Product();
        product.id = freshExpress.id;
        product.title = freshExpress.title;
        product.discountPrice = freshExpress.discountPrice;
        product.retailPrice = freshExpress.retailPrice;
        product.spec = freshExpress.spec;
        product.madein = freshExpress.madein;
        product.imgUrl = freshExpress.imgUrl;
        product.detailsUrl = freshExpress.detailsUrl;
        product.shopId = freshExpress.shopId;
        product.shopName = freshExpress.shopName;
        product.freight = freshExpress.freight;
        product.free_delivery_pirce = freshExpress.free_delivery_pirce;
        product.cid = freshExpress.cid;
        product.count = freshExpress.count;
        product.shopCardId = freshExpress.shopCardId;
        product.serviceArea = freshExpress.serviceArea;
        return product;
    }

    public static Shop convertFreshExpress2Shop(FreshExpress freshExpress) {
        Shop shop = new Shop();
        shop.id = freshExpress.shopId;
        shop.name = freshExpress.shopName;
        shop.imgUrl = freshExpress.imgUrl;
        return shop;
    }

    public static Shop getShopInfo(Product product) {
        Shop shop = new Shop();
        shop.id = product.shopId;
        shop.name = product.shopName;
        shop.fee = product.freight;
        shop.free_delivery_pirce = product.free_delivery_pirce;
        return shop;
    }

    public static void keepListViewHeight(ListView listView) {
        //获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {   //listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);  //计算子项View 的宽高
            totalHeight += listItem.getMeasuredHeight();  //统计所有子项的总高度
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        //listView.getDividerHeight()获取子项间分隔符占用的高度
        //params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);

    }

    /**
     * 过滤特殊字符
     */
    public static boolean CheckString(String input) {
        boolean flag = false;
        try {
            String regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
            Pattern regex = Pattern.compile(regEx);
            Matcher matcher = regex.matcher(input);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * dip，dp转化成px 用来处理不同分辨路的屏幕
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;//屏幕寬度
    }

    public static boolean isLowEndDevice(Context context) {
        return getScreenWidth(context) < 600;
    }

    /**
     * 加载图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

}
