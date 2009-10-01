/* 
 * Copyright (C) 2009 Roman Masek, Vit Hnilica
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package cz.romario.opensudoku.gui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.gui.importing.AbstractImportTask;
import cz.romario.opensudoku.gui.importing.ImportOpenSudokuTask;
import cz.romario.opensudoku.gui.importing.ImportOptions;
import cz.romario.opensudoku.gui.importing.SdmImportTask;
import cz.romario.opensudoku.gui.importing.StringImportTask;
import cz.romario.opensudoku.gui.importing.AbstractImportTask.OnImportFinishedListener;

/**
 * This activity is responsible for importing puzzles from various sources
 * (web, file, .opensudoku, .sdm, extras).
 * 
 * @author romario
 *
 */
public class ImportSudokuActivity extends Activity {

	/**
	 * Name of folder to which games should be imported.
	 */
	public static final String EXTRA_FOLDER_NAME = "FOLDER_NAME";
	/**
	 * Indicates whether games should be appended to the existing folder if such
	 * folder exists.
	 */
	public static final String EXTRA_APPEND_TO_FOLDER = "APPEND_TO_FOLDER";
	/**
	 * Games (puzzles) to import. String should be in this format:
	 * 120001232...0041\n 456000213...1100\n
	 */
	public static final String EXTRA_GAMES = "GAMES";
	
	private static final String TAG = "ImportSudokuActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.import_sudoku);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.opensudoku);

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);

		AbstractImportTask importTask;
		ImportOptions importOptions = new ImportOptions();
		Intent intent = getIntent();
		Uri dataUri = intent.getData();
		if (dataUri != null) {
			if (intent.getType() == "application/x-opensudoku"
					|| dataUri.toString().endsWith(".opensudoku")) {
				
				importTask = new ImportOpenSudokuTask(this, progressBar);
				importOptions.setUri(dataUri);
			
			} else if (dataUri.toString().endsWith(".sdm")) {

				importTask = new SdmImportTask(this, progressBar);
				importOptions.setUri(dataUri);
			
			} else {
				
				Log.e(
					TAG,
					String.format(
						"Unknown type of data provided (mime-type=%s; uri=%s), exiting.",
						intent.getType(), dataUri));
				finish();
				return;
			
			}
		} else if (intent.getStringExtra(EXTRA_FOLDER_NAME) != null) {
			
			String folderName = intent.getStringExtra(EXTRA_FOLDER_NAME);
			String games = intent.getStringExtra(EXTRA_GAMES);
			boolean appendToFolder = intent.getBooleanExtra(
					EXTRA_APPEND_TO_FOLDER, false);
			importTask = new StringImportTask(this, progressBar, games);
			importOptions.setFolderName(folderName);
			importOptions.setAppendToFolder(appendToFolder);

		} else {
			Log.e(TAG, "No data provided, exiting.");
			finish();
			return;
		}

		importTask.setOnImportFinishedListener(mOnImportFinishedListener);
		importTask.execute(importOptions);
	}
	
	private OnImportFinishedListener mOnImportFinishedListener = new OnImportFinishedListener() {
		
		@Override
		public void onImportFinished(boolean importSuccessful, long folderId) {
			if (importSuccessful) { 
				Intent i = new Intent(ImportSudokuActivity.this,
						 SudokuListActivity.class);
						 i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, folderId);
						 startActivity(i);
			}
			// call finish, so this activity won't be part of history
			finish();
		}
	};
}
