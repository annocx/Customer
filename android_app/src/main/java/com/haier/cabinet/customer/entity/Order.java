package com.haier.cabinet.customer.entity;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {

	private String orderId;															//  订单ID

	private String orderSn;                                                        //  订单编号

	private String paySn;

	private String orderState;

	private String payState;                                                        // 付款状态 0无效

	private String storeName;

	private String storeId;

	private String buyerName;

	private String reciverName;

	private String finishTime;

	private String phone;

	private String address;

	private String shippingFee;

	private String shippingCode;

	private String orderAmount;

	private String couponUse;								//  是否使用优惠券  0 未使用；1 使用

	private String couponDiscount;                         //  优惠金额

	private String evaluationState;

	private List<OrderProduct2> dataList;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderState() {
		return orderState == null || orderState.equals("null") ? "-1" : orderState;
	}

	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}

	public String getPayState() {
		return payState == null || payState.equals("null") ? "-1" : payState;
	}

	public void setPayState(String payState) {
		this.payState = payState;
	}

	public String getOrderSn() {
		return orderSn;
	}

	public void setOrderSn(String orderSn) {
		this.orderSn = orderSn;
	}

	public String getPaySn() {
		return paySn;
	}

	public void setPaySn(String paySn) {
		this.paySn = paySn;
	}

	public String getAddress() {
		return address == null ? "" : address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone == null ? "" : phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getBuyerName() {
		return buyerName;
	}

	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getShippingFee() {
		return shippingFee == null || shippingFee.equals("null") ? "0.00" : shippingFee;
	}

	public void setShippingFee(String shippingFee) {
		this.shippingFee = shippingFee;
	}

	public String getShippingCode() {
		return shippingCode == null || shippingCode.equals("null") ? "" : shippingCode;
	}

	public void setShippingCode(String shippingCode) {
		this.shippingCode = shippingCode;
	}

	public String getEvaluationState() {
		return evaluationState;
	}

	public void setEvaluationState(String evaluationState) {
		this.evaluationState = evaluationState;
	}

	public String getOrderAmount() {
		return orderAmount == null || orderAmount.equals("null") ? "0.00" : orderAmount;
	}

	public void setOrderAmount(String orderAmount) {
		this.orderAmount = orderAmount;
	}

	public List<OrderProduct2> getDataList() {
		return dataList;
	}

	public void setDataList(List<OrderProduct2> dataList) {
		this.dataList = dataList;
	}

	public String getReciverName() {
		return reciverName;
	}

	public void setReciverName(String reciverName) {
		this.reciverName = reciverName;
	}

	public String getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}

	public String getCouponUse() {
		return couponUse == null ? "0" : couponUse;
	}

	public void setCouponUse(String couponUse) {
		this.couponUse = couponUse;
	}

	public String getCouponDiscount() {
		return couponDiscount == null ? "0.0" : couponDiscount;
	}

	public void setCouponDiscount(String couponDiscount) {
		this.couponDiscount = couponDiscount;
	}

}
