package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.utli.HttpCallbackListener;
import com.coolweather.app.utli.HttpUtil;
import com.coolweather.app.utli.Utility;

public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout WeatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView currentDataText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private Button switchCity;
	private Button refreshWeather;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		WeatherInfoLayout=(LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText=(TextView) findViewById(R.id.city_name);
		publishText=(TextView) findViewById(R.id.publish_text);
		currentDataText=(TextView) findViewById(R.id.current_data);
		weatherDespText=(TextView) findViewById(R.id.weather_desp);
		temp1Text=(TextView) findViewById(R.id.temp1);
		temp2Text=(TextView) findViewById(R.id.temp2);
		switchCity=(Button) findViewById(R.id.switch_city);
		refreshWeather=(Button) findViewById(R.id.refresh_weather);
		String countyCode=getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode)){
			publishText.setText("同步中。。。。");
			WeatherInfoLayout.setVisibility(View.VISIBLE);
			cityNameText.setVisibility(View.VISIBLE);
			queryWeaterToCode(countyCode);
		}else{
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中。。。。。。。。。");
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode=prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherToInfo(weatherCode);
			}
			break;

		default:
			break;
		}
		
	}
	private void queryWeaterToCode(String countyCode) {
		String address="http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address,"countyCode");
	}
	private void queryWeatherToInfo(String weatherCode) {
		String address="http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address,"weatherCode");
	}
	private void queryFromServer(final String address,final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if("countyCode".equals(type)){
					if(TextUtils.isEmpty(response)){
						String[] array=response.split("\\|");
						if(array!=null&&array.length==2){
							String weatherCode=array[0];
							queryWeatherToInfo(weatherCode);
						}
					}
				}else if("weatherCode".equals(type)){
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						publishText.setText("同步失败。");						
					}
				});
				
			}
		});
		
	}
	private void showWeather() {
		SharedPreferences prfs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prfs.getString("city_name", ""));
		publishText.setText("今天 "+prfs.getString("publish_time","")+" 发布");
		currentDataText.setText(prfs.getString("current_data", ""));
		weatherDespText.setText(prfs.getString("weather_desp", ""));
		temp1Text.setText(prfs.getString("temp1", ""));
		temp2Text.setText(prfs.getString("temp2", ""));
		WeatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent=new Intent(this, AutoUpdateService.class);
		startActivity(intent);
	}
}
