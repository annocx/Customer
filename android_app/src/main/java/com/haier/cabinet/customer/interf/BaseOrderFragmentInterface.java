package com.haier.cabinet.customer.interf;


import com.haier.cabinet.customer.entity.Order;

import java.util.List;

/**
 * 基类fragment实现接口
 * @author lzx
 *
 */
public interface BaseOrderFragmentInterface {
	List<Order> getListByJosn(String json);
	String getRequestUrl(boolean isStart);
}
