package cz.romario.opensudoku.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import cz.romario.opensudoku.game.SudokuCellCollection;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class OpenSudokuActivity extends Activity {
    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SudokuCellCollection sudoku = SudokuCellCollection.CreateDebugGame();
        String data = sudoku.serialize();
        SudokuCellCollection nove = SudokuCellCollection.deserialize(data);
		
        
        if (savedInstanceState != null) {
        	
        	//SudokuCell cell = savedInstanceState.getParcelable("cell");
        	SudokuCellCollection s = savedInstanceState.getParcelable("s");
        	Log.d("ROMAK", String.format("sudoku: %s", s));
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    	
    	SudokuCellCollection s = SudokuCellCollection.CreateDebugGame();
    	outState.putParcelable("s", s);
    }
}