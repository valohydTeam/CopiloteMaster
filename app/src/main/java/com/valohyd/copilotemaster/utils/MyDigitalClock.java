/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valohyd.copilotemaster.utils;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

/**
 * Like AnalogClock, but digital. Shows seconds.
 * 
 */
public class MyDigitalClock extends TextView {

	static Calendar mCalendar;
	private final static String m12 = "h:mm:ss aa";
	private final static String m24 = "k:mm:ss";

	// We must keep a reference to this observer
	private FormatChangeObserver mFormatChangeObserver;

	private Runnable mTicker;
	private Handler mHandler;

	private boolean isSystemTime = true;

	private boolean mTickerStopped = false;

	String mFormat;

	public MyDigitalClock(Context context) {
		super(context);
		initClock();
	}

	public MyDigitalClock(Context context, AttributeSet attrs) {
		super(context, attrs);
		initClock();
	}

	private void initClock() {
		if (mCalendar == null) {
			mCalendar = Calendar.getInstance();
		}

		mFormatChangeObserver = new FormatChangeObserver();
		getContext().getContentResolver().registerContentObserver(
				Settings.System.CONTENT_URI, true, mFormatChangeObserver);

		setFormat();
	}

	public void setIsSystemTime(boolean isSystemTime) {
		this.isSystemTime = isSystemTime;
		if (isSystemTime)
			initClock();
	}

	public void setTime(long newTime) {
		isSystemTime = false;

		mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(newTime);

		mFormatChangeObserver = new FormatChangeObserver();
		getContext().getContentResolver().registerContentObserver(
				Settings.System.CONTENT_URI, true, mFormatChangeObserver);

		setFormat();
	}

	public Date getTime() {
		return mCalendar.getTime();
	}

	@Override
	protected void onAttachedToWindow() {
		mTickerStopped = false;
		super.onAttachedToWindow();
		mHandler = new Handler();

		/**
		 * requests a tick on the next hard-second boundary
		 */
		mTicker = new Runnable() {
			public void run() {
				if (mTickerStopped)
					return;
				if (!isSystemTime)
					mCalendar
							.setTimeInMillis(mCalendar.getTimeInMillis() + 1000);
				else
					mCalendar.setTimeInMillis(System.currentTimeMillis());
				setText(DateFormat.format(mFormat, mCalendar));
				invalidate();
				// long now = SystemClock.uptimeMillis();
				// long next = now + (1000 - now % 1000);
				mHandler.postDelayed(mTicker, 1000);
			}
		};
		mTicker.run();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mTickerStopped = true;
	}

	/**
	 * Pulls 12/24 mode from system settings
	 */
	private boolean get24HourMode() {
		return android.text.format.DateFormat.is24HourFormat(getContext());
	}

	private void setFormat() {
		if (get24HourMode()) {
			mFormat = m24;
		} else {
			mFormat = m12;
		}
	}

	private class FormatChangeObserver extends ContentObserver {
		public FormatChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			setFormat();
		}
	}

	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		// noinspection deprecation
		event.setClassName(MyDigitalClock.class.getName());
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		// noinspection deprecation
		info.setClassName(MyDigitalClock.class.getName());
	}
}
