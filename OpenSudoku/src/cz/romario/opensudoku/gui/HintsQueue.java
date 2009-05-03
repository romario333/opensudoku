package cz.romario.opensudoku.gui;

import java.util.LinkedList;
import java.util.Queue;

import cz.romario.opensudoku.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class HintsQueue {
	// TODO: should be persisted in activity's state
	private Queue<Message> mMessages;
	
	private static final String PREF_FILE_NAME = "hints";
	
	private Context mContext;
	private SharedPreferences mPrefs;
	private AlertDialog mHintDialog;
	
	private boolean mOneTimeHintsEnabled;

	public HintsQueue(Context context) {
		mContext = context;
		mPrefs = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		
		SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
		gameSettings.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key == "show_hints") {
					mOneTimeHintsEnabled = sharedPreferences.getBoolean("show_hints", true);
				}
			}
			
		});
		mOneTimeHintsEnabled = gameSettings.getBoolean("show_hints", true);
		
		mHintDialog = new AlertDialog.Builder(context)
			.setIcon(android.R.drawable.ic_menu_info_details)
			.setTitle(R.string.hint)
			.setMessage("")
			.setPositiveButton(R.string.close, mHintClosed).create();
		
		mHintDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					processQueue();					
				}
				
			});
		
		mMessages = new LinkedList<Message>();
	}
	
	private void addHint(Message hint) {
		synchronized (mMessages) {
			mMessages.add(hint);
		}
		
		synchronized (mHintDialog) {
			if (!mHintDialog.isShowing()) {
				processQueue();
			}
		}
	}
	
	private void processQueue() {
		Message hint;
		
		synchronized (mMessages) {
			hint = mMessages.poll();
		}
		
		if (hint != null) {
			showHintDialog(hint);
		}
	}
	
	private void showHintDialog(Message hint) {
		synchronized (mHintDialog) {
			mHintDialog.setTitle(mContext.getString(hint.titleResID));
			// TODO: hint.args !!
			mHintDialog.setMessage(mContext.getText(hint.messageResID));
			mHintDialog.show();
		}
	}

	private OnClickListener mHintClosed = new OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			//processQueue();
		}
		
	};
	
	public void showHint(int titleResID, int messageResID, Object... args) {
		Message hint = new Message();
		hint.titleResID = titleResID;
		hint.messageResID = messageResID;
		hint.args = args;
		addHint(hint);
	}
	
	public void showOneTimeHint(int titleResID, int messageResID, Object... args) {
		if (mOneTimeHintsEnabled) {
			String hintKey = "hint_" + messageResID;
			if (!mPrefs.getBoolean(hintKey, false)) {
				showHint(titleResID, messageResID, args);
				Editor editor = mPrefs.edit();
				editor.putBoolean(hintKey, true);
				editor.commit();
			}
		}
		
	}
	
	public void resetOneTimeHints() {
		Editor editor = mPrefs.edit();
		editor.clear();
		editor.commit();
	}
	
	/**
	 * This should be called when activity is paused.
	 */
	public void pause() {
		// get rid of WindowLeakedException in logcat
		if (mHintDialog != null) {
			mHintDialog.cancel();
		}
	}
	
	private static class Message {
		int titleResID;
		int messageResID;
		Object[] args;
	}

}
