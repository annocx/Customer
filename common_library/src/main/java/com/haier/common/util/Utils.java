package com.haier.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class Utils {

	// 用share preference来实现是否绑定的开关。在ionBind且成功时设置true，unBind且成功时设置false
	public static boolean hasBind(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		String flag = sp.getString("bind_flag", "");
		if ("ok".equalsIgnoreCase(flag)) {
			return true;
		}
		return false;
	}

	public static void setBind(Context context, boolean flag) {
		String flagStr = "not";
		if (flag) {
			flagStr = "ok";
		}
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString("bind_flag", flagStr);
		editor.commit();
	}

	public static List<String> getTagsList(String originalText) {
		if (originalText == null || originalText.equals("")) {
			return null;
		}
		List<String> tags = new ArrayList<String>();
		int indexOfComma = originalText.indexOf(',');
		String tag;
		while (indexOfComma != -1) {
			tag = originalText.substring(0, indexOfComma);
			tags.add(tag);

			originalText = originalText.substring(indexOfComma + 1);
			indexOfComma = originalText.indexOf(',');
		}

		tags.add(originalText);
		return tags;
	}

	public static String getLogText(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sp.getString("log_text", "");
	}

	public static void setLogText(Context context, String text) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString("log_text", text);
		editor.commit();
	}

	// 将数据存储进入共享参数
	public static boolean saveMsg(Context context, String fileName,
			Map<String, Object> map) {
		boolean flag = false;
		// 一般Mode都使用private,比较安全
		SharedPreferences preferences = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		// Map类提供了一个称为entrySet()的方法，这个方法返回一个Map.Entry实例化后的对象集。
		// 接着，Map.Entry类提供了一个getKey()方法和一个getValue()方法，
		// 因此，上面的代码可以被组织得更符合逻辑
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object object = entry.getValue();
			// 根据值得不同类型，添加
			if (object instanceof Boolean) {
				Boolean new_name = (Boolean) object;
				editor.putBoolean(key, new_name);
			} else if (object instanceof Integer) {
				Integer integer = (Integer) object;
				editor.putInt(key, integer);
			} else if (object instanceof Float) {
				Float f = (Float) object;
				editor.putFloat(key, f);
			} else if (object instanceof Long) {
				Long l = (Long) object;
				editor.putLong(key, l);
			} else if (object instanceof String) {
				String s = (String) object;
				editor.putString(key, s);
			}
		}
		flag = editor.commit();
		return flag;

	}

	// 读取数据
	public static Map<String, ?> getMsg(Context context, String fileName) {
		Map<String, ?> map = null;
		// 读取数据用不到edit
		SharedPreferences preferences = context.getSharedPreferences(fileName,
				Context.MODE_APPEND);
		// Context.MODE_APPEND可以对已存在的值进行修改
		map = preferences.getAll();
		return map;
	}

	public static byte[] getBytes(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			baos.write(buffer, 0, len);

		}

		return baos.toByteArray();
	}

	/*
	 * MD5加密
	 */
	public static String getMD5Text(String text) {
		if (TextUtils.isEmpty(text)) {
			return null;
		}
		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance("MD5");

			messageDigest.reset();

			messageDigest.update(text.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException caught!");
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		// 16位加密，从第9位到25位
		/*return md5StrBuff.substring(8, 24).toString().toLowerCase();*/
		//32位加密
		return md5StrBuff.toString().toLowerCase();
	}
	
	/**
	 * 获取当前应用程序的版本号
	 */
	public static String getVersion(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo packinfo = pm.getPackageInfo(context.getPackageName(), 0);
			String version = packinfo.versionName;
			return "V " + version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 获取当前应用程序的版本号
	 */
	public static String getVersionNumber(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo packinfo = pm.getPackageInfo(context.getPackageName(), 0);
			String version = packinfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getDateText(Long time){
		SimpleDateFormat format = new SimpleDateFormat( "MM-dd HH:mm", Locale.CHINA);
		String date = format.format(time);
		/*String date = new java.text.SimpleDateFormat("MM-dd HH:mm:ss").format(new java.util.Date(time));*/
		return date;
	}
	
	//http://203.130.41.104:8060/guizi-manager-diaochan/down_load.jsp?terminalNo=00176D01&corpType=hzdcdz&boxNo=16&arm=1
	//http://203.130.41.104:8060/guizi-manager-diaochan/down_load.jsp?terminalNo=00176D01&corpType=hzdcdz&arm=1
	public static Map<String , String> getQRCodeContent(String result){
		Map<String , String> map = new HashMap<String , String>();   
		if (!TextUtils.isEmpty(result) && result.contains("=")) {
			String[] array = result.split("=");
			if (array.length == 5) {
				map.put("terminalNo", array[1].substring(0, array[1].indexOf("&")));
				map.put("corpType", array[2].substring(0, array[2].indexOf("&")));
				map.put("boxNo", array[3].substring(0, array[3].indexOf("&")));
				map.put("arm", array[4]);
				
			} else if (array.length == 4) {
				map.put("terminalNo", array[1].substring(0, array[1].indexOf("&")));
				map.put("corpType", array[2].substring(0, array[2].indexOf("&")));
				if (result.contains("randomCode")) {
					map.put("randomCode", array[3]);
				} else if (result.contains("arm"))  {
					map.put("arm", array[3]);
				}
			}
			
		}
		return map;
	}
	
	/**
	 * 验证手机格式
	 */
	public static boolean isMobileNO(String mobiles) {
		/*
		移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		联通：130、131、132、152、155、156、185、186
		电信：133、153、180、189、（1349卫通）
		总结起来就是第一位必定为1，第二位必定为3或5或8或7（电信运营商），其他位置的可以为0-9
		*/
		String telRegex = "[1][3578]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		if (TextUtils.isEmpty(mobiles))
			return false;
		else
			return mobiles.matches(telRegex);
    }
	
	public static long getOvertime(String endTiem){
		try {
			
			SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			java.util.Date end = dfs.parse(endTiem);
			java.util.Date now = new Date();
			long between = (now.getTime() - end.getTime()) / 1000;// 除以1000是为了转换成秒
			long day = between / (24 * 3600);
			return day;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void call(Context context, String number){
		 //用intent启动拨打电话  
       Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+number));  
       context.startActivity(intent);
	}
	
	static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;  
    public static String bd_encrypt(double gg_lat, double gg_lon)  
    {  
        double x = gg_lon, y = gg_lat, bd_lon, bd_lat;  
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);  
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);  
        bd_lon = z * Math.cos(theta) + 0.0065;  
        bd_lat = z * Math.sin(theta) + 0.006;  
        String location = bd_lon + "," + bd_lat;
        return location;
    }  
}
