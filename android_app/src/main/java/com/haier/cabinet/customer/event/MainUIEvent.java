package com.haier.cabinet.customer.event;

/**
 * Created by Administrator on 2015/12/15.
 */
public class MainUIEvent {
    private int position = 0;//索引

    public MainUIEvent(int index)
    {
        this.position = index;
    }

    public int getPosition() {
        return position;
    }
}
