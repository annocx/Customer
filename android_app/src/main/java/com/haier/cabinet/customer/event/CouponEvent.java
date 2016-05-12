package com.haier.cabinet.customer.event;

/**
 * Created by Administrator on 2016/2/19.
 */
public class CouponEvent {
    public int getResult() {
        return result;
    }

    public CouponEvent(int result) {
        this.result = result;
    }

    private int result;//1：成功 0：失败

}
