package com.haier.cabinet.customer.entity;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

import kankan.wheel.widget.adapters.Area;
import kankan.wheel.widget.adapters.City;
import kankan.wheel.widget.adapters.Province;

/**
 * 物流参数
 */
public class LogisticsInfo implements Serializable{
	
	public String express_name;//物流公司

	public String shipping_code;//订单号

	public String express_status_name;//物流状态

	public String goods_image;//商品图片

}
