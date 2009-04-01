package cz.romario.opensudoku.gui;

import java.util.Formatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.game.SudokuCellCollection.OnChangeListener;
import cz.romario.opensudoku.gui.EditCellDialog.OnNoteEditListener;
import cz.romario.opensudoku.gui.EditCellDialog.OnNumberEditListener;
import cz.romario.opensudoku.gui.SelectMultipleNumbersDialog.OnNumbersSelectListener;
import cz.romario.opensudoku.gui.SelectNumberDialog.OnNumberSelectListener;
import cz.romario.opensudoku.gui.SudokuBoardView.OnCellTapListener;

/*
 * TODO:
 * - timer
 * - folder view (v detailu 10 puzzles / 3 solved)
 * - sudoku list (v detailu stav, cas a tak)
 */
//TODO: vyresit proc tuhne, kdyz vytahnu klavesnici
public class SudokuPlayActivity extends Activity{
	
	public static final int MENU_ITEM_RESTART = Menu.FIRST;
	
	private static final String TAG = "SudokuPlayActivity";
	
	public static final String EXTRAS_SUDOKU_ID = "sudoku_id";
	
	private static final int DIALOG_WELL_DONE = 3;
	private static final int DIALOG_EDIT_CELL = 4;
	
	private static final int REQUEST_SELECT_NUMBER = 1;

	
	private long sudokuGameID;
	private SudokuGame sudokuGame;
	private int inputMode;
	
	private EditCellDialog editCellDialog;
	private SudokuBoardView sudokuBoard;
	
	private StringBuilder timeText;
	private Formatter timeFormatter;
	
	private GameTimer gameTimer;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sudoku_play);
        
        sudokuBoard = (SudokuBoardView)findViewById(R.id.sudoku_board);
        editCellDialog = new EditCellDialog(this);
        editCellDialog.setOnNumberEditListener(onNumberEditListener);
        editCellDialog.setOnNoteEditListener(onNoteEditListener);
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
        	inputMode = savedInstanceState.getInt("input_mode");
        }
        
        if (sudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
        	sudokuGame.setState(SudokuGame.GAME_STATE_PLAYING);
        }
        
        if (sudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
        	updateTime();
        	sudokuBoard.setReadOnly(true);
        }
        
        if (sudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
        	sudokuGame.start();
        }
        
        sudokuBoard.setCells(sudokuGame.getCells());
        sudokuBoard.setOnCellTapListener(cellTapListener);
        
		sudokuGame.getCells().addOnChangeListener(cellsOnChangeListener);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_RESTART, 0, "Restart")
                .setShortcut('3', 'r')
                .setIcon(android.R.drawable.ic_menu_rotate);

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
        case MENU_ITEM_RESTART:
            // Restart game
        	sudokuGame.restart();
        	sudokuBoard.postInvalidate();
        	gameTimer.start();
            return true;
        }
        return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (sudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			sudokuGame.start();
			gameTimer.start();
		}
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
		
		// we will save game to the database as we might not be able to get back
		SudokuDatabase sudokuDB = new SudokuDatabase(SudokuPlayActivity.this);
		sudokuDB.updateSudoku(sudokuGame);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
		// TODO: tady bych mel mimo jiny zastavovat ten pitomej timer
    	if (sudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			sudokuGame.pause();
		}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
		
    	// TODO: doresit timer, poradne retestnout, funguje divne
    	gameTimer.stop();
    	outState.putParcelable("sudoku_game", sudokuGame);
    	outState.putInt("input_mode", inputMode);
    	gameTimer.saveState(outState);
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
            .setTitle("Well Done!")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked OK so do some stuff */
                }
            })
            .create();
    	case DIALOG_EDIT_CELL:
    		return editCellDialog.getDialog();
    	}
    	return null;
    }
    
    
    private OnCellTapListener cellTapListener = new OnCellTapListener() {

		@Override
		public boolean onCellTap(SudokuCell cell) {
			if (cell != null && cell.getEditable()) {
				
				SudokuCell selectedCell = sudokuBoard.getSelectedCell();
				editCellDialog.updateNumber(selectedCell.getValue());
				editCellDialog.updateNote(selectedCell.getNoteNumbers());
				showDialog(DIALOG_EDIT_CELL);
			}
			return true;
		}
    	
    };
    
	/**
	 * Occurs when user selects number in EditCellDialog.
	 */
    private OnNumberEditListener onNumberEditListener = new OnNumberEditListener() {
		@Override
		public boolean onNumberEdit(int number) {
    		SudokuCell selectedCell = sudokuBoard.getSelectedCell();
    		if (number != -1) {
                // set cell number selected by user
				sudokuGame.getCells().setValue(selectedCell, number);
    		}
			return true;
		}
	};
	
	/**
	 * Occurs when user edits note in EditCellDialog
	 */
	private OnNoteEditListener onNoteEditListener = new OnNoteEditListener() {
		@Override
		public boolean onNoteEdit(Integer[] numbers) {
			SudokuCell selectedCell = sudokuBoard.getSelectedCell();
			if (selectedCell != null) {
				sudokuGame.getCells().setNoteNumbers(selectedCell, numbers);
			}
			return true;
		}
	};
    
	/**
	 * Occurs when any value in sudoku's cells changes.
	 */
	private OnChangeListener cellsOnChangeListener = new OnChangeListener() {
		@Override
		public boolean onChange() {
			sudokuGame.validate();
			
            // check whether game is completed, if so, finish the game
			if (sudokuGame.isCompleted()) {
                showDialog(DIALOG_WELL_DONE);
                sudokuGame.setState(SudokuGame.GAME_STATE_COMPLETED);
                sudokuBoard.setReadOnly(true);
            }
                
            // update board view
			sudokuBoard.postInvalidate();
			return true;
		}
	};
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case REQUEST_SELECT_NUMBER:
                SudokuCell selectedCell = sudokuBoard.getSelectedCell();
                if (resultCode == RESULT_OK && selectedCell != null) {
                        int selectedNumber = data.getIntExtra(SelectNumberActivity.EXTRAS_SELECTED_NUMBER, -1);
                        if (selectedNumber != -1) {
                        // set cell number selected by user
                                if (selectedCell.getEditable()) {
                                        sudokuGame.getCells().setValue(selectedCell, selectedNumber);
                                }
                        }
                }
                break;
        }
    }

    
	// TODO: can Chronometer replace this?
	/**
     * Update the time of game-play.
     */
	void updateTime() {
		// Use StringBuilders and a Formatter to avoid allocating new
		// String objects every time -- this function is called often!
		long time = sudokuGame.getTime();
		timeText.setLength(0);
		timeFormatter.format("%02d:%02d", time / 60000, time / 1000 % 60);
		setTitle(timeText);
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
