package cz.romario.opensudoku.gui.importing;

import android.content.Context;
import android.widget.ProgressBar;

public class StringImportTask extends AbstractImportTask {

	private String mGames;

	public StringImportTask(Context context, ProgressBar progressBar, String games) {
		super(context, progressBar);
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
