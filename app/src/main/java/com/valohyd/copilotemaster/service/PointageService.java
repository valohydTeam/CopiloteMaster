package com.valohyd.copilotemaster.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.valohyd.copilotemaster.MainActivity;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.fragments.PointageFragment;

/**
 * 
 * @author Valentin Lecavelier
 * 
 */
public class PointageService extends Service {
	private final IBinder mBinder = new MyBinder(); // binder pour accéder au
													// service
	private CountDownTimer remainTimer; // Temps restants dans le temps imparti
	private PointageFragment hook; // hook pour intéragir avec le fragment

	// WAKE LOCK
	PowerManager pm;
	PowerManager.WakeLock wakelock;

	// notification manager
	private static final int NOTIFICATION_ID = 42;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder notifBuilder;
	private Notification notif;
	private Intent intent; // intent lors
							// du click
							// sur notif
	private PendingIntent pIntent;

	private long seconds;
	private Timer timer; // timer for the past time
	private boolean useNotif = true;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"PointageService");
		wakelock.acquire();
		return START_NOT_STICKY;
	}

	public void setUseNotif(boolean useNotif) {
		this.useNotif = useNotif;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		notifBuilder = new NotificationCompat.Builder(this)
				.setLargeIcon(
						BitmapFactory.decodeResource(getResources(),
								R.drawable.ic_launcher))
				.setContentTitle(getString(R.string.pointage_title_notif))
				.setContentText(getString(R.string.back_to_app))
				.setOngoing(true).setContentIntent(pIntent);

		notif = notifBuilder.build();

		startForeground(NOTIFICATION_ID, notif);
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	public CountDownTimer getRemainTimer() {
		return remainTimer;
	}

	public void setHook(PointageFragment hook) {
		this.hook = hook;
	}

	/**
	 * start le remainTimer et appelle le onTick et onFinish de PointageFragment
	 * 
	 * @param millisInFuture
	 * @param countDownInterval
	 */
	public void startCountDownTimer(final long millisInFuture,
			long countDownInterval) {
		// stop the other timer
		stopCountDownTimer();
		stopPastTimer();

		notifBuilder.setContentTitle(getString(R.string.pointage_title_notif));

		// start the new
		remainTimer = new CountDownTimer(millisInFuture, countDownInterval) {

			@Override
			public void onTick(long millisUntilFinished) {
				if (hook != null) {
					hook.onTick(millisUntilFinished);
				}
				// Decoupage du temps restant pour l'affichage
				long sec = (millisUntilFinished / 1000) % 60;
				String secondes = "" + sec;
				if (sec < 10) {
					secondes = "0" + sec;
				}
				long min = (millisUntilFinished / (1000 * 60)) % 60;
				String minutes = "" + min;
				if (min < 10) {
					minutes = "0" + min;
				}
				long hrs = (millisUntilFinished / (1000 * 60 * 60)) % 24;
				String hours = "" + hrs;
				if (hrs < 10) {
					hours = "0" + hrs;
				}

				if (useNotif) {
					if (millisUntilFinished < 600000) {
						notifBuilder
								.setSmallIcon(android.R.drawable.presence_away);
					} else {
						notifBuilder
								.setSmallIcon(android.R.drawable.presence_online);
					}
					notifBuilder
							.setProgress(
									(int) millisInFuture,
									(int) (millisInFuture - millisUntilFinished),
									false);
					notifBuilder.setContentText("temps : -" + hours + ":"
							+ minutes + ":" + secondes);
					notif = notifBuilder.build();
					notificationManager.notify(NOTIFICATION_ID,
							notifBuilder.build());
				}
			}

			@Override
			public void onFinish() {
				if (hook != null) {
					hook.onFinish();
				}
				// start the past timer
				startPastTimer(millisInFuture);
			}
		}.start();
	}

	/**
	 * stop le count down timer
	 */
	public void stopCountDownTimer() {
		// Si le timer est deja actif
		if (remainTimer != null)
			remainTimer.cancel(); // On le stoppe
	}

	public void startPastTimer(long millisSecondes) {

		// démarrer le timer au temps passé à 0
		if (millisSecondes < 0) {
			seconds = -millisSecondes / 1000;
		} else
			seconds = 0;

		// arrete le timer si il y en a dejà un de lancé
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		// lancer un nouveau timer
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				seconds++;
				// Decoupage du temps écoulé pour l'affichage
				long sec = seconds % 60;
				String secondes = "" + sec;
				if (sec < 10) {
					secondes = "0" + sec;
				}
				long min = (seconds / 60) % 60;
				String minutes = "" + min;
				if (min < 10) {
					minutes = "0" + min;
				}
				long hrs = (seconds / 3600) % 24;
				String hours = "" + hrs;
				if (hrs < 10) {
					hours = "0" + hrs;
				}

				if (useNotif) {
					// notification : temps dépassé et panneau attention
					// enlever la progress bar
					notifBuilder.setProgress(0, 0, false);
					notifBuilder
							.setSmallIcon(android.R.drawable.stat_notify_error);
					notifBuilder.setContentText("ATTENTION ! Temps : +" + hours
							+ ":" + minutes + ":" + secondes);
					notificationManager.notify(NOTIFICATION_ID,
							notifBuilder.build());
				}
				if (hook != null) {
					hook.displayElapsedTime(hours + ":" + minutes + ":"
							+ secondes);

				}
			}
		}, 1000, 1000);
	}

	public void stopPastTimer() {
		// arrete le timer si il y en a dejà un de lancé
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	public void stopTout() {
		stopForeground(true);
		stopCountDownTimer();
		stopPastTimer();
		notificationManager.cancel(NOTIFICATION_ID);
		if (wakelock.isHeld())
			wakelock.release();
	}

	/**
	 * classe du binder, pour obtenir le service
	 * 
	 * @author Valentin Lecavelier
	 * 
	 */
	public class MyBinder extends Binder {
		public PointageService getService() {
			return PointageService.this;
		}
	}
}
