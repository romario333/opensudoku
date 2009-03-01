package cz.romario.opensudoku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cz.romario.opensudoku.db.SudokuDatabase;

public class SudokuDetailActivity extends Activity {

	public static final String EXTRAS_SUDOKU_ID = "sudoku_id";
	
	private long sudokuGameID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sudoku_detail);
		
    	sudokuGameID = getIntent().getLongExtra(EXTRAS_SUDOKU_ID, 0);
    	
    	SudokuDatabase sudokuDB = new SudokuDatabase(this);
    	SudokuGame sudokuGame = sudokuDB.getSudoku(sudokuGameID);
    	
        SudokuBoard sudokuBoard = (SudokuBoard)findViewById(R.id.sudoku_board);
        sudokuBoard.setCells(sudokuGame.getCells());
        sudokuBoard.setReadOnly(true);
        
        TextView sudokuName = (TextView)findViewById(R.id.sudoku_name);
        sudokuName.setText(sudokuGame.getName());
        
        Button buttonPlay = (Button)findViewById(R.id.button_play);
        buttonPlay.setOnClickListener(buttonPlayClickListener);
        
        Button buttonBack = (Button)findViewById(R.id.button_back);
        buttonBack.setOnClickListener(buttonBackClickListener);
	}
	
	private OnClickListener buttonPlayClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent i = new Intent(SudokuDetailActivity.this, SudokuPlayActivity.class);
			i.putExtra(SudokuPlayActivity.EXTRAS_SUDOKU_ID, sudokuGameID);
			startActivity(i);
		}
	};	

	private OnClickListener buttonBackClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
		
	};	
}
