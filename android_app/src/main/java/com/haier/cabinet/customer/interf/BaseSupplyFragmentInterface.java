package com.haier.cabinet.customer.interf;


import com.haier.cabinet.customer.entity.Supply;

import java.util.List;

/**
 * 基类fragment实现接口
 * @author lzx
 *
 */
public interface BaseSupplyFragmentInterface {
	List<Supply> getListByJosn(String json);
	String getRequestUrl(boolean isStart);
}
