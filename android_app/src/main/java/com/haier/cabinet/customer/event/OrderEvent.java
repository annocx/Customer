package com.haier.cabinet.customer.event;

/**
 * Created by Administrator on 2015/12/14.
 */
public class OrderEvent {

    private int position;
    private int type = 1;//1支付成功 2 评论完成

    public OrderEvent(int position, int type)
    {
        this.position = position;
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public int getType() {
        return type;
    }
}
