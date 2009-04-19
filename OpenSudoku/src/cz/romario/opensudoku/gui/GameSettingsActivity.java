package cz.romario.opensudoku.gui;

import cz.romario.opensudoku.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class GameSettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.game_settings);
	}

}
