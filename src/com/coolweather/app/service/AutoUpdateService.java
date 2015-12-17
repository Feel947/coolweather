package com.coolweather.app.service;

import com.coolweather.app.utli.HttpCallbackListener;
import com.coolweather.app.utli.HttpUtil;
import com.coolweather.app.utli.Utility;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateWeather();	
			}
		});
		return super.onStartCommand(intent, flags, startId);
	}

	protected void updateWeather() {
		SharedPreferences prfs=PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode=prfs.getString("weather_code", "");
		String address="http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}
			
			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}
		});
	}

}
