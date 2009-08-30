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
import cz.romario.opensudoku.game.SudokuGame;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ImportSudokuActivity extends Activity {

	public static final String EXTRA_FOLDER_NAME = "FOLDER_NAME";
	public static final String EXTRA_GAMES = "GAMES";
	
	private static final String TAG = "ImportSudokuActivity";
	static final Pattern SUDOKU_PATT = Pattern.compile(".*\\D([\\d]{81})\\D.*");
	private ProgressBar mProgress;

	private abstract class AbstractImportTask extends
			AsyncTask<Uri, Integer, Boolean> {
		static final int MAX_FOLDER_SIZE = 250;
		static final int NUM_OF_PROGRESS_UPDATES = 20;

		private FolderInfo mFolderInfo = new FolderInfo();
		private List<String> mGames = new ArrayList<String>();
		private String mImportError;

		@Override
		protected Boolean doInBackground(Uri... params) {

			if (params != null && params.length != 1) {
				throw new IllegalArgumentException("Only one URI expected.");
			}

			try {
				return importUri(params != null ? params[0] : null);
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
				i.putExtra(SudokuListActivity.EXTRAS_FOLDER_ID, mFolderInfo.id);
				startActivity(i);
			} else {
				Toast.makeText(ImportSudokuActivity.this, mImportError,
						Toast.LENGTH_LONG).show();
			}

			// call finish, so this activity won't be part of history
			finish();
		}

		private Boolean importUri(Uri uri) {

			// let subclass handle the URI
			processUri(uri);

			if (mGames.size() == 0) {
				setError(getString(R.string.no_puzzles_found));
				return false;
			}

			publishProgress(0, mGames.size());

			SudokuDatabase sudokuDB = new SudokuDatabase(
					getApplicationContext());

			// TODO: quick & dirty version
			long start = System.currentTimeMillis();
			long folderID = -1;
			int updateStatusEveryNItems = 1;
			if (mGames.size() > NUM_OF_PROGRESS_UPDATES) {
				updateStatusEveryNItems = mGames.size() / NUM_OF_PROGRESS_UPDATES;
			}
			try {
				sudokuDB.beginTransaction();

				folderID = sudokuDB.insertFolder(mFolderInfo.name);
				for (int i = 0; i < mGames.size(); i++) {
					try {
						sudokuDB.insertSudokuImport(folderID, mGames.get(i));
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
			mFolderInfo.id = folderID;

			long end = System.currentTimeMillis();

			Log.i(TAG, String.format("Imported in %f seconds.",
					(end - start) / 1000f));

			return true;
		}

		protected abstract Boolean processUri(Uri uri);

		protected void setFolderName(String name) {
			mFolderInfo.name = name;
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
		protected Boolean processUri(Uri uri) {
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

				setFolderName(folderName);

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
		protected Boolean processUri(Uri uri) {
			setFolderName(uri.getLastPathSegment());

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

		private String mFolderName;
		private String mGames;
		
		public StringImportTask(String folderName, String games) {
			mFolderName = folderName;
			mGames = games;
		}
		
		@Override
		protected Boolean processUri(Uri uri) {
			setFolderName(mFolderName);
			
			for (String game : mGames.split("\n")) {
				importGame(game);
			}
			
			return true;
		}
		
	}
		
	

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
			if (intent.getType() == "application/x-opensudoku" || dataUri.toString().endsWith(".opensudoku")) {
				new ImportOpenSudokuTask().execute(dataUri);
			} else if (dataUri.toString().endsWith(".sdm")) {
				new SdmImportTask().execute(dataUri);
			} else {
				Log.e(TAG, String.format("Unknown type of data provided (mime-type=%s; uri=%s), exiting.", intent.getType(), dataUri));
				finish();
				return;
			}
		} else if (intent.getStringExtra(EXTRA_FOLDER_NAME) != null) {
			String folderName = intent.getStringExtra(EXTRA_FOLDER_NAME);
			String games = intent.getStringExtra(EXTRA_GAMES);
			new StringImportTask(folderName, games).execute(null);
		} else {
			Log.e(TAG, "No data provided, exiting.");
			finish();
			return;
		}
	}
}
