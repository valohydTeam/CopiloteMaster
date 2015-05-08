package com.valohyd.copilotemaster.utils;

/*
 * The Android chronometer widget revised so as to count milliseconds
 */

import java.text.DecimalFormat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

public class Chronometer extends TextView {

	/**
	 * A callback that notifies when the chronometer has incremented on its own.
	 */
	public interface OnChronometerTickListener {

		/**
		 * Notification that the chronometer has changed.
		 */
		void onChronometerTick(Chronometer chronometer);

	}

	private long mBase;
	private boolean mVisible;
	private boolean mStarted;
	private boolean mRunning;
	private String mFormat;
	private StringBuilder mFormatBuilder;
	private OnChronometerTickListener mOnChronometerTickListener;

	private static final int TICK_WHAT = 2;

	/**
	 * Initialize this Chronometer object. Sets the base to the current time.
	 */
	public Chronometer(Context context) {
		this(context, null, 0);
	}

	/**
	 * Initialize with standard view layout information. Sets the base to the
	 * current time.
	 */
	public Chronometer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Initialize with standard view layout information and style. Sets the base
	 * to the current time.
	 */
	public Chronometer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	private void init() {
		mBase = SystemClock.elapsedRealtime();
		updateText(mBase);
	}

	/**
	 * Set the time that the count-up timer is in reference to.
	 * 
	 * @param base
	 *            Use the {@link SystemClock#elapsedRealtime} time base.
	 */
	public void setBase(long base) {
		mBase = base;
		dispatchChronometerTick();
		updateText(SystemClock.elapsedRealtime());
	}

	/**
	 * Return the base time as set through {@link #setBase}.
	 */
	public long getBase() {
		return mBase;
	}

	/**
	 * Sets the format string used for display. The Chronometer will display
	 * this string, with the first "%s" replaced by the current timer value in
	 * "MM:SS" or "H:MM:SS" form.
	 * 
	 * If the format string is null, or if you never call setFormat(), the
	 * Chronometer will simply display the timer value in "MM:SS" or "H:MM:SS"
	 * form.
	 * 
	 * @param format
	 *            the format string.
	 */
	public void setFormat(String format) {
		mFormat = format;
		if (format != null && mFormatBuilder == null) {
			mFormatBuilder = new StringBuilder(format.length() * 3);
		}
	}

	/**
	 * Returns the current format string as set through {@link #setFormat}.
	 */
	public String getFormat() {
		return mFormat;
	}

	/**
	 * Sets the listener to be called when the chronometer changes.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void setOnChronometerTickListener(OnChronometerTickListener listener) {
		mOnChronometerTickListener = listener;
	}

	/**
	 * @return The listener (may be null) that is listening for chronometer
	 *         change events.
	 */
	public OnChronometerTickListener getOnChronometerTickListener() {
		return mOnChronometerTickListener;
	}

	/**
	 * Start counting up. This does not affect the base as set from
	 * {@link #setBase}, just the view display.
	 * 
	 * Chronometer works by regularly scheduling messages to the handler, even
	 * when the Widget is not visible. To make sure resource leaks do not occur,
	 * the user should make sure that each start() call has a reciprocal call to
	 * {@link #stop}.
	 */
	public void start() {
		mStarted = true;
		updateRunning();
	}

	/**
	 * Stop counting up. This does not affect the base as set from
	 * {@link #setBase}, just the view display.
	 * 
	 * This stops the messages to the handler, effectively releasing resources
	 * that would be held as the chronometer is running, via {@link #start}.
	 */
	public void stop() {
		mStarted = false;
		updateRunning();
	}

	/**
	 * The same as calling {@link #start} or {@link #stop}.
	 * 
	 * @hide pending API council approval
	 */
	public void setStarted(boolean started) {
		mStarted = started;
		updateRunning();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mVisible = false;
		updateRunning();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		mVisible = visibility == VISIBLE;
		updateRunning();
	}

	private synchronized void updateText(long now) {
		long timeElapsed = now - mBase;

		DecimalFormat df = new DecimalFormat("00");

		int hours = (int) (timeElapsed / (3600 * 1000));
		int remaining = (int) (timeElapsed % (3600 * 1000));

		int minutes = (int) (remaining / (60 * 1000));
		remaining = (int) (remaining % (60 * 1000));

		int seconds = (int) (remaining / 1000);
		remaining = (int) (remaining % (1000));

		int milliseconds = (int) (((int) timeElapsed % 1000) / 100);

		String text = "";

		if (hours > 0) {
			text += df.format(hours) + ":";
		}

		text += df.format(minutes) + ":";
		text += df.format(seconds) + ":";
		text += Integer.toString(milliseconds);
		setText(text);
	}

	private void updateRunning() {
		boolean running = mVisible && mStarted;
		if (running != mRunning) {
			if (running) {
				updateText(SystemClock.elapsedRealtime());
				dispatchChronometerTick();
				mHandler.sendMessageDelayed(
						Message.obtain(mHandler, TICK_WHAT), 100);
			} else {
				mHandler.removeMessages(TICK_WHAT);
			}
			mRunning = running;
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message m) {
			if (mRunning) {
				updateText(SystemClock.elapsedRealtime());
				dispatchChronometerTick();
				sendMessageDelayed(Message.obtain(this, TICK_WHAT), 100);
			}
		}
	};

	void dispatchChronometerTick() {
		if (mOnChronometerTickListener != null) {
			mOnChronometerTickListener.onChronometerTick(this);
		}
	}
}