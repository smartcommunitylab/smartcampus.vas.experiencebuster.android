/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.eb;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;

@SuppressLint("NewApi")
public class SettingsActivity extends PreferenceActivity {

	private static int prefs = R.xml.preferences;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Preference sizeFilePref = null;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(prefs);
			sizeFilePref = findPreference(EBHelper.CONF_FILE_SIZE);
			sizeFilePref.setOnPreferenceChangeListener(new PreferenceChecker(
					this));
		} else {
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new PrefFragment()).commit();

		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class PrefFragment extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(SettingsActivity.prefs);
			getPreferenceManager().findPreference(EBHelper.CONF_FILE_SIZE)
					.setOnPreferenceChangeListener(
							new PreferenceChecker(getActivity()));
		}

	}
}

class PreferenceChecker implements OnPreferenceChangeListener {

	Context ctx;

	public PreferenceChecker(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(EBHelper.CONF_FILE_SIZE)) {
			String value = (String) newValue;
			try {
				if (Float.valueOf(value) > 0) {
					return true;
				} else {
					Toast.makeText(ctx, "Value could not be negative",
							Toast.LENGTH_SHORT).show();
				}
			} catch (NumberFormatException e) {
				Toast.makeText(ctx, "Value must be a number",
						Toast.LENGTH_SHORT).show();
			}
		}
		return false;
	}

}
