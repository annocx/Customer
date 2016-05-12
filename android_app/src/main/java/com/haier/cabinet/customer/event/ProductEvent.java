package com.haier.cabinet.customer.event;

/**
 * Created by Administrator on 2015/12/14.
 */
public class ProductEvent {

    private int id;//产品id

    private int count;//产品数量

    public ProductEvent(int id,int count)
    {
        this.id = id;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public int getId() {
        return id;
    }
}
