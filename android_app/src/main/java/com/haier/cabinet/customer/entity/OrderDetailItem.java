package com.haier.cabinet.customer.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/10.
 */
public class OrderDetailItem implements Serializable{
    public Shop shop ;
    public List<OrderProduct2> products = new ArrayList<OrderProduct2>();
}
