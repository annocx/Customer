package com.haier.cabinet.customer.entity;

import java.io.Serializable;

/**
 * 实体基类：实现序列化
 * Created by lizhixian on 16/2/27.
 */
@SuppressWarnings("serial")
public abstract class Entity  implements Serializable {
    public int id;
}
