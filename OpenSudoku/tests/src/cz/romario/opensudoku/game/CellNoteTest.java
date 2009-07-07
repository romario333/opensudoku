package cz.romario.opensudoku.game;

import android.util.Log;
import junit.framework.TestCase;

// TODO: check whether tests affect release apk size

public class CellNoteTest extends TestCase {

	private static final String TAG = "CellNoteTest";
	
	public void testCellNote() {
		CellNote note = new CellNote();
		assertNotNull(note.getNotedNumbers());
	}

	public void testGetNotedNumbers() {
		CellNote note = new CellNote();
		assertEquals(0, note.getNotedNumbers().size());
		note.toggleNumber(1);
		assertEquals(1, note.getNotedNumbers().size());
	}

	public void testClear() {
		CellNote note = new CellNote();
		note.toggleNumber(1);
		note.toggleNumber(2);
		assertEquals(2, note.getNotedNumbers().size());
		note.clear();
		assertEquals(0, note.getNotedNumbers().size());
	}

	public void testClone() {
		CellNote noteA = new CellNote();
		noteA.toggleNumber(1);
		CellNote noteB = noteA.clone();
		noteB.toggleNumber(2);
		
		assertTrue(noteA.getNotedNumbers().contains(1));
		assertEquals(1, noteA.getNotedNumbers().size());

		assertTrue(noteB.getNotedNumbers().contains(1));
		assertTrue(noteB.getNotedNumbers().contains(2));
		assertEquals(2, noteB.getNotedNumbers().size());
	}

	public void testToggleNumberOutOfBounds() {
		CellNote note = new CellNote();
		
		try { note.toggleNumber(0);} catch (IllegalArgumentException e) {}
		try { note.toggleNumber(10);} catch (IllegalArgumentException e) {}
	}
	
	public void testToggleNumber() {
		CellNote note = new CellNote();
		
		note.toggleNumber(1);
		assertTrue(note.getNotedNumbers().contains(1));
		assertFalse(note.getNotedNumbers().contains(2));
		assertEquals(1, note.getNotedNumbers().size());
		
		note.toggleNumber(2);
		assertTrue(note.getNotedNumbers().contains(1));
		assertTrue(note.getNotedNumbers().contains(2));
		assertEquals(2, note.getNotedNumbers().size());

		note.toggleNumber(1);
		assertFalse(note.getNotedNumbers().contains(1));
		assertTrue(note.getNotedNumbers().contains(2));
		assertEquals(1, note.getNotedNumbers().size());

		note.toggleNumber(2);
		assertFalse(note.getNotedNumbers().contains(1));
		assertFalse(note.getNotedNumbers().contains(2));
		assertEquals(0, note.getNotedNumbers().size());
	}

	public void testIsEmpty() {
		
		CellNote note = new CellNote();
		assertTrue(note.isEmpty());
		
		note.toggleNumber(1);
		assertFalse(note.isEmpty());
		
		note.toggleNumber(1);
		assertTrue(note.isEmpty());
	}
	
	public void testToString() {
		CellNote note = new CellNote();
		assertEquals("-", note.toString());
		
		note.toggleNumber(5);
		assertEquals("5,", note.toString());
		
		note.toggleNumber(7);
		assertEquals("5,7,", note.toString());
	}
	
	public void testFromString() {
		CellNote note = CellNote.fromString("-");
		assertEquals(0, note.getNotedNumbers().size());
		
		note = CellNote.fromString("5,");
		assertEquals(1, note.getNotedNumbers().size());
		assertTrue(note.getNotedNumbers().contains(5));
		
		note = CellNote.fromString("5,7,");
		assertEquals(2, note.getNotedNumbers().size());
		assertTrue(note.getNotedNumbers().contains(5));
		assertTrue(note.getNotedNumbers().contains(7));
	}
}
