package com.valohyd.copilotemaster;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.WindowDecorActionBar;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuItemImpl;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.valohyd.copilotemaster.fragments.ChronoFragment;
import com.valohyd.copilotemaster.fragments.ContactFragment;
import com.valohyd.copilotemaster.fragments.MeteoFragment;
import com.valohyd.copilotemaster.fragments.NavigationFragment;
import com.valohyd.copilotemaster.fragments.PointageFragment;
import com.valohyd.copilotemaster.fragments.TimeFragment;
import com.valohyd.copilotemaster.service.PointageService;
import com.valohyd.copilotemaster.utils.AnalyticsManager;
import com.valohyd.copilotemaster.utils.MyDigitalClock;



public class MainActivity extends AppCompatActivity implements ActionBar.TabListener {

	public static boolean mMapIsTouched = false;

	// FRAGMENTS
	PointageFragment pointageFragment;
	ChronoFragment chronoFragment;
	TimeFragment timeFragment;
	NavigationFragment mapFragment;
	ContactFragment contactFragment;
	MeteoFragment meteoFragment;

	// SERVICES
	private PointageService servicePointage;
	// service connection : pour se connecter au service
	private ServiceConnection mConnection;
	private boolean isServiceBounded = false;

	// Views
	private static MyDigitalClock digitalClock;
	private TimePicker timePicker;
	private CheckBox isCheckSystemTime;

	// PREFS
	private long offset = 0;
	boolean useNotif = true;
	SharedPreferences sharedPrefs;
	Editor edit;
	private final String TAG_PREF_FILE = "pref_file",
			TAG_PREF_HOUR = "offset_time";

	private boolean doubleback = false;

	private static Date newDate = null; // Nouvelle date saisie

	/**
	 * Lancement du Tracker Google Analytics
	 */
	@Override
	public void onStart() {
		super.onStart();
		AnalyticsManager.start(this);
	}

	/**
	 * Fermeture du Tracker Google Analytics
	 */
	@Override
	public void onStop() {
		super.onStop();

		AnalyticsManager.stop(this);
	}

	/**
	 * Retourne l'heure affichée dans l'appli
	 * 
	 * @return heure du rallye
	 */
	public static Date getRallyeDate() {
		return digitalClock.getTime();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// PREFS
		sharedPrefs = getSharedPreferences(TAG_PREF_FILE, Activity.MODE_PRIVATE);
		edit = sharedPrefs.edit();

		loadPreferences();

		digitalClock = (MyDigitalClock) findViewById(R.id.digitalClock);

		digitalClock.setTime((new Date().getTime()) - offset);

		digitalClock.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Date now = new Date();
				newDate = null;

				AlertDialog.Builder d = new AlertDialog.Builder(
						MainActivity.this);
				d.setTitle("Changer l'heure du rallye");
				View change_view = LayoutInflater.from(MainActivity.this)
						.inflate(R.layout.change_time_layout, null);

				timePicker = (TimePicker) change_view
						.findViewById(R.id.timePicker);
				timePicker.setIs24HourView(true);
				timePicker.setCurrentHour(now.getHours());
				timePicker.setCurrentMinute(now.getMinutes());

				isCheckSystemTime = (CheckBox) change_view
						.findViewById(R.id.checkboxSysTime);

				timePicker
						.setOnTimeChangedListener(new OnTimeChangedListener() {

							@Override
							public void onTimeChanged(TimePicker view,
									int hourOfDay, int minute) {
								// Construction de la date
								newDate = new Date();
								newDate.setHours(hourOfDay);
								newDate.setMinutes(minute);
								newDate.setSeconds(0);
							}
						});
				d.setPositiveButton(getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								if (!isCheckSystemTime.isChecked()
										&& newDate == null) {
									now.setSeconds(0);
									digitalClock.setTime(now.getTime());
									pointageFragment.setRemainingTime(now);
									savePreferences(now.getTime());
								} else if (!isCheckSystemTime.isChecked()
										&& newDate != null) {
									digitalClock.setTime(newDate.getTime());
									pointageFragment.setRemainingTime(newDate);
									savePreferences(newDate.getTime());
								} else {
									digitalClock.setIsSystemTime(true);
									pointageFragment
											.setRemainingTime(new Date());
									savePreferences(new Date().getTime());
								}

							}
						});
				d.setNeutralButton(getString(android.R.string.cancel), null);
				d.setView(change_view);
				d.show();
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		createTabs(Configuration.ORIENTATION_PORTRAIT);

		// check if the app has already been opened
		if (!sharedPrefs.getBoolean("opened", false)) {
			// open the dialog of the first use
			final Dialog d = new Dialog(this,
					android.R.style.Theme_Translucent_NoTitleBar);
			d.setTitle(getString(R.string.help_first_user_title));
			d.setContentView(R.layout.help_first_use);
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.copyFrom(d.getWindow().getAttributes());
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = WindowManager.LayoutParams.MATCH_PARENT;
			d.getWindow().setAttributes(lp);
			d.show();

			// set the onclick du bouton (show the next help)
			final Button b1 = (Button) d.findViewById(R.id.help_button_1);
			final Button b2 = (Button) d.findViewById(R.id.help_button_2);
			final ViewFlipper viewFlipper = (ViewFlipper) d
					.findViewById(R.id.viewFlipper1);
			if (b1 != null && b2 != null && viewFlipper != null) {
				b1.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// aller à l'aide d'avant
						viewFlipper.showPrevious();

						// si c''est la première vue : cacher le bouton
						int displayedChild = viewFlipper.getDisplayedChild();
						int childCount = viewFlipper.getChildCount();
						b2.setText(getString(R.string.next));
						if (displayedChild == 0) {
							b1.setVisibility(View.INVISIBLE);
						}
					}
				});
				b2.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// si on est à la dernière page, fermer
						int displayedChild = viewFlipper.getDisplayedChild();
						int childCount = viewFlipper.getChildCount();
						if (displayedChild == childCount - 1) {
							d.dismiss();
						} else {
							// aller à la prochaine help
							viewFlipper.showNext();
							b1.setVisibility(View.VISIBLE);
							// si c''est la dernière vue : changer le texte du
							// bouton
							if (displayedChild == childCount - 2) {
								// changer le texte du bouton
								b2.setText(getString(R.string.close));
							}
						}
					}
				});
			}

			// remember that the app has been opened
			edit.putBoolean("opened", true);
			edit.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainActivity.this);

		useNotif = prefs.getBoolean("prefUseNotif", true);

		if (!useNotif) {
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(0);
		}
		// BIND SERVICE
		mConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName className,
					IBinder binder) {
				// récupérer le service
				servicePointage = ((PointageService.MyBinder) binder)
						.getService();
				// donner un hook de du fragment
				servicePointage.setHook(pointageFragment);

				servicePointage.setUseNotif(useNotif);

				pointageFragment.setService(servicePointage, mConnection);

				isServiceBounded = true;

			}

			public void onServiceDisconnected(ComponentName className) {
				servicePointage = null;
				isServiceBounded = false;
			}
		};
		// /!\ les deux lignes ici sont nécessaires : on veut que le service
		// continue de tourner jusqu'à qu'on appelle stopService => donc on doit
		// faire un startService et on veux être bindé au service, donc bind
		Intent i = new Intent(MainActivity.this, PointageService.class);
		startService(i);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
		AnalyticsManager.trackScreen(MainActivity.this, this.getClass()
				.getName());
		AnalyticsManager.dispatch();

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (servicePointage != null && mConnection != null && isServiceBounded) {
			unbindService(mConnection);
			servicePointage.setHook(null);
			isServiceBounded = false;
		}
	}

	@Override
	public void onBackPressed() {
		if (doubleback)
			super.onBackPressed();
		else {
			doubleback = true;
			Toast.makeText(this, R.string.quit_message, Toast.LENGTH_SHORT)
					.show();
		}
		// On repasse a false au bout de deux secondes et demi
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleback = false;
			}
		}, 2500);

	}

	/**
	 * Permet de créer la barre des Tabs
	 */
	private void createTabs(int orientation) {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// TAB POINTAGE
		TextView tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.horaire_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_home, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_home,
					0, 0, 0);
		}
		ActionBar.Tab tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		// TAB NAV
		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.navigation_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_map, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_map,
					0, 0, 0);
		}
		tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		// TAB CHRONO
		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.chrono_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_chrono, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_menu_chrono, 0, 0, 0);
		}
		tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		// TAB FFSA
		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.times_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_ffsa, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_ffsa,
					0, 0, 0);
		}
		tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		// TAB METEO
		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.meteo_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_weather, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_menu_weather, 0, 0, 0);
		}
		tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		// TAB CONTACTS
		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.contacts_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_contact, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_menu_contact, 0, 0, 0);
		}
		tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

		if (tab.getPosition() == 0) {
			if (this.pointageFragment == null) {
				this.pointageFragment = new PointageFragment();
				ft.add(R.id.container, this.pointageFragment);
			}
			ft.show(this.pointageFragment);
			AnalyticsManager.trackScreen(MainActivity.this,
					AnalyticsManager.KEY_PAGE_HOME);
			AnalyticsManager.dispatch();
		}
		if (tab.getPosition() == 1) {
			if (this.mapFragment == null) {
				this.mapFragment = new NavigationFragment();
				ft.add(R.id.container, this.mapFragment, null);
			}
			ft.show(this.mapFragment);
			AnalyticsManager.trackScreen(MainActivity.this,
					AnalyticsManager.KEY_PAGE_MAP);
			AnalyticsManager.dispatch();
		}
		if (tab.getPosition() == 2) {
			if (this.chronoFragment == null) {
				this.chronoFragment = new ChronoFragment();
				ft.add(R.id.container, this.chronoFragment, null);
			}
			ft.show(this.chronoFragment);
			AnalyticsManager.trackScreen(MainActivity.this,
					AnalyticsManager.KEY_PAGE_CHRONO);
			AnalyticsManager.dispatch();
		}
		if (tab.getPosition() == 3) {
			if (this.timeFragment == null) {
				this.timeFragment = new TimeFragment();
				ft.add(R.id.container, this.timeFragment, null);
			}
			ft.show(this.timeFragment);
			AnalyticsManager.trackScreen(MainActivity.this,
					AnalyticsManager.KEY_PAGE_FFSA);
			AnalyticsManager.dispatch();
		}
		if (tab.getPosition() == 4) {
			if (this.meteoFragment == null) {
				this.meteoFragment = new MeteoFragment();
				ft.add(R.id.container, this.meteoFragment, null);
			}
			ft.show(this.meteoFragment);
			AnalyticsManager.trackScreen(MainActivity.this,
					AnalyticsManager.KEY_PAGE_METEO);
			AnalyticsManager.dispatch();
		}
		if (tab.getPosition() == 5) {
			if (this.contactFragment == null) {
				this.contactFragment = new ContactFragment();
				ft.add(R.id.container, this.contactFragment, null);
			}
			ft.show(this.contactFragment);
			AnalyticsManager.trackScreen(MainActivity.this,
					AnalyticsManager.KEY_PAGE_CONTACTS);
			AnalyticsManager.dispatch();
		}

	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
		if (tab.getPosition() == 0) {
			ft.hide(this.pointageFragment);
		}
		if (tab.getPosition() == 1) {
			ft.hide(this.mapFragment);
		}
		if (tab.getPosition() == 2) {
			ft.hide(this.chronoFragment);
		}
		if (tab.getPosition() == 3) {
			ft.hide(this.timeFragment);
		}
		if (tab.getPosition() == 4) {
			ft.hide(this.meteoFragment);
		}
		if (tab.getPosition() == 5) {
			ft.hide(this.contactFragment);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, UserSettingActivity.class);
			startActivity(i);
			break;
		case R.id.close_app:
			if (servicePointage != null && mConnection != null
					&& isServiceBounded) {
				servicePointage.stopTout();
				unbindService(mConnection);
				servicePointage.setHook(null);
				servicePointage.stopSelf();
				servicePointage = null;
			}
			doubleback = true;
			onBackPressed();
			break;
		case R.id.about:
			Dialog d = new Dialog(this,
					android.R.style.Theme_Translucent_NoTitleBar);
			d.setTitle(getString(R.string.menu_about));
			d.setContentView(R.layout.about_layout);
			View v = LayoutInflater.from(this).inflate(R.layout.about_layout,
					null);
			TextView version = (TextView) v.findViewById(R.id.version);
			String vers = "";
			try {
				vers = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			version.setText(vers);
			Button contact = (Button) v.findViewById(R.id.contact_mail_button);
			contact.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String emailAddress = getString(R.string.contact_mail);
					String emailSubject = "Contact Android "
							+ getString(R.string.app_name);

					String emailAddressList[] = { emailAddress };

					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("plain/text");
					intent.putExtra(Intent.EXTRA_EMAIL, emailAddressList);
					intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
					startActivity(intent);
				}
			});
			d.setContentView(v);
			d.show();
			break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings, menu);
		if (mapFragment != null && mapFragment.isVisible()) {
			MenuItem item = menu.findItem(R.id.help);
			item.setVisible(true);
			item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					Dialog help_dialog = new Dialog(MainActivity.this,
							android.R.style.Theme_Translucent_NoTitleBar);
					help_dialog.setTitle(getString(R.string.menu_help));
					help_dialog.setContentView(R.layout.help_navigation_layout);
					help_dialog.show();
					return false;
				}
			});
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getSupportActionBar().removeAllTabs();
		createTabs(newConfig.orientation);
	}

	/**
	 * Sauvegarde des préférences
	 */
	private void savePreferences(long newTime) {
		edit.putLong(TAG_PREF_HOUR, (new Date().getTime()) - newTime);
		edit.commit();
	}

	/**
	 * Chargement des préférences
	 */
	private void loadPreferences() {
		offset = sharedPrefs.getLong(TAG_PREF_HOUR, 0);
	}

	public void reloadMap() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment map = getSupportFragmentManager().findFragmentById(R.id.map);
		if (map != null)
			ft.remove(map);
		else {
			mapFragment = new NavigationFragment();
			ft.add(R.id.container, mapFragment);
		}
		ft.commit();
	}

}
