package cz.romario.opensudoku;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import cz.romario.opensudoku.SudokuBoard.OnCellSelectedListener;
import cz.romario.opensudoku.db.SudokuDatabase;

public class SudokuPlayActivity extends Activity{
	
	public static final String EXTRAS_SUDOKU_ID = "sudoku_id";
	
	private static final int DIALOG_SELECT_NUMBER = 1;
	
	private static final int TIMER_UPDATE_TIME = 1;
	
	private long sudokuGameID;
	private SudokuGame sudokuGame;
	
	private SudokuBoard sudokuBoard;
	private SudokuCell selectedCell;
	// TODO: je tohle OK, precist si znovu ten clanek o leakovani pameti
	private Dialog selectNumberDialog;
	
	private long gameStartTime;
	private TextView timeLabel;
	private NumberFormat timeFormat = new DecimalFormat("00");
	
	private Timer timer;
	private Handler timerHandler = new Handler() {
		public void handleMessage(Message msg) {
            if (msg.what == TIMER_UPDATE_TIME) {
            	long gameEndTime = System.currentTimeMillis();
            	long time = gameEndTime - gameStartTime + sudokuGame.getTime().getTime();
            	
    	    	long totalSeconds = time / 1000;
    	    	long minutes = totalSeconds / 60;
    	    	long seconds = totalSeconds - (minutes * 60);
    	    	timeLabel.setText(String.format("%s:%s", 
    	    			timeFormat.format(minutes), 
    	    			timeFormat.format(seconds) ));
    	    	// TODO: jak moc se podepise na spotrebe, udelat test
            }
        }

	};

	private OnClickListener buttonLeaveClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			updateSudokuGameTime();
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
        
        gameStartTime = System.currentTimeMillis();
        timeLabel = (TextView)findViewById(R.id.time_label);
        
        if (savedInstanceState != null) {
        	sudokuGame = (SudokuGame)savedInstanceState.getParcelable("sudoku_game");
        } else {
        	// TODO: by id
        	sudokuGameID = getIntent().getLongExtra(EXTRAS_SUDOKU_ID, 0);
        	
        	SudokuDatabase sudokuDB = new SudokuDatabase(this);
        	sudokuGame = sudokuDB.getSudoku(sudokuGameID);
        }
        
    	setTitle(sudokuGame.getName());

        
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
            	if (selectedCell != null) {
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
        
        
        // TODO: budu ho muset stopovat az pujde do pozadi?
        timer = new Timer(false);
        timer.schedule(timerTask, 0, 1000);
    }
    
    private TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			// TODO: musim resit thread-safety?
			timerHandler.sendEmptyMessage(TIMER_UPDATE_TIME);
		}
    	
    };
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    	updateSudokuGameTime();
    	outState.putParcelable("sudoku_game", sudokuGame);
    }
    
    private void updateSudokuGameTime() {
    	long gameEndTime = System.currentTimeMillis(); 
    	long time = gameEndTime - gameStartTime;
    	sudokuGame.addTime(time);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id){
    	case DIALOG_SELECT_NUMBER:
            return selectNumberDialog; 
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
				
				
				sudokuGame.validate();
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
}
