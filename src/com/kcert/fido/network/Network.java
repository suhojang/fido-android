package com.kcert.fido.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {
	public static final int	NETWORK_STATUS_DISABLE	= -1;
	public static final int	NETWORK_STATUS_WIFI		= 1;
	public static final int	NETWORK_STATUS_MOBILE	= 2;
	
	public static int getNetworkType(Context context){
		ConnectivityManager cm	= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = cm.getActiveNetworkInfo();
		
		if(activeNetInfo==null || !activeNetInfo.isAvailable() || !activeNetInfo.isConnected()){
			return NETWORK_STATUS_DISABLE;
		}
		int netType		= activeNetInfo.getType();
		
		if (netType == ConnectivityManager.TYPE_WIFI) {
			return NETWORK_STATUS_WIFI;
		}else{
			return NETWORK_STATUS_MOBILE;
		}
	}
}
