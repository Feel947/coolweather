package com.coolweather.app.utli;

public interface HttpCallbackListener {
	void onFinish(String response);
	void onError(Exception e);
}
