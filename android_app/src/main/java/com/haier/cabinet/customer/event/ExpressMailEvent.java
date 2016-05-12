package com.haier.cabinet.customer.event;

/**
 * Created by SK on 2016/3/29.
 */
public class ExpressMailEvent {

    private int total = 0;

    public ExpressMailEvent(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }
}
