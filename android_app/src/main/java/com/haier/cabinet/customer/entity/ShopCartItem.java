package com.haier.cabinet.customer.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/10.
 */
public class ShopCartItem implements Serializable{
    public Shop shop ;
    public List<Product> products = new ArrayList<Product>();
    public double freight = 0.0;//订单运费
    public double orderTotalPirce;//订单总价
}
