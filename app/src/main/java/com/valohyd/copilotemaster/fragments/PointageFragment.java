package com.valohyd.copilotemaster.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.valohyd.copilotemaster.MainActivity;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.service.PointageService;

/**
 * Classe representant le fragment de pointage
 * 
 * @author parodi
 * 
 */
public class PointageFragment extends Fragment {

	private View mainView;

	private TimePickerDialog dialogPointage, dialogImparti; // Selecteur de
															// temps

	private Button pointageDialogButton, impartiTimeButton;

	private LinearLayout layoutHorizontal, layoutVertical, layout_2,
			layoutRemainingTime;

	private TextView pointageTime, impartiTime, remainingTime, finishTime,
			signRemainingTime;

	private Chronometer elapsedTime; // Temps écoulés hors temps

	private Date pointageDate, impartiDate, finalDate, now; // Heure de
															// pointage,
															// temps imparti et
															// heure d'arrivée

	private SimpleDateFormat minuteFormat = new SimpleDateFormat("HH:mm"); // Formatteur
																			// de
																			// date

	private PointageService servicePointage; // service pointage

	// PREFERENCES
	SharedPreferences sharedPrefs;
	Editor edit;
	private final static String TAG_PREF_POINTAGE = "pointage_time",
			TAG_PREF_IMPARTI = "imparti_time", TAG_PREF_FILE = "pref_file";

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// paysage
			layoutVertical.removeView(layout_2);
			layoutHorizontal.removeView(layout_2);
			layoutHorizontal.addView(layout_2);
			layout_2.setOrientation(LinearLayout.VERTICAL);
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			// portrait
			layoutHorizontal.removeView(layout_2);
			layoutVertical.removeView(layout_2);
			layoutVertical.addView(layout_2);
			layout_2.setOrientation(LinearLayout.HORIZONTAL);
		}

		super.onConfigurationChanged(newConfig);
	}

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mainView = inflater.inflate(R.layout.pointage_layout, container, false);

		now = new Date();

		// PREFERENCES
		sharedPrefs = getActivity().getSharedPreferences(TAG_PREF_FILE,
				Activity.MODE_PRIVATE);
		edit = sharedPrefs.edit();

		// LINEAR LAYOUTS
		layout_2 = (LinearLayout) mainView.findViewById(R.id.layout_pointage_2);
		layoutHorizontal = (LinearLayout) mainView
				.findViewById(R.id.layout_pointage_horizontal);
		layoutVertical = (LinearLayout) mainView
				.findViewById(R.id.layout_pointage_portrait);
		layoutRemainingTime = (LinearLayout) mainView
				.findViewById(R.id.layoutRemainingTime);

		// TEXTVIEWS
		pointageTime = (TextView) mainView.findViewById(R.id.pointageTime);
		impartiTime = (TextView) mainView.findViewById(R.id.impartiTime);
		remainingTime = (TextView) mainView.findViewById(R.id.remainingTime);
		finishTime = (TextView) mainView.findViewById(R.id.finishHour);

		// BUTTONS
		pointageDialogButton = (Button) mainView
				.findViewById(R.id.pointageDialogButton);
		impartiTimeButton = (Button) mainView
				.findViewById(R.id.impartiTimeDialogButton);
		impartiTimeButton.setEnabled(false);

		pointageDialogButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				now = new Date();
				dialogPointage = new TimePickerDialog(getActivity(),
						new OnTimeSetListener() {

							@Override
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {

								// Construction de la date
								Date d = new Date(); // ATTENTION on part de la
														// date
														// actuelle pour avoir
														// deja
														// l'année le mois et le
														// jour de
														// selectionné
								d.setHours(hourOfDay);
								d.setMinutes(minute);
								d.setSeconds(0);
								pointageDate = d; // Heure de pointage créée
								String newString = new SimpleDateFormat("HH:mm")
										.format(pointageDate);
								pointageTime.setText(newString); // On affiche
																	// le retour
																	// pour
																	// l'utilisateur
								if (impartiDate != null)
									setRemainingTime(MainActivity
											.getRallyeDate()); // On affiche le
																// temps
								// restant
								// si le temps imparti
								// est deja
								// rempli
								else
									impartiTimeButton.setEnabled(true); // Sinon
																		// on
																		// active
																		// la
																		// suite

								savePreferences(); // On sauvegarde les
													// preferences pour
													// un retour rapide

							}
						}, now.getHours(), now.getMinutes(), true);
				dialogPointage.setTitle(R.string.pointage_time);
				dialogPointage.show();

			}
		});
		impartiTimeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialogImparti.show();
			}
		});

		// TIMEPICKER DIALOGS

		// TEMPS IMPARTI
		dialogImparti = new TimePickerDialog(getActivity(),
				new OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						try {
							impartiDate = new SimpleDateFormat("HH:mm")
									.parse(hourOfDay + ":" + minute); // Creation
																		// de
																		// la
																		// date
																		// imparti
							String newString = new SimpleDateFormat("HH:mm")
									.format(impartiDate);
							impartiTime.setText(newString);
							setRemainingTime(MainActivity.getRallyeDate()); // On
																			// affiche
																			// le
																			// temps
																			// restant
							savePreferences(); // On sauvegarde
							getActivity().invalidateOptionsMenu(); //on reconstruit les boutons de l'action bar pour le bouton de pointage
						} catch (ParseException e) {
							e.printStackTrace();
						}

					}
				}, 0, 0, true);
		dialogImparti.setTitle(R.string.imparti_time);

		// CHRONOMETER
		elapsedTime = (Chronometer) mainView
				.findViewById(R.id.chronometerElapsed);

		// SIGNE
		signRemainingTime = (TextView) mainView
				.findViewById(R.id.signRemainingTime);

		// CHARGEMENT DES PREFS
		loadPreferences();
		setHasOptionsMenu(true);

		return mainView;
	}

	@Override
	public void onResume() {
		loadPreferences();
		super.onResume();
	}

	public void setService(PointageService service,
			ServiceConnection mConnection) {
		this.servicePointage = service;
		//HACK
		loadPreferences();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem item = menu.findItem(R.id.stop_pointage);
		String imparti = sharedPrefs.getString(TAG_PREF_IMPARTI,
				getActivity().getString(R.string.unknown));
		boolean hasPointage = !imparti.equals(getString(R.string.unknown));
		item.setVisible(hasPointage);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());

				builder.setTitle(R.string.stop_pointage);
				builder.setMessage(R.string.confirm_stop_pointage);
				builder.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (elapsedTime != null) {
									elapsedTime.stop();
									elapsedTime.setVisibility(View.GONE);
								}
								layoutRemainingTime
										.setBackgroundDrawable(getResources()
												.getDrawable(
														R.drawable.vignette_bg));
								finishTime.setText(R.string.unknown);
								impartiTime.setText(R.string.unknown);
								impartiTimeButton.setEnabled(false);
								pointageTime.setText(R.string.unknown);
								remainingTime.setText(R.string.unknown);
								signRemainingTime.setText("");
								remainingTime.setVisibility(View.VISIBLE);
								if (servicePointage != null) {
									servicePointage.stopTout();
								}
								finalDate = null;
								impartiDate = null;
								pointageDate = null;
								signRemainingTime.setText("");
								signRemainingTime
										.setBackgroundColor(getResources()
												.getColor(
														android.R.color.transparent));
								signRemainingTime.setTextColor(getResources()
										.getColor(R.color.black));
								savePreferences();
								getActivity().invalidateOptionsMenu();
							}
						});
				builder.setNegativeButton(android.R.string.no, null);
				builder.show();
				return false;
			}
		});
		item = menu.findItem(R.id.help);
		item.setVisible(true);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Dialog help_dialog = new Dialog(getActivity(),
						android.R.style.Theme_Translucent_NoTitleBar);
				help_dialog.setTitle(getString(R.string.menu_help));
				help_dialog.setContentView(R.layout.help_pointage_layout);
				help_dialog.show();
				return false;
			}
		});
	}

	/**
	 * permet de dire de redessiner le menu
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		getActivity().supportInvalidateOptionsMenu();
	}

	/**
	 * Affiche le temps restant en se basant sur l'heure de pointage et le temps
	 * imparti accordé
	 */
	public void setRemainingTime(Date heureDuRallye) {
		if (pointageDate != null && impartiDate != null) {
			// CONSTRUCTION DE L'HEURE D'ARRIVEE
			Calendar c = new GregorianCalendar();
			c.setTime(new Date(pointageDate.getTime()));
			c.add(Calendar.HOUR, impartiDate.getHours());
			c.add(Calendar.MINUTE, impartiDate.getMinutes());
			finalDate = c.getTime();

			// Affichage de l'heure d'arrivée
			finishTime.setText(minuteFormat.format(finalDate));

			// Calcul des temps
			final long futurems = finalDate.getTime();
			long nowms = heureDuRallye.getTime();
			final long remaining = futurems - nowms;

			// stopper le timer
			if (servicePointage != null) {
				servicePointage.stopCountDownTimer();
			}
			// Si le temps etait dépassé on cache le chrono
			if (elapsedTime.getVisibility() == View.VISIBLE) {
				elapsedTime.setVisibility(View.GONE);
			}

			// On recréer le timer avec le nouveau temps
			// si le service est null, attendre un peu et retester (temps de
			// connexion au service)
			if (servicePointage == null) {
				remainingTime.postDelayed(new Runnable() {

					@Override
					public void run() {
						if (servicePointage != null) {
							servicePointage
									.startCountDownTimer(remaining, 1000);
						}
					}
				}, 250);
			} else {
				servicePointage.startCountDownTimer(remaining, 1000);
			}
		}
	}

	/**
	 * on Tick du remainTimer
	 * 
	 * @param millisUntilFinished
	 */
	public void onTick(long millisUntilFinished) {
		signRemainingTime.setText("-");
		signRemainingTime.setBackgroundColor(getResources().getColor(
				android.R.color.transparent));
		signRemainingTime.setTextColor(getResources().getColor(R.color.black));
		if (millisUntilFinished < 600000) {
			layoutRemainingTime.setBackgroundColor(getResources().getColor(
					R.color.holo_orange_dark));
		} else {
			layoutRemainingTime.setBackgroundDrawable(getResources()
					.getDrawable(R.drawable.vignette_bg));
		}
		remainingTime.setVisibility(View.VISIBLE); // On affiche le
													// timer a chaque
													// tick
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

		remainingTime.setText(hours + ":" + minutes + ":" + secondes);
	}

	/**
	 * Lors de la fin du temps imparti du remainTimer
	 */
	public void onFinish() {
		remainingTime.setVisibility(View.GONE); // On cache le timer
		// On RAZ le chrono
		/*
		 * if (remaining > 0)
		 * elapsedTime.setBase(SystemClock.elapsedRealtime()); else
		 * elapsedTime.setBase(SystemClock.elapsedRealtime() + remaining);
		 */
		elapsedTime.setVisibility(View.VISIBLE); // On affiche le chrono
		signRemainingTime.setText("+");
		signRemainingTime.setBackgroundColor(getResources().getColor(
				R.color.holo_red_light));
		signRemainingTime.setTextColor(getResources().getColor(R.color.white));
		// elapsedTime.start(); // On declenche le chrono du temps
		// supplémentaire
	}

	/**
	 * appelée par le service
	 * 
	 * @param toDisplay
	 */
	public void displayElapsedTime(final String toDisplay) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onFinish();
				elapsedTime.setText(toDisplay);
			}
		});
	}

	/**
	 * Chargement des préférences
	 */
	private void loadPreferences() {
		String pointage = sharedPrefs.getString(TAG_PREF_POINTAGE,
				getActivity().getString(R.string.unknown));
		String imparti = sharedPrefs.getString(TAG_PREF_IMPARTI,
				getActivity().getString(R.string.unknown));
		pointageTime.setText(pointage);
		impartiTime.setText(imparti);

		if (!pointage.equals(getString(R.string.unknown))) {
			try {
				pointageDate = new SimpleDateFormat("HH:mm").parse(pointage);
				Date d = new Date();
				pointageDate.setYear(d.getYear());
				pointageDate.setMonth(d.getMonth());
				pointageDate.setDate(d.getDate());
				Log.d("DATE", pointageDate.toGMTString());
				impartiTimeButton.setEnabled(true);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (!imparti.equals(getString(R.string.unknown))) {
			try {
				impartiDate = new SimpleDateFormat("HH:mm").parse(imparti);
				setRemainingTime(MainActivity.getRallyeDate());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		else{
			if(servicePointage!=null) {
				servicePointage.stopTout();
			}
		}

		getActivity().invalidateOptionsMenu();
	}

	/**
	 * Sauvegarde des préférences
	 */
	private void savePreferences() {
		edit.putString(TAG_PREF_POINTAGE, pointageTime.getText().toString());
		edit.putString(TAG_PREF_IMPARTI, impartiTime.getText().toString());
		edit.commit();
	}
}
