package com.haier.cabinet.customer.entity;

import android.text.TextUtils;

import java.io.Serializable;

import kankan.wheel.widget.adapters.Area;
import kankan.wheel.widget.adapters.City;
import kankan.wheel.widget.adapters.Province;

public class AddressInfo extends Entity{
	
	public String id;
	public String provincialCityArea;//省市区
	public Province province;
	public City city;
	public Area area;
	public String street;
	public String name;
	public String phone;
	public boolean status = false;
	public String getCurCity(){
		if(TextUtils.isEmpty(provincialCityArea)){
			if (null != province && null != city && null != area) {
				provincialCityArea = province.name + city.name + area.name;
				return provincialCityArea;
			} else {
				return "";
			}
		}else {
			return provincialCityArea;
		}

		
	}
	
}
