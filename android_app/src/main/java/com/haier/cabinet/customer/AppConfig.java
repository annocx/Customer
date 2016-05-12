package com.haier.cabinet.customer;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Created by lzx on 15/9/12.
 */
public class AppConfig {
    private final static String APP_CONFIG = "app_config";//文件名

    private Context mContext;
    private static AppConfig appConfig;

    String sdpath = Environment.getExternalStorageDirectory() + "/app_config";

    public static AppConfig getAppConfig(Context context) {
        if (null == appConfig) {
            appConfig = new AppConfig();
            appConfig.mContext = context;
        }
        return appConfig;
    }

    public Properties getProperties() {
        FileInputStream fis = null;
        Properties properties = new Properties();
        //File dirFile = new File(sdpath);
        try {
            // 读取app_config目录下的config
            //fis = new FileInputStream(dirFile.getPath() + "/"+APP_CONFIG);
            fis = mContext.openFileInput(APP_CONFIG);
            properties.load(fis);
            /*Enumeration en = properties.propertyNames();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String Property = properties.getProperty (key);
                Log.e("lzx", "key " + key + "  value = " + Property);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }

        }

        return properties;
    }

    /**
     * 将Properties写入Properties文件
     *
     * @param properties
     */
    private void setProperties(Properties properties) {
        FileOutputStream fos = null;
        try {
            /*
            // 把config建在(自定义)app_config的目录下
            File dirFile =  new File(sdpath);
            // 判断文件目录是否存在
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            File configFile = new File(sdpath, APP_CONFIG);
            fos = new FileOutputStream(configFile, true);*/
            fos = mContext.openFileOutput(APP_CONFIG, Context.MODE_PRIVATE);
            properties.store(fos, null);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 保存属性值
     *
     * @param props
     */
    public void setProps(Properties props) {
        Properties properties = getProperties();
        properties.putAll(props);
        setProperties(props);
    }

    public String get(String key) {
        Properties properties = getProperties();
        return (properties != null) ? properties.getProperty(key) : null;
    }

    public void set(String key, String value) {
        Properties properties = getProperties();
        properties.setProperty(key, value);
        setProperties(properties);
    }

    /**
     * 删除配置
     *
     * @param keys
     */
    public void remove(String... keys) {
        Properties properties = getProperties();
        for (String key : keys) {
            properties.remove(key);
        }
        setProperties(properties);
    }


}
