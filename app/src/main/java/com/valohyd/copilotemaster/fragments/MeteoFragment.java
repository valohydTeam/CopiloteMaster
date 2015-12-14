package com.valohyd.copilotemaster.fragments;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.utils.NetworkUtils;

public class MeteoFragment extends Fragment{

	private View mainView;

	private WebView web; // WebView

	private Bundle etatSauvegarde; // Sauvegarde de la vue

	private boolean dejaCharge = false; // Etat de la page

	private ProgressBar progress; // ProgressBar

	private EditText searchText; // Champs de recherche

	private ImageButton searchButton; // Bouton de recherche

	private String home_url = "http://www.google.fr/search?q=Meteo";

	private static final String URL_IDS = "http://www.valohyd.com/copilotemaster/weather_ids.txt";

	private ArrayList<String> ids_blocks; // ID des Block a cacher

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mainView = inflater.inflate(R.layout.meteo_layout, container, false);

		progress = (ProgressBar) mainView.findViewById(R.id.progressWeb);

		searchText = (EditText) mainView.findViewById(R.id.search_text_meteo);
		searchText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							if(NetworkUtils.networkConnectionAvailable(getActivity())){
								performSearch();
							}
							else{
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
				if(NetworkUtils.networkConnectionAvailable(getActivity())){
					performSearch();
				}
				else{
					Toast.makeText(getActivity(), "Aucune connexion internet disponible !", Toast.LENGTH_SHORT).show();
				}
			}
		});

		// récupérer la web view
		web = (WebView) mainView.findViewById(R.id.webView);

		if (web != null) {
			if (!dejaCharge) {
				progress.setVisibility(View.GONE);
				web.setVisibility(View.INVISIBLE);
			} else if (etatSauvegarde != null) {
				web.restoreState(etatSauvegarde);
			}

			// paramétrer la page
			web.getSettings().setBuiltInZoomControls(false);
			web.getSettings().setSupportZoom(false);
			web.getSettings().setGeolocationEnabled(true);
			web.getSettings().setUseWideViewPort(true);
			web.getSettings().setJavaScriptEnabled(true);
			web.setVerticalScrollBarEnabled(false);
			web.setHorizontalScrollBarEnabled(false);

			// autoriser la navigation dans les pages
			web.setWebViewClient(new MyWebViewClient());
			web.setWebChromeClient(new MyWebChromeClient());

			dejaCharge = true;
		}

		// POUR L'ICONE DU MENU !
		setHasOptionsMenu(true);
		return mainView;
	}

	@Override
	public void onPause() {
		etatSauvegarde = new Bundle();
		web.saveState(etatSauvegarde);

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
	private void performSearch() {
		if (searchText.getText().length() != 0) {
			if (ids_blocks == null)
				new LoadWeatherAsynctask().execute();
			else
				web.loadUrl(home_url + "+" + searchText.getText().toString());
		}
		// close keyboard
		((InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
				searchText.getWindowToken(), 0);

	}

	private class MyWebViewClient extends WebViewClient {

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			for (String id : ids_blocks) {
				web.loadUrl("javascript:(function() { "
						+ "document.getElementById('" + id
						+ "').style.display = 'none'; " + "})()");
			}
			web.loadUrl("javascript:(function() { "
					+ "var elements = document.getElementsByClassName('rc');"
					+ "for (i=0; i<elements.length; i++){"
					+ "elements[i].style.display = 'none'" + "}" + "})()");
			progress.setVisibility(View.GONE);
			web.setVisibility(View.VISIBLE);

		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			progress.setVisibility(View.VISIBLE);
			web.setVisibility(View.GONE);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}

	}

	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			progress.setProgress(newProgress);
			super.onProgressChanged(view, newProgress);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem item = menu.findItem(R.id.refresh_web);
		item.setVisible(true);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				web.reload();
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

	private class LoadWeatherAsynctask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL ids_url = new URL(URL_IDS);
				Scanner s = new Scanner(ids_url.openStream());
				while (s.hasNextLine()) {
					ids_blocks = new ArrayList<String>(Arrays.asList(s
							.nextLine().split(";")));
				}
				s.close();
			} catch (IOException ex) {
				// there was some connection problem, or the file did not exist
				ex.printStackTrace(); // for now, simply output it.
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			web.loadUrl(home_url + "+" + searchText.getText().toString());
		}

	}

}
