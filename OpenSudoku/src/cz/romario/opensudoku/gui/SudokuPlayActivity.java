package cz.romario.opensudoku.gui;

import java.util.Formatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.game.SudokuGame.OnPuzzleSolvedListener;
import cz.romario.opensudoku.gui.EditCellDialog.OnNoteEditListener;
import cz.romario.opensudoku.gui.EditCellDialog.OnNumberEditListener;
import cz.romario.opensudoku.gui.SudokuBoardView.OnCellTapListener;

/*
 * TODO:
 * - timer does not work properly
 * - sudoku list (v detailu stav, cas a tak)
 */
public class SudokuPlayActivity extends Activity{
	
	public static final int MENU_ITEM_RESTART = Menu.FIRST;
	public static final int MENU_ITEM_CLEAR_ALL_NOTES = Menu.FIRST + 1;
	public static final int MENU_ITEM_UNDO = Menu.FIRST + 2;
	public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 3;
	
	
	//private static final String TAG = "SudokuPlayActivity";
	
	public static final String EXTRAS_SUDOKU_ID = "sudoku_id";
	
	private static final int DIALOG_RESTART = 1;
	private static final int DIALOG_WELL_DONE = 2;
	private static final int DIALOG_CLEAR_NOTES = 3;
	
	private long sudokuGameID;
	private SudokuGame sudokuGame;
	
	private SudokuBoardView sudokuBoard;
	
	private StringBuilder timeText;
	private Formatter timeFormatter;
	
	private GameTimer gameTimer;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sudoku_play);
        
        sudokuBoard = (SudokuBoardView)findViewById(R.id.sudoku_board);
        timeText = new StringBuilder(5);
        timeFormatter = new Formatter(timeText);
        gameTimer = new GameTimer();
        
        // create sudoku game instance
        if (savedInstanceState == null) {
        	// activity runs for the first time, read game from database
        	sudokuGameID = getIntent().getLongExtra(EXTRAS_SUDOKU_ID, 0);
        	SudokuDatabase sudokuDB = new SudokuDatabase(this);
        	sudokuGame = sudokuDB.getSudoku(sudokuGameID);
        	//gameTimer.setTime(sudokuGame.getTime());
        } else {
        	// activity has been running before, restore its state
        	sudokuGame = (SudokuGame)savedInstanceState.getParcelable("sudoku_game");
        	gameTimer.restoreState(savedInstanceState);
        }
        
        if (sudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
        	sudokuGame.start();
        } else if (sudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
        	sudokuGame.resume();
        } 
        
        if (sudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
        	sudokuBoard.setReadOnly(true);
        }

        sudokuBoard.setGame(sudokuGame);
		sudokuGame.setOnPuzzleSolvedListener(onSolvedListener);
		
		updateTime();
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (sudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			sudokuGame.resume();
			gameTimer.start();
		}
		
        // read game settings
		SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean highlightWrongValues = gameSettings.getBoolean("highlight_wrong_values", true);
        sudokuGame.setHighlightWrongVals(highlightWrongValues);
		
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
		
    	if (sudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			sudokuGame.pause();
		}
    	
    	// we will save game to the database as we might not be able to get back
		SudokuDatabase sudokuDB = new SudokuDatabase(SudokuPlayActivity.this);
		sudokuDB.updateSudoku(sudokuGame);
		
		gameTimer.stop();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
		
    	gameTimer.stop();
    	outState.putParcelable("sudoku_game", sudokuGame);
    	gameTimer.saveState(outState);
    }	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
        menu.add(0, MENU_ITEM_UNDO, 0, R.string.undo)
        .setShortcut('1', 'u')
        .setIcon(android.R.drawable.ic_menu_revert);
		
		// TODO: I should really get my own icons ;-)
        menu.add(0, MENU_ITEM_CLEAR_ALL_NOTES, 0, R.string.clear_all_notes)
        .setShortcut('3', 'a')
        .setIcon(android.R.drawable.ic_menu_delete);

        menu.add(0, MENU_ITEM_RESTART, 1, R.string.restart)
        .setShortcut('7', 'r')
        .setIcon(android.R.drawable.ic_menu_rotate);

        menu.add(0, MENU_ITEM_SETTINGS, 1, R.string.settings)
        .setShortcut('9', 's')
        .setIcon(android.R.drawable.ic_menu_preferences);
        
        

        

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
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.findItem(MENU_ITEM_UNDO).setEnabled(sudokuGame.hasSomethingToUndo());
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_RESTART:
        	showDialog(DIALOG_RESTART);
            return true;
        case MENU_ITEM_CLEAR_ALL_NOTES:
        	showDialog(DIALOG_CLEAR_NOTES);
        	return true;
        case MENU_ITEM_UNDO:
        	sudokuGame.undo();
        	sudokuBoard.postInvalidate();
        	return true;
        case MENU_ITEM_SETTINGS:
        	Intent i = new Intent();
        	i.setClass(this, GameSettingsActivity.class);
        	startActivity(i);
        	return true;
        }
        return super.onOptionsItemSelected(item);
	}
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id){
//    	case DIALOG_SELECT_NUMBER:
//    		return selectNumberDialog.getDialog();
//    	case DIALOG_SELECT_MULTIPLE_NUMBERS:
//			return selectMultipleNumbersDialog.getDialog();
    	case DIALOG_WELL_DONE:
            return new AlertDialog.Builder(SudokuPlayActivity.this)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle(R.string.well_done)
            .setMessage(getString(R.string.congrats, getTime()))
            .setPositiveButton(android.R.string.ok, null)
            .create();
    	case DIALOG_RESTART:
            return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_menu_rotate)
            .setTitle(R.string.app_name)
            .setMessage(R.string.restart_confirm)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Restart game
                	sudokuGame.reset();
                	sudokuGame.start();
                	sudokuBoard.setReadOnly(false);
                	sudokuBoard.postInvalidate();
                	gameTimer.start();
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .create();
    	case DIALOG_CLEAR_NOTES:
            return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_menu_delete)
            .setTitle(R.string.app_name)
            .setMessage(R.string.clear_all_notes_confirm)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	sudokuGame.clearAllNotes();
                	sudokuBoard.postInvalidate();
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .create();
    	}
    	return null;
    }
    
    /**
     * Occurs when puzzle is solved.
     */
    private OnPuzzleSolvedListener onSolvedListener = new OnPuzzleSolvedListener() {

		@Override
		public void onPuzzleSolved() {
			sudokuBoard.setReadOnly(true);
			sudokuBoard.postInvalidate();
			showDialog(DIALOG_WELL_DONE);
		}
    	
    };
    
	// TODO: can Chronometer replace this?
	/**
     * Update the time of game-play.
     */
	void updateTime() {
		setTitle(getTime());
	}
	
	public String getTime() {
		long time = sudokuGame.getTime();
		timeText.setLength(0);
		timeFormatter.format("%02d:%02d", time / 60000, time / 1000 % 60);
		return timeText.toString();
	}
	
	// This class implements the game clock.  All it does is update the
    // status each tick.
	private final class GameTimer extends Timer {
		
		GameTimer() {
    		super(1000);
    	}
		
    	@Override
		protected boolean step(int count, long time) {
    		updateTime();
            
            // Run until explicitly stopped.
            return false;
        }
        
	}
	
}
