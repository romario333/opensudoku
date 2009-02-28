package cz.romario.opensudoku;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import cz.romario.opensudoku.SudokuBoard.OnCellSelectedListener;

public class PlaySudokuActivity extends Activity {
	
	private static final int DIALOG_SELECT_NUMBER = 1;
	
	private Sudoku sudoku;
	
	private SudokuBoard sudokuBoard;
	private SudokuCell selectedCell;
	// TODO: je tohle OK, precist si znovu ten clanek o leakovani pameti
	private Dialog selectNumberDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sudoku);
        
        sudoku = Sudoku.CreateDebugGame();
        
        sudokuBoard = (SudokuBoard)findViewById(R.id.sudoku_board);
        sudokuBoard.setSudoku(sudoku);
        
        sudokuBoard.setOnCellSelectedListener(sudoBoardCellSelected);
        
        //TextView pokus = (TextView)findViewById(R.id.textpokus);
        //createSudoku();
        
        // TODO: temp verze dialogu
        // TODO: .setIcon
        selectNumberDialog = new AlertDialog.Builder(PlaySudokuActivity.this)
        .setTitle("Select number")
        .setView(CreateSelectNumberView())
        .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /* User clicked No so do some stuff */
            	if (selectedCell != null) {
            		// TODO: do fce
            		selectedCell.setValue(0);
            		sudoku.validate();
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
				
				
				sudoku.validate();
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
    
    
    
    
    // TODO: asi ho budu cpat do nejakyho kontejneru
//    private void createSudoku() {
//    	TableLayout sudokuTable = (TableLayout)findViewById(R.id.sudoku_table);
//    	
//    	for (int x=0; x<3; x++) {
//    		TableRow row = new TableRow(this);
//    		row.setBackgroundColor(Color.YELLOW);
//
//    		for (int y=0; y<3; y++) {
//	    		row.addView(createSector());
//    		}
//    		
//    		sudokuTable.addView(row, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//    		
//    	}
//    	
//    }
//    
//    private View createSector() {
//    	TextView cell = new TextView(this);
//    	cell.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
//    	cell.setBackgroundColor(Color.BLUE);
//    	cell.setText("haha");
//    	return cell;
//
////    	LinearLayout sector = new LinearLayout(this); 
//////    	sector.setLayoutParams(new LinearLayout.LayoutParams(
//////    			LayoutParams.WRAP_CONTENT,
//////    			LayoutParams.WRAP_CONTENT));
////    	sector.setBackgroundColor(Color.RED);
////    	TextView cell = new TextView(this);
////    	cell.setText("haha");
////    	sector.addView(cell, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//
//    	
////    	TableLayout sector = new TableLayout(this);
////    	sector.setLayoutParams(new TableLayout.LayoutParams(
////    			LayoutParams.FILL_PARENT,
////    			LayoutParams.FILL_PARENT));
////    	sector.setBackgroundColor(Color.RED);
////    			
////    	TextView cell = new TextView(this);
////    	cell.setText("haha");
////    	sector.addView(cell);
//
//    	
////    	for (int x=0; x<3; x++) {
////    		TableRow row = new TableRow(this);
////    		
////    		for (int y=0; y<3; y++) {
////	    		TextView cell = new TextView(this);
////		    	cell.setLayoutParams(new LayoutParams(
////		    			LayoutParams.FILL_PARENT,
////		    			LayoutParams.FILL_PARENT));
////
////
////	    		cell.setText("5");
////	    		row.addView(cell);
////    		}
////    		
////    		sector.addView(row);
////    	}
//    	
////    	return sector;
//    }
	

}
