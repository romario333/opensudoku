package cz.romario.opensudoku.game;

import junit.framework.TestCase;

// TODO: check how much tests affect release apk size

public class CellNoteTest extends TestCase {

	public void testCellNote() {
		CellNote note = new CellNote();
		assertNotNull(note.getNotedNumbers());
	}

	public void testGetNotedNumbers() {
		CellNote note = new CellNote();
		assertEquals(0, note.getNotedNumbers().size());
		note = note.toggleNumber(1);
		assertEquals(1, note.getNotedNumbers().size());
	}

	public void testToggleNumberOutOfBounds() {
		CellNote note = new CellNote();
		
		try { note.toggleNumber(0);} catch (IllegalArgumentException e) {}
		try { note.toggleNumber(10);} catch (IllegalArgumentException e) {}
	}
	
	public void testToggleNumber() {
		CellNote note = new CellNote();
		
		note = note.toggleNumber(1);
		assertTrue(note.getNotedNumbers().contains(1));
		assertFalse(note.getNotedNumbers().contains(2));
		assertEquals(1, note.getNotedNumbers().size());
		
		note = note.toggleNumber(2);
		assertTrue(note.getNotedNumbers().contains(1));
		assertTrue(note.getNotedNumbers().contains(2));
		assertEquals(2, note.getNotedNumbers().size());

		note = note.toggleNumber(1);
		assertFalse(note.getNotedNumbers().contains(1));
		assertTrue(note.getNotedNumbers().contains(2));
		assertEquals(1, note.getNotedNumbers().size());

		note = note.toggleNumber(2);
		assertFalse(note.getNotedNumbers().contains(1));
		assertFalse(note.getNotedNumbers().contains(2));
		assertEquals(0, note.getNotedNumbers().size());
	}

	public void testIsEmpty() {
		
		CellNote note = new CellNote();
		assertTrue(note.isEmpty());
		
		note = note.toggleNumber(1);
		assertFalse(note.isEmpty());
		
		note = note.toggleNumber(1);
		assertTrue(note.isEmpty());
	}
	
	public void testSerialize() {
		CellNote note = new CellNote();
		assertEquals("-", note.serialize());
		
		note = note.toggleNumber(5);
		assertEquals("5,", note.serialize());
		
		note = note.toggleNumber(7);
		assertEquals("5,7,", note.serialize());
	}
	
	public void testDeserialize() {
		CellNote note = CellNote.deserialize("-");
		assertEquals(0, note.getNotedNumbers().size());
		
		note = CellNote.deserialize("5,");
		assertEquals(1, note.getNotedNumbers().size());
		assertTrue(note.getNotedNumbers().contains(5));
		
		note = CellNote.deserialize("5,7,");
		assertEquals(2, note.getNotedNumbers().size());
		assertTrue(note.getNotedNumbers().contains(5));
		assertTrue(note.getNotedNumbers().contains(7));
	}
}
