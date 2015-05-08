package com.valohyd.copilotemaster.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * Tools for Analytics Tracking
 * 
 * @author LPARODI
 * 
 */
public class AnalyticsManager {

	private static String PARAMETER_SEPARATOR = "-";

	// Pages
	public static final String KEY_PAGE_HOME = "Pointage";
	public static final String KEY_PAGE_MAP = "Map";
	public static final String KEY_PAGE_CHRONO = "Chrono";
	public static final String KEY_PAGE_FFSA = "FFSA";
	public static final String KEY_PAGE_METEO = "Meteo";
	public static final String KEY_PAGE_CONTACTS = "Contacts";

	private static final boolean ACTIVITY_TRACKING_ENABLED = true;

	/**
	 * Start tracking for Activity
	 */
	public static void start(Activity activity) {
//		if (ACTIVITY_TRACKING_ENABLED) {
//			EasyTracker.getInstance().activityStart(activity);
//			Log.d("ANALYTICS", "Started with ID : "
//					+ EasyTracker.getTracker().getTrackingId());
//		}
	}

	/**
	 * Stop tracking for Activity
	 */
	public static void stop(Activity activity) {
//		if (ACTIVITY_TRACKING_ENABLED) {
//			EasyTracker.getInstance().activityStop(activity);
//			Log.d("ANALYTICS", "Stopped");
//		}
	}

	/**
	 * Send pending hits
	 */
	public static void dispatch() {

//		EasyTracker.getInstance().dispatch();

	}

	/**
	 * Track application screen
	 */
	public static void trackScreen(Context context, String screenName) {

//		Log.d("ANALYTICS", "Trackpage : " + screenName);
//		EasyTracker.getInstance().setContext(context);
//		EasyTracker.getTracker().trackView(screenName);

	}

	/**
	 * Report an event
	 */
	public static void reportEvent(Context context, String eventName) {
		//doReportEvent(context, eventName, null);
	}

	public static void reportEvent(Context context, String eventName,
			String[] eventValues) {
//		String paramsConcat = "";
//		if (eventValues != null) {
//			// Concatenate parameters
//			for (int i = 0; i < eventValues.length; i++) {
//				paramsConcat += eventValues[i]
//						+ ((i + 1 == eventValues.length) ? ""
//								: PARAMETER_SEPARATOR);
//			}
//		}
//		doReportEvent(context, eventName, paramsConcat);
	}

	private static void doReportEvent(Context context, String eventName,
			String eventValue) {
		//EasyTracker.getInstance().setContext(context);
		// EasyTracker.getInstance().trackEvent("ui_action", "button_press",
		// "play_button", opt_value);
		//EasyTracker.getTracker().trackEvent(eventName, eventValue, null,
//				(long) 0);

	}

}
