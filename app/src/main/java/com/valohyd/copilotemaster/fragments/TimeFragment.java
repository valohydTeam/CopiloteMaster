package com.valohyd.copilotemaster.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.valohyd.copilotemaster.R;

/**
 * Classe representant le fragment de la vue des temps
 * 
 * @author parodi
 * 
 */
public class TimeFragment extends SherlockFragment {

	private View mainView;

	private WebView web; // WebView

	private Bundle etatSauvegarde; // Sauvegarde de la vue

	private boolean dejaCharge = false; // Etat de la page

	private String home_url; // Page d'accueil

	private ProgressBar progress;

	// PREFERENCES
	SharedPreferences sharedPrefs;
	Editor edit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mainView = inflater.inflate(R.layout.web_layout, container, false);

		progress = (ProgressBar) mainView.findViewById(R.id.progressWeb);

		// PREFERENCES
		sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		edit = sharedPrefs.edit();

		home_url = sharedPrefs.getString("prefHomepage",
				getString(R.string.url_ffsa)); // Page d'accueil

		// récupérer la web view
		web = (WebView) mainView.findViewById(R.id.webView);

		if (web != null) {
			if (!dejaCharge) {
				// charger la page
				web.loadUrl(home_url);
			} else if (etatSauvegarde != null) {
				web.restoreState(etatSauvegarde);
			}
			// Action au clic long sur un lien
			web.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					final WebView.HitTestResult hr = ((WebView) v)
							.getHitTestResult();
					// Si on a bien cliqué sur un lien
					if (hr != null
							&& hr.getType() == HitTestResult.SRC_ANCHOR_TYPE) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setTitle(R.string.change_home_title);
						builder.setMessage(getActivity().getString(
								R.string.message_homepage)
								+ hr.getExtra() + " ?");
						builder.setPositiveButton(android.R.string.ok,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										home_url = hr.getExtra(); // On remplace
																	// la page
																	// d'accueil
																	// actuelle
										web.loadUrl(home_url);
										savePreferences();
									}
								});
						builder.setNegativeButton(android.R.string.cancel, null);
						builder.setCancelable(true);
						builder.show();
					}
					return false;
				}
			});
			// paramétrer la page
			web.getSettings().setBuiltInZoomControls(true);
			web.getSettings().setSupportZoom(true);
			web.getSettings().setRenderPriority(RenderPriority.HIGH);
			web.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
			web.getSettings().setGeolocationEnabled(false);
			web.getSettings().setUseWideViewPort(true);
			web.getSettings().setLoadWithOverviewMode(true);

			// autoriser la navigation dans les pages
			web.setWebViewClient(new MyWebViewClient());
			web.setWebChromeClient(new MyWebChromeClient());
			web.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK
							&& event.getAction() == KeyEvent.ACTION_DOWN) {
						if (web.canGoBack()) {
							web.goBack();
							return true;
						}
					}
					return false;
				}
			});
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

	private class MyWebViewClient extends WebViewClient {

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			progress.setVisibility(View.GONE);
//			web.setVisibility(View.VISIBLE);
//			web.bringToFront();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			progress.setVisibility(View.VISIBLE);
//			web.setVisibility(View.GONE);
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
	public void onHiddenChanged(boolean hidden) {
		getActivity().supportInvalidateOptionsMenu();
		if (!hidden) {
			String new_home = sharedPrefs.getString("prefHomepage",
					getString(R.string.url_ffsa));
			if (!home_url.equals(new_home)) {
				home_url = new_home;
				web.loadUrl(home_url); // Page d'accueil
			}
		}
		super.onHiddenChanged(hidden);
	}

	/**
	 * Sauvegarde des préférences
	 */
	private void savePreferences() {
		edit.putString("prefHomepage", home_url);
		edit.commit();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem item = menu.findItem(R.id.refresh_web);
		item.setVisible(true);
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				web.reload();
				return false;
			}
		});
		item = menu.findItem(R.id.help);
		item.setVisible(true);
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Dialog help_dialog = new Dialog(getActivity(),
						android.R.style.Theme_Translucent_NoTitleBar);
				help_dialog.setTitle(getString(R.string.menu_help));
				help_dialog.setContentView(R.layout.help_web_layout);
				help_dialog.show();
				return false;
			}
		});
	}

}
