package cz.romario.opensudoku;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class OpenSudokuActivity extends Activity {
    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (savedInstanceState != null) {
        	
        	//SudokuCell cell = savedInstanceState.getParcelable("cell");
        	Sudoku s = savedInstanceState.getParcelable("s");
        	Log.d("ROMAK", String.format("sudoku: %s", s));
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    	
    	Sudoku s = Sudoku.CreateDebugGame();
    	outState.putParcelable("s", s);
    }
}