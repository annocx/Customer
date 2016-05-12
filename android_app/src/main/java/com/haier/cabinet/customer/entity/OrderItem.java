package com.haier.cabinet.customer.entity;

import java.util.ArrayList;
import java.util.List;

public class OrderItem {
	public int sid;
	public double fee;
	public List<OrderProduct> list = new ArrayList<>();
}
