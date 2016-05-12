package com.haier.cabinet.customer.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.haier.cabinet.customer.entity.HaierUser;
import com.haier.common.util.Utils;

import android.content.Context;

public class FileService {

	private Context mContext;
	
	public FileService(Context context) {
		this.mContext = context;
	}
	
	public boolean saveUserInfo2Rom(HaierUser user, String fileName) throws IOException{
		//以私有的方式打开一个文件
		FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
		String result = user.mobile+":"+user.password;
		fos.write(result.getBytes());
		fos.flush();
		fos.close();
		return false;
		
	}
	
	public Map<String,String> getUserInfo(String fileName) throws IOException{
		//以上的两句代码也可以通过以下的代码实现：
		FileInputStream fis = mContext.openFileInput(fileName);
		byte[] data = Utils.getBytes(fis);
		String content = new String(data);
		String results[] = content.split(":");
		Map<String,String> map = new HashMap<String,String>();
		map.put("username", results[0]);
		map.put("password", results[1]);
		return map;
	}
	
}
