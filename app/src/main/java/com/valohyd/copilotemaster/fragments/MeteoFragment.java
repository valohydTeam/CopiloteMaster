package com.valohyd.copilotemaster.fragments;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.adapters.MeteoListAdapter;
import com.valohyd.copilotemaster.models.WeatherCity;
import com.valohyd.copilotemaster.utils.JSONParser;
import com.valohyd.copilotemaster.utils.NetworkUtils;

import org.json.JSONObject;

public class MeteoFragment extends Fragment {

    private static final String API_KEY = "34b48b18d65467d70d068c7471e9ea42";

    private static final String API_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?units=metric";

    private View mainView;

    private Bundle etatSauvegarde; // Sauvegarde de la vue

    private EditText searchText; // Champs de recherche

    private ImageButton searchButton; // Bouton de recherche

    private ImageButton searchButtonGPS; // Bouton de recherche GPS

    private ListView mListViewVilles; // listview de la mÃ©tÃ©o de chaque ville

    //private String home_url = "http://www.google.fr/search?q=Meteo";

    private static final String URL_IDS = "http://www.valohyd.com/copilotemaster/weather_ids.txt";

    private ArrayList<String> ids_blocks; // ID des Block a cacher

    public MeteoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mainView = inflater.inflate(R.layout.meteo_layout, container, false);

        searchText = (EditText) mainView.findViewById(R.id.search_text_meteo);
        searchText
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (NetworkUtils.networkConnectionAvailable(getActivity())) {
                                performSearch(false);
                            } else {
                                Toast.makeText(getActivity(), "Aucune connexion internet disponible !", Toast.LENGTH_LONG).show();
                            }
                            return true;
                        }
                        return false;
                    }
                });
        searchButton = (ImageButton) mainView.findViewById(R.id.search_meteo);
        searchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (NetworkUtils.networkConnectionAvailable(getActivity())) {
                    performSearch(false);
                } else {
                    Toast.makeText(getActivity(), "Aucune connexion internet disponible !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        searchButtonGPS = (ImageButton) mainView.findViewById(R.id.search_gps);
        searchButtonGPS.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (NetworkUtils.networkConnectionAvailable(getActivity())) {
                    performSearch(true);
                } else {
                    Toast.makeText(getActivity(), "Aucune connexion internet disponible !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // rÃ©cupÃ©rer la listview des mÃ©teo
        mListViewVilles = (ListView) mainView.findViewById(R.id.listview_meteo);
        if (mListViewVilles != null) {
            mListViewVilles.setEmptyView(mainView.findViewById(R.id.emptyView));
            //MeteoListAdapter adapter = new MeteoListAdapter(getActivity(), new String[] {"Blop", "Antibes", "FrÃ©jus)"});
            //mListViewVilles.setAdapter(adapter);
        }

        // POUR L'ICONE DU MENU !
        setHasOptionsMenu(true);
        return mainView;
    }

    @Override
    public void onPause() {
        etatSauvegarde = new Bundle();

        super.onPause();
    }

    /**
     * permet de dire de redessiner le menu
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        getActivity().supportInvalidateOptionsMenu();
    }

    // Recherche
    private void performSearch(boolean fromGPS) {
        if (fromGPS) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(getView(),"Pas la permission",Snackbar.LENGTH_SHORT).show();
                }
                else{
                    Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocationGPS != null) {
                        Log.d("POSITION GPS",lastKnownLocationGPS.toString());

                        new LoadWeatherAsynctask().execute("&lat=" + lastKnownLocationGPS.getLatitude() + "&lon=" + lastKnownLocationGPS.getLongitude());
                    }
                    else {
                        Location lastKnownLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (lastKnownLocationNetwork != null) {
                            Log.d("POSITION RESEAU",lastKnownLocationNetwork.toString());
                            new LoadWeatherAsynctask().execute("&lat=" + lastKnownLocationNetwork.getLatitude() + "&lon=" + lastKnownLocationNetwork.getLongitude());
                        } else {
                            Snackbar.make(getView(), "Pas de position", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
            }

        }
        else {
            if ((searchText.getText().length() != 0)) {
                new LoadWeatherAsynctask().execute("&q="+searchText.getText().toString());
            }
        }
		// close keyboard
		((InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                searchText.getWindowToken(), 0);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem item = menu.findItem(R.id.refresh_web);
		item.setVisible(true);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// todo web.reload();
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
				help_dialog.setContentView(R.layout.help_weather_layout);
				help_dialog.show();
				return false;
			}
		});

	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	}

	private class LoadWeatherAsynctask extends AsyncTask<String, Void, Void> {
	private MeteoListAdapter adapter;
		@Override
		protected Void doInBackground(String... params) {
			try {
				//TEST
				if(params.length>0) {
					ArrayList<WeatherCity> weatherCities = new ArrayList<>();
					JSONParser parser = new JSONParser();
					JSONObject json = parser.getJSONFromUrl(API_BASE_URL + params[0].trim() + "&appid=" + API_KEY + "&lang=" + getString(R.string.country_code));
					if(json!=null){
						weatherCities.add(new WeatherCity(json));
					}
					// créer l'adapter avec les villes
					adapter = new MeteoListAdapter(getActivity(), weatherCities);
				}
			} catch (Exception ex) {
				// there was some connection problem, or the file did not exist
				ex.printStackTrace(); // for now, simply output it.
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//web.loadUrl(home_url + "+" + searchText.getText().toString());

			mListViewVilles.setAdapter(adapter);
		}

	}

}
