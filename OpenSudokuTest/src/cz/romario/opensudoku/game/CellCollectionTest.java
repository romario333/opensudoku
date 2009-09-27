package cz.romario.opensudoku.game;

import android.os.Bundle;
import junit.framework.TestCase;

public class CellCollectionTest extends TestCase {
	
	public void testCreateEmpty() {
		CellCollection x = CellCollection.createEmpty();
		
		for (int r=0; r<CellCollection.SUDOKU_SIZE; r++) {
			for (int c=0; c<CellCollection.SUDOKU_SIZE; c++) {
				Cell cell = x.getCell(r, c);
				assertEquals(0, cell.getValue());
				assertEquals(true, cell.isEditable());
				assertEquals(true, cell.isValid());
			}
		}
	}
	
	public void testParcelableVsToString() {
		CellCollection cells = CellCollection.createDebugGame();
		cells.getCell(2, 2).setNote(CellNote.deserialize("5,7,"));
		cells.getCell(1, 4).setNote(CellNote.deserialize("5,7,"));
		cells.getCell(5, 4).setNote(CellNote.deserialize("5,7,"));
		cells.getCell(7, 7).setNote(CellNote.deserialize("5,7,"));
		cells.getCell(6, 6).setNote(CellNote.deserialize("5,7,"));
		cells.getCell(6, 7).setNote(CellNote.deserialize("5,7,"));
		cells.getCell(7, 6).setNote(CellNote.deserialize("5,7,"));
		
		Bundle bundle = new Bundle();
		bundle.putParcelable("test", cells);
		
		
		
	}
	

	
	

}
