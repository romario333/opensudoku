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
        	Sudoku sudoku = savedInstanceState.getParcelable("sudoku");
        	Log.d("ROMAK", String.format("sudoku: %s", sudoku));
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    	
    	outState.putParcelable("game", Sudoku.CreateDebugGame());
    }
}