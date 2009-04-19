package cz.romario.opensudoku.gui;

import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.SudokuCellCollection;
import cz.romario.opensudoku.game.SudokuGame;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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

    private int mState;
    private long mFolderID;
    private long mSudokuID;
    
    private SudokuDatabase mSudokuDB;
    private SudokuBoardView mBoard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sudoku_edit);

		Button buttonSave = (Button)findViewById(R.id.button_save);
		Button buttonCancel = (Button)findViewById(R.id.button_cancel);
		mBoard = (SudokuBoardView)findViewById(R.id.sudoku_board);
		
		buttonSave.setOnClickListener(buttonSaveClickListener);
		buttonCancel.setOnClickListener(buttonCancelClickListener);
		
        mSudokuDB = new SudokuDatabase(this);
		
		Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            mState = STATE_EDIT;
            if (intent.hasExtra(EXTRAS_SUDOKU_ID)) {
            	mSudokuID = intent.getLongExtra(EXTRAS_SUDOKU_ID, 0);
            } else {
            	throw new IllegalArgumentException(String.format("Extra with key '%s' is required.", EXTRAS_SUDOKU_ID));
            }
        } else if (Intent.ACTION_INSERT.equals(action)) {
        	mState = STATE_INSERT;
        	mSudokuID = 0;
        	
            if (intent.hasExtra(EXTRAS_FOLDER_ID)) {
            	mFolderID = intent.getLongExtra(EXTRAS_FOLDER_ID, 0);
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
        	mBoard.setCells((SudokuCellCollection)savedInstanceState.getParcelable("cells"));
        } else {
        	if (mSudokuID != 0) {
        		// existing sudoku, read it from database
        		SudokuGame sudoku = mSudokuDB.getSudoku(mSudokuID);
        		SudokuCellCollection cells = sudoku.getCells();
        		cells.markAllCellsAsEditable();
        		mBoard.setCells(cells);
        	} else {
        		// new sudoku
        		mBoard.setCells(SudokuCellCollection.createEmpty());
        	}
        }
        
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putParcelable("cells", mBoard.getCells());
	}

	private OnClickListener buttonSaveClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			SudokuCellCollection cells = mBoard.getCells();
			cells.markFilledCellsAsNotEditable();
			
			switch (mState) {
			case STATE_EDIT:
				// TODO: figure out how to handle edit properly - what to do with cell's editable state?
				SudokuGame game = mSudokuDB.getSudoku(mSudokuID);
				game.setCells(cells);
				mSudokuDB.updateSudoku(game);
				break;
			case STATE_INSERT:
				mSudokuDB.insertSudoku(mFolderID, cells);
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
