package cz.romario.opensudoku;

import cz.romario.opensudoku.db.SudokuDatabase;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SudokuEditActivity extends Activity {
	private static final String TAG = "SudokuEditActivity";
	
	/**
	 * When inserting new data, I need to know folder in which will new sudoku be stored.
	 */
	public static final String EXTRAS_FOLDER_ID = "folder_id";
	public static final String EXTRAS_SUDOKU_ID = "sudoku_id";
	
	// The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private int state;
    private long folderID;
    private long sudokuID;
    
    private SudokuDatabase sudokuDB;
    private TextView sudokuData;
    private TextView sudokuName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sudoku_edit);

		Button buttonSave = (Button)findViewById(R.id.button_save);
		Button buttonCancel = (Button)findViewById(R.id.button_cancel);
		sudokuData = (TextView)findViewById(R.id.sudoku_data);
		sudokuName = (TextView)findViewById(R.id.sudoku_name);
		
		buttonSave.setOnClickListener(buttonSaveClickListener);
		buttonCancel.setOnClickListener(buttonCancelClickListener);
		
        sudokuDB = new SudokuDatabase(this);
		
		Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            state = STATE_EDIT;
            if (intent.hasExtra(EXTRAS_SUDOKU_ID)) {
            	sudokuID = intent.getLongExtra(EXTRAS_SUDOKU_ID, 0);
            } else {
            	throw new IllegalArgumentException(String.format("Extra with key '%s' is required.", EXTRAS_SUDOKU_ID));
            }
        } else if (Intent.ACTION_INSERT.equals(action)) {
        	state = STATE_INSERT;
        	sudokuID = 0;
        	
            if (intent.hasExtra(EXTRAS_FOLDER_ID)) {
            	folderID = intent.getLongExtra(EXTRAS_FOLDER_ID, 0);
            } else {
            	throw new IllegalArgumentException(String.format("Extra with key '%s' is required.", EXTRAS_FOLDER_ID));
            }
        	
        } else {
            // Whoops, unknown action!  Bail.
            Log.e(TAG, "Unknown action, exiting.");
            finish();
            return;
        }
        
        if (savedInstanceState != null) {
    		sudokuData.setText(savedInstanceState.getString("sudoku_data"));
    		sudokuName.setText(savedInstanceState.getString("sudoku_name"));
        } else {
        	if (sudokuID != 0) {
        		// existing sudoku, read it from database
        		SudokuGame sudoku = sudokuDB.getSudoku(sudokuID);
        		sudokuData.setText(sudoku.getCells().toString());
        		sudokuName.setText(sudoku.getName());
        	}
        }
        
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		
		outState.putString("sudoku_data", sudokuData.getText().toString());
		outState.putString("sudoku_name", sudokuName.getText().toString());
	}

	private OnClickListener buttonSaveClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String data = sudokuData.getText().toString();
			String name = sudokuName.getText().toString();
			
			
			switch (state) {
			case STATE_EDIT:
				SudokuGame game = sudokuDB.getSudoku(sudokuID);
				game.setName(name);
				game.getCells().updateFromString(data, false);
				sudokuDB.updateSudoku(game);
				break;
			case STATE_INSERT:
				SudokuCellCollection cells = SudokuCellCollection.CreateEmpty();
				cells.updateFromString(data, true);
				sudokuDB.insertSudoku(folderID, name, cells);
				break;
			}
			
			finish();
		}
		
	};

	private OnClickListener buttonCancelClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
		
	};

	
}
