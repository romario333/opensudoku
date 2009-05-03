package cz.romario.opensudoku.gui;

import java.util.Date;

import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.inputmethod.IMControlPanel;
import cz.romario.opensudoku.gui.inputmethod.IMNumpad;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SudokuEditActivity extends Activity {
	private static final String TAG = "SudokuEditActivity";
	
	/**
	 * When inserting new data, I need to know folder in which will new sudoku be stored.
	 */
	public static final String EXTRAS_FOLDER_ID = "folder_id";
	public static final String EXTRAS_SUDOKU_ID = "sudoku_id";
	
	public static final int MENU_ITEM_SAVE = Menu.FIRST;
	public static final int MENU_ITEM_CANCEL = Menu.FIRST + 1;
	
	// The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private int mState;
    private long mFolderID;
    private long mSudokuID;
    
    private SudokuDatabase mSudokuDB;
    private SudokuGame mGame;
    private SudokuBoardView mBoard;
    private IMControlPanel mInputMethods;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// TODO: landscape
			setContentView(R.layout.sudoku_edit);
		} else {
			setContentView(R.layout.sudoku_edit);
		}

		mBoard = (SudokuBoardView)findViewById(R.id.sudoku_board);
		
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
        	mGame = (SudokuGame)savedInstanceState.getParcelable("game");
        } else {
        	if (mSudokuID != 0) {
        		// existing sudoku, read it from database
        		mGame = mSudokuDB.getSudoku(mSudokuID);
        		mGame.getCells().markAllCellsAsEditable();
        	} else {
        		mGame = SudokuGame.createEmptyGame();
        	}
        }
        mBoard.setGame(mGame);
        
        // TODO: OMG, rethink input methods initialization !
        mInputMethods = (IMControlPanel)findViewById(R.id.input_methods);
        mInputMethods.setGame(mGame);
        mInputMethods.setBoard(mBoard);
        mInputMethods.addInputMethod(new IMNumpad());
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putParcelable("game", mGame);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        // This is our one standard application action -- inserting a
        // new note into the list.
		menu.add(0, MENU_ITEM_SAVE, 0, R.string.save)
                .setShortcut('1', 's')
                .setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, MENU_ITEM_CANCEL, 1, android.R.string.cancel)
	        .setShortcut('3', 'c')
	        .setIcon(android.R.drawable.ic_menu_close_clear_cancel);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, FolderListActivity.class), null, intent, 0, null);

        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case MENU_ITEM_SAVE:
			mGame.getCells().markFilledCellsAsNotEditable();
			
			switch (mState) {
			case STATE_EDIT:
				mSudokuDB.updateSudoku(mGame);
				break;
			case STATE_INSERT:
				mGame.setCreated(new Date());
				mSudokuDB.insertSudoku(mFolderID, mGame);
				break;
			}
			
			finish();
            return true;
        case MENU_ITEM_CANCEL:
        	finish();
        	return true;
        }
        return super.onOptionsItemSelected(item);
	}
}
