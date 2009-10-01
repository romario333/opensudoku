package cz.romario.opensudoku.gui.importing;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.db.SudokuInvalidFormatException;
import cz.romario.opensudoku.game.FolderInfo;
import cz.romario.opensudoku.utils.Const;

public abstract class AbstractImportTask extends
		AsyncTask<ImportOptions, Integer, Boolean> {
	static final int NUM_OF_PROGRESS_UPDATES = 20;

	private Context mContext;
	private ProgressBar mProgressBar;
	
	private OnImportFinishedListener mOnImportFinishedListener;
	
	private ImportOptions mOptions;
	private FolderInfo mFolderInfo;
	private List<String> mGames = new ArrayList<String>();
	private String mImportError;

	public AbstractImportTask(Context context, ProgressBar progressBar) {
		mContext = context;
		mProgressBar = progressBar;
	}
	
	public void setOnImportFinishedListener(OnImportFinishedListener listener) {
		mOnImportFinishedListener = listener;
	}
	
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
			Log.e(Const.TAG, "Exception occurred during import.", e);
			setError(mContext.getString(R.string.unknown_import_error));
		}

		return false;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values.length == 2) {
			mProgressBar.setMax(values[1]);
		}
		mProgressBar.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {

			Toast
					.makeText(
							mContext,
							mContext.getString(R.string.puzzles_saved,
									mFolderInfo.name), Toast.LENGTH_LONG)
					.show();

		} else {
			Toast.makeText(mContext, mImportError, Toast.LENGTH_LONG).show();
		}

		if (mOnImportFinishedListener != null) {
			mOnImportFinishedListener.onImportFinished(result, mFolderInfo.id);
		}
	}

	private Boolean processImport(ImportOptions options) {
		mOptions = new ImportOptions(options);

		// let subclass handle the import
		processImport();

		if (mGames.size() == 0) {
			setError(mContext.getString(R.string.no_puzzles_found));
			return false;
		}

		publishProgress(0, mGames.size());

		SudokuDatabase sudokuDB = new SudokuDatabase(mContext);

		// TODO: quick & dirty version
		long start = System.currentTimeMillis();
		int updateStatusEveryNItems = 1;
		if (mGames.size() > NUM_OF_PROGRESS_UPDATES) {
			updateStatusEveryNItems = mGames.size() / NUM_OF_PROGRESS_UPDATES;
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
					setError(mContext.getString(R.string.invalid_format));
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

		Log.i(Const.TAG, String.format("Imported in %f seconds.",
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
	 * Gets import options. Note that subclasses can and will modify contents of
	 * options object.
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
	
	public interface OnImportFinishedListener
	{
		/**
		 * Occurs when import is finished.
		 * 
		 * @param importSuccessful Indicates whether import was successful.
		 * @param folderId Contains id of imported folder, or -1 if multiple folders were imported.
		 */
		void onImportFinished(boolean importSuccessful, long folderId);
	}
	
}
