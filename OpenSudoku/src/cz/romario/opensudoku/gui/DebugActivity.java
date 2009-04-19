package cz.romario.opensudoku.gui;

import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.SudokuDatabase;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DebugActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.debug);
		
		Button genButton = (Button)findViewById(R.id.generate_puzzles);
		genButton.setOnClickListener(generateOnClick);
		
		
	}
	
	private OnClickListener generateOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			EditText numOfFoldersInput  = (EditText)findViewById(R.id.num_of_folders);
			EditText numOfFoldersPuzzles  = (EditText)findViewById(R.id.num_of_puzzles);
			int numOfFolders = Integer.parseInt(numOfFoldersInput.getText().toString());
			int numOfPuzzles = Integer.parseInt(numOfFoldersPuzzles.getText().toString());
			
			SudokuDatabase db = new SudokuDatabase(DebugActivity.this);
			db.generateDebugPuzzles(numOfFolders, numOfPuzzles);
			
		}
		
	};
}
