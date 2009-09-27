package cz.romario.opensudoku.game;

import junit.framework.TestCase;

public class CellGroupTest extends TestCase {

	public void testValidateValid() {
		CellGroup group = new CellGroup();
		group.addCell(new Cell(1));
		group.addCell(new Cell(2));
		group.addCell(new Cell(3));
		group.addCell(new Cell(4));
		group.addCell(new Cell(5));
		group.addCell(new Cell(6));
		group.addCell(new Cell(7));
		group.addCell(new Cell(8));
		group.addCell(new Cell(9));
		
		assertEquals(true, group.validate()); 
	}
	
	public void testValidateSomeCellsNotFilled() {
		CellGroup group = new CellGroup();
		group.addCell(new Cell(1));
		group.addCell(new Cell(2));
		group.addCell(new Cell(3));
		group.addCell(new Cell(4));
		group.addCell(new Cell(0));
		group.addCell(new Cell(6));
		group.addCell(new Cell(7));
		group.addCell(new Cell(8));
		group.addCell(new Cell(9));
		
		assertEquals(true, group.validate()); 
	}
	
	public void testValidateInvalid() {
		CellGroup group = new CellGroup();
		group.addCell(new Cell(1));
		group.addCell(new Cell(2));
		group.addCell(new Cell(3));
		group.addCell(new Cell(4));
		group.addCell(new Cell(5));
		group.addCell(new Cell(6));
		group.addCell(new Cell(7));
		group.addCell(new Cell(4));
		group.addCell(new Cell(9));
		
		assertEquals(false, group.validate()); 
	}

}
