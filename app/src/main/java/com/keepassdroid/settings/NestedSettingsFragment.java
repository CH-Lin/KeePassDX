/*
 * Copyright 2017 Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.keepassdroid.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.android.keepass.R;
import com.keepassdroid.Database;
import com.keepassdroid.app.App;
import com.keepassdroid.database.PwEncryptionAlgorithm;
import com.keepassdroid.stylish.Stylish;

public class NestedSettingsFragment extends PreferenceFragment {

    public static final int NESTED_SCREEN_APP_KEY = 1;
    public static final int NESTED_SCREEN_DB_KEY = 2;

    private static final String TAG_KEY = "NESTED_KEY";

    public static NestedSettingsFragment newInstance(int key) {
        NestedSettingsFragment fragment = new NestedSettingsFragment();
        // supply arguments to bundle.
        Bundle args = new Bundle();
        args.putInt(TAG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPreferenceResource();
    }

    private void checkPreferenceResource() {
        int key = getArguments().getInt(TAG_KEY);
        // Load the preferences from an XML resource
        switch (key) {
            case NESTED_SCREEN_APP_KEY:
                addPreferencesFromResource(R.xml.app_preferences);

                Preference keyFile = findPreference(getString(R.string.keyfile_key));
                keyFile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        Boolean value = (Boolean) newValue;

                        if (!value) {
                            App.getFileHistory().deleteAllKeys();
                        }

                        return true;
                    }
                });

                Preference recentHistory = findPreference(getString(R.string.recentfile_key));
                recentHistory.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        Boolean value = (Boolean) newValue;

                        if (value == null) {
                            value = true;
                        }

                        if (!value) {
                            App.getFileHistory().deleteAll();
                        }

                        return true;
                    }
                });

                Preference stylePreference = findPreference(getString(R.string.setting_style_key));
                stylePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String styleString = (String) newValue;
                        Stylish.assignStyle(getActivity(), styleString);
                        getActivity().recreate();
                        return true;
                    }
                });

                break;

            case NESTED_SCREEN_DB_KEY:
                addPreferencesFromResource(R.xml.db_preferences);

                Database db = App.getDB();
                if (db.Loaded() && db.pm.appSettingsEnabled()) {

                    Preference rounds = findPreference(getString(R.string.rounds_key));
                    rounds.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            setRounds(App.getDB(), preference);
                            return true;
                        }
                    });

                    setRounds(db, rounds);

                    Preference algorithm = findPreference(getString(R.string.algorithm_key));
                    setAlgorithm(db, algorithm);

                } else {
                    Log.e(getClass().getName(), "Database isn't ready");
                }

                break;

            default:
                break;
        }
    }

    private void setRounds(Database db, Preference rounds) {
        rounds.setSummary(Long.toString(db.pm.getNumRounds()));
    }

    private void setAlgorithm(Database db, Preference algorithm) {
        int resId;
        if ( db.pm.getEncAlgorithm() == PwEncryptionAlgorithm.Rjindal ) {
            resId = R.string.rijndael;
        } else  {
            resId = R.string.twofish;
        }

        algorithm.setSummary(resId);
    }
}
