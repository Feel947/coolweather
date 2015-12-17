package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.utli.HttpCallbackListener;
import com.coolweather.app.utli.HttpUtil;
import com.coolweather.app.utli.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int PROVINCE_LEVEL=0;
	public static final int CITY_LEVEL=1;
	public static final int COUNTY_LEVEL=2;
	private TextView titleText;
	private ListView listView;
	private CoolWeatherDB coolWeatherDB;
	private ArrayAdapter<String> arrayAdapter;
	private ProgressDialog progressDialog;
	private List<String> dataList=new ArrayList<String>();
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	private int currentLevel;
	private boolean isFromWeatherActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prfs=PreferenceManager.getDefaultSharedPreferences(this);
		if(prfs.getBoolean("city_selected", false)&&!isFromWeatherActivity){
			Intent intent=new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		titleText=(TextView) findViewById(R.id.title_text);
		listView=(ListView) findViewById(R.id.list_view);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		arrayAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentLevel==PROVINCE_LEVEL){
					selectedProvince=provinceList.get(position);
					queryCities();
				}else if(currentLevel==CITY_LEVEL){
					selectedCity=cityList.get(position);
					queryCounties();
				}else if(currentLevel==COUNTY_LEVEL){
					String countyCode=selectedCounty.getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
			
		});
		queryProvinces();
	}
	private void queryProvinces() {
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			arrayAdapter.notifyDataSetChanged();
			titleText.setText("中国");
			listView.setSelection(0);
			currentLevel=PROVINCE_LEVEL;
		}else{
			queryFromServer(null,"province");
		}
	}
	
	protected void queryCities() {
		cityList=coolWeatherDB.loadCites(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			arrayAdapter.notifyDataSetChanged();
			titleText.setText("中国:"+selectedProvince.getProvinceName());
			listView.setSelection(0);
			currentLevel=CITY_LEVEL;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	protected void queryCounties() {
		countyList=coolWeatherDB.loadCountes(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			arrayAdapter.notifyDataSetChanged();
			titleText.setText("中国:"+selectedProvince.getProvinceName()+":"+selectedCity.getCityName());
			listView.setSelection(0);
			currentLevel=COUNTY_LEVEL;
		}else{
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	private void queryFromServer(final String code,final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city" + code + ".xml";
		}else{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvinceResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result=Utility.handleCityResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountyResponse(coolWeatherDB, response, selectedCity.getId());
				}
				
				if(result){
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
							
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						clossProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载错误", 0).show();
					}

				});
			}
		});
	}
	private void showProgressDialog() {
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载。。。。。");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	private void clossProgressDialog() {
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
		
	}
	
	@Override
	public void onBackPressed() {
		if(currentLevel==CITY_LEVEL){
			queryProvinces();
		}else if(currentLevel==COUNTY_LEVEL){
			queryCities();
		}else{
			if(isFromWeatherActivity){
				Intent intent=new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
