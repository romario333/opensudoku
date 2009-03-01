package cz.romario.opensudoku;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class OpenSudokuActivity extends Activity {
    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Sudoku sudoku = Sudoku.CreateDebugGame();
        String data = sudoku.serialize();
        Sudoku nove = Sudoku.deserialize(data);
		
        
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