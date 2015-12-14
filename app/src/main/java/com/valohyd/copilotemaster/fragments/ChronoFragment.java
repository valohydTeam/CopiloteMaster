package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.utils.Chronometer;

/**
 * Classe representant le fragment du chronometre
 * 
 * @author parodi
 * 
 */
public class ChronoFragment extends Fragment {

	private View mainView;
	private Button partielButton, startButton, stopButton; // Boutons chronos
	private Chronometer chrono; // Chronometre
	private ListView partielList; // Liste des temps partiels
	private long timeWhenStopped = 0; // Stocke un temps pour le resume du
										// chrono

	ArrayList<String> partielValues = new ArrayList<String>(); // Les partiels
	ArrayAdapter<String> listAdapter;

	// PREFERENCES
	SharedPreferences sharedPrefs;
	Editor edit;
	private final static String TAG_PREF_CHRONO = "chrono",
			TAG_PREF_FILE = "pref_file";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mainView = inflater.inflate(R.layout.chrono_layout, container, false);

		// PREFERENCES
		sharedPrefs = getActivity().getSharedPreferences(TAG_PREF_FILE,
				Activity.MODE_PRIVATE);
		edit = sharedPrefs.edit();

		// CHRONOMETER
		chrono = (Chronometer) mainView.findViewById(R.id.chrono);

		// LIST
		partielList = (ListView) mainView.findViewById(R.id.partielList);
		listAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, partielValues);
		partielList.setAdapter(listAdapter);
		// BUTTONS
		partielButton = (Button) mainView.findViewById(R.id.partielButton);
		partielButton.setEnabled(false); // Desactivation des partiels si chrono
											// eteint

		startButton = (Button) mainView.findViewById(R.id.startChronoButton);
		stopButton = (Button) mainView.findViewById(R.id.stopChronoButton);
		stopButton.setEnabled(false);

		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				partielButton.setEnabled(true); // activation des partiels
				// Si on doit reprendre le chrono
				if (startButton.getText().equals(
						getActivity().getString(R.string.resume_chrono))) {
					chrono.setBase(SystemClock.elapsedRealtime()
							+ timeWhenStopped); // On reprend ou on s'etait
												// arrêté
				} else {
					// RAZ pour un nouveau chrono
					chrono.setBase(SystemClock.elapsedRealtime());
					savePreferences();
				}
				chrono.start();
				startButton.setEnabled(false); // On ne peux plus rappuyer sur
												// start
				stopButton.setText(R.string.stop_chrono);
				stopButton.setEnabled(true);
			}
		});
		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				partielButton.setEnabled(false); // desactivation des partiels
				edit.remove(TAG_PREF_CHRONO);
				edit.commit();
				// Reset du chrono

				if (stopButton.getText().equals(
						getActivity().getString(R.string.reset_chrono))) {
					chrono.stop(); // On stop le chrono
					chrono.setBase(SystemClock.elapsedRealtime()); // On RAZ le
																	// chrono
					startButton.setText(R.string.start_chrono); // On remet
																// start
					startButton.setEnabled(true); // On reactive le bouton start
					partielValues.clear(); // On vide la liste des partiels
					listAdapter.notifyDataSetChanged(); // On notifie le
														// changement des
														// données
				}
				// Sinon c'est qu'on pause le chrono
				else {
					timeWhenStopped = chrono.getBase()
							- SystemClock.elapsedRealtime(); // On stocke le
																// temps
					chrono.stop(); // On stoppe le chrono
					startButton.setEnabled(true);
					startButton.setText(R.string.resume_chrono);
					stopButton.setText(R.string.reset_chrono);
				}
			}
		});

		partielButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Ajout d'un temps partiel
				listAdapter.add(chrono.getText().toString());
				listAdapter.notifyDataSetChanged();
				partielList.setSelection(partielList.getCount() - 1); // On
																		// descend
																		// la
																		// liste
																		// au
																		// dernier
																		// element
			}
		});
		setHasOptionsMenu(true);
		loadPreferences();
		return mainView;
	}

	/**
	 * Chargement des préférences
	 */
	private void loadPreferences() {
		Long baseChrono = sharedPrefs.getLong(TAG_PREF_CHRONO, -1);

		if (baseChrono != -1) {
			chrono.setBase(baseChrono);
			chrono.start();
			partielButton.setEnabled(true);
			startButton.setEnabled(false); // On ne peux plus rappuyer sur
			// start
			stopButton.setText(R.string.stop_chrono);
			stopButton.setEnabled(true);
		}
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
	 * Sauvegarde des préférences
	 */
	private void savePreferences() {
		edit.putLong(TAG_PREF_CHRONO, SystemClock.elapsedRealtime());
		edit.commit();
	}
}
