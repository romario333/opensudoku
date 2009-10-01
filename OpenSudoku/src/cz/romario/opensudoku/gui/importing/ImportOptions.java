package cz.romario.opensudoku.gui.importing;

import android.net.Uri;

public class ImportOptions {
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
