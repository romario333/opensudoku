package cz.romario.opensudoku.game;

import junit.framework.TestCase;

public class CellTest extends TestCase {

	public void testSetValue() {
		Cell cell = new Cell();
		cell.setValue(0);
		assertEquals(0, cell.getValue());
		cell.setValue(9);
		assertEquals(9, cell.getValue());
	}
	
	public void testSetValueOutOfBounds() {
		Cell cell = new Cell();
		try { cell.setValue(-1);} catch (IllegalArgumentException e) {}
		try { cell.setValue(10);} catch (IllegalArgumentException e) {}
	}
	
	
	
//	public void testSudokuCell() {
//		fail("Not yet implemented");
//	}

}
