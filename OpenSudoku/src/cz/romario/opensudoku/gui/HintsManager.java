package cz.romario.opensudoku.gui;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class HintsManager {
	private SharedPreferences mPrefs;
	
	private static final String PREF_FILE_NAME = "hints";
	
	private boolean mHintsEnabled;
	
	public HintsManager(Context context) {
		mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		
		SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
		gameSettings.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key == "show_hints") {
					mHintsEnabled = sharedPreferences.getBoolean("show_hints", true);
				}
			}
			
		});
		mHintsEnabled = gameSettings.getBoolean("show_hints", true);
	}
	
	public boolean wasDisplayed(String hintKey) {
		if (mHintsEnabled) {
			return mPrefs.getBoolean(hintKey, false);
		} else {
			return true;
		}
	}
	
	public void markAsDisplayed(String hintKey) {
		Editor editor = mPrefs.edit();
		editor.putBoolean(hintKey, true);
		editor.commit();
	}
	
	public void reset() {
		Editor editor = mPrefs.edit();
		editor.clear();
		editor.commit();
	}

}
