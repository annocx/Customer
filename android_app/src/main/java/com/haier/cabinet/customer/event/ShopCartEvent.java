package com.haier.cabinet.customer.event;

/**
 * Created by Administrator on 2015/12/14.
 */
public class ShopCartEvent {
    private int id;//产品id
    private int shopCartId;//购物车id
    private int count;//产品数量

    public ShopCartEvent(int cardId) {
        this.shopCartId = cardId;
    }

    public ShopCartEvent(int id,int cardId, int count) {
        this.id = id;
        this.shopCartId = cardId;
        this.count = count;
    }

    public int getShopCartId() {
        return shopCartId;
    }
    public int getId() {
        return id;
    }
    public int getCount() {
        return count;
    }
}
