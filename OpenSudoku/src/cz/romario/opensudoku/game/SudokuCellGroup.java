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
 * @author EXT91365
 *
 */
public class SudokuCellGroup {
	private SudokuCell[] cells = new SudokuCell[SudokuCellCollection.SUDOKU_SIZE];
	private int pos = 0;
	
	public void addCell(SudokuCell cell) {
		cells[pos] = cell;
		pos++;
	}
	

	/**
	 * Validates numbers in given sudoku group - numbers must be unique. Cells with invalid
	 * numbers are marked (see SudokuCell.getInvalid).
	 * 
	 * Method expects that cell's invalid properties has been set to false 
	 * (SudokuCellCollection.validate does this).
	 * 
	 * @return True if validation is successful.
	 */
	public boolean validate() {
		// TODO: quick and dirty implementation
		boolean valid = true;
		
		// count number of occurences of numbers in group
		Map<Integer, List<SudokuCell>> cellsByValue = 
			new HashMap<Integer, List<SudokuCell>>();
		for (int i=0; i<cells.length; i++) {
			int value = cells[i].getValue();
			
			if (!cellsByValue.containsKey(value)) {
				cellsByValue.put(value, new ArrayList<SudokuCell>());
			}
			cellsByValue.get(value).add(cells[i]);
		}
		
		// if some number is in group more than once, mark cells holding this number as invalid.
		for (Entry<Integer, List<SudokuCell>> cellsForValue : cellsByValue.entrySet()) {
			if (cellsForValue.getValue() != null && cellsForValue.getValue().size() > 1) {
				for (SudokuCell cell : cellsForValue.getValue()) {
					cell.setInvalid(true);
					valid = false;
				}
			}
		}
	
		return valid;
	}
	

}
