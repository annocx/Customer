package com.haier.cabinet.customer.util;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.OrderItem;
import com.haier.cabinet.customer.entity.OrderProduct;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.ShopCartItem;
import com.haier.cabinet.customer.entity.VASOrderItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    public static int getStateFromServer(String json) {
        int state = 600;
        try {
            JSONObject jsonObject = new JSONObject(json);
            state = jsonObject.getInt("infoCode");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return state;
    }

    public static int getStateFromJdbServer(String json) {
        int state = 1002;
        try {
            JSONObject jsonObject = new JSONObject(json);
            state = jsonObject.getInt("state");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return state;
    }

    public static int getStateFromShopServer(String json) {
        int state = 600;
        try {
            JSONObject jsonObject = new JSONObject(json);
            state = jsonObject.getInt("state");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return state;
    }

    public static int getExpressMailFromServer(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            boolean state = jsonObject.getBoolean("success");
            if (state) {
                String data = jsonObject.getString("data");
                if (data.length() == 0) {
                    return 1002;
                } else {
                    return 1001;
                }
            } else {
                return 2001;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 2001;
    }

    public static int getCouponFromServer(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            boolean state = jsonObject.getBoolean("success");
            if (state) {
                JSONArray data = jsonObject.getJSONArray("data");
                if (data.length() == 0) {
                    return 1002;
                } else {
                    return 1001;
                }
            } else {
                return 2001;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 2001;
    }

    public static String getOrderNoFromServer(String json) {
        String orderNo = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject resultObject = jsonObject.getJSONObject("result");
            orderNo = resultObject.getString("pay_sn");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return orderNo;
    }

    public static boolean isHaveData(String json) {
        boolean flag = false;
        try {
            JSONObject jsonObject = new JSONObject(json);
            flag = !jsonObject.isNull("result");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean isHaveOrderListData(String json) {
        boolean flag = false;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (!jsonObject.isNull("result")) {
                JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                JSONArray jsonArray = jsonObject1.getJSONArray("order_list");
                flag = !(jsonArray.length() == 0);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static ArrayList<Integer> getOrderCount(String json) {
        ArrayList<Integer> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray data = jsonObject.getJSONArray("result");

            if (null != data) {
                for (int i = 0; i < data.length(); i ++) {
                    list.add(data.getInt(i));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int getShopCartId(String json) {
        int shopCardId = 0;
        try {
            JSONObject jsonObject = new JSONObject(json);
            shopCardId = jsonObject.getInt("result");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return shopCardId;
    }

    public static String getUserToken(String json) {
        String token = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            token = jsonObject.getString("data");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return token;
    }

    public static int getAuthenticationState(String json) {
        int state = 3;
        try {
            JSONObject jsonObject = new JSONObject(json);
            state = jsonObject.getInt("statusData");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return state;
    }

    public static Map<String, String> getUserInfo(String json) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject object = jsonObject.getJSONObject("data");
            map.put("nickName", object.getString("nickName"));
            return map;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getUserPoints(String json) {
        String points = "0";
        try {
            JSONObject jsonObject = new JSONObject(json);
            points = jsonObject.getString("data");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return points;
    }

    public static int getIdentificationState(String json) {
        int state = 0;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String result = jsonObject.getString("data");
            if (!TextUtils.isEmpty(result)) {
                state = Integer.parseInt(result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return state;
    }

    public static String getEncodeJsonText(List<ShopCartItem> list) {
        List<OrderItem> orderList = new ArrayList<>();
        for (ShopCartItem cartItem : list) {
            OrderItem orderItem = new OrderItem();
            orderItem.sid = cartItem.shop.id;
            orderItem.fee = getOrderFreight(cartItem, calculatingOrderPrice(cartItem));
            for (Product product : cartItem.products) {
                OrderProduct orderProduct = new OrderProduct();
                orderProduct.gid = product.id;
                orderProduct.name = product.title;
                orderProduct.num = product.count;
                orderProduct.cid = product.cid;
                orderProduct.cartid = product.shopCardId;

                if (product.cid == 99) {//是增值延保产品
                    Bracket bracket = Util.getDiscount(product);
                    if (bracket == null) {
                        orderProduct.price = product.discountPrice;
                    } else {
                        orderProduct.price = bracket.price;
                    }

                } else {
                    orderProduct.price = product.discountPrice;
                }
                orderItem.list.add(orderProduct);

            }
            orderList.add(orderItem);
        }

        String json = JSON.toJSONString(orderList);
        return Base64.encodeToString(json.getBytes(), Base64.DEFAULT);
    }

    public static String getOderJsonText(List<ShopCartItem> list) {
        StringBuilder sb = new StringBuilder();
        for (ShopCartItem cartItem : list) {
            for (Product product : cartItem.products) {
                sb.append(product.shopCardId + "|" + product.count).append(",");
            }
        }

        String text = sb.toString().substring(0, sb.length() - 1);
        return text;
    }

    /**
     * 拼接优惠劵参数
     *
     * @param list
     * @return
     */
    public static String getCouponJsonText(List<ShopCartItem> list) {
        StringBuilder sb = new StringBuilder();
        String text = "";
        for (ShopCartItem cartItem : list) {
            for (Product product : cartItem.products) {
                if (product.couponPrice != 0) {
                    sb.append(product.shopId + "|" + product.couponCode + "|" + product.couponPrice + "|" + product.goodsCouponId).append(",");
                }
            }
        }
        if (!TextUtils.isEmpty(sb.toString())) {
            text = sb.toString().substring(0, sb.length() - 1);
        }
        return text;
    }

    /**
     * 不包含运费
     *
     * @param cartItem
     * @return
     */
    public static double calculatingOrderPrice(ShopCartItem cartItem) {
        double total = 0.00;
        for (Product product : cartItem.products) {
            if (product.cid == 99) {
                Bracket bracket = Util.getDiscount(product);
                if (bracket == null) {
                    total += MathUtil.mul(product.count, product.discountPrice);
                } else {
                    total += MathUtil.mul(product.count, bracket.price);
                }
            } else {
                total += MathUtil.mul(product.count, product.discountPrice);
            }

        }

        //double值保留 2 位小数,使用银行家舍入法
        return MathUtil.round(total, 2, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * 计算运费
     *
     * @return
     */
    public static double getOrderFreight(ShopCartItem cartItem, double orderPrice) {
        double freight = cartItem.shop.fee;
        if (orderPrice >= cartItem.shop.free_delivery_pirce) {
            freight = 0.0;
        }
        return freight;
    }

    public static int getShopCartTotal(String json) {
        int num = 0;
        try {
            JSONObject jsonObject = new JSONObject(json);
            num = jsonObject.getInt("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return num;
    }

    public static String getResultFromJson(String json) {
        String orderNo = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            orderNo = jsonObject.getString("result");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return orderNo;
    }

    public static String getEncodeJsonTextVAS(List<ShopCartItem> list) {
        List<VASOrderItem> orderList = new ArrayList<>();
        for (ShopCartItem cartItem : list) {
            VASOrderItem orderItem = new VASOrderItem();
            orderItem.sid = cartItem.shop.id;
            for (Product product : cartItem.products) {
                OrderProduct orderProduct = new OrderProduct();
                orderProduct.gid = product.id;
                orderProduct.name = product.title;
                orderProduct.num = product.count;
                orderProduct.cid = product.cid;
                orderProduct.cartid = product.shopCardId;

                if (product.cid == 99) {//是增值延保产品
                    Bracket bracket = Util.getDiscount(product);
                    if (bracket == null) {
                        orderProduct.price = product.discountPrice;
                    } else {
                        orderProduct.price = bracket.price;
                    }

                } else {
                    orderProduct.price = product.discountPrice;
                }
                orderItem.list.add(orderProduct);

            }
            orderList.add(orderItem);
        }

        String json = JSON.toJSONString(orderList);
        return Base64.encodeToString(json.getBytes(), Base64.DEFAULT);
    }
}
