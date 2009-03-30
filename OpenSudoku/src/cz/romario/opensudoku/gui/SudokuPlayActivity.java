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
import cz.romario.opensudoku.gui.SelectMultipleNumbersDialog.OnNumbersSelectListener;
import cz.romario.opensudoku.gui.SelectNumberDialog.OnNumberSelectListener;
import cz.romario.opensudoku.gui.SudokuBoardView.OnCellTapListener;

/*
 * TODO:
 * - timer
 * - notes
 * - folder view (v detailu 10 puzzles / 3 solved)
 * - sudoku list (v detailu stav, cas a tak)
 * - select number dialog
 * 
 */
// TODO: look at Chronometer widget
//TODO: vyresit proc tuhne, kdyz vytahnu klavesnici
public class SudokuPlayActivity extends Activity{
	
	public static final int MENU_ITEM_RESTART = Menu.FIRST;
	
	private static final String TAG = "SudokuPlayActivity";
	
	public static final String EXTRAS_SUDOKU_ID = "sudoku_id";
	
	//private static final int REQUEST_SELECT_NUMBER = 1;
	
	private static final int DIALOG_SELECT_NUMBER = 1;
	private static final int DIALOG_SELECT_MULTIPLE_NUMBERS = 2;
	private static final int DIALOG_WELL_DONE = 3;
	
	private static final int INPUT_MODE_NORMAL = 1;
	private static final int INPUT_MODE_NOTES = 2;
	
	private long sudokuGameID;
	private SudokuGame sudokuGame;
	private int inputMode;
	
	private SelectNumberDialog selectNumberDialog;
	private SelectMultipleNumbersDialog selectMultipleNumbersDialog;
	private SudokuBoardView sudokuBoard;
	private TextView timeLabel;
	private Button inputModeButton;
	
	private StringBuilder timeText;
	private Formatter timeFormatter;
	
	private GameTimer gameTimer;

	//PowerManager.WakeLock wakeLock;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sudoku_play);
        
        sudokuBoard = (SudokuBoardView)findViewById(R.id.sudoku_board);
        selectNumberDialog = new SelectNumberDialog(this);
        selectNumberDialog.setOnNumberSelectListener(onNumberSelectListener);
        selectMultipleNumbersDialog = new SelectMultipleNumbersDialog(this);
        selectMultipleNumbersDialog.setOnNumbersSelectListener(onNumbersSelectListener);
        inputModeButton = (Button) findViewById(R.id.input_mode);
        inputModeButton.setOnClickListener(inputModeClickListener);
        timeLabel = (TextView)findViewById(R.id.time_label);
        timeText = new StringBuilder(5);
        timeFormatter = new Formatter(timeText);
        gameTimer = new GameTimer();
        
        
        // create sudoku game instance
        if (savedInstanceState == null) {
        	// activity runs for the first time, read game from database
        	sudokuGameID = getIntent().getLongExtra(EXTRAS_SUDOKU_ID, 0);
        	SudokuDatabase sudokuDB = new SudokuDatabase(this);
        	sudokuGame = sudokuDB.getSudoku(sudokuGameID);
        	inputMode = INPUT_MODE_NORMAL;
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
        	updateTimeLabel(sudokuGame.getTime());
        	sudokuBoard.setReadOnly(true);
        }
        
        if (sudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
        	sudokuGame.start();
        }
        
    	setTitle(sudokuGame.getName());
    	
    	updateInputModeText();

    	//PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    	// TODO
    	//wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
    	//wakeLock.acquire(5 * 60 * 1000);
        
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
	
	
	private OnClickListener inputModeClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (inputMode == INPUT_MODE_NORMAL) {
				inputMode = INPUT_MODE_NOTES;
			} else {
				inputMode = INPUT_MODE_NORMAL;
			}
			updateInputModeText();
		}
	};
	
	private void updateInputModeText() {
		if (inputMode == INPUT_MODE_NORMAL) {
			inputModeButton.setText("Normal");
		} else {
			inputModeButton.setText("Notes");
		}
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
    	// TODO Auto-generated method stub
    	super.onPause();
    	//if (wakeLock.isHeld()) {
    	//	wakeLock.release();
    	//}
		if (sudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			sudokuGame.pause();
		}
		
		// we will save game to the database as we might not be able to get back
		SudokuDatabase sudokuDB = new SudokuDatabase(SudokuPlayActivity.this);
		sudokuDB.updateSudoku(sudokuGame);
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
    	case DIALOG_SELECT_NUMBER:
    		return selectNumberDialog.getDialog();
    	case DIALOG_SELECT_MULTIPLE_NUMBERS:
			return selectMultipleNumbersDialog.getDialog();
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
    	}
    	return null;
    }
    
    
    private OnCellTapListener cellTapListener = new OnCellTapListener() {

		@Override
		public boolean onCellTap(SudokuCell cell) {
			if (cell != null && cell.getEditable()) {
				
				if (inputMode == INPUT_MODE_NORMAL) {
					showDialog(DIALOG_SELECT_NUMBER);
				} else {
					SudokuCell selectedCell = sudokuBoard.getSelectedCell();
					selectMultipleNumbersDialog.updateNumbers(selectedCell.getNoteNumbers());
					showDialog(DIALOG_SELECT_MULTIPLE_NUMBERS);
				}
			}
			return true;
		}
    	
    };
    
	/**
	 * Occurs when number is selected in SelectNumberDialog.
	 */
    private OnNumberSelectListener onNumberSelectListener = new OnNumberSelectListener() {
		@Override
		public boolean onNumberSelect(int number) {
    		SudokuCell selectedCell = sudokuBoard.getSelectedCell();
    		if (number != -1) {
                // set cell number selected by user
				sudokuGame.getCells().setValue(selectedCell, number);
    		}
			return true;
		}
	};
	
	// TODO: OnNumberSelectListener and OnNumbersSelectListener are too close to each other
	/**
	 * Occurs when user selects numbers in SelectMultipleNumbersDialog.
	 */
	private OnNumbersSelectListener onNumbersSelectListener = new OnNumbersSelectListener() {
		@Override
		public boolean onNumbersSelect(Integer[] numbers) {
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

    
	/**
     * Update the status line to the current game state.
     */
	void updateStatus() {
		// Use StringBuilders and a Formatter to avoid allocating new
		// String objects every time -- this function is called often!
		
		//long time = gameTimer.getTime();
		long time = sudokuGame.getTime();
		updateTimeLabel(time);
	}
	
	void updateTimeLabel(long time) {
		timeText.setLength(0);
		timeFormatter.format("%02d:%02d", time / 60000, time / 1000 % 60);
		timeLabel.setText(timeText);
	}
    
    
	// This class implements the game clock.  All it does is update the
    // status each tick.
	private final class GameTimer extends Timer {
		
		GameTimer() {
    		super(1000);
    	}
		
    	@Override
		protected boolean step(int count, long time) {
    		updateStatus();
            
            // Run until explicitly stopped.
            return false;
        }
        
	}
	
}
