package cz.romario.opensudoku.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.FolderInfo;
import cz.romario.opensudoku.game.SudokuGame;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

// TODO: quick and dirty version
public class ImportSudokuActivity extends Activity {

	private static final String TAG = "ImportSudokuActivity";

	private ProgressBar mProgress;

	private class ImportSudokuTask extends AsyncTask<Uri, Integer, FolderInfo> {

		@Override
		protected FolderInfo doInBackground(Uri... params) {

			if (params.length != 1) {
				throw new IllegalArgumentException("Only one URI expected.");
			}

			return importUri(params[0]);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (values.length == 2) {
				mProgress.setMax(values[1]);
			}
			mProgress.setProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(FolderInfo result) {
			Toast.makeText(ImportSudokuActivity.this, getString(
					R.string.puzzles_saved, result.name), Toast.LENGTH_LONG).show();
			
			Intent i = new Intent(ImportSudokuActivity.this, SudokuListActivity.class);
			i.putExtra(SudokuListActivity.EXTRAS_FOLDER_ID, result.id);
			startActivity(i);
			
			// call finish, so this activity won't be part of history
			finish();
		}

		public FolderInfo importUri(android.net.Uri auri) {
			Log.i(TAG, auri.toString());
			try {
				java.net.URI juri;
				juri = new java.net.URI(auri.getScheme(), auri
						.getSchemeSpecificPart(), auri.getFragment());
				InputStreamReader isr = new InputStreamReader(juri.toURL()
						.openStream());
				FolderInfo newFolderInfo;
				try {
					newFolderInfo = importXml(isr);
				} finally {
					isr.close();
				}
				return newFolderInfo;
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}

		public FolderInfo importXml(Reader in) {
			String name = "import";
			List<String> games = new ArrayList<String>();

			BufferedReader inBR = new BufferedReader(in);
			/*
			 * while((s=in.readLine())!=null){ Log.i(tag, "radek: "+s); }
			 */

			// parse xml
			XmlPullParserFactory factory;
			XmlPullParser xpp;
			try {
				factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				xpp = factory.newPullParser();
				xpp.setInput(inBR);
				int eventType = xpp.getEventType();
				String lastTag = "";
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						lastTag = xpp.getName();
					} else if (eventType == XmlPullParser.END_TAG) {
						lastTag = "";
					} else if (eventType == XmlPullParser.TEXT) {
						if (lastTag.equals("name")) {
							name = xpp.getText();
						} else if (lastTag.equals("game")) {
							games.add(xpp.getText());
						}

					}
					eventType = xpp.next();
				}
			} catch (XmlPullParserException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			SudokuGame sudoku = new SudokuGame();

			publishProgress(0, games.size());

			SudokuDatabase sudokuDB = new SudokuDatabase(
					getApplicationContext());
			
			long start = System.currentTimeMillis();
			long folderID = -1;
			try {

				// store to db
				folderID = sudokuDB.insertFolder(name);
				for (int i = 0; i < games.size(); i++) {
					sudoku.parseString(games.get(i));
					sudokuDB.insertSudokuFast(folderID, sudoku);
					// if (i % 10 == 0) {
					publishProgress(i);
					// }
				}
			} finally {
				if (sudokuDB != null) {
					sudokuDB.close();
				}
			}
			
			long end = System.currentTimeMillis();
			
			Log.i(TAG, String.format("Imported in %f seconds.", (end - start) / 1000f));
			
			return new FolderInfo(folderID, name);
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
		String action = intent.getAction();
		if (action != null && action.equals(Intent.ACTION_VIEW)
				&& intent.getData() != null) {
			new ImportSudokuTask().execute(intent.getData());
		} else {
			Log.e(TAG, "No data provided, exiting.");
			finish();
			return;
		}
	}
}
