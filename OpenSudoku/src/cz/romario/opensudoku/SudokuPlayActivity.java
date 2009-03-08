package cz.romario.opensudoku;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Formatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import cz.romario.opensudoku.SudokuBoard.OnCellSelectedListener;
import cz.romario.opensudoku.db.SudokuDatabase;

public class SudokuPlayActivity extends Activity{
	
	private static final String TAG = "SudokuPlayActivity";
	
	public static final String EXTRAS_SUDOKU_ID = "sudoku_id";
	
	private static final int DIALOG_SELECT_NUMBER = 1;
	private static final int DIALOG_WELL_DONE = 2;
	
	private static final int TIMER_UPDATE_TIME = 1;
	
	private long sudokuGameID;
	private SudokuGame sudokuGame;
	
	private SudokuBoard sudokuBoard;
	private SudokuCell selectedCell;
	// TODO: je tohle OK, precist si znovu ten clanek o leakovani pameti
	private TextView timeLabel;
	private Dialog selectNumberDialog;
	
	
	private StringBuilder timeText;
	private Formatter timeFormatter;
	
	private GameTimer gameTimer;
	
	//PowerManager.WakeLock wakeLock;

	

	private OnClickListener buttonLeaveClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			sudokuGame.setTime(gameTimer.getTime());
			SudokuDatabase sudokuDB = new SudokuDatabase(SudokuPlayActivity.this);
			sudokuDB.updateSudoku(sudokuGame);
			finish();
		}
		
	};
	
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sudoku_play);
        
        Button buttonLeave = (Button) findViewById(R.id.button_leave);
        buttonLeave.setOnClickListener(buttonLeaveClickListener );

        timeLabel = (TextView)findViewById(R.id.time_label);
        timeText = new StringBuilder(5);
        timeFormatter = new Formatter(timeText);
        gameTimer = new GameTimer();
        
        
        if (savedInstanceState != null) {
        	sudokuGame = (SudokuGame)savedInstanceState.getParcelable("sudoku_game");
        	gameTimer.restoreState(savedInstanceState); // TODO: mel bych asi sladit se sudokuGame
        	
        } else {
        	
        	sudokuGameID = getIntent().getLongExtra(EXTRAS_SUDOKU_ID, 0);
        	SudokuDatabase sudokuDB = new SudokuDatabase(this);
        	sudokuGame = sudokuDB.getSudoku(sudokuGameID);
        	gameTimer.setTime(sudokuGame.getTime());
        }
        
        if (sudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
        	sudokuGame.setState(SudokuGame.GAME_STATE_PLAYING);
        }
        
        if (sudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
        	updateTimeLabel(sudokuGame.getTime());
        }
        
    	setTitle(sudokuGame.getName());

    	//PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    	// TODO
    	//wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
    	//wakeLock.acquire(5 * 60 * 1000);
        
        sudokuBoard = (SudokuBoard)findViewById(R.id.sudoku_board);
        sudokuBoard.setCells(sudokuGame.getCells());
        
        sudokuBoard.setOnCellSelectedListener(sudoBoardCellSelected);
        
        
        // TODO: temp verze dialogu
        // TODO: .setIcon
        selectNumberDialog = new AlertDialog.Builder(SudokuPlayActivity.this)
        .setTitle("Select number")
        .setView(CreateSelectNumberView())
        .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /* User clicked No so do some stuff */
            	if (selectedCell != null && selectedCell.getEditable()) {
            		// TODO: do fce
            		selectedCell.setValue(0);
            		sudokuGame.validate();
            		sudokuBoard.postInvalidate();
            	}
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                /* User clicked No so do some stuff */
            }
        })
       .create(); 
        
        
        
        //updateTimeLabel();
    }
    
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (sudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
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
			gameTimer.stop();
		}
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	// TODO Auto-generated method stub
    	super.onWindowFocusChanged(hasFocus);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    	outState.putParcelable("sudoku_game", sudokuGame);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id){
    	case DIALOG_SELECT_NUMBER:
            return selectNumberDialog;
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
    
    private View CreateSelectNumberView() {
        LinearLayout selectNumberView = new LinearLayout(this);
        selectNumberView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        selectNumberView.setOrientation(LinearLayout.VERTICAL);
        
        for (int x=0; x<3; x++) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int y=0; y<3; y++) {
            	Button b = new Button(this);
            	b.setWidth(0);
            	b.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            	
            	int buttonNumber = (x * 3) + (y + 1);
            	
            	b.setText(new Integer(buttonNumber).toString());
            	b.setTag(buttonNumber);
            	
            	b.setOnClickListener(selectNumberClickListener);
            	
            	row.addView(b);
            }
            selectNumberView.addView(row);
        }
        
        return selectNumberView;
    }
    
	private OnClickListener selectNumberClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Integer selectedNumber = (Integer)v.getTag();
			if (selectedCell != null) {
				if (selectedCell.getEditable()) {
					selectedCell.setValue(selectedNumber);
				}
				
        		if (sudokuGame.isCompleted()) {
        			showDialog(DIALOG_WELL_DONE);
        			sudokuGame.setState(SudokuGame.GAME_STATE_COMPLETED);
        			sudokuBoard.setReadOnly(true);
        		}
				
				sudokuBoard.postInvalidate();
			}
			selectNumberDialog.dismiss();
		}
	
	};
    
    private OnCellSelectedListener sudoBoardCellSelected = new OnCellSelectedListener() {

		@Override
		public boolean onCellSelected(SudokuCell cell) {
			selectedCell = cell;
			if (cell != null && cell.getEditable()) {
				showDialog(DIALOG_SELECT_NUMBER);
			}
			return true;
		}
    	
    };
    
	/**
     * Update the status line to the current game state.
     */
	void updateStatus() {
		// Use StringBuilders and a Formatter to avoid allocating new
		// String objects every time -- this function is called often!
		
		long time = gameTimer.getTime();
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
