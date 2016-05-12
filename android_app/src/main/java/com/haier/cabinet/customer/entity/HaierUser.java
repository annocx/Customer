package com.haier.cabinet.customer.entity;

public class HaierUser {

	public int id;
	public String name = "";
	public String password;
	public String mobile;
	public int authentication_state = 3;//认证状态  0，申请中；1审批通过；2审批拒绝 3未认证

	public HaierUser(){
		super();
	}

}
