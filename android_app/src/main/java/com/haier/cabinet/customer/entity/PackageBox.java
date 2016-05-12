package com.haier.cabinet.customer.entity;

import java.io.Serializable;

public class PackageBox implements Serializable{
	public static final int ITEM = 0;
	public static final int SECTION = 1;
	public int type = ITEM;
	public String content = "标题测试";
	
	public int boxNo;//箱子编号
	public String cabinetNo;//柜子编号
	public String cabinetName;//柜子名称
	public String cabinetAddress;//柜子地址
	public String pickUpNo;//取件码
	public String expressCompany;//快递公司
	public String deliveredTime;//投递时间
	public String pickTime;//被取走时间
	public String overdueTime;//逾期时间
	public String postmanMobile;//投递员手机
	public String customerMobile;//取件人手机
	public int packageStatus;//包裹状态   0:在箱正常;5.投递员取回;6.逾期回收;7.异常回收;8.已取件9.管理员取回
	public String tradeWaterNo;//流水号
	public String corpType;//厂商标识
	public String packageNo;//订单号、快递单号
	public boolean isTimeout;//是否超时
	public String remainTime;//在箱时长
}  
