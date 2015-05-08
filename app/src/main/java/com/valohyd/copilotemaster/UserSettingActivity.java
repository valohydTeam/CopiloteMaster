package com.valohyd.copilotemaster;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ViewFlipper;

public class UserSettingActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        Preference firstHelp = getPreferenceScreen().findPreference("prefFirstHelp");
        firstHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Dialog d = new Dialog(UserSettingActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
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
                final ViewFlipper viewFlipper = (ViewFlipper) d.findViewById(R.id.viewFlipper1);
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
                                // fermer
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
                return false;
            }
        });

    }
}