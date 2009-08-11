package cz.romario.opensudoku.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * This activity handles import of files with extension.
 * 
 * It's sole purpose is to catch intents to view files with .opensudoku
 * extension and forward it to the ImportSudokuList activity.
 * 
 * I'm doing it this way, because I don't know how to add this kind of
 * intent filtering to the ImportSudokuList activity.
 * 
 * @author romario
 *
 */
public class ImportFileActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = new Intent(this, ImportSudokuActivity.class);
		i.setData(getIntent().getData());
		startActivity(i);
		finish();
	}

}
