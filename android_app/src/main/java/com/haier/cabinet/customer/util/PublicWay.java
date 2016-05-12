package com.haier.cabinet.customer.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 存放所有的list在最后退出时一起关闭
 *
 * @author jinbiao.wu
 */
public class PublicWay {
	public static List<Activity> activityList = new ArrayList<Activity>();
	
	public static int num = 3;
	
}
