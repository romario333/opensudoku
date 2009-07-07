//package cz.romario.opensudoku;
//
//import junit.framework.TestCase;
//import cz.romario.opensudoku.game.CellCollection;
//import android.test.PerformanceTestCase;
//import android.util.Log;
//
//public class PerformanceTest extends TestCase implements PerformanceTestCase {
//
//	private static final String TAG = "PerformanceTest";
//	
//	private Intermediates mIntermediates;
//	
//	@Override
//	public boolean isPerformanceOnly() {
//		return true;
//	}
//
//	@Override
//	public int startPerformance(Intermediates intermediates) {
//		mIntermediates = intermediates;
//		
//		return 0;
//	}
//	
//	public void testPerf() throws Exception {
//		mIntermediates.startTiming(true);
//		//CellCollection cells = CellCollection.createDebugGame();
//		Log.e(TAG, "testPerf()");
//		mIntermediates.finishTiming(true);
//	}
//
//}
