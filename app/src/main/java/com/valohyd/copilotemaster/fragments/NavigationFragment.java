package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.valohyd.copilotemaster.MainActivity;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.models.Contact;
import com.valohyd.copilotemaster.models.POI;
import com.valohyd.copilotemaster.sqlite.ContactsBDD;
import com.valohyd.copilotemaster.sqlite.PoisBDD;
import com.valohyd.copilotemaster.utils.MySupportMapFragment;

/**
 * Classe representant le fragment de navigation
 * 
 * @author parodi
 * 
 */
public class NavigationFragment extends MySupportMapFragment implements
		OnMyLocationChangeListener {

	private static final String SEPARATEUR = "\n\t";
	// CONSTANTES VITESSE
	public static final int INDEX_KM = 0;
	public static final int INDEX_MILES = 0;
	public static final int DEFAULT_SPEED_LIMIT = 80; // TODO ?
	public static final int HOUR_MULTIPLIER = 3600;
	public static final double UNIT_MULTIPLIERS[] = { 0.001, 0.000621371192 };

	// GPS
	protected static final long GPS_UPDATE_TIME_INTERVAL = 3000; // millis
	protected static final float GPS_UPDATE_DISTANCE_INTERVAL = 0; // meters
	private LocationManager mlocManager;
	private MyGPSListener mGpsListener;

	private boolean firstFix = true, firstTime = true;

	// BDD
	ContactsBDD bdd;
	PoisBDD pois_bdd;

	// TAGS

	LinearLayout layoutButtons; // Layout par dessus la map

	ImageButton radarButton, gpsButton; // Bouton de radar

	AlertDialog.Builder contact_dialog; // Dialog de contact

	private ArrayList<String> contacts, selected_contacts; // Contacts et
															// contacts
															// selectionnés

	private ArrayList<POI> list_pois; // Liste des POIs

	String[] poi_types;// Types
	// des
	// POI

	int[] poi_icons = { R.drawable.parc_ferme_icon, R.drawable.assistance_icon,
			R.drawable.start_icon, R.drawable.end_icon, R.drawable.poi_icon }; // Icones
																				// des
																				// POI

	/**
	 * La carte
	 */
	private GoogleMap map;

	/**
	 * Le container
	 */
	private View mainView;

	/**
	 * Ma position
	 */
	GeoPoint myLocation;

	private TextView speedText, accuracyText; // Texte Vitesse et précision
	private double speed, accuracy; // vitesse,precision

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		// TEST si on a deja une instance de la vue alors on defonce tout
		if (mainView != null) {
			ViewGroup parent = (ViewGroup) mainView.getParent();
			if (parent != null)
				parent.removeView(mainView);
		}
		// Et on refait
		try {
			mainView = inflater.inflate(R.layout.navigation_layout, container,
					false);

			// POI
			poi_types = new String[] {
					getActivity().getString(R.string.poi_parc_ferme),
					getActivity().getString(R.string.poi_parc_assistance),
					getActivity().getString(R.string.poi_depart_es),
					getActivity().getString(R.string.poi_arrivee_es),
					getActivity().getString(R.string.poi_divers) };

			// BDD
			bdd = new ContactsBDD(getActivity());
			pois_bdd = new PoisBDD(getActivity());

			selected_contacts = new ArrayList<String>();

			layoutButtons = (LinearLayout) mainView
					.findViewById(R.id.layoutButtonsMap);

			speedText = (TextView) mainView.findViewById(R.id.speedTextMap);
			accuracyText = (TextView) mainView
					.findViewById(R.id.accuracyTextMap);

			// GPS
			mGpsListener = new MyGPSListener();
			mlocManager = (LocationManager) getActivity().getSystemService(
					Context.LOCATION_SERVICE);
			mlocManager.addGpsStatusListener(mGpsListener);

			gpsButton = (ImageButton) mainView.findViewById(R.id.gpsButtonMap);
			gpsButton.bringToFront();
			gpsButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(
							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					getActivity().startActivity(intent);
				}
			});

			// RADAR
			radarButton = (ImageButton) mainView
					.findViewById(R.id.radarButtonMap);

			radarButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					initContacts(); // Initialisation des contacts
					if (contacts.isEmpty()) {
						Toast.makeText(getActivity(), R.string.aucun_contacts,
								Toast.LENGTH_SHORT).show();

					} else {
						// CONSTRUCTION DIALOG
						contact_dialog = new AlertDialog.Builder(getActivity());
						contact_dialog.setTitle(R.string.titre_choix_contact);
						contact_dialog.setMultiChoiceItems(
								// Selection multiple
								contacts.toArray(new CharSequence[contacts
										.size()]), null,
								new OnMultiChoiceClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which, boolean isChecked) {
										// Selection d'un contact ou deselection
										if (isChecked)
											selected_contacts.add(contacts.get(
													which).toString());
										else
											selected_contacts.remove(contacts
													.get(which).toString());
									}
								});

						// PARTAGE DU RADAR

						contact_dialog.setPositiveButton(R.string.share_radar,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// CONSTRUCTION DU DIALOG
										AlertDialog.Builder builder = new AlertDialog.Builder(
												getActivity());
										builder.setTitle(R.string.envoi_sms_title);
										builder.setMessage(getActivity()
												.getString(
														R.string.confirmation_envoi_sms)
												+ selected_contacts + " ?");
										builder.setPositiveButton(
												android.R.string.ok,
												new OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														for (String nb : selected_contacts) {
															sendSms(nb
																	.split(SEPARATEUR)[1],
																	getActivity()
																			.getString(
																					R.string.message_sms)); // On
															// envoi
															// le
															// sms
														}
														selected_contacts = new ArrayList<String>(); // on
																										// vide
																										// la
																										// selection
													}
												});
										builder.setNegativeButton(
												android.R.string.cancel, null);
										builder.show();

									}
								});

						contact_dialog.show();
						contact_dialog.setCancelable(true);
						contact_dialog
								.setOnCancelListener(new OnCancelListener() {

									@Override
									public void onCancel(DialogInterface dialog) {
										selected_contacts = new ArrayList<String>();// on
										// vide
										// la
										// selection
									}
								});
					}
				}
			});

			layoutButtons.bringToFront(); // Pour voir le layout par dessus la
											// map

			setHasOptionsMenu(true);

			// MAP
			map = ((SupportMapFragment) ((MainActivity) getActivity())
					.getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			if (map != null) {
				// GEOLOCALISATION API V2
				map.setMyLocationEnabled(true);
				map.setOnMyLocationChangeListener(this);
				map.setTrafficEnabled(true);
				// Ajout d'un POI au longClick
				map.setOnMapLongClickListener(new OnMapLongClickListener() {

					@Override
					public void onMapLongClick(final LatLng position) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setTitle(R.string.poi_title);
						builder.setItems(poi_types, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// Construction du POI
								map.addMarker(new MarkerOptions()
										.position(position)
										.title(poi_types[which])
										.icon(BitmapDescriptorFactory
												.fromResource(poi_icons[which])));
								POI p = new POI(which, position);
								savePOI(p);
							}

						});
						builder.show();
						builder.setCancelable(true);
						builder.setNeutralButton(android.R.string.cancel, null);

					}
				});
				// Action au clic sur le POI
				map.setOnMarkerClickListener(new OnMarkerClickListener() {

					@Override
					public boolean onMarkerClick(final Marker marker) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setTitle(marker.getTitle());
						builder.setCancelable(true);
						builder.setNeutralButton(android.R.string.cancel, null);
						builder.setNegativeButton(R.string.erase_poi,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										deletePOI(marker);
									}
								});
						builder.setPositiveButton(R.string.navigate_to,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										try {
											Intent intent = new Intent(
													android.content.Intent.ACTION_VIEW,
													Uri.parse("google.navigation:q="
															+ marker.getPosition().latitude
															+ ","
															+ marker.getPosition().longitude));
											startActivity(intent);
										} catch (Exception e) {
											AlertDialog.Builder d = new AlertDialog.Builder(
													getActivity());
											d.setMessage(R.string.message_google_maps_introuvable);
											d.setPositiveButton(
													getString(R.string.close),
													null);
											d.show();
										}
									}
								});
						builder.show();

						return false;
					}
				});

				// Initialisation des POIS
				initPOIs();
			}
		} catch (InflateException e) {
			((MainActivity) getActivity()).reloadMap();
		}

		// récupérer la map
		return mainView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}

	/**
	 * permet de dire de redessiner le menu
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		getActivity().supportInvalidateOptionsMenu();
	}

	// Initialisation des contacts via sqlite
	private void initContacts() {
		bdd.open();

		ArrayList<Contact> contact_temp = bdd.getAllContacts();
		contacts = new ArrayList<String>();
		for (Contact c : contact_temp) {
			contacts.add(c.getName() + SEPARATEUR + c.getNumber());
		}
		selected_contacts = new ArrayList<String>();// on vide la selection
		bdd.close();
	}

	// Initialisation des contacts via sqlite
	private void initPOIs() {
		pois_bdd.open();

		list_pois = pois_bdd.getAllPOIs();

		for (POI p : list_pois) {
			map.addMarker(new MarkerOptions()
					.position(p.getLocation())
					.title(poi_types[p.getType()])
					.icon(BitmapDescriptorFactory.fromResource(poi_icons[p
							.getType()])));
		}

		pois_bdd.close();
	}

	// Sauvegarde d'un POI dans la bdd
	private void savePOI(POI p) {
		pois_bdd.open();

		pois_bdd.insertPOI(p);

		pois_bdd.close();
	}

	// Suppression d'un POI
	private void deletePOI(Marker marker) {
		pois_bdd.open();

		int res = pois_bdd.removePOIWithLocation(marker.getPosition());

		// Si tout se passe bien navette
		if (res > 0) {
			marker.remove(); // Suppression du marker
			Log.e("POI", "Suppression OK du POI : " + marker.getPosition());
			Log.e("POI", "BBD : " + pois_bdd.getAllPOIs());
		} else {
			Log.e("POI",
					"Erreur à la suppression du POI : " + marker.getPosition());
			Log.e("POI", "BBD : " + pois_bdd.getAllPOIs());
		}

		pois_bdd.close();
	}

	// Envoi d'un sms
	private void sendSms(String number, String message) {
		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(number, null, message, null, null);
		} catch (Exception e) {
			Toast.makeText(getActivity(), R.string.sms_error, Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
		}

	}

	// Ecouteur sur la position de l'utilisateur
	@Override
	public void onMyLocationChange(Location location) {
		// Latitude
		double latitude = location.getLatitude();

		// Longitude
		double longitude = location.getLongitude();

		// Position
		LatLng latLng = new LatLng(latitude, longitude);

		// FIX pour eviter de recentrer la map si on ne bouge pas
		if (!MainActivity.mMapIsTouched && speed > 5 || firstTime) {
			// Centrage sur la position
			map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			firstTime = false;
		}

		// Zoom sur la position
		if (firstFix)
			map.animateCamera(CameraUpdateFactory.zoomTo(18));

		// Vitesse
		speed = location.getSpeed();
		speedText.setText("" + Math.round(convertSpeed(speed)));

		// Precision
		accuracy = Math.round(location.getAccuracy());
		accuracyText.setText("" + accuracy);

		firstFix = false;
	}

	// Conversion de la vitesse selon l'unité choisie
	private double convertSpeed(double speed) {
		return ((speed * HOUR_MULTIPLIER) * UNIT_MULTIPLIERS[INDEX_KM]);
	}

	private class MyGPSListener implements GpsStatus.Listener {
		public void onGpsStatusChanged(int event) {
			if (gpsButton != null && getActivity() != null
					&& getActivity().getResources() != null) {
				switch (event) {

				case GpsStatus.GPS_EVENT_FIRST_FIX:
					gpsButton.setImageDrawable(getActivity().getResources()
							.getDrawable(R.drawable.gps_on));
					break;
				case GpsStatus.GPS_EVENT_STARTED:
					gpsButton.setImageDrawable(getActivity().getResources()
							.getDrawable(R.drawable.gps_started));
					break;
				case GpsStatus.GPS_EVENT_STOPPED:
					gpsButton.setImageDrawable(getActivity().getResources()
							.getDrawable(R.drawable.gps_off));
					break;
				}
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.findItem(R.id.help);
		item.setVisible(true);
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Dialog help_dialog = new Dialog(getActivity(),
						android.R.style.Theme_Translucent_NoTitleBar);
				help_dialog.setTitle(getString(R.string.menu_help));
				help_dialog.setContentView(R.layout.help_navigation_layout);
				help_dialog.show();
				return false;
			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}

}
