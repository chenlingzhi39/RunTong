package com.callba.phone.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkDetector
{
	/**
	 * 监测网络是否开启
	 * @param act
	 * @return 当前有可用网络 返回 true 反之false
	 */
	public static boolean detect(Context context){
		
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(manager == null){
			return false;
		}
		try {
			NetworkInfo networkInfo = manager.getActiveNetworkInfo();
			if(networkInfo == null || !networkInfo.isAvailable()){
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
