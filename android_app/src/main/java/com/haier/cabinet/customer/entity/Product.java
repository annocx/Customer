package com.haier.cabinet.customer.entity;

import com.haier.cabinet.customer.activity.adapter.HotsGridAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Product implements Serializable{
	public int id;
	public int shopCardId = 0;
	public int shopId;
	public String shopName;
	public double freight;//店铺运费
	public double free_delivery_pirce;//包邮价
	public String shopLogo;//店铺logo
	public String shopLevel;//店铺等级

	//评论
	public int gevalId;//用户id
	public String geval_tel;//用户手机号
	public String geval_content;//解释内容
	public String geval_image;//晒单图片
	public long geval_addtime;//时间

	public String title;
	public boolean storage_state;//库存状态
	public int goods_storage;//商品库存
	public String goods_percent;//好评率
	public String goods_salenum;//销售数量
	public double retailPrice;//市场价
	public double discountPrice;//折扣价
	public double productUnit;//商品单位
	public String productDescription;//产品介绍
	public String spec;//规格
	public String madein;//产地
	public String serviceArea;//服务区域
	public String bannerImgUrl;//广告图
	public String thumbUrl;//图片缩略图
	public String imgUrl;//大图片路径,详情页的大图
	public String detailsUrl;//详情页url
	public String phone;//卖家电话
	public int count = 0;//购物车中的数量
	public boolean isChecked = false;
	public int pay_state;//是否可以下单，为0不能下单

	public int cid = 0;// 当cid为99是增值延保产品
	public int ladder_total;//阶梯价档数
	public int minNumber = 0;//最小购买数量
	public List<Bracket> bracketList = new ArrayList<>();

	public String have_gift;//是否拥有赠品
	public String xianshi_info;//限时折扣
	public String groupbuy_info;//限量信息
	public String mansong_infol;//满即送
//	public String boutique;//是否精品
	public double couponPrice;//优惠劵金额
	public int couponId;//优惠劵id
	public String couponCode;//优惠劵code
	public String goodsCouponId;//可使用优惠劵的商品id数组

}
