package cz.romario.opensudoku;

import java.text.SimpleDateFormat;

import junit.framework.TestCase;
import cz.romario.opensudoku.game.CellCollection;
import cz.romario.opensudoku.game.CellNote;
import android.os.Bundle;
import android.test.PerformanceTestCase;
import android.util.Log;

public class PerformanceTest extends TestCase implements PerformanceTestCase {

	private static final String TAG = "OpenSudoku.PerformanceTest";
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	
	private static String elapsedToString(long elapsed) {
		return dateFormat.format(elapsed);
	}
	
	/**
	   * A class to help benchmark code
	   * It simulates a real stop watch
	   */
	class Stopwatch {

	    private long startTime = -1;
	    private long stopTime = -1;
	    private boolean running = false;
	    
	    

	    public Stopwatch start() {
	       startTime = System.currentTimeMillis();
	       running = true;
	       return this;
	    }
	    public Stopwatch stop(String message) {
	       stopTime = System.currentTimeMillis();
	       running = false;
	       log(message + " " + elapsedToString(getElapsedTime()));
	       reset();
	       return this;
	    }
	    /** returns elapsed time in milliseconds
	      * if the watch has never been started then
	      * return zero
	      */
	    public long getElapsedTime() {
	       if (startTime == -1) {
	          return 0;
	       }
	       if (running){
	       return System.currentTimeMillis() - startTime;
	       } else {
	       return stopTime-startTime;
	       } 
	    }

	    public Stopwatch reset() {
	       startTime = -1;
	       stopTime = -1;
	       running = false;
	       return this;
	    }

	}
	
	
	
	private Intermediates mIntermediates;
	
	@Override
	public boolean isPerformanceOnly() {
		return true;
	}

	@Override
	public int startPerformance(Intermediates intermediates) {
		mIntermediates = intermediates;
		
		return 0;
	}
	
	public void testPerf() throws Exception {
		Stopwatch sw = new Stopwatch();
		
		CellCollection cells = CellCollection.createDebugGame();
		
		for (int r=0; r < CellCollection.SUDOKU_SIZE; r++) {
			for (int c=0; c < CellCollection.SUDOKU_SIZE; c++) {
				cells.getCell(r, c).setNote(CellNote.fromString("1,2,3,4,5,6,7,8,9,"));
			}
		}
		
		
		
		sw.start();
		Bundle b = new Bundle();
		b.putParcelable("test", cells);
		sw.stop("putParcelable");
		
		sw.start();
		b.toString();
		sw.stop("toString");

	}
	
	private void log(String message) {
		Log.e(TAG, TAG + ": " + message);
	}
	
	
	


}
