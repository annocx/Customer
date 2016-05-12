package com.haier.cabinet.customer.entity;

import java.io.Serializable;


@SuppressWarnings("serial")
public class DeliveryBox implements Serializable{

	public int boxNo;
	public String trackingNo;//快递单号
	public String deliveredTime;//投递时间
	public String customerMobile;//取件人手机
	public int packageStatus;//包裹状态
	public String tradeWaterNo;//流水号
}
