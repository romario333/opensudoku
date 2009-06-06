package cz.romario.opensudoku.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;




/**
 * Represents group of cells which must each contain unique number.
 * 
 * Typical examples of instances are sudoku row, column or sector (3x3 group of cells).
 * 
 * @author romario
 *
 */
public class CellGroup {
	private Cell[] mCells = new Cell[CellCollection.SUDOKU_SIZE];
	private int mPos = 0;
	
	public void addCell(Cell cell) {
		mCells[mPos] = cell;
		mPos++;
	}
	

	/**
	 * Validates numbers in given sudoku group - numbers must be unique. Cells with invalid
	 * numbers are marked (see {@link Cell#isInvalid}).
	 * 
	 * Method expects that cell's invalid properties has been set to false 
	 * ({@link CellCollection#validate} does this).
	 * 
	 * @return True if validation is successful.
	 */
	public boolean validate() {
		// TODO: quick and dirty implementation
		boolean valid = true;
		
		// count number of occurences of numbers in group
		Map<Integer, List<Cell>> cellsByValue = 
			new HashMap<Integer, List<Cell>>();
		for (int i=0; i<mCells.length; i++) {
			int value = mCells[i].getValue();
			
			if (value != 0) {
				if (!cellsByValue.containsKey(value)) {
					cellsByValue.put(value, new ArrayList<Cell>());
				}
				cellsByValue.get(value).add(mCells[i]);
			}
		}
		
		// if some number is in group more than once, mark cells holding this number as invalid.
		for (Entry<Integer, List<Cell>> cellsForValue : cellsByValue.entrySet()) {
			if (cellsForValue.getValue() != null && cellsForValue.getValue().size() > 1) {
				for (Cell cell : cellsForValue.getValue()) {
					cell.setInvalid(true);
					valid = false;
				}
			}
		}
	
		return valid;
	}
	

}
