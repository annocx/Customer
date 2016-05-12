package com.haier.cabinet.customer.entity;

import java.io.Serializable;

/**
 * Created by SK on 2016/2/22.
 */
public class Coupon implements Serializable {

    public String coupon_id;

    public String coupon_sn;

    public String coupon_member_id;

    public String member_phone;

    public String coupon_export_time;

    public String coupon_receive_time;

    public String coupon_use_time;

    public String coupon_order_id;

    public String coupon_good_id;

    public String template_Code;

    public String coupon_Show_Summary;

    public String discountShowName;

    public String couponShowName;

    public String discount;

    public String discount_Level;

    public String discount_Type;

    public String status;

    public String sponsor;

    public String showStatus;

    public String create_Time;

    public String useFromTime;

    public String getUseFromTime() {
        return useFromTime;
    }

    public void setUseFromTime(String useFromTime) {
        this.useFromTime = useFromTime;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getDiscount() {
        return discount == null ? "0.0" : discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDiscount_Level() {
        return discount_Level;
    }

    public void setDiscount_Level(String discount_Level) {
        this.discount_Level = discount_Level;
    }

    public String getDiscount_Type() {
        return discount_Type;
    }

    public void setDiscount_Type(String discount_Type) {
        this.discount_Type = discount_Type;
    }

    public String getCoupon_Show_Summary() {
        return coupon_Show_Summary;
    }

    public void setCoupon_Show_Summary(String coupon_Show_Summary) {
        this.coupon_Show_Summary = coupon_Show_Summary;
    }

    public String getDiscountShowName() {
        return discountShowName;
    }

    public void setDiscountShowName(String discountShowName) {
        this.discountShowName = discountShowName;
    }

    public String getCouponShowName() {
        return couponShowName;
    }

    public void setCouponShowName(String couponShowName) {
        this.couponShowName = couponShowName;
    }

    public String getTemplate_Code() {
        return template_Code;
    }

    public void setTemplate_Code(String template_Code) {
        this.template_Code = template_Code;
    }

    public String getShowStatus() {
        return showStatus;
    }

    public void setShowStatus(String showStatus) {
        this.showStatus = showStatus;
    }

    public String getCoupon_id() {
        return coupon_id;
    }

    public void setCoupon_id(String coupon_id) {
        this.coupon_id = coupon_id;
    }

    public String getCoupon_sn() {
        return coupon_sn;
    }

    public void setCoupon_sn(String coupon_sn) {
        this.coupon_sn = coupon_sn;
    }

    public String getCoupon_member_id() {
        return coupon_member_id;
    }

    public void setCoupon_member_id(String coupon_member_id) {
        this.coupon_member_id = coupon_member_id;
    }

    public String getMember_phone() {
        return member_phone;
    }

    public void setMember_phone(String member_phone) {
        this.member_phone = member_phone;
    }

    public String getCoupon_export_time() {
        return coupon_export_time;
    }

    public void setCoupon_export_time(String coupon_export_time) {
        this.coupon_export_time = coupon_export_time;
    }

    public String getCoupon_receive_time() {
        return coupon_receive_time;
    }

    public void setCoupon_receive_time(String coupon_receive_time) {
        this.coupon_receive_time = coupon_receive_time;
    }


    public String getCoupon_use_time() {
        return coupon_use_time;
    }

    public void setCoupon_use_time(String coupon_use_time) {
        this.coupon_use_time = coupon_use_time;
    }

    public String getCoupon_order_id() {
        return coupon_order_id;
    }

    public void setCoupon_order_id(String coupon_order_id) {
        this.coupon_order_id = coupon_order_id;
    }

    public String getCoupon_good_id() {
        return coupon_good_id == null ? "0" : coupon_good_id;
    }

    public void setCoupon_good_id(String coupon_good_id) {
        this.coupon_good_id = coupon_good_id;
    }

}
