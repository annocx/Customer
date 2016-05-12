package com.haier.cabinet.customer.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间类转换
 * Created by Administrator on 2015/12/15.
 */

public class DateUtil {

    private static  SimpleDateFormat sf = null;

    /**
     * 将时间戳转换为时间格式
     * @param time
     * @return
     */
    public static String getDateToString(long time) {
        Date d = new Date(time * 1000L);
        sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sf.format(d);
    }

    public static String getYYMMDD(String dateStr) {
        Date date = null;
        try {
            sf = new SimpleDateFormat("yyyy-MM-dd");
            date = sf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sf.format(date);
    }
}
