package com.haier.cabinet.customer.event;

import com.haier.cabinet.customer.entity.AddressInfo;

/**
 * Created by SK on 2016/3/3.
 */
public class AddressInfoEvent {

    public AddressInfoEvent(AddressInfo addressInfo) {
        this.addressInfo = addressInfo;
    }

    public AddressInfo addressInfo;

}
