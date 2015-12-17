package com.coolweather.app.utli;

import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;


public class Utility {

	/**
	 * 处理网络中返回的省级信息
	 *
	 */
	public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB, String response){
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces=response.split(",");
			if(allProvinces!=null&&allProvinces.length>0){
				for (String p : allProvinces) {
					Province province=new Province();
					String[] provinceText=p.split("\\|");
					province.setProvinceName(provinceText[0]);
					province.setProvinceCode(provinceText[1]);
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * 处理网络中返回的市级信息
	 *
	 */
	public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB, String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities=response.split(",");
			if(allCities!=null&&allCities.length>0){
				for (String p : allCities) {
					City city=new City();
					String[] cityText=p.split("\\|");
					city.setCityName(cityText[0]);
					city.setCityCode(cityText[1]);
					city.setProvinceId(provinceId);
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * 处理网络中返回的县级信息
	 *
	 */
	public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB, String response, int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounties=response.split(",");
			if(allCounties!=null&&allCounties.length>0){
				for (String p : allCounties) {
					County county=new County();
					String[] countyText=p.split("\\|");
					county.setCountyName(countyText[0]);
					county.setCountyCode(countyText[1]);
					county.setCityId(cityId);
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
