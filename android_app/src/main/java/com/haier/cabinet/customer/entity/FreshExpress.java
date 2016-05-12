package com.haier.cabinet.customer.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/16.
 */
public class FreshExpress {
    public static final int ITEM = 0;
    public static final int SECTION = 1;

    public static final int CATEGORY_PRODUCT = 0;
    public static final int CATEGORY_SHOP = 1;
    public int type = ITEM;
    public String content = "标题测试";

    public int category_type = CATEGORY_PRODUCT;

    public int id;//商品id
    public int shopCardId;
    public String title;
    public double retailPrice;//市场价
    public double discountPrice;//折扣价
    public double productUnit;//商品单位
    public String productDescription;//产品介绍
    public String spec;//规格
    public String madein;//产地
    public String thumbUrl;//图片缩略图
    public String imgUrl;//大图片路径,详情页的大图
    public String detailsUrl;//详情页url
    public String phone;//卖家电话
    public int count;//总数
    public boolean isChecked = true;
    public int product_count = 0;
    public int cid = 0;// 当cid为99是增值延保产品

    //店铺
    public int shopId;
    public String shopName;
    public double freight;//店铺运费
    public double free_delivery_pirce;//包邮价
    public String serviceArea;//服务区域
}
