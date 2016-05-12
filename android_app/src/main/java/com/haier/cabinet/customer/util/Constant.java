package com.haier.cabinet.customer.util;

public class Constant {

    public static final String SKEYT = "Baidu.xWei@2014";

    /**
     * 百度推送
     * API_KEY
     */
    public final static String API_KEY = "E3TurU8VSXaLwW5wGs8KDjdw";
    /**
     * SECRET_KEY
     */
    public final static String SECRIT_KEY = "pXShHQ0U7GIukHsLB56lcSk25oKhAFpG";
    public static final String CABINET_FILE_NAME = "cabinet_sp";


    public static final String COUPON_IP = "http://www.api2.rrslj.com";//优惠劵正式接口
//    public static final String COUPON_IP = "http://203.130.41.108:9000";//优惠劵测试接口
    public static final String IP = "http://203.130.41.104:8050";
    //public static final String IP = "http://203.130.41.108:8011";
    public static final String DOMAIN = IP + "/guizi-app-jiqimao/haier";

    public static final String SHOP_DOMAIN = "http://testapi.rrslj.com"; //商品列表正式服务器
//    public static final String SHOP_DOMAIN = "http://203.130.41.108:8083";//商品列表测试服务器

    public static final String URL_POINTS_EXPLANATION = "file:///android_asset/integral/Integral.html";
    public static final String URL_ABOUT_US = "file:///android_asset/about/About.html";
    public static final String URL_PRIVACY_POLICY = "file:///android_asset/Agreement.html";
    public static final String URL_COUPON_RULES = SHOP_DOMAIN + "/h5/useRule.html";

    public static final String URL_ORDER_COUNT = SHOP_DOMAIN + "/appapi/index.php?act=member_order&op=ordercount";

    public static final String URL_QUESTIONNAIRE = IP
            + "/guizi-app-jiqimao/userSurveyController/toSurveyPage.action";
    public static final String URL_PUSH_CHANNELID = DOMAIN
//    public static final String URL_PUSH_CHANNELID = "http://203.130.41.108:8011/guizi-app-jiqimao/haier"
            + "/user/updateChannelId.json";
    public static final String URL_SHARE_WEIXIN = DOMAIN
            + "/userIntegral/weChatIntegral.json";
    public static final String URL_SHARE_QQ = DOMAIN
            + "/userIntegral/qqZoneIntegral.json";
    public static final String URL_SHARE_QQ_INVITATION = DOMAIN
            + "/userIntegral/qqInvitationIntegral.json";
    public static final String URL_SHARE_WEIXIN_INVITATION = DOMAIN
            + "/userIntegral/weChatInvitationIntegral.json";
    public static final String URL_USER_POINTS = DOMAIN
            + "/userIntegral/getUserIntegral.json";
    public static final String URL_USER_UN_PICKUP = DOMAIN
            + "/order/getNoTakePackageCount.json";
    public static final String URL_OTHER_PEOPLE_FETCHING_EXPRESS = DOMAIN
            + "/order/otherPersonPickup.json";
    public static final String URL_SHARE_FETCHING_EXPRESS = IP
            + "/guizi-app-jiqimao/appOrderController/otherPersonPick.action?tradeWaterNo=";
    public static final String URL_OPEN_PACKAGEBOX_ARM = DOMAIN + "/order/armCustomerOpenBox.json";
    public static final String URL_OPEN_PACKAGEBOX = DOMAIN + "/order/customerOpenBox.json";
    public static final String URL_NEARBY_POSTMAN = DOMAIN + "/user/findHighQualityCourierAround.json";
    public static final String URL_EVALUATION_SERVICE = IP + "/guizi-app-jiqimao/userSurveyController/createUserSurvey.action";
    public static final String URL_IDENTITY_AUTHENTICATION = IP + "/guizi-app-jiqimao/userSurveyController/applySellerValidatePage.action";
    public static final String URL_GET_IDENTITY_AUTHENTICATION_STATE = DOMAIN + "/user/getUserValidateStatus.json";
    public static final String URL_REFUSE_TO_USE = IP + "/guizi-app-jiqimao/userSurveyController/createBlackUserPage.action";
    public static final String URL_INVITE_FRIENDS = SHOP_DOMAIN + "/h5/Activity-invite.html";
    public static final String URL_INVITE_CODE = SHOP_DOMAIN + "/h5/Activity-inviteCode.html";


    //   ------------------------------- 优惠券 -------------------------------   //
    public static final String URL_COUPON = COUPON_IP + "/promotion/coupon";

    public static final String CHANGE_ADDRESS = "change_address";
    public static final int REQUEST_BRAND = 2001;
    public static final int REQUEST_SUB_BRAND = 2002;
    public static final int REQUEST_PRODUCT_TYPE = 2003;
    public static final int REQUEST_PRODUCT = 2004;
    public static final int REQUEST_SERVICE_TYPE = 2005;
    public static final int REQUEST_CHANGE_ADDRESS = 2006;
    public static final int REQUEST_FAULT_DESCRIPTION = 2007;
    public static final int REQUEST_COUPON_PIRCE = 2008;

    public static final int GET_VERIFY_CODE = 60;
    public static final int PERIOD_VERIFY_CODE = 300000;

    public static final String GLADEYEKEY = "rO0ABXQAD1YawoQ8w5h2QsK8ekYrOA==";

    public static final int USER_TYPE = 4;
    public static final int PHONE_TYPE = 3;
    public static final int HOME_FRAGMENT_INDEX = 0;
    public static final int CATEGOGR_FRAGMENT_INDEX = 1;
    public static final int SHOPC_CART_FRAGMENT_INDEX = 2;
    public static final int USER_FRAGMENT_INDEX = 3;

    /*activity显示fragment*/
    public static final String FRAGMENT_DETAILS = "fragment_details";
    public static final int FRAGMENT_MAIL = 1;
    public static final int FRAGMENT_LIFE = 2;

    // 微信平台
    public static final String weixin_appID = "wxbddadd14346c44bc";
    public static final String weixin_appSecret = "ea19c1f7524041f585323378c36eca97";
    // QQ平台
    public static final String qq_appID = "1104495665";
    public static final String qq_appSecret = "yOq9XEOWtaKH54iT";

    /**
     * 支付宝
     */
    // 商户PID
    public static final String PARTNER = "2088546501843907";
    // 商户收款账号
    public static final String SELLER = "lisan@rrslj.com";
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALI/C9+TId30ttgK/kmG8kM63zHsulC8X2vN6fhWX0Wp09mvs8B1G7EGNicXZrzKpBEmUyxV5Hc60Xnyl/+BhfKeBvRFEKx5oeEL/b0qKgqnY0CpkxTD75CaxlryBi0Fdaa6QIDEHLU/WPiyPewv0jc/EqS+88r9BKn1ZnhpscSRAgMBAAECgYADwNgbKRn5doGzIugf3DuVttOkVXvG33CS8I8Qr6Dp8p2HY+/BCbY/kAXr5M5BA3NY+RmfQV/CMvbePoF829laLX1+YtuWDD/sYx6SI5KZPqgWz3yrLP1kJRsiik7TmKCiJoKhig9rVI++FC61JiROfxYNU1CWPerr7I8c44xMUQJBAORodUnXABW++S8GXviQUjGlUkkEJZxgp3+M/yDaY1s+rzpj634FBW0GxSftxHPEQ/WUmu/+DuwE3yiAfRuwY9MCQQDHx1Se2Qqoh4Sb3GvlCN+N+z9WRDzCiYGARgntHVYCeTvsy4HaUgyUKaRXtZDx89xllnLsSUuOL66zGyOfaIuLAkBHntGYU8h9CSMNscu52VdMpfBFYP2CKXScNFQTsycQh3ler9I5880dwM+1k4LUYiiFKxHiSyHWtDhddNzF/+ttAkEAsVJR37SR60huAdHBysb3dpR9gVqL+7gFRA/mm5ogT43ADMwNc/TDyXa0sk0sXDWxeFHzq5ra/1d/XuZF1iX7RQJAWQlLto6VTD9RdiQK+xcHHguK8tZhsFLsax6wtjzSsvGf+Sx+TofU3/DZvQqbDYy4Knc63ZeWnFfgxFGZH1F3tQ==";
    // 支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";


    /**
     * 微信支付
     */
    // appid
    // 请同时修改 androidmanifest.xml里面，.PayActivityd里的属性<data
    // android:scheme="wxb4ba3c02aa476ea1"/>为新设置的appid 见weixin_appID

    // 商户号
    public static final String MCH_ID = "126058796561";

    // API密钥，在商户平台设置
    public static final String API_KEY_WEIXIN_PAY = "C73ADA2695E81D675190DAEB9456DA48";

	public static final String URL_EXPRESS_MAIL_COUNT = DOMAIN + "/order/getUnpickedOrderNumberBycustomer.json";

    public static final String INTENT_ACTION_SHOP_CART_TOTAL_CHANGE = "com.haier.cabinet.customer.action.SHOPCART_TOTAL_CHANGE";
    public static final String INTENT_ACTION_PAY_SUCCESS = "com.haier.cabinet.customer.action.PAY_SUCCESS";//订单支付成功
    public static final String INTENT_ACTION_PAY_FAILED = "com.haier.cabinet.customer.action.PAY_FAILED";//订单支付失败
    public static final String URL_ALIPAY_PAY_NOTIFY = "http://203.130.41.107/newnotify/alipaynotifymsg";//支付宝支付回调通知
    public static final String URL_WEIXIN_PAY_NOTIFY = "http://203.130.41.107/newnotify/notifymsg";//微信支付回调通知

    public static final String URL_USER_ADDRESS_LIST = SHOP_DOMAIN + "/appapi/index.php?act=member_address&op=address_list";
    public static final String URL_GET_USER_DEFAULT_ADDRESS = SHOP_DOMAIN + "/appapi/index.php?act=member_address&op=address_default";
    public static final String URL_SET_USER_DEFAULT_ADDRESS = SHOP_DOMAIN + "/address/setdefault";
    public static final String URL_ADD_USER_ADDRESS = SHOP_DOMAIN + "/appapi/index.php?act=member_address&op=address_add";
    //public static final String URL_DELETE_USER_ADDRESS_ = SHOP_DOMAIN + "/address/delete";
    public static final String URL_DELETE_USER_ADDRESS_ = SHOP_DOMAIN + "/appapi/index.php?act=member_address&op=address_del";
    public static final String URL_MODIFY_USER_ADDRESS_ = SHOP_DOMAIN + "/appapi/index.php?act=member_address&op=address_edit";

    public static final String URL_GET_ORDER_FREIGHT = SHOP_DOMAIN + "/appapi/index.php?act=member_buy&op=get_order_freight";
    public static final String URL_SUMIT_ORDER = SHOP_DOMAIN + "/appapi/index.php?act=member_buy&op=buy_step2&v=2015";

    public static final String URL_INCREMENT_PRODUCT_ADD = "http://203.130.41.107/order/addorder";
    public static final String URL_SHOPPING_CART_LIST = SHOP_DOMAIN + "/appapi/index.php?act=member_cart&op=cart_list&v=3";
    public static final String URL_SHOPPING_CART_ADD = SHOP_DOMAIN + "/appapi/index.php?act=member_cart&op=cart_add";
    public static final String URL_SHOPPING_CART_MODIFY = SHOP_DOMAIN + "/appapi/index.php?act=member_cart&op=cart_edit_quantity";
    public static final String URL_SHOPPING_CART_DELETE = SHOP_DOMAIN + "/appapi/index.php?act=member_cart&op=cart_del";
    public static final String URL_SHOPPING_CART_COUNT = SHOP_DOMAIN + "/appapi/index.php?act=member_cart&op=cart_total";

    public static final String URL_PAY_ORDER_SUCCESS = SHOP_DOMAIN + "/order/orderpay";
    public static final String URL_EXPRESS_QUERY = "http://m.kuaidi100.com/result.jsp";

    public static final String URL_HOME_TEST = "http://203.130.41.108:8083/appapi/index.php?act=activity&op=list&v=3";
    public static final String URL_HOME = SHOP_DOMAIN + "/appapi/index.php?act=activity&op=list&v=3";
    //public static final String URL_HOME = SHOP_DOMAIN + "/appapi/index.php?act=activity&op=list&v=2";
    public static final String URL_COMMENT_LIST = SHOP_DOMAIN + "/appapi/index.php?act=evaluate&op=goods_evaluate";//评论列表
    //    public static final String URL_COMMENT_LIST = "http://203.130.41.108:8083/appapi/index.php?act=evaluate&op=goods_evaluate";//评论列表测试
    public static final String URL_COMMENT_ADD = SHOP_DOMAIN + "/appapi/index.php?act=evaluate&op=add_evaluate";//添加评论
    public static final String URL_COMMENT_IMAGE_ADD = "http://203.130.41.108:8083/appapi/index.php?act=evaluate&op=uploadimg";//评论上传照片
    public static final String URL_SHOP_LIST = SHOP_DOMAIN + "/appapi/index.php?act=goods&op=goods_list";//商品列表
    public static final String URL_SHOP_PRODUCT = SHOP_DOMAIN + "/appapi/index.php?act=goods&op=goods_detail";//商品详情
    public static final String URL_HOT_SEARCH = SHOP_DOMAIN + "/appapi/index.php?act=goods&op=hot_search";//热门搜索
    public static final String URL_LOGISTICS = SHOP_DOMAIN + "/appapi/index.php?act=member_order&op=search_deliver";//物流查询

    // 订单详细信息
    public static final String INTENT_KEY_ORDER_DETAIL = "order_detail";
    // 订单id
    public static final String INTENT_KEY_ORDER_ID = "order_id";
    // 支付签名
    public static final String INTENT_KEY_PAY_SN = "pay_sn";
    // 订单编号
    public static final String INTENT_KEY_ORDER_SN = "order_sn";

    //  订单状态  0(已取消)10(默认):未付款;20:已付款;30:已发货;40:已收货;
    public static final int ORDER_STATE_CANCEL = 0;

    public static final int ORDER_STATE_NEW = 10;

    public static final int ORDER_STATE_PAY = 20;

    public static final int ORDER_STATE_SEND = 30;

    public static final int ORDER_STATE_SUCCESS = 40;

    //  返回值  有数据 1001; 无数据 1002; 异常/报错 2001;
    public static final int GET_LIST_DATA = 1001;

    public static final int UPDATE_LIST = 1002;

    public static final int NO_LIST_DATA = 1003;

    public static final int GET_LIST_DATA_FAILURE = 1005;

    public static final int NO_MORE_LIST_DATA = 1006;

    public static final int GO_TO_PAY = 1007;

    // 中国七大区
    public static final int PRODUCER_NORTHEAST = 1;

    public static final int PRODUCER_NORTH = 2;

    public static final int PRODUCER_EAST = 3;

    public static final int PRODUCER_NORTHWEST = 4;

    public static final int PRODUCER_SOUTH = 5;

    public static final int PRODUCER_SOUTHWEST = 6;

    public static final int PRODUCER_CENTRAL = 7;

    //下单付款方式
    public static final int PAY_FROM_SHOPCART = 0;//购物车直接下单付款
    public static final int PAY_FROM_ORDER = 1;//订单中下单付款

    //分享机会次数接口
    public static final String URL_INCREASE = URL_COUPON + "/increase_qualification";

    // ---------------  优惠券  --------------- //
    // 商品ID
    public static final String INTENT_KEY_GOODS_ID = "order_goods_id";
    // 优惠券唯一标识Code
    public static final String INTENT_KEY_COUPON_CODE = "coupon_code";
    // 优惠券金额
    public static final String INTENT_KEY_COUPON_DISCOUNT = "coupon_discount";

    // 从哪个界面进入优惠券
    public static final String INTENT_KEY_FROM = "to_coupons";

    public static String URL_ONLINE_SERVICE_PRODUCT = SHOP_DOMAIN + "/h5/consultation-commodity.html";
    public static String URL_ONLINE_SERVICE_ORDER = SHOP_DOMAIN + "/h5/consultation-order.html";

    public static int DISPLAY_ADDRESS_LIST = 1;
    public static int CHOOSE_ADDRESS_LIST = 2;

}
