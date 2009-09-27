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
	
	public void testSerialize() {
		Cell cell = new Cell();
		assertEquals("0|-|1|", cell.serialize());
		
		cell.setValue(5);
		assertEquals("5|-|1|", cell.serialize());
		
		cell.setEditable(false);
		assertEquals("5|-|0|", cell.serialize());
		
		cell.setNote(cell.getNote().toggleNumber(4));
		assertEquals("5|4,|0|", cell.serialize());
	}
	
	public void testDeserialize() {
		Cell cell = Cell.deserialize("5|-|1|");
		assertEquals(5, cell.getValue());
		assertEquals(true, cell.getNote().isEmpty());
		assertEquals(true, cell.isEditable());

		cell = Cell.deserialize("5|4,|0|");
		assertEquals(5, cell.getValue());
		assertEquals(false, cell.getNote().isEmpty());
		assertEquals(false, cell.isEditable());
	}
}
