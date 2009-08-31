/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package cz.romario.opensudoku.gui;

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
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.game.SudokuGame.OnPuzzleSolvedListener;
import cz.romario.opensudoku.gui.inputmethod.IMControlPanel;
import cz.romario.opensudoku.gui.inputmethod.IMNumpad;
import cz.romario.opensudoku.gui.inputmethod.IMPopup;
import cz.romario.opensudoku.gui.inputmethod.IMSingleNumber;

/*
 */
public class SudokuPlayActivity extends Activity{
	
	public static final int MENU_ITEM_RESTART = Menu.FIRST;
	public static final int MENU_ITEM_CLEAR_ALL_NOTES = Menu.FIRST + 1;
	public static final int MENU_ITEM_FILL_IN_NOTES = Menu.FIRST + 2;
	public static final int MENU_ITEM_UNDO = Menu.FIRST + 3;
	public static final int MENU_ITEM_HELP = Menu.FIRST + 4; 
	public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 5;
	
	
	//private static final String TAG = "SudokuPlayActivity";
	
	public static final String EXTRAS_SUDOKU_ID = "sudoku_id";
	
	private static final int DIALOG_RESTART = 1;
	private static final int DIALOG_WELL_DONE = 2;
	private static final int DIALOG_CLEAR_NOTES = 3;

	private SudokuDatabase mDatabase;
	
	private long mSudokuGameID;
	private SudokuGame mSudokuGame;
	
	private SudokuBoardView mSudokuBoard;
	
	private IMControlPanel mInputMethods;
	private IMPopup mIMPopup;
	private IMSingleNumber mIMSingleNumber;
	private IMNumpad mIMNumpad;
	
	private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();
	
	private GameTimer mGameTimer;
	
	private HintsQueue mHintsQueue;
	
	private boolean mShowTime = true;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.sudoku_play);
		
		mDatabase = new SudokuDatabase(getApplicationContext());
    
		mHintsQueue = new HintsQueue(this);
		
        mSudokuBoard = (SudokuBoardView)findViewById(R.id.sudoku_board);
        
        mGameTimer = new GameTimer();
        
        
        
        // create sudoku game instance
        if (savedInstanceState == null) {
        	// activity runs for the first time, read game from database
        	mSudokuGameID = getIntent().getLongExtra(EXTRAS_SUDOKU_ID, 0);
        	mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
        	//gameTimer.setTime(sudokuGame.getTime());
        } else {
        	// activity has been running before, restore its state
        	mSudokuGame = (SudokuGame)savedInstanceState.getParcelable("sudoku_game");
        	mGameTimer.restoreState(savedInstanceState);
        }
        
        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
        	mSudokuGame.start();
        } else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
        	mSudokuGame.resume();
        } 
        
        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
        	mSudokuBoard.setReadOnly(true);
        }
        
        mSudokuBoard.setGame(mSudokuGame);
		mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);
		
		mHintsQueue.showOneTimeHint(R.string.welcome, R.string.first_run_hint);		
		
        mInputMethods = (IMControlPanel)findViewById(R.id.input_methods);
        mInputMethods.setBoard(mSudokuBoard);
        mInputMethods.setGame(mSudokuGame);
        mInputMethods.setHintsQueue(mHintsQueue);
        
        mIMPopup = mInputMethods.getInputMethod(IMControlPanel.INPUT_METHOD_POPUP);
        mIMSingleNumber = mInputMethods.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
        mIMNumpad = mInputMethods.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
        // read game settings
		SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean("highlight_wrong_values", true));

        mShowTime = gameSettings.getBoolean("show_time", true);
        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.resume();
			
			if (mShowTime) {
				mGameTimer.start();
			}
		}

        
        mIMPopup.enabled = gameSettings.getBoolean("im_popup", true);
        mIMSingleNumber.enabled = gameSettings.getBoolean("im_single_number", true);
        mIMNumpad.enabled = gameSettings.getBoolean("im_numpad", true);
        mIMNumpad.moveCellSelectionOnPress = gameSettings.getBoolean("im_numpad_move_right", false);

        mInputMethods.activateFirstInputMethod();

		updateTitle();
	}
	
	
	
    @Override
    protected void onPause() {
    	super.onPause();
		
    	if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.pause();
		}
    	
    	// we will save game to the database as we might not be able to get back
		mDatabase.updateSudoku(mSudokuGame);
		
		mGameTimer.stop();
		
		mInputMethods.pause();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mDatabase.close();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
		
    	mGameTimer.stop();
    	outState.putParcelable("sudoku_game", mSudokuGame);
    	mGameTimer.saveState(outState);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
        menu.add(0, MENU_ITEM_UNDO, 0, R.string.undo)
        .setShortcut('1', 'u')
        .setIcon(android.R.drawable.ic_menu_revert);
		
        menu.add(0, MENU_ITEM_CLEAR_ALL_NOTES, 0, R.string.clear_all_notes)
        .setShortcut('3', 'a')
        .setIcon(android.R.drawable.ic_menu_delete);

        menu.add(0, MENU_ITEM_FILL_IN_NOTES, 0, R.string.fill_in_notes)
        .setIcon(android.R.drawable.ic_menu_edit);
        
        menu.add(0, MENU_ITEM_RESTART, 1, R.string.restart)
        .setShortcut('7', 'r')
        .setIcon(android.R.drawable.ic_menu_rotate);

        menu.add(0, MENU_ITEM_HELP, 1, R.string.help)
        .setShortcut('0', 'h')
        .setIcon(android.R.drawable.ic_menu_help);
        
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
		
		menu.findItem(MENU_ITEM_UNDO).setEnabled(mSudokuGame.hasSomethingToUndo());
		
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
        case MENU_ITEM_FILL_IN_NOTES:
        	mSudokuGame.fillInNotes();
        	return true;
        case MENU_ITEM_UNDO:
        	mSudokuGame.undo();
        	return true;
        case MENU_ITEM_SETTINGS:
        	Intent i = new Intent();
        	i.setClass(this, GameSettingsActivity.class);
        	startActivity(i);
        	return true;
        case MENU_ITEM_HELP:
        	mHintsQueue.showHint(R.string.help, R.string.help_text);
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
            return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle(R.string.well_done)
            .setMessage(getString(R.string.congrats, mGameTimeFormatter.format(mSudokuGame.getTime())))
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
                	mSudokuGame.reset();
                	mSudokuGame.start();
                	mSudokuBoard.setReadOnly(false);
                	if (mShowTime) {
                		mGameTimer.start();
                	}
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
                	mSudokuGame.clearAllNotes();
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
			mSudokuBoard.setReadOnly(true);
			showDialog(DIALOG_WELL_DONE);
		}
    	
    };
    
	/**
     * Update the time of game-play.
     */
	void updateTitle() {
		if (mShowTime) {
			setTitle(mGameTimeFormatter.format(mSudokuGame.getTime()));
		} else {
			setTitle(R.string.app_name);
		}
	}
	
	// This class implements the game clock.  All it does is update the
    // status each tick.
	private final class GameTimer extends Timer {
		
		GameTimer() {
    		super(1000);
    	}
		
    	@Override
		protected boolean step(int count, long time) {
    		updateTitle();
            
            // Run until explicitly stopped.
            return false;
        }
        
	}
}
