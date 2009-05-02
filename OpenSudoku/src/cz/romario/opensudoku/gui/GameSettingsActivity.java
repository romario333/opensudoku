package cz.romario.opensudoku.gui;

import cz.romario.opensudoku.R;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;

public class GameSettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.game_settings);
		
		findPreference("show_hints").setOnPreferenceChangeListener(mShowHintsChanged);

	}

	private OnPreferenceChangeListener mShowHintsChanged = new OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean newVal = (Boolean)newValue;
			
			HintsQueue hm = new HintsQueue(GameSettingsActivity.this);
			if (newVal) {
				hm.resetOneTimeHints();
			};
			return true;
		}
		
	};

}
