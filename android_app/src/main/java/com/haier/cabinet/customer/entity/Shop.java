package com.haier.cabinet.customer.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/10/10.
 */
public class Shop implements Serializable{
    public int id;
    public String name;
    public double fee = 0.0;//运费
    public double free_delivery_pirce;//包邮价
    public String imgUrl;//广告图

    public int getId() {
        return id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public double getFee() {
        return fee;
    }

    public String getImgUrl() {
        return imgUrl == null ? "" : imgUrl;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

}
