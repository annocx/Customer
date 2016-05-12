package com.haier.cabinet.customer.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * josn解析类
 * Created by jinbiao.wu on 2015/12/4.
 */
public class GsonUtils {
    private static Gson gson = new Gson();

    public static <T> ArrayList<T> jsonToList(String json, Class<T> clz) {
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();

        ArrayList<T> listOfT = new ArrayList<T>();
        try {
            ArrayList<JsonObject> jsonObjs = gson.fromJson(json, type);
            for (JsonObject jsonObj : jsonObjs) {
                listOfT.add(gson.fromJson(jsonObj, clz));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return listOfT;
        }

        return listOfT;
    }

    public static <T> T fromJson(String json, Class<T> clz) {
        return gson.fromJson(json, clz);
    }

    public static <T> T deserialiFromJson(String json, Type type) {
        return gson.fromJson(json,type);
    }

    public static <T> String serializToStr(T t) {
        return gson.toJson(t);
    }
}
