package com.haier.cabinet.customer.entity;

import java.io.Serializable;

/**
 * 增值延保分档
 * Created by Administrator on 2015/9/22.
 */
public class Bracket implements Serializable{
    public int num;
    public double price;

    public Bracket(int num,double price){
        this.num = num;
        this.price = price;
    }

}
