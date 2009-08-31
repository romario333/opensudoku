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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.db.SudokuInvalidFormatException;
import cz.romario.opensudoku.game.FolderInfo;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

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
	private static final Pattern SUDOKU_PATT = Pattern.compile(".*\\D([\\d]{81})\\D.*");
	
	private ProgressBar mProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.import_sudoku);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.opensudoku);

		mProgress = (ProgressBar) findViewById(R.id.progress);

		Intent intent = getIntent();
		Uri dataUri = intent.getData();
		if (dataUri != null) {
			if (intent.getType() == "application/x-opensudoku"
					|| dataUri.toString().endsWith(".opensudoku")) {
				new ImportOpenSudokuTask().execute(new ImportOptions()
						.setUri(dataUri));
			} else if (dataUri.toString().endsWith(".sdm")) {
				new SdmImportTask()
						.execute(new ImportOptions().setUri(dataUri));
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
			new StringImportTask(games).execute(new ImportOptions()
					.setFolderName(folderName)
					.setAppendToFolder(appendToFolder));
		} else {
			Log.e(TAG, "No data provided, exiting.");
			finish();
			return;
		}
	}
	
	private static class ImportOptions {
		private Uri mUri;
		private String mFolderName;
		private boolean mAppendToFolder;
		
		public ImportOptions() {
			
		}
		
		/**
		 * Copy constructor.
		 * 
		 * @param options
		 */
		public ImportOptions(ImportOptions options) {
			mUri = options.getUri();
			mFolderName = options.getFolderName();
			mAppendToFolder = options.isAppendToFolder();
		}
		
		public Uri getUri() {
			return mUri;
		}

		public ImportOptions setUri(Uri uri) {
			mUri = uri;
			return this;
		}

		public String getFolderName() {
			return mFolderName;
		}

		public ImportOptions setFolderName(String folderName) {
			mFolderName = folderName;
			return this;
		}

		public boolean isAppendToFolder() {
			return mAppendToFolder;
		}

		public ImportOptions setAppendToFolder(boolean appendToFolder) {
			mAppendToFolder = appendToFolder;
			return this;
		}
	}

	private abstract class AbstractImportTask extends
			AsyncTask<ImportOptions, Integer, Boolean> {
		static final int NUM_OF_PROGRESS_UPDATES = 20;

		private ImportOptions mOptions;
		
		private FolderInfo mFolderInfo;
		private List<String> mGames = new ArrayList<String>();
		private String mImportError;

		@Override
		protected Boolean doInBackground(ImportOptions... params) {

			if (params == null) {
				throw new IllegalArgumentException("Import options expected.");
			}

			if (params.length != 1) {
				throw new IllegalArgumentException("Only one param expected.");
			}

			try {
				return processImport(params[0]);
			} catch (Exception e) {
				Log.e(TAG, "Exception occurred during import.", e);
				setError(getString(R.string.unknown_import_error));
			}

			return false;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (values.length == 2) {
				mProgress.setMax(values[1]);
			}
			mProgress.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (result) {
				Toast.makeText(ImportSudokuActivity.this,
						getString(R.string.puzzles_saved, mFolderInfo.name),
						Toast.LENGTH_LONG).show();

				Intent i = new Intent(ImportSudokuActivity.this,
						SudokuListActivity.class);
				i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, mFolderInfo.id);
				startActivity(i);
			} else {
				Toast.makeText(ImportSudokuActivity.this, mImportError,
						Toast.LENGTH_LONG).show();
			}

			// call finish, so this activity won't be part of history
			finish();
		}

		private Boolean processImport(ImportOptions options) {
			mOptions = new ImportOptions(options);
			

			// let subclass handle the import
			processImport();

			if (mGames.size() == 0) {
				setError(getString(R.string.no_puzzles_found));
				return false;
			}

			publishProgress(0, mGames.size());

			SudokuDatabase sudokuDB = new SudokuDatabase(
					getApplicationContext());

			// TODO: quick & dirty version
			long start = System.currentTimeMillis();
			int updateStatusEveryNItems = 1;
			if (mGames.size() > NUM_OF_PROGRESS_UPDATES) {
				updateStatusEveryNItems = mGames.size()
						/ NUM_OF_PROGRESS_UPDATES;
			}
			try {
				sudokuDB.beginTransaction();

				if (mOptions.isAppendToFolder()) {
					mFolderInfo = sudokuDB.findFolder(mOptions.getFolderName());
				}
				
				if (mFolderInfo == null) {
					mFolderInfo = new FolderInfo();
					mFolderInfo.name = mOptions.getFolderName();
					mFolderInfo.id = sudokuDB.insertFolder(mFolderInfo.name); 
				}
				

				for (int i = 0; i < mGames.size(); i++) {
					try {
						sudokuDB.insertSudokuImport(mFolderInfo.id, mGames.get(i));
					} catch (SudokuInvalidFormatException e) {
						setError(getString(R.string.invalid_format));
						return false;
					}

					if (i % updateStatusEveryNItems == 0) {
						publishProgress(i);
					}
				}
				sudokuDB.setTransactionSuccessful();
			} finally {
				sudokuDB.endTransaction();
				sudokuDB.close();
			}

			long end = System.currentTimeMillis();

			Log.i(TAG, String.format("Imported in %f seconds.",
					(end - start) / 1000f));

			return true;
		}

		/**
		 * Subclasses should do all import work in this method.
		 * 
		 * @return
		 */
		protected abstract Boolean processImport();

		/**
		 * Gets import options. Note that subclasses can and will modify
		 * contents of options object.
		 * 
		 * @return
		 */
		protected ImportOptions getOptions() {
			return mOptions;
		}

		protected void importGame(String game) {
			mGames.add(game);
		}

		protected void setError(String error) {
			mImportError = error;
		}
	}

	private class ImportOpenSudokuTask extends AbstractImportTask {

		@Override
		protected Boolean processImport() {
			Uri uri = getOptions().getUri();
			try {
				java.net.URI juri;
				juri = new java.net.URI(uri.getScheme(), uri
						.getSchemeSpecificPart(), uri.getFragment());
				InputStreamReader isr = new InputStreamReader(juri.toURL()
						.openStream());
				try {
					return importXml(isr);
				} finally {
					isr.close();
				}
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private boolean importXml(Reader in) {
			String folderName = "import";

			BufferedReader inBR = new BufferedReader(in);
			/*
			 * while((s=in.readLine())!=null){ Log.i(tag, "radek: "+s); }
			 */

			// parse xml
			XmlPullParserFactory factory;
			XmlPullParser xpp;
			try {
				factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(false);
				xpp = factory.newPullParser();
				xpp.setInput(inBR);
				int eventType = xpp.getEventType();
				String lastTag = "";
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						lastTag = xpp.getName();
						if (lastTag.equals("game")) {
							importGame(xpp.getAttributeValue(null, "data"));
						}
					} else if (eventType == XmlPullParser.END_TAG) {
						lastTag = "";
					} else if (eventType == XmlPullParser.TEXT) {
						if (lastTag.equals("name")) {
							folderName = xpp.getText();
						} else if (lastTag.equals("parse-page")) {
							// download page and find sudoku strings
							URL url = new URL(xpp.getText());
							InputStreamReader isr = new InputStreamReader(url
									.openStream());
							BufferedReader br = new BufferedReader(isr);
							String s;
							while ((s = br.readLine()) != null) {
								Matcher m = SUDOKU_PATT.matcher(s);
								if (m.find()) {
									importGame(m.group(1));
								}
							}
						}

					}
					eventType = xpp.next();
				}

				getOptions().setFolderName(folderName);

				return true;
			} catch (XmlPullParserException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private class SdmImportTask extends AbstractImportTask {

		@Override
		protected Boolean processImport() {
			Uri uri = getOptions().getUri();
			getOptions().setFolderName(uri.getLastPathSegment());

			try {
				URL url = new URL(uri.toString());
				InputStreamReader isr = new InputStreamReader(url.openStream());
				BufferedReader br = new BufferedReader(isr);
				String s;
				while ((s = br.readLine()) != null) {
					if (!s.equals("")) {
						importGame(s);
					}
				}

				return true;
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}
	
	private class StringImportTask extends AbstractImportTask {

		private String mGames;

		public StringImportTask(String games) {
			mGames = games;
		}
		
		@Override
		protected Boolean processImport() {
			for (String game : mGames.split("\n")) {
				importGame(game);
			}
			
			return true;
		}
		
	}

}
