package com.haier.cabinet.customer.entity;

public class PushMsg {
	public String username;
	public String title;
	public String content;
	public String msg_type;//01：快递员投递 02：快递员取回 03：包裹超期
	public String isRead;
	public String createTime;
	public String proId;//订单id

}
