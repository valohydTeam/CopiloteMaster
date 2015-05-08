package com.valohyd.copilotemaster.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

public class NetworkUtils {

	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(
				context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	
	public static boolean isWifiConnected(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ConnectivityManager.TYPE_WIFI==ni.getType())
	            return ni.isConnected();
	    }
	    return false;
	}
	
	public static boolean networkConnectionAvailable(Context context) {
	    boolean wifiAvailable = false;
	    boolean mobileAvailable = false;

	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ConnectivityManager.TYPE_WIFI==ni.getType())
	            if (ni.isConnected())
	                wifiAvailable = true;
	        if (ConnectivityManager.TYPE_MOBILE==ni.getType())
	            if (ni.isConnected())
	                mobileAvailable = true;
	    }
	    return wifiAvailable || mobileAvailable;
	}
	
//	public static TpgException detectNetworkProblem(Context context) {
//		TpgException exception = null;
//		
//		if(!NetworkUtils.networkConnectionAvailable(context)) {
//			if(NetworkUtils.isAirplaneModeOn(context))
//				exception = new NetworkUtils.AirplaneModeException(context, null);
//			else if(!NetworkUtils.isWifiConnected(context))
//				exception = new NetworkUtils.NoConnectionException(context, null);
//		}
//		
//		return exception;
//	}
//	
//	@SuppressWarnings("serial")
//	public static class AirplaneModeException extends TpgException {
//
//		public AirplaneModeException(Context context,Exception exception) {
//			super(context,exception);
//			setSettingIntent(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
//			setErrorTitle(R.string.error_network_airplane_title);
//			setErrorMessage(R.string.error_network_airplane_message);
//		}
//	}
//	
//	@SuppressWarnings("serial")
//	public static class NoConnectionException extends TpgException {
//		
//		public NoConnectionException(Context context,Exception exception) {
//			super(context,exception);
//			setSettingIntent(new Intent(Settings.ACTION_WIFI_SETTINGS));
//			setErrorTitle(R.string.error_network_connection_title);
//			setErrorMessage(R.string.error_network_connection_message);
//		}
//	}
	
}