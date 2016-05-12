package com.haier.cabinet.customer.entity;

public class Express {

	public static final int ITEM = 0;
	public static final int SECTION = 1;
	public static final int POSTMAN = 0;
	public static final int COMPANY = 1;
	public int property = POSTMAN;
	public int type = ITEM;
	public String content = "标题测试";
	
	public String name; //快递公司名称
	public String username; //投递员姓名
	public String phone; 
	public int icon_resId;
}
